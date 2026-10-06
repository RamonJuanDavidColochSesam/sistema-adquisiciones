# Instalación reproducible y comprobaciones

Complemento de los capítulos 4 y 22. Los comandos se revisaron en las fuentes; esta tarea documental no los ejecutó. Usar PowerShell desde la raíz del proyecto, con rutas y accesos propios.

## 1. Requisitos

JDK 17, Maven (la evidencia usa 3.9.9), Tomcat 10.1 compatible con Servlet 6, SQL Server y PostgreSQL. WPF requiere Windows, SDK compatible con net10.0-windows y Windows Desktop Runtime 10. SSRS requiere configuración propia; los scripts SOAP utilizan Windows PowerShell 5.1. Python ejecuta las pruebas HTTP; pdfplumber se utilizó para comparar PDF.

La configuración de servicios y del conector Tomcat es externa. La demo usa 127.0.0.1:18080. Eclipse es opcional. El encabezado de Visual Studio de la solución no garantiza compatibilidad de cualquier IDE con .NET 10: debe usarse uno que soporte el target. Jasper y Docusaurus no forman parte de esta instalación.

## 2. Bases nuevas y existentes

Para una instalación vacía crear ambas bases GuateCompras antes del DDL. SQL Server requiere una herramienta que interprete GO; PostgreSQL ejecuta db/schema_postgres.sql contra la base destino. Ejemplos:

```powershell
sqlcmd -S localhost -E -i schema.sql
psql -h localhost -U usuario_instalacion -d GuateCompras -f db/schema_postgres.sql
```

La base PostgreSQL debe tener el nombre exacto indicado en la configuración; `CREATE DATABASE "GuateCompras";` conserva mayúsculas. No incluir contraseñas en comandos. Crear usuarios y privilegios con el administrador del motor; no se certificaron todos los GRANT efectivos.

En el equipo existente no volver a ejecutar el DDL base. MigrarBases gestiona versiones y no crea las 18 tablas originales. Las utilidades que recorren ambos motores necesitan ambos disponibles; el CRUD web opera en SQL Server.

## 3. Configuración externa

Copiar el ejemplo únicamente si local-db.properties no existe:

```powershell
Copy-Item config/db.example.properties config/local-db.properties
$env:JAVA_HOME='C:\Herramientas\jdk17'
```

Editar localmente las propiedades URL, usuario y contraseña de ambos motores. No publicar ese archivo. El ejemplo usa SQL Server loopback 1433 con encrypt/trustServerCertificate para demo y PostgreSQL loopback 5432.

El archivo se resuelve por `-Dguatecompras.config`, después GUATECOMPRAS_CONFIG y finalmente config/local-db.properties. Las variables GUATECOMPRAS_SQLSERVER_URL/USER/PASSWORD y sus equivalentes POSTGRES prevalecen sobre valores del archivo. No se requiere db.properties dentro del WAR. La contraseña JDBC es distinta de la identidad Windows y de la clave de Usuario del sistema.

## 4. Compilar, migrar y preparar la demo

```powershell
./scripts/build.ps1 -Maven 'C:\Herramientas\apache-maven-3.9.9\bin\mvn.cmd'
./scripts/bases.ps1 -Accion Migrar -Java "$env:JAVA_HOME/bin/java.exe"
./scripts/bases.ps1 -Accion Semilla -Java "$env:JAVA_HOME/bin/java.exe"
./scripts/bases.ps1 -Accion Verificar -Java "$env:JAVA_HOME/bin/java.exe"
```

En el entorno Windows con ZIPFS restringido se usa -RestrictedWindows y -Repository con una ruta real. El perfil compila con ECJ 3.36.0; la evidencia de clean es distinta de un WAR incremental. La salida es target/sistema-adquisiciones.war; las utilidades usan sus clases y bibliotecas descomprimidas.

Semilla es opcional. Genera datos académicos y config/demo-access.properties privado; no sobrescribe contraseñas de usuarios existentes. Ejecutarla después de las migraciones.

## 5. Tomcat y portal

Configurar conf/server.xml del Tomcat con dirección/puerto adecuados. La demo usa 127.0.0.1 y 18080. Elegir el servicio SSRS antes del arranque:

```powershell
$env:GUATECOMPRAS_SSRS_URL='http://localhost/ReportServer'
./scripts/iniciar_tomcat.ps1 -TomcatHome 'C:\Herramientas\apache-tomcat-10.1.60' -Java "$env:JAVA_HOME/bin/java.exe"
```

El script copia el WAR y ejecuta Tomcat en primer plano; no configura automáticamente el conector. Mantener la consola activa; Ctrl+C detiene ese proceso. Abrir http://127.0.0.1:18080/sistema-adquisiciones/. Login conduce al portal; la API cuelga de /api/.

Un despliegue en red requiere configurar TLS, cookies y límites propios. La demo loopback no acredita ese despliegue.

## 6. Escritorio WPF

```powershell
./scripts/build_desktop.ps1 -Dotnet dotnet
./desktop/GuateCompras.Desktop/bin/Release/net10.0-windows/GuateCompras.Desktop.exe
```

Agregar -Isolated solamente si el perfil NuGet es inaccesible. También puede abrirse desktop/GuateCompras.sln con un IDE compatible. El EXE depende del runtime Windows Desktop .NET 10; no es self-contained. La URL de login es http://127.0.0.1:18080/sistema-adquisiciones/api/. Web y WPF comparten cuentas y necesitan el backend activo.

## 7. SSRS

Configurar ReportServer con sus herramientas y probar URLs. La identidad Windows debe estar autorizada en SSRS y en SQL. Usar Windows PowerShell 5.1 desde la raíz:

```powershell
./scripts/publicar_ssrs.ps1 -ReportServer 'http://localhost/ReportServer' -SqlServer localhost -Database GuateCompras
./scripts/verificar_ssrs.ps1 -ReportServer 'http://localhost/ReportServer' -IdOrden 10 -Salida 'reports/evidencia' -Desde '2025-01-01' -Hasta '2026-12-31'
```

Orden 10 corresponde a la semilla de esta evidencia; en otra instalación elegir una orden adjudicada con ofertas. Los diseños se publican en /GuateCompras y la fuente usa Windows Integrated. El script verifica cabecera PDF; la comparación de datos y revisión visual son controles adicionales.

Este equipo ya completó los ocho informes. Leer los PDF existentes no requiere republicarlos. Un 401 por credenciales Windows exige una sesión autorizada; no se resuelve retirando autenticación. Revisar identidad/permisos ante un error SQL de la fuente. La política PowerShell pertenece al entorno y no se cambia permanentemente por esta guía.

## 8. Verificación

Iniciar sesión, revisar menú por rol, consultar catálogo, diagnóstico, orden e informes. Abrir SSRS con la identidad Windows autorizada. Los siguientes comandos realizan operaciones reales y deben ejecutarse sobre una base de prueba:

```powershell
python tests/integration/run.py
./scripts/bases.ps1 -Accion Verificar -Java "$env:JAVA_HOME/bin/java.exe"
./desktop/GuateCompras.Desktop/bin/Release/net10.0-windows/GuateCompras.Desktop.exe --self-test config/demo-access.properties docs/evidencias/wpf-local
```

Las pruebas leen accesos privados y limpian sus IDs; Auditoria permanece. No existe una suite JUnit que pueda atribuirse a mvn verify. Para estudiar resultados existentes seguir [PRUEBAS.md](PRUEBAS.md), [MANUAL_INSTALACION.md](MANUAL_INSTALACION.md) y [CIERRE_SSRS.md](CIERRE_SSRS.md).
