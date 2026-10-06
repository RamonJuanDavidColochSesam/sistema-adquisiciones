# GuateCompras

Sistema institucional de adquisiciones para Base de Datos I, UMG Salamá: sucursales, departamentos, artículos, proveedores, pedidos, órdenes, ofertas, adjudicación transaccional, evaluación, cuatro roles, permisos, dashboard y auditoría.

## Tecnologías y arquitectura

Java17 · Maven3.9.9 · Jersey/Jakarta Servlet6 · JDBC/HikariCP · SQL Server operativo · PostgreSQL como segundo modelo/verificación · Tomcat10.1 · HTML/CSS/JavaScript · WPF/.NET10 · ocho RDL SSRS2016.

Portal y WPF → API REST → autorización/servicios → DAO → SQL Server. PostgreSQL tiene conexión, migraciones y pruebas independientes; sin réplica automática. SSRS consulta SQL Server mediante identidad Windows separada.

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

Abrir http://127.0.0.1:18080/sistema-adquisiciones/ y mantener Tomcat activo. Ejecutar EXE Release WPF o abrir desktop/GuateCompras.sln en Visual Studio. URL de API editable en login. Para restricciones del sandbox Windows, build Java -RestrictedWindows y WPF -Isolated. [Manual completo](docs/MANUAL_INSTALACION.md).

## Bases y usuarios de prueba

DDL SQL Server: schema.sql; PostgreSQL: db/schema_postgres.sql. Migraciones V001–V004 por motor. Semilla idempotente, mínimos 5/10/50/20/100. Usuarios admin, gestor, proveedor y auditor; contraseñas aleatorias en config/demo-access.properties, privado/excluido. No se incluyen claves reales en README o paquetes.

## Reportes y WPF

Ocho consultas en db/queries y API /reportes; CSV WPF. RDL en reports/ssrs. SSRS instalado, catálogo/URLs, publicación y ocho PDF pendientes. [Inventario](docs/SSRS_INVENTARIO.md), [publicación/aceptación](docs/REPORTES_SSRS.md). Integración web/WPF requiere GUATECOMPRAS_SSRS_URL y permisos Windows efectivos.

## Pruebas y calidad

```powershell
python tests/integration/run.py
./scripts/calidad.ps1 -Maven mvn
```

Suite HTTP, ambos motores y self-test WPF reales; no JUnit ni cobertura unitaria certificada. PMD/CPD:10→5 advertencias y un grupo de duplicación. [Detalle](docs/CALIDAD_SOFTWARE.md). Documento oficial exige Sonat para entrega; no exige Sonar.

## Documentación oficial preparada para GitBook

[Inicio](gitbook/README.md) · [16 capítulos](gitbook/SUMMARY.md) · [sincronización/entrega](gitbook/sincronizar.md). Espacio solicitado: https://app.gitbook.com/o/bcbMuvR3ChdMMJmBjl68/s/I4ADiWkYzeTnxOULsd7Y/ . Preparada; publicación no realizada sin sesión/conexión autenticada.

## Estructura

```text
src/main/java/     Backend y módulos
src/main/webapp/   Portal y páginas conservadas
desktop/          WPF Visual Studio
db/               DDL, migraciones y consultas
reports/ssrs/     Ocho RDL y fuente compartida
scripts/          Build, bases, Tomcat, SSRS, calidad
tests/integration Pruebas reales y limpieza de fixtures
docs/             Manuales, matriz, auditoría, evidencia
gitbook/          Documentación web/activos para Git Sync
config/           Ejemplos públicos; privados ignorados
```

[75 requisitos](docs/MATRIZ_REQUISITOS.md) · [Evidencia](docs/EVIDENCIA_VERIFICACION.md) · [Auditoría](docs/AUDITORIA_FINAL.md). Cierre externo incompleto: SSRS real, GitBook publicado y Sonat pendientes. No se declara 100%; paquetes sin credenciales ni herramientas de trabajo.
