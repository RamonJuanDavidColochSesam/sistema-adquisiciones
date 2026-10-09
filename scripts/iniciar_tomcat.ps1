<#
  Compila (opcional), prepara una base de Tomcat en target\tomcat-base y
  arranca el servidor en primer plano sobre el puerto de conf/server.xml (8080).

  Uso:
    scripts\iniciar_tomcat.ps1 -Compilar   # compila y arranca (lo que usa VS Code con F5)
    scripts\iniciar_tomcat.ps1             # solo arranca (requiere compilacion previa)

  Ctrl+C detiene unicamente este proceso.
#>
param(
    [string]$TomcatHome = '',
    [string]$Java = '',
    [string]$Config = '',
    [switch]$Compilar
)
$ErrorActionPreference = 'Stop'
$raiz = Split-Path $PSScriptRoot -Parent

function Obtener-Tomcat {
    $candidatos = @()
    if ($TomcatHome) { $candidatos += $TomcatHome }
    if ($env:CATALINA_HOME) { $candidatos += $env:CATALINA_HOME }
    $carpetas = @($raiz, (Split-Path $raiz -Parent), (Split-Path (Split-Path $raiz -Parent) -Parent))
    foreach ($base in $carpetas) {
        if ($base -and (Test-Path $base)) {
            foreach ($t in (Get-ChildItem $base -Directory -Filter 'apache-tomcat-*' -ErrorAction SilentlyContinue)) {
                $candidatos += $t.FullName
            }
        }
    }
    foreach ($c in $candidatos) {
        if ($c -and (Test-Path (Join-Path $c 'bin\bootstrap.jar'))) { return (Resolve-Path $c).Path }
    }
    throw 'No se encontro Tomcat 10 (bin\bootstrap.jar). Use -TomcatHome "C:\ruta\apache-tomcat-10.x" o defina CATALINA_HOME.'
}

