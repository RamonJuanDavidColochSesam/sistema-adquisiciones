# Evidencia de verificación

5 de octubre de 2026. Ejecuciones desde `C:\Users\dinae\Desktop\ProyectoBD\sistema-adquisiciones`.

## Herramientas

```text
Apache Maven 3.9.9 (8e8579a9e76f7d015ee5ec7bfcdc97d260186937)
Java version: 17.0.20.1, vendor: Microsoft
openjdk version "17.0.20.1" 2026-08-18 LTS
```

Herramientas portables en el workspace, no instalación permanente. La fecha del build es la observada por el comando; no se toma la versión de Eclipse como Java17.

## Comandos exactos Maven

```powershell
Set-Location 'C:\Users\dinae\Desktop\ProyectoBD\sistema-adquisiciones'
$env:JAVA_HOME='C:\Users\dinae\Documents\Codex\2026-10-05\files-pasted-by-the-user-misi\work\tooling\jdk17\jdk-17.0.20.1+1'
$auditMaven='C:\Users\dinae\Documents\Codex\2026-10-05\files-pasted-by-the-user-misi\work\tooling\apache-maven-3.9.9\bin\mvn.cmd'
$auditRepo='-Dmaven.repo.local=C:\Users\dinae\Documents\Codex\2026-10-05\files-pasted-by-the-user-misi\work\maven-repository'
& "$env:JAVA_HOME\bin\java.exe" -version
& $auditMaven -version
& $auditMaven -B -e $auditRepo clean verify
# Diagnóstico alternativo, no aceptación del error de compilador:
& $auditMaven -B -e $auditRepo '-Dmaven.compiler.fork=true' clean verify
```

En otra computadora sustituir rutas por herramientas instaladas y ejecutar `mvn -B -e clean verify`. La caché aislada no cambia dependencias del pom.

## Resultado Maven estándar limpio

```text
Compiling 30 source files with javac [debug target 17] to target\classes
BUILD FAILURE
Failed to execute goal maven-compiler-plugin:3.13.0:compile
Caused by: java.nio.file.AccessDeniedException:
...\jakarta.activation-api\2.1.0\jakarta.activation-api-2.1.0.jar
```

Código 1. La excepción se origina al cerrar un sistema ZIP/JAR en Windows (`WindowsLinkSupport.getRealPath`, `ZipFileSystem.close`). No hay evidencia de error de sintaxis Java que justifique cambiar código funcional.

## Resultado del modo fork

```text
Compiling 30 source files with javac [forked debug target 17] to target\classes
[ERROR] An exception has occurred in the compiler (17.0.20.1)
java.nio.file.AccessDeniedException: ...jakarta.activation-api-2.1.0.jar
No tests to run.
Building war: ...\target\sistema-adquisiciones.war
BUILD SUCCESS
```

Código 0, pero existe error de compilador. Se generaron 30 clases Java17 y WAR; **no se certifica compilación limpia sin incidencias**. Los éxitos incrementales anteriores indicaban `Nothing to compile` y solo demostraban empaquetado.

## Inspección WAR

```text
Clases propias: 30
Major bytecode: 61 (Java 17), en las 30 clases
JARs en WEB-INF/lib: 30
db.properties en WEB-INF/classes: presente
SHA256: f38279b894246620f857442541088b738b1f36b53604dd068a134539bc7f168a
```

No se distribuye el WAR porque empaqueta configuración privada. No se imprimieron sus valores. No se probó arranque/HTTP.

## JDBC y pool real

```text
sqlserver: CONNECTED; SELECT 1=1; ms=9747
sqlserver tablas=0
Sucursal/Departamento/Articulo/Proveedor/Pedido/OrdenCompra/Oferta/
Adjudicacion/Usuario/Rol/Permiso:
NO_VERIFICADO; SQLState=S0002; code=208
postgres: FAILED; tipo=PSQLException; ms=295
SQLState=28P01; code=0
database=GuateCompras; view_definition=1; esquema=dbo
HIKARI_MANAGER=FAILED; tipo=PSQLException
SQLState=28P01; code=0
```

JDBC directo SQL Server está comprobado. GuateCompras no tiene el esquema necesario. PostgreSQL rechaza autenticación y bloquea construcción del manager conjunto. Duraciones incluyen carga/conexión/consulta; no son métricas de rendimiento aisladas. SLF4J informó falta de implementación de logging; el pool no dispone de backend de logs útil en la sonda.

## Reproducir sondas sin revelar secretos

`ProbeDb.java` y `ProbeContext.java` están en `docs/evidencias`. Leen la ruta de configuración recibida como argumento, sin contraseñas incrustadas; imprimen resultados, tipos de error y SQLState/código. Solo lectura; no crean tablas/usuarios/datos. `ProbeDb` usa drivers reales; `ProbeContext` invoca por reflexión el ConexionManager existente.

Desde la raíz con JDK17 y clases/dependencias disponibles:

```powershell
$auditCp='target/classes;target/sistema-adquisiciones/WEB-INF/lib/*'
& "$env:JAVA_HOME\bin\java.exe" -cp $auditCp docs/evidencias/ProbeDb.java src/main/resources/db.properties
& "$env:JAVA_HOME\bin\java.exe" -cp $auditCp docs/evidencias/ProbeContext.java src/main/resources/db.properties
```

Si el launcher de fuente no resuelve correctamente clases del proyecto, compilar las sondas a un directorio temporal autorizado con javac y ejecutarlas por nombre usando el mismo classpath. La sonda Context final se ejecutó con classpath explícito a target/classes y JAR de drivers/Hikari/SLF4J en la caché aislada. No confundir sondas con pruebas E2E.

## Archivos y límites

Logs completos en `docs/evidencias`: maven-baseline.log, maven-jdk17.log, maven-clean-verify.log, maven-fork-verify.log, maven-clean-fork.log, jdbc-probe.log, hikari-probe.log y war-inspection.json. No incluyen valores de configuración privada.

Verificado: fuentes/materiales, herramientas, dependencias, intentos Maven, WAR/bytecode y JDBC. No verificado: build limpio sin incidencias, arranque, login/BCrypt con datos reales, CRUD/permisos, restricciones SQL ejecutadas, flujo, reportes/PDF y E2E. No se instalaron esquema ni nuevas funcionalidades.
