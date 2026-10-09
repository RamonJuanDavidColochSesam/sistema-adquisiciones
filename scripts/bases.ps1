param([ValidateSet('Migrar','Semilla','Verificar')][string]$Accion='Verificar',[string]$Java='java',[string]$Config='')
$ErrorActionPreference='Stop'
$projectPath=Split-Path $PSScriptRoot -Parent
if(!$Config){$Config=Join-Path $projectPath 'config/local-db.properties'}
if(!(Test-Path -LiteralPath $Config)){throw 'Falta config/local-db.properties. Copie src/main/resources/db.properties.example a config/local-db.properties y ajuste las credenciales'}
$warDirectory=Join-Path $projectPath 'target/sistema-adquisiciones'
if(!(Test-Path -LiteralPath "$warDirectory/WEB-INF/classes")){throw 'Compile primero con scripts/compilar_backend.ps1 (o build.ps1 si tiene Maven instalado)'}
$classes=@{Migrar='MigrarBases';Semilla='SemillaDemo';Verificar='VerificarBases'}
& $Java "-Dguatecompras.config=$Config" '-cp' "$warDirectory/WEB-INF/classes;$warDirectory/WEB-INF/lib/*" "com.adquisiciones.util.$($classes[$Accion])" $projectPath
if($LASTEXITCODE -ne 0){throw "Operación falló: $LASTEXITCODE"}
