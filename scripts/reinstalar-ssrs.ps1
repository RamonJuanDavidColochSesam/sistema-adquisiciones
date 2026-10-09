<#
  Recuperación total de SSRS: desinstala, elimina los catálogos ReportServer/ReportServerTempDB,
  reinstala SSRS 2022 (modo Native, edición Developer) y vuelve a ejecutar la configuración
  completa con scripts\instalar_ssrs.ps1.

  Uso (consola elevada):
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\reinstalar-ssrs.ps1

  No toca la base GuateCompras ni ningún otro dato del proyecto.
#>
param(
 [string]$Instalador = "$env:TEMP\opencode\SSRS\SQLServerReportingServices.exe",
 [string]$Log = "$env:TEMP\opencode\SSRS\reinstalar-ssrs.log"
)
$ErrorActionPreference='Continue'
New-Item -ItemType Directory -Force -Path (Split-Path $Log -Parent) | Out-Null
function Nota($m){ Add-Content -LiteralPath $Log -Value "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $m"; Write-Host $m }

try {
 $identidad=[Security.Principal.WindowsIdentity]::GetCurrent()
 if(-not ([Security.Principal.WindowsPrincipal]$identidad).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)){ throw 'Consola no elevada: ejecute como administrador.' }
 if(-not (Test-Path -LiteralPath $Instalador)){ throw "No existe el instalador: $Instalador. Descárguelo de https://www.microsoft.com/download/details.aspx?id=104502" }

 Nota '1/4 Desinstalando la instalación actual...'
 $p=Start-Process -FilePath $Instalador -ArgumentList '/uninstall','/quiet','/norestart' -Wait -PassThru
 Nota "   desinstalador exit=$($p.ExitCode)"

 Nota '2/4 Eliminando los catálogos SSRS (GuateCompras no se toca)...'
 $salida=& sqlcmd -S localhost -E -C -Q "IF DB_ID('ReportServer') IS NOT NULL BEGIN ALTER DATABASE [ReportServer] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [ReportServer]; END; IF DB_ID('ReportServerTempDB') IS NOT NULL BEGIN ALTER DATABASE [ReportServerTempDB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [ReportServerTempDB]; END;" 2>&1
 $salida | ForEach-Object { Nota "   sqlcmd: $_" }
 Nota "   exit=$LASTEXITCODE"

 Nota '3/4 Reinstalando SSRS 2022 (edición Developer, silencioso)...'
 $p2=Start-Process -FilePath $Instalador -ArgumentList '/quiet','/norestart','/IAcceptLicenseTerms','/Edition=Dev' -Wait -PassThru
 Nota "   instalador exit=$($p2.ExitCode)"
 $svc=$null
 for($i=0;$i -lt 150 -and -not $svc;$i++){ Start-Sleep -Seconds 2; $svc=Get-Service -Name 'SQLServerReportingServices' -ErrorAction SilentlyContinue }
 if(-not $svc){ throw 'El servicio SQLServerReportingServices no apareció tras la reinstalación.' }
 Start-Service 'SQLServerReportingServices' -ErrorAction SilentlyContinue
 Start-Sleep -Seconds 20

 Nota '4/4 Configuración completa (catálogo, URLs, inicialización)...'
 & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'instalar_ssrs.ps1') -Instalador $Instalador
 Nota "   configuración exit=$LASTEXITCODE"
 Nota 'REINSTALACION COMPLETADA'
 exit 0
} catch {
 Nota "ERROR: $($_.Exception.Message)"
 exit 1
}
