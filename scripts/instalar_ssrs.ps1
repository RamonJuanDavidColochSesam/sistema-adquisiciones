<#
  Instala y configura SQL Server 2022 Reporting Services (modo Native, edición Developer)
  con el catálogo en el SQL Server local. Pensado para ejecutarse elevado:

    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\instalar_ssrs.ps1 `
                -Instalador "C:\ruta\SQLServerReportingServices.exe"

  Idempotente: si ya está instalado o inicializado, solo completa lo que falte.
  No toca la base GuateCompras; solo crea el catálogo ReportServer (+ temporal).
#>
param(
 [string]$Instalador = "$env:TEMP\opencode\SSRS\SQLServerReportingServices.exe",
 [string]$ServidorSql = 'localhost',
 [string]$BaseCatalogo = 'ReportServer',
 [string]$Log = "$env:TEMP\opencode\SSRS\instalar-ssrs.log"
)
$ErrorActionPreference='Stop'
New-Item -ItemType Directory -Force -Path (Split-Path $Log -Parent) | Out-Null
function Nota($m){ Add-Content -LiteralPath $Log -Value "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $m"; Write-Host $m }
# Invoca un método WMI y valida su ReturnValue solo si el proveedor lo expone (algunos devuelven void).
function Usa-Wmi($etiqueta,$resultado){
 $v=$null
 if($null -ne $resultado -and ($resultado.PSObject.Properties.Name -contains 'ReturnValue')){ $v=[int]$resultado.ReturnValue }
 Nota "$etiqueta -> retorno=$v"
 if($null -ne $v -and $v -ne 0){ throw "$etiqueta devolvió $v" }
 return $resultado
}

try {
 $identidad=[Security.Principal.WindowsIdentity]::GetCurrent()
 if(-not ([Security.Principal.WindowsPrincipal]$identidad).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)){ throw 'Consola no elevada: ejecute como administrador.' }
 Nota "Inicio como $($identidad.Name)"

 # 1) Instalación silenciosa si aún no existe el servicio
 $svc=Get-Service -Name 'SQLServerReportingServices' -ErrorAction SilentlyContinue
 if(-not $svc){
  if(-not (Test-Path -LiteralPath $Instalador)){ throw "No existe el instalador: $Instalador" }
  Nota 'Instalando SSRS 2022 en silencio (edición Developer)...'
  $p=Start-Process -FilePath $Instalador -ArgumentList '/quiet','/norestart','/IAcceptLicenseTerms','/Edition=Dev' -Wait -PassThru
  Nota "Instalador terminó con código $($p.ExitCode)"
  for($i=0;$i -lt 150 -and -not $svc;$i++){ Start-Sleep -Seconds 2; $svc=Get-Service -Name 'SQLServerReportingServices' -ErrorAction SilentlyContinue }
  if(-not $svc){ throw 'El servicio SQLServerReportingServices no apareció tras la instalación.' }
 }
 Nota "Servicio: $($svc.Name) [$(($svc=Get-Service -Name 'SQLServerReportingServices').Status)]"
 if((Get-Service 'SQLServerReportingServices').Status -ne 'Running'){ Start-Service 'SQLServerReportingServices'; Start-Sleep -Seconds 3 }

 # 2) Proveedor WMI de configuración (busca la instancia y versión reales)
 function Get-ConfigSet {
  $raiz='root\Microsoft\SqlServer\ReportServer'
  foreach($ns in (Get-WmiObject -Namespace $raiz -Class '__NAMESPACE' | ForEach-Object { "$raiz\$($_.Name)" })){
   foreach($ruta in (Get-WmiObject -Namespace $ns -Class '__NAMESPACE' | ForEach-Object { "$ns\$($_.Name)\Admin" })){
    try { $cfg=Get-WmiObject -Namespace $ruta -Class 'MSReportServer_ConfigurationSetting' -ErrorAction Stop }
    catch { Nota "WMI $ruta -> $($_.Exception.Message) [$($_.Exception.HResult)]"; continue }
    if($cfg){ Nota "WMI $ruta -> instancia obtenida"; return $cfg }
   }
  }
  throw 'No se encontró el proveedor WMI de SSRS.'
 }
 $cfg=Get-ConfigSet
 $nsInstance=$cfg.__PATH -replace '\\\\[^\\]+\\','\\' -replace '\\Admin$',''
 Nota "Instancia WMI: $nsInstance  (servicio $($cfg.ServiceName))"

 # 3) Catálogo en el SQL Server local
 $existe=(& sqlcmd -S $ServidorSql -E -C -h -1 -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.databases WHERE name='$BaseCatalogo'" | Select-Object -Last 1).ToString().Trim()
 if($cfg.IsInitialized -and $existe -eq '1'){
  Nota 'El report server ya estaba inicializado; el catálogo se conserva.'
 } else {
  if($existe -eq '1'){
   Nota "El catálogo $BaseCatalogo ya existe; se omite la creación."
  } else {
   $contenido=$cfg.GenerateDatabaseCreationScript($BaseCatalogo,1033,$false).Script
   if([string]::IsNullOrWhiteSpace($contenido)){ throw 'El proveedor WMI devolvió un script de creación vacío. Recree la instalación con scripts/reinstalar-ssrs.ps1.' }
   $creacion=Join-Path $env:TEMP 'ssrs-creacion-catalogo.sql'
   [IO.File]::WriteAllText($creacion,$contenido)
   Nota "Creando catálogo $BaseCatalogo en $ServidorSql ($([math]::Round((Get-Item $creacion).Length/1KB)) KB)..."
   $salida=& sqlcmd -S $ServidorSql -E -C -b -i $creacion 2>&1; $codigo=$LASTEXITCODE
   $salida | ForEach-Object { Nota "sqlcmd: $_" }
   if($codigo -ne 0){ throw "Fallo la creación del catálogo (sqlcmd $codigo)." }

   $permisos=Join-Path $env:TEMP 'ssrs-permisos-catalogo.sql'
   [IO.File]::WriteAllText($permisos,$cfg.GenerateDatabaseRightsScript($cfg.WindowsServiceIdentityConfigured,$BaseCatalogo,$false,$true).Script)
   Nota "Otorgando permisos a $($cfg.WindowsServiceIdentityConfigured)..."
   $salida=& sqlcmd -S $ServidorSql -E -C -d $BaseCatalogo -b -i $permisos 2>&1; $codigo=$LASTEXITCODE
   $salida | ForEach-Object { Nota "sqlcmd: $_" }
   if($codigo -ne 0){ throw "Fallo la concesión de permisos (sqlcmd $codigo)." }
  }

  $null=Usa-Wmi 'SetDatabaseConnection' ($cfg.SetDatabaseConnection($ServidorSql,$BaseCatalogo,2,'',''))
  Nota 'Conexión al catálogo establecida.'
 }

 # 4) URLs (web service + portal); una reserva que ya exista solo genera aviso
 try { $null=Usa-Wmi 'SetVirtualDirectory(ReportServerWebService)' ($cfg.SetVirtualDirectory('ReportServerWebService','ReportServer',1033)) } catch { Nota "AVISO: $($_.Exception.Message)" }
 try { $null=Usa-Wmi 'ReserveURL(ReportServerWebService)' ($cfg.ReserveURL('ReportServerWebService','http://+:80',1033)) } catch { Nota "AVISO: $($_.Exception.Message)" }
 try { $null=Usa-Wmi 'SetVirtualDirectory(ReportServerWebApp)' ($cfg.SetVirtualDirectory('ReportServerWebApp','Reports',1033)) } catch { Nota "AVISO: $($_.Exception.Message)" }
 try { $null=Usa-Wmi 'ReserveURL(ReportServerWebApp)' ($cfg.ReserveURL('ReportServerWebApp','http://+:80',1033)) } catch { Nota "AVISO: $($_.Exception.Message)" }

 if(-not $cfg.IsInitialized){
  try { $null=Usa-Wmi 'InitializeReportServer' ($cfg.InitializeReportServer($cfg.InstallationID)) } catch { Nota "AVISO: InitializeReportServer -> $($_.Exception.Message)" }
 }

 # 5) Reinicio del servicio para que apliquen catálogo y URLs
 $cfg.SetServiceState($false,$false,$false) | Out-Null
 Restart-Service -Name $cfg.ServiceName -Force
 Start-Sleep -Seconds 5
 $cfg2=Get-ConfigSet
 $cfg2.SetServiceState($true,$true,$true) | Out-Null
 Nota 'Servicio reiniciado y habilitado.'

 # 6) Verificación final
 $cfg2=Get-ConfigSet
 Nota "Estado: IsInitialized=$($cfg2.IsInitialized) IsWebServiceEnabled=$($cfg2.IsWebServiceEnabled) IsReportManagerEnabled=$($cfg2.IsReportManagerEnabled)"
 foreach($u in 'http://localhost/ReportServer','http://localhost/Reports'){
  try { $resp=Invoke-WebRequest -Uri $u -UseDefaultCredentials -UseBasicParsing -TimeoutSec 30; Nota "$u -> HTTP $($resp.StatusCode)" }
  catch { $codigo=$_.Exception.Response.StatusCode.value__; if($codigo){ Nota "$u -> HTTP $codigo (responde; requiere credencial Windows)" } else { Nota "$u -> ERROR $($_.Exception.Message)" } }
 }
 Nota 'INSTALACION SSRS COMPLETADA'
 exit 0
} catch {
 Nota "ERROR: $($_.Exception.Message)"
 exit 1
}
