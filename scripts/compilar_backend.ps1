<#
  Compila el backend con el JDK local y arma target/sistema-adquisiciones
  con la misma estructura que genera Maven (WEB-INF/classes + WEB-INF/lib),
  sin necesitar una instalacion de Maven.

  Uso:
    scripts\compilar_backend.ps1              # compila y arma el despliegue
    scripts\compilar_backend.ps1 -SoloClases  # solo genera target\classes
    scripts\compilar_backend.ps1 -Limpiar     # borra target y compila de cero
#>
param(
    [string]$Java = '',
    [string]$Repo = '',
    [switch]$Limpiar,
    [switch]$SoloClases
)
$ErrorActionPreference = 'Stop'
$raiz = Split-Path $PSScriptRoot -Parent

function Obtener-Javac {
    param([string]$Sugerido)
    $candidatos = @()
    if ($Sugerido) { $candidatos += $Sugerido }
    if ($env:JAVA_HOME) { $candidatos += $env:JAVA_HOME }
    $enPath = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($enPath) { $candidatos += (Split-Path (Split-Path $enPath.Source -Parent) -Parent) }
    $bases = @(
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Amazon Corretto',
        'C:\Program Files\Zulu'
    )
    foreach ($base in $bases) {
        if (Test-Path $base) {
            foreach ($hijo in (Get-ChildItem $base -Directory -ErrorAction SilentlyContinue)) { $candidatos += $hijo.FullName }
        }
    }
    foreach ($c in $candidatos) {
        if ($c -and (Test-Path (Join-Path $c 'bin\javac.exe'))) { return (Join-Path $c 'bin\javac.exe') }
    }
    throw 'No se encontro un JDK 17 (javac). Instale un JDK o ejecute con -Java "C:\ruta\al\jdk".'
}

