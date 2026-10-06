param([Parameter(Mandatory)][string]$TomcatHome,[string]$Java='java',[string]$Config='')
$ErrorActionPreference='Stop'
if(!$env:GUATECOMPRAS_SSRS_URL){$env:GUATECOMPRAS_SSRS_URL='http://localhost/ReportServer'}
$projectPath=Split-Path $PSScriptRoot -Parent
$TomcatHome=(Resolve-Path -LiteralPath $TomcatHome).Path
if(!$Config){$Config=Join-Path $projectPath 'config/local-db.properties'}
if(!(Test-Path -LiteralPath $Config)){throw 'Falta configuración externa'}
Copy-Item -LiteralPath (Join-Path $projectPath 'target/sistema-adquisiciones.war') -Destination (Join-Path $TomcatHome 'webapps/sistema-adquisiciones.war')
# Ejecución en primer plano: Ctrl+C termina únicamente este proceso.
& $Java "-Dguatecompras.config=$Config" "-Dcatalina.home=$TomcatHome" "-Dcatalina.base=$TomcatHome" '-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager' "-Djava.util.logging.config.file=$TomcatHome/conf/logging.properties" '-cp' "$TomcatHome/bin/bootstrap.jar;$TomcatHome/bin/tomcat-juli.jar" 'org.apache.catalina.startup.Bootstrap' 'run'
