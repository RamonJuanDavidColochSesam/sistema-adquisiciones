# Preparación, compilación y despliegue

## Manual de instalación y ejecución

GuateCompras · revisión de cierre 6 de octubre de 2026.

## Entorno y alcance

Backend Java 17, Maven 3.9.9, Jersey/Jakarta Servlet 6 y Tomcat 10.1.60. Escritorio WPF .NET 10 para Windows; Visual Studio con carga de trabajo de escritorio .NET. SQL Server es el motor operativo. PostgreSQL conserva el mismo modelo, migraciones, semilla y pruebas reales. No existe réplica automática entre motores.

Las bases y sus conexiones ya estaban preparadas por el usuario. **No ejecutar los DDL iniciales sobre las bases existentes.** `schema.sql` y `db/schema_postgres.sql` se conservan para una instalación nueva sobre bases vacías. Los cambios de las bases actuales se realizan con migraciones versionadas.

## Configuración privada

Desde la raíz del proyecto, copiar `config/db.example.properties` a `config/local-db.properties` y completar sus seis propiedades con las credenciales propias. En esta computadora ese archivo ya existe y conserva los accesos comprobados. No incluirlo en Git ni en entregas.

`ConexionManager` acepta `-Dguatecompras.config=RUTA_ABSOLUTA`, `GUATECOMPRAS_CONFIG` o las variables `GUATECOMPRAS_SQLSERVER_URL`, `GUATECOMPRAS_SQLSERVER_USER`, `GUATECOMPRAS_SQLSERVER_PASSWORD`, `GUATECOMPRAS_POSTGRES_URL`, `GUATECOMPRAS_POSTGRES_USER` y `GUATECOMPRAS_POSTGRES_PASSWORD`. La propiedad Java tiene precedencia para seleccionar archivo; las variables individuales prevalecen sobre sus valores. No imprimir contraseñas en comandos ni logs.

## Backend y bases

Ejecutar en PowerShell desde la raíz del repositorio:

```powershell
$env:JAVA_HOME='RUTA_DEL_JDK_17'
./scripts/build.ps1 -Maven 'RUTA_MAVEN/bin/mvn.cmd'
./scripts/bases.ps1 -Accion Migrar -Java "$env:JAVA_HOME/bin/java.exe"
./scripts/bases.ps1 -Accion Semilla -Java "$env:JAVA_HOME/bin/java.exe"
./scripts/bases.ps1 -Accion Verificar -Java "$env:JAVA_HOME/bin/java.exe"
```

Si el sandbox de Windows bloquea la lectura ZIPFS de dependencias por `AccessDeniedException`, usar `./scripts/build.ps1 -Maven ... -RestrictedWindows -Repository 'RUTA_REPOSITORIO_MAVEN'`. Este perfil descomprime dependencias y compila todas las fuentes con ECJ 3.36.0 mediante una tarea Maven que falla ante errores. La compilación Java estándar sigue disponible sin ese parámetro. No aceptar un WAR parcial como prueba.

El WAR es `target/sistema-adquisiciones.war`. Los scripts de bases usan las clases y bibliotecas ensambladas en `target/sistema-adquisiciones/WEB-INF`. Las migraciones V001/V002/V003/V004 se ejecutan una sola vez por motor mediante `SchemaVersion`; cada versión confirma o revierte su propia transacción. La semilla es repetible por claves naturales y no borra información existente ni cambia contraseñas de usuarios que ya existen. Antes de migrar una base con datos ajenos a esta demo, realizar el respaldo normal del motor y revisar las restricciones nuevas: una contradicción debe resolverse, no ocultarse.

La semilla crea cuatro contraseñas aleatorias en el archivo privado `config/demo-access.properties` para el primer acceso. Los hashes BCrypt se almacenan en ambas bases; el archivo local es una hoja de acceso de la demostración, no una tabla ni un recurso del WAR. Mantener acceso local restringido a ese archivo, cambiar las contraseñas después de la presentación si se reutiliza el sistema y no entregar la hoja privada en Sonat. Si un usuario demo ya existía, su contraseña no se reemplaza por la semilla.