function Resolver-Jars {
    param([string[]]$Rutas, [string]$Repositorio)
    $archivos = @()
    $faltantes = @()
    foreach ($ruta in $Rutas) {
        $rutaWin = $ruta.Replace('/', '\')
        $ruta = Join-Path $Repositorio $rutaWin
        if (Test-Path $ruta) { $archivos += $ruta } else { $faltantes += $ruta }
    }
    if ($faltantes.Count -gt 0) {
        $salto = [Environment]::NewLine
        throw "Faltan dependencias en ${Repositorio}:${salto}$($faltantes -join $salto)"
    }
    return $archivos
}

# Dependencias del pom.xml mas las transitivas que instala Maven en WEB-INF/lib.
# La misma lista que produce el despliegue de Eclipse/WTP.
$empaquetar = @(
    'org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar',
    'org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar',
    'org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar',
    'com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar',
    'com/microsoft/sqlserver/mssql-jdbc/12.8.1.jre11/mssql-jdbc-12.8.1.jre11.jar',
    'org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar',
    'org/glassfish/jersey/containers/jersey-container-servlet/3.1.5/jersey-container-servlet-3.1.5.jar',
    'org/glassfish/jersey/containers/jersey-container-servlet-core/3.1.5/jersey-container-servlet-core-3.1.5.jar',
    'org/glassfish/jersey/core/jersey-client/3.1.5/jersey-client-3.1.5.jar',
    'org/glassfish/jersey/core/jersey-common/3.1.5/jersey-common-3.1.5.jar',
    'org/glassfish/jersey/core/jersey-server/3.1.5/jersey-server-3.1.5.jar',
    'org/glassfish/jersey/ext/jersey-entity-filtering/3.1.5/jersey-entity-filtering-3.1.5.jar',
    'org/glassfish/jersey/inject/jersey-hk2/3.1.5/jersey-hk2-3.1.5.jar',
    'org/glassfish/jersey/media/jersey-media-json-jackson/3.1.5/jersey-media-json-jackson-3.1.5.jar',
    'org/glassfish/hk2/hk2-api/3.0.5/hk2-api-3.0.5.jar',
    'org/glassfish/hk2/hk2-locator/3.0.5/hk2-locator-3.0.5.jar',
    'org/glassfish/hk2/hk2-utils/3.0.5/hk2-utils-3.0.5.jar',
    'org/glassfish/hk2/osgi-resource-locator/1.0.3/osgi-resource-locator-1.0.3.jar',
    'org/glassfish/hk2/external/aopalliance-repackaged/3.0.5/aopalliance-repackaged-3.0.5.jar',
    'org/javassist/javassist/3.29.2-GA/javassist-3.29.2-GA.jar',
    'org/checkerframework/checker-qual/3.42.0/checker-qual-3.42.0.jar',
    'jakarta/ws/rs/jakarta.ws.rs-api/3.1.0/jakarta.ws.rs-api-3.1.0.jar',
    'jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar',
    'jakarta/inject/jakarta.inject-api/2.0.1/jakarta.inject-api-2.0.1.jar',
    'jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar',
    'jakarta/activation/jakarta.activation-api/2.1.0/jakarta.activation-api-2.1.0.jar',
    'jakarta/xml/bind/jakarta.xml.bind-api/4.0.0/jakarta.xml.bind-api-4.0.0.jar',
    'com/fasterxml/jackson/core/jackson-annotations/2.15.3/jackson-annotations-2.15.3.jar',
    'com/fasterxml/jackson/core/jackson-core/2.15.3/jackson-core-2.15.3.jar',
    'com/fasterxml/jackson/core/jackson-databind/2.15.3/jackson-databind-2.15.3.jar',
    'com/fasterxml/jackson/module/jackson-module-jakarta-xmlbind-annotations/2.15.3/jackson-module-jakarta-xmlbind-annotations-2.15.3.jar'
)

# Scope provided de Maven: solo para compilar, no se copia a WEB-INF/lib.
$provided = @(
    'jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar'
)

$javac = Obtener-Javac -Sugerido $Java
Write-Host "JDK: $javac"

if (!$Repo) { $Repo = Join-Path $env:USERPROFILE '.m2\repository' }
if (!(Test-Path $Repo)) { throw "No existe el repositorio local de Maven: $Repo" }

if ($Limpiar) {
    $target = Join-Path $raiz 'target'
    if (Test-Path $target) { Remove-Item $target -Recurse -Force; Write-Host 'target eliminado' }
}

$clases = Join-Path $raiz 'target\classes'
New-Item -ItemType Directory -Path $clases -Force | Out-Null

$jars = Resolver-Jars -Rutas $empaquetar -Repositorio $Repo
$jarsProvided = Resolver-Jars -Rutas $provided -Repositorio $Repo
$jarsCompilacion = $jars + $jarsProvided

$fuentes = @(Get-ChildItem (Join-Path $raiz 'src\main\java') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
if ($fuentes.Count -eq 0) { throw 'No hay fuentes Java en src\main\java' }

# javac recibe los argumentos en un archivo auxiliar: evita el limite de longitud
# de la linea de comandos de Windows. Las rutas van con barras diagonales.
$classpath = ($jarsCompilacion -join ';').Replace('\', '/')
$argfile = Join-Path $env:TEMP ("guatecompras-javac-" + $PID + ".txt")
$lineas = @(
    '-encoding', 'UTF-8',
    '--release', '17',
    '-nowarn',
    '-cp', ('"' + $classpath + '"'),
    '-d', ('"' + $clases.Replace('\', '/') + '"')
)
foreach ($fuente in $fuentes) { $lineas += ('"' + $fuente.Replace('\', '/') + '"') }
Set-Content -Path $argfile -Value $lineas -Encoding ASCII
try {
    & $javac "@$argfile"
    if ($LASTEXITCODE -ne 0) { throw "javac termino con codigo $LASTEXITCODE" }
} finally {
    Remove-Item $argfile -ErrorAction SilentlyContinue
}
Write-Host "$($fuentes.Count) fuentes Java compiladas"

# Recursos: mismo criterio que pom.xml (db.properties queda fuera del despliegue).
$recursos = Join-Path $raiz 'src\main\resources'
if (Test-Path $recursos) {
    Copy-Item -Path (Join-Path $recursos '*') -Destination $clases -Recurse -Force
    Remove-Item -Path (Join-Path $clases 'db.properties') -ErrorAction SilentlyContinue
}

if ($SoloClases) {
    Write-Host "Clases listas en $clases"
    exit 0
}

# Despliegue equivalente al WAR: src/main/webapp + clases + librerias.
$despliegue = Join-Path $raiz 'target\sistema-adquisiciones'
if (Test-Path $despliegue) {
    try {
        Remove-Item $despliegue -Recurse -Force -ErrorAction Stop
    } catch {
        # El servidor activo bloquea archivos en Windows: se actualizan en lugar de recrear.
        Write-Warning 'No se pudo regenerar el despliegue desde cero (servidor activo); se actualizan los archivos existentes.'
    }
}
$webInf = Join-Path $despliegue 'WEB-INF'
New-Item -ItemType Directory -Path $webInf -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $webInf 'classes') -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $webInf 'lib') -Force | Out-Null
Copy-Item -Path (Join-Path $raiz 'src\main\webapp\*') -Destination $despliegue -Recurse -Force
Copy-Item -Path (Join-Path $clases '*') -Destination (Join-Path $webInf 'classes') -Recurse -Force
foreach ($jar in $jars) {
    $destinoJar = Join-Path (Join-Path $webInf 'lib') (Split-Path $jar -Leaf)
    if (!(Test-Path $destinoJar)) { Copy-Item -Path $jar -Destination (Join-Path $webInf 'lib') -Force }
}

Write-Host "Despliegue listo en $despliegue ($($jars.Count) librerias)"
