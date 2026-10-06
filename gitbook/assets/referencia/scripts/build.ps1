param([string]$Maven='mvn',[switch]$RestrictedWindows,[string]$Repository='')
$ErrorActionPreference='Stop'
$projectPath=Split-Path $PSScriptRoot -Parent
$buildArgs=@('-B','-f',(Join-Path $projectPath 'pom.xml'),'clean','verify')
if($RestrictedWindows){$buildArgs+='-Prestricted-windows'}
if($Repository){$buildArgs+="-Dmaven.repo.local=$Repository"}
& $Maven @buildArgs
if($LASTEXITCODE -ne 0){throw "Compilación falló: $LASTEXITCODE"}
Get-Item (Join-Path $projectPath 'target/sistema-adquisiciones.war') | Select-Object FullName,Length