function Obtener-Java {
    $candidatos = @()
    if ($Java) { $candidatos += $Java }
    if ($env:JAVA_HOME) { $candidatos += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
    $enPath = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($enPath) { $candidatos += $enPath.Source }
    $bases = @(
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Amazon Corretto'
    )
    foreach ($base in $bases) {
        if (Test-Path $base) {
            foreach ($hijo in (Get-ChildItem $base -Directory -ErrorAction SilentlyContinue)) {
                $candidatos += (Join-Path $hijo.FullName 'bin\java.exe')
            }
        }
    }
    foreach ($c in $candidatos) {
        if ($c -and (Test-Path $c)) { return $c }
    }
    throw 'No se encontro java.exe. Use -Java "C:\ruta\al\jdk\bin\java.exe" o defina JAVA_HOME.'
}

try {
    if ($Compilar) { & (Join-Path $PSScriptRoot 'compilar_backend.ps1') }

    $tomcat = Obtener-Tomcat
    $javaExe = Obtener-Java

    $despliegue = Join-Path $raiz 'target\sistema-adquisiciones'
    $war = Join-Path $raiz 'target\sistema-adquisiciones.war'
    $explorado = Test-Path (Join-Path $despliegue 'WEB-INF\classes')
    if (!$explorado -and !(Test-Path $war)) {
        throw 'No hay compilacion en target. Ejecute scripts\compilar_backend.ps1 o vuelva a iniciar con -Compilar.'
    }

    if (!$Config) { $Config = Join-Path $raiz 'config\local-db.properties' }
    if (Test-Path $Config) { $Config = (Resolve-Path $Config).Path }
    else {
        Write-Warning "No se encontro $Config; la aplicacion usara db.properties del classpath o las variables de entorno."
        $Config = ''
    }

    # Base propia en target: no toca la instalacion de Tomcat ni el servidor de Eclipse.
    $base = Join-Path $raiz 'target\tomcat-base'

    # Puerto leido de la instalacion de Tomcat: la base propia se reconstruye mas abajo.
    $xml = [xml](Get-Content (Join-Path $tomcat 'conf\server.xml'))
    $conectores = @($xml.Service.Connector) | Where-Object { $_.protocol -eq 'HTTP/1.1' }
    $puerto = 8080
    if ($conectores) { $puerto = [int](@($conectores)[0].port) }

    # Si el puerto lo ocupa una instancia propia previa (F5 repetido), se recicla;
    # cualquier otro proceso en el puerto se reporta y no se toca.
    $ocupado = Get-NetTCPConnection -State Listen -LocalPort $puerto -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($ocupado) {
        $anterior = Get-CimInstance Win32_Process -Filter "ProcessId=$($ocupado.OwningProcess)" -ErrorAction SilentlyContinue
        $esNuestra = $anterior -and $anterior.CommandLine -and $anterior.CommandLine -like "*-Dcatalina.base=$base*"
        if (!$esNuestra) {
            $proceso = Get-Process -Id $ocupado.OwningProcess -ErrorAction SilentlyContinue
            $nombre = if ($proceso) { $proceso.ProcessName } else { 'desconocido' }
            throw "El puerto $puerto ya esta en uso (proceso $($ocupado.OwningProcess): $nombre). Cierre ese servidor antes de iniciar desde VS Code."
        }
        Write-Host "Puerto $puerto ocupado por una instancia propia: deteniendo la anterior (PID $($ocupado.OwningProcess))..."
        Stop-Process -Id $ocupado.OwningProcess -Force -ErrorAction SilentlyContinue
        for ($intento = 0; $intento -lt 40 -and (Get-NetTCPConnection -State Listen -LocalPort $puerto -ErrorAction SilentlyContinue); $intento++) {
            Start-Sleep -Milliseconds 250
        }
        if (Get-NetTCPConnection -State Listen -LocalPort $puerto -ErrorAction SilentlyContinue) {
            throw "El puerto $puerto sigue ocupado tras detener la instancia propia. Cierre ese servidor antes de iniciar desde VS Code."
        }
    }

    if (Test-Path $base) { Remove-Item $base -Recurse -Force }
    foreach ($carpeta in @('conf', 'logs', 'temp', 'work', 'webapps')) {
        New-Item -ItemType Directory -Path (Join-Path $base $carpeta) -Force | Out-Null
    }
    Copy-Item -Path (Join-Path $tomcat 'conf\*') -Destination (Join-Path $base 'conf') -Recurse -Force

    if ($explorado) {
        Copy-Item -Path $despliegue -Destination (Join-Path $base 'webapps\sistema-adquisiciones') -Recurse -Force
    } else {
        Copy-Item -Path $war -Destination (Join-Path $base 'webapps') -Force
    }

    Write-Host "Tomcat : $tomcat"
    Write-Host "Java   : $javaExe"
    Write-Host "Base   : $base"
    Write-Host "URL    : http://127.0.0.1:$puerto/sistema-adquisiciones/"

    Push-Location $raiz
    try {
        $argumentos = @(
            '--enable-native-access=ALL-UNNAMED',
            # Necesarios: Tomcat 10.1 verifica que el cache de rutas canonicas este
            # desactivado (CVE-2024-56337) y para eso requiere estos --add-opens.
            '--add-opens=java.base/java.lang=ALL-UNNAMED',
            '--add-opens=java.base/java.io=ALL-UNNAMED',
            '--add-opens=java.base/java.util=ALL-UNNAMED',
            '--add-opens=java.base/java.util.concurrent=ALL-UNNAMED',
            '--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED',
            '-Dfile.encoding=UTF-8',
            "-Dcatalina.base=$base",
            "-Dcatalina.home=$tomcat",
            "-Djava.io.tmpdir=$(Join-Path $base 'temp')",
            '-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager',
            "-Djava.util.logging.config.file=$(Join-Path $base 'conf\logging.properties')",
            "-Djava.library.path=$(Join-Path $tomcat 'bin')"
        )
        if ($Config) { $argumentos += "-Dguatecompras.config=$Config" }
        $argumentos += @('-cp', "$(Join-Path $tomcat 'bin\bootstrap.jar');$(Join-Path $tomcat 'bin\tomcat-juli.jar')", 'org.apache.catalina.startup.Bootstrap')

        & $javaExe @argumentos
        Write-Host 'Servidor detenido.'
    } finally {
        Pop-Location
    }
} catch {
    Write-Host "SERVIDOR-NO-INICIADO: $($_.Exception.Message)"
    exit 1
}
