# Windows PowerShell 5.1 (powershell.exe). Requiere SSRS Native Mode ya configurado.
param([Parameter(Mandatory)][string]$ReportServer,[string]$SqlServer='localhost',[string]$Database='GuateCompras')
$ErrorActionPreference='Stop'
if($PSVersionTable.PSEdition -eq 'Core'){throw 'Ejecute con Windows PowerShell 5.1: powershell.exe -File scripts/publicar_ssrs.ps1 ...'}
$root=Split-Path $PSScriptRoot -Parent
$server=$ReportServer.TrimEnd('/')
$proxy=New-WebServiceProxy -Uri "$server/ReportService2010.asmx?wsdl" -UseDefaultCredential -Namespace GuateComprasSSRS
$exists=$proxy.ListChildren('/', $false) | Where-Object {$_.Path -eq '/GuateCompras'}
if(!$exists){$null=$proxy.CreateFolder('GuateCompras','/',$null)}
$source=New-Object GuateComprasSSRS.DataSourceDefinition
$source.Extension='SQL'
$source.ConnectString="Data Source=$SqlServer;Initial Catalog=$Database"
$source.CredentialRetrieval=[GuateComprasSSRS.CredentialRetrievalEnum]::Integrated
$source.Enabled=$true;$source.EnabledSpecified=$true;$source.WindowsCredentials=$true
$null=$proxy.CreateDataSource('GuateComprasSQL','/GuateCompras',$true,$source,$null)
foreach($file in Get-ChildItem -LiteralPath (Join-Path $root 'reports/ssrs') -Filter '*.rdl'){
 $warnings=$null
 $null=$proxy.CreateCatalogItem('Report',$file.BaseName,'/GuateCompras',$true,[IO.File]::ReadAllBytes($file.FullName),$null,[ref]$warnings)
 if($warnings){$warnings | ForEach-Object {Write-Warning "$($_.Code): $($_.Message)"}}
 Write-Output "PUBLICADO $($file.BaseName)"
}
Write-Output 'Ocho informes publicados. Ejecute verificar_ssrs.ps1 para comprobar el renderizado PDF.'