## Tomcat y web

En `conf/server.xml` de Tomcat configurar el conector HTTP de la demo en `address="127.0.0.1" port="18080"`. Ejecutar:

```powershell
./scripts/iniciar_tomcat.ps1 -TomcatHome 'RUTA_TOMCAT_10_1' -Java "$env:JAVA_HOME/bin/java.exe"
```

Abrir `http://127.0.0.1:18080/sistema-adquisiciones/`. Mantener el servidor activo mientras se usa web o escritorio. Ctrl+C termina el proceso de ese script. No desplegar simultáneamente otro contexto con la misma semilla de pruebas. Para acceso en una red, configurar HTTPS, cookies seguras y permisos de red de acuerdo al entorno; esta entrega fue comprobada en loopback HTTP.

## Visual Studio / WPF

Abrir `desktop/GuateCompras.sln`. Restaurar y compilar Release. Alternativa:

```powershell
./scripts/build_desktop.ps1
## Solo cuando el entorno restringido impide leer el perfil NuGet normal:
./scripts/build_desktop.ps1 -Isolated
./desktop/GuateCompras.Desktop/bin/Release/net10.0-windows/GuateCompras.Desktop.exe
```

La ventana de login permite editar la URL API. Predeterminada: `http://127.0.0.1:18080/sistema-adquisiciones/api/`. También se puede establecer `GUATECOMPRAS_API_URL` antes de iniciar. WPF consume la API; no guarda contraseñas ni necesita JDBC. El ejecutable entregado requiere el runtime Windows Desktop .NET 10 instalado.

## SSRS

**SSRS está instalado, configurado y publicado en este equipo (9 de octubre de 2026): catálogo ReportServer, URLs de servicio y portal, ocho RDL publicados y ocho PDF reales verificados.** Instalación y configuración automatizadas en `scripts/instalar_ssrs.ps1` (consola elevada, un solo aviso de Control de Cuenta de Usuario) y `scripts/reinstalar_ssrs.ps1` para un ciclo completo; publicación y verificación en `scripts/publicar_ssrs.ps1` y `scripts/verificar_ssrs.ps1`. Seguir `REPORTES_SSRS.md`; no se incluyen PDFs simulados.

## Pruebas

Con Tomcat iniciado y la semilla instalada:

```powershell
python tests/integration/run.py
./scripts/bases.ps1 -Accion Verificar
./desktop/GuateCompras.Desktop/bin/Release/net10.0-windows/GuateCompras.Desktop.exe --self-test config/demo-access.properties docs/evidencias/wpf-local
```

La suite Python usa solo la biblioteca estándar, lee las contraseñas locales sin imprimirlas y crea/elimina registros ficticios `TEST-*`; los eventos de auditoría permanecen. Usarla sobre una base de demostración. `GUATECOMPRAS_TEST_URL` permite cambiar el contexto HTTP de pruebas. Maven verifica compilación y empaquetado; no contiene pruebas JUnit y su mensaje «No tests to run» no cuenta como prueba funcional.

## Errores conocidos resueltos

- Pools antes acoplados: ahora lazy e independientes, cierre al destruir contexto.
- JAR/ZIPFS inaccesible en sandbox: perfil ECJ real, conservando compilación estándar.
- NuGet lee un perfil inaccesible: aislamiento de variables solo durante el proceso de build; no se altera el perfil del usuario.
- Formatos de fechas: ISO en API, día local en web/WPF; no convertir fechas de negocio mediante UTC.
- Referencias al borrar: HTTP 409 o regla 400 explicada; conservar historial y usar estado Inactivo.
- SSRS no disponible: configurar el servidor; CSV y consulta web no equivalen a renderizado SSRS.


> Cierre: SSRS instalado, configurado y publicado con ocho PDF verificados. En otra máquina, seguir `scripts/instalar_ssrs.ps1` según REPORTES_SSRS.md; no reinstalar innecesariamente.

[Volver a Manual de instalación](README.md)
