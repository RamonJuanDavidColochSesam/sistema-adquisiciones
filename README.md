# GuateCompras

Sistema institucional de adquisiciones para Base de Datos I, UMG Salamá: sucursales, departamentos, artículos, proveedores, pedidos, órdenes, ofertas, adjudicación transaccional, evaluación, cuatro roles, permisos, dashboard y auditoría.

## Tecnologías y arquitectura

Java17 · Maven3.9.9 · Jersey/Jakarta Servlet6 · JDBC/HikariCP · SQL Server operativo · PostgreSQL como segundo modelo/verificación · Tomcat10.1 · HTML/CSS/JavaScript · WPF/.NET10 · ocho RDL SSRS2016.

Portal y WPF → API REST → autorización/servicios → DAO → SQL Server. PostgreSQL tiene conexión y pruebas independientes; sin réplica automática. SSRS consulta SQL Server mediante identidad Windows separada.

## Requisitos y preparación

Java17, Maven, Tomcat10.1, SQL Server/PostgreSQL y .NET Desktop Runtime10 en Windows para WPF. Visual Studio para editar. Conservar bases actuales: **no ejecutar DDL inicial sobre ellas**. Configurar config/local-db.properties desde el ejemplo, sin subir secretos.

## Instalación y ejecución

```powershell
./scripts/build.ps1 -Maven mvn
./scripts/bases.ps1 -Accion Migrar -Java java
./scripts/bases.ps1 -Accion Semilla -Java java
./scripts/bases.ps1 -Accion Verificar -Java java
./scripts/iniciar_tomcat.ps1 -Java RUTA_JAVA17 -TomcatHome RUTA_TOMCAT10
./scripts/build_desktop.ps1
```

Abrir http://127.0.0.1:18080/sistema-adquisiciones/ y mantener Tomcat activo. Ejecutar EXE Release WPF o abrir desktop/GuateCompras.sln en Visual Studio. URL de API editable en login. Para restricciones del sandbox Windows, build Java -RestrictedWindows y WPF -Isolated.

### Ejecución local en este equipo (VS Code y Eclipse)

En este equipo Tomcat corre en el **puerto 8080** (no 18080) y no hay Maven instalado, así que la compilación local usa `scripts/compilar_backend.ps1` (javac + librerías del repositorio `~/.m2`).

- **VS Code:** F5 con `GuateCompras: portal web (Edge)` ejecuta la tarea `GuateCompras: iniciar backend` (compila, crea `target/tomcat-base` y arranca Tomcat) y abre el navegador en `http://127.0.0.1:8080/sistema-adquisiciones/portal.html`. Si el servidor ya está corriendo, use la configuración `GuateCompras: portal web (servidor ya corriendo)`. Para detenerlo: panel de Tareas → detener tarea.
- **Eclipse:** servidor `apache-tomcat-10.1.36 at localhost` (mismo puerto 8080); `ConexionManager` localiza `config/local-db.properties` aunque el directorio de trabajo del servidor no sea el del proyecto.
- Tareas útiles: `GuateCompras: compilar backend`, `GuateCompras: verificar bases`, `GuateCompras: verificar JavaScript`.

## Bases y usuarios de prueba

DDL de estructura: `db/ddl/sqlserver.sql` y `db/ddl/postgres.sql` (21 tablas, vista vw_OrdenesAbiertas, índices, restricciones y 10 triggers de reglas de negocio). Verificado el 14 de octubre de 2026 contra ambas bases reales: recuentos idénticos de tablas, columnas, claves, índices, vistas y triggers. Semilla idempotente, mínimos 5/10/50/20/100. Usuarios admin, gestor, proveedor y auditor; contraseñas aleatorias en config/demo-access.properties, privado/excluido. No se incluyen claves reales en README o paquetes.

## Reportes y WPF

Ocho consultas en db/queries y API /reportes; CSV WPF. RDL en reports/ssrs. SSRS 2022 instalado y configurado por script (scripts/instalar_ssrs.ps1); ocho informes publicados y ocho PDF reales verificados el 9 de octubre de 2026. Integración web/WPF usa GUATECOMPRAS_SSRS_URL y autenticación Windows efectivos.

## Pruebas y calidad

```powershell
python tests/integration/run.py
./scripts/calidad.ps1 -Maven mvn
```

Suite HTTP, ambos motores y self-test WPF reales; no JUnit ni cobertura unitaria certificada. PMD/CPD:10→5 advertencias y un grupo de duplicación. Documento oficial exige Sonat para entrega; no exige Sonar.

## Estructura

```text
src/main/java/     Backend y módulos
src/main/webapp/   Portal y páginas conservadas
desktop/          WPF Visual Studio
db/               DDL de ambos motores y consultas
reports/ssrs/     Ocho RDL y fuente compartida
scripts/          Build, bases, Tomcat, SSRS, calidad
tests/integration Pruebas reales y limpieza de fixtures
config/           Ejemplos públicos; privados ignorados
```

Matriz de 75 requisitos y auditoría final: documentación archivada el 14 de octubre de 2026 (recuperable desde el historial de git, carpeta docs/). Cierre externo incompleto: entrega Sonat pendiente. No se declara 100%; paquetes sin credenciales ni herramientas de trabajo.
