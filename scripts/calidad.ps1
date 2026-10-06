param([string]$Maven='mvn',[string]$Repository='')
$ErrorActionPreference='Stop'
$projectPath=Split-Path $PSScriptRoot -Parent
$argsQuality=@('-B','-f',(Join-Path $projectPath 'pom.xml'),'-DtargetJdk=17','-DlinkXRef=false','org.apache.maven.plugins:maven-pmd-plugin:3.28.0:pmd','org.apache.maven.plugins:maven-pmd-plugin:3.28.0:cpd')
if($Repository){$argsQuality+="-Dmaven.repo.local=$Repository"}
& $Maven @argsQuality
if($LASTEXITCODE -ne 0){throw "Análisis PMD/CPD falló: $LASTEXITCODE"}
[xml]$report=Get-Content -LiteralPath (Join-Path $projectPath 'target/pmd.xml')
$ns=New-Object Xml.XmlNamespaceManager($report.NameTable);$ns.AddNamespace('p','http://pmd.sourceforge.net/report/2.0.0')
$errors=$report.SelectNodes('//p:error',$ns)
if($errors.Count){throw 'PMD encontró errores de procesamiento. Revisar target/pmd.xml.'}
Write-Output ('Advertencias PMD: '+$report.SelectNodes('//p:violation',$ns).Count)
Write-Output 'Informes target/pmd.xml y target/cpd.xml. No equivale a Quality Gate Sonar.'
