## Link hacia la documentacion en gitbook: 

https://sistema-de-adquisiciones.gitbook.io/sistema-de-adquisiciones-docs



# GuateCompras

Sistema institucional de adquisiciones para Base de Datos I, UMG Salamá: sucursales, departamentos, artículos, proveedores, pedidos, órdenes, ofertas, adjudicación transaccional, evaluación, cuatro roles, permisos, dashboard y auditoría.

## Tecnologías y arquitectura

Java 17 · Maven 3.9 · Jersey 3.1 / Jakarta Servlet 6 · JDBC/HikariCP · SQL Server operativo · PostgreSQL como segundo modelo/verificación · Tomcat 10.1 · HTML/CSS/JavaScript · WPF/.NET 10 · ocho RDL en formato SSRS 2016 publicados en SSRS 2022.

Portal y WPF → API REST → autorización/servicios → DAO → SQL Server. PostgreSQL tiene conexión y pruebas independientes; sin réplica automática. SSRS consulta SQL Server mediante identidad Windows separada.

## Requisitos y preparación

Java 17, Maven 3.9, Tomcat 10.1, SQL Server/PostgreSQL y .NET Desktop Runtime 10 en Windows para WPF. Visual Studio para editar. Conservar bases actuales: **no ejecutar sobre ellas el DDL ni el DML**, ambos exigen bases vacías. Copiar `src/main/resources/db.properties.example` a `config/local-db.properties` y ajustar las credenciales, sin subir secretos.

## Instalación y ejecución

powershell
./scripts/build.ps1 -Maven mvn
./scripts/bases.ps1 -Accion Semilla -Java java
./scripts/bases.ps1 -Accion Verificar -Java java
./scripts/iniciar_tomcat.ps1 -Java RUTA_JAVA17 -TomcatHome RUTA_TOMCAT10
./scripts/build_desktop.ps1

Abrir http://127.0.0.1:18080/sistema-adquisiciones/ y mantener Tomcat activo. Ejecutar EXE Release WPF o abrir desktop/GuateCompras.sln en Visual Studio. URL de API editable en login. Para restricciones del sandbox Windows, build Java -RestrictedWindows y WPF -Isolated.

Ejecución local en este equipo (VS Code y Eclipse)
En este equipo Tomcat corre en el puerto 8080 (no 18080) y no hay Maven instalado, así que la compilación local usa scripts/compilar_backend.ps1 (javac + librerías del repositorio ~/.m2).

VS Code: F5 con GuateCompras: portal web (Edge) ejecuta la tarea GuateCompras: iniciar backend (compila, crea target/tomcat-base y arranca Tomcat; equivale a ./scripts/iniciar_tomcat.ps1 -Compilar) y abre el navegador en http://127.0.0.1:8080/sistema-adquisiciones/portal.html. Si el servidor ya está corriendo, use la configuración GuateCompras: portal web (servidor ya corriendo). Para detenerlo: panel de Tareas → detener tarea.

Eclipse: servidor apache-tomcat-10.1.36 at localhost (mismo puerto 8080); ConexionManager localiza config/local-db.properties aunque el directorio de trabajo del servidor no sea el del proyecto.

Tareas útiles: GuateCompras: compilar backend, GuateCompras: verificar bases, GuateCompras: verificar JavaScript.

## Bases, datos de prueba y usuarios

DDL de estructura en db/ddl/sqlserver.sql y db/ddl/postgres.sql: 21 tablas, vista vw_OrdenesAbiertas, índices, restricciones y 10 triggers de reglas de negocio. Verificado el 14 de octubre de 2026 contra ambas bases reales: recuentos idénticos de tablas, columnas, claves, índices, vistas y triggers.

## Hay dos formas de poblar una base vacía:

Semilla Java (./scripts/bases.ps1 -Accion Semilla): idempotente, mínimos 5/10/50/20/100, genera contraseñas aleatorias para los cuatro usuarios y las guarda en config/demo-access.properties (privado, excluido de git). Si se pierde ese archivo y se repite la semilla, las claves anteriores dejan de servir.


DML incluido (db/dml/DML SQLSERVER.sql y db/dml/DML POSTGRESQL.sql): cargador de una sola transacción con guardas de tablas vacías. Carga 5 sucursales, 10 departamentos, 50 artículos, 20 proveedores, 200 relaciones de catálogo, 5 relaciones comerciales, 2 tipos y 2 subtipos de orden, 4 roles, 20 pantallas, 80 permisos, 4 usuarios, 10 teléfonos, 20 rubros, 20 órdenes, 100 pedidos, 180 ofertas, 10 adjudicaciones, 30 evaluaciones y 40 entradas de auditoría ficticia. Credenciales de demostración fijas, documentadas en documentacion/Accesos-IniciosDeSesion_de_demostración.md.
Ambos caminos dejan los usuarios admin, gestor, proveedor y auditor. No se incluyen claves reales en README ni en los paquetes.

## Reportes y WPF
Ocho consultas en db/queries y API /reportes; CSV WPF. RDL en reports/ssrs. SSRS 2022 instalado y configurado por script (scripts/instalar_ssrs.ps1); ocho informes publicados y ocho PDF reales verificados el 9 de octubre de 2026. Integración web/WPF usa GUATECOMPRAS_SSRS_URL y autenticación Windows efectivos.

## Pruebas y calidad
python tests/integration/run.py
./scripts/calidad.ps1 -Maven mvn

La suite de integración exige Tomcat en marcha y config/demo-access.properties (resultado de la semilla o de la carga de datos). Suite HTTP, ambos motores y self-test WPF reales; no JUnit ni cobertura unitaria certificada. PMD/CPD:10→5 advertencias y un grupo de duplicación. Documento oficial exige Sonat para entrega; no exige Sonar.

## Estructura
src/main/java/      Backend y módulos
src/main/webapp/    Portal y páginas conservadas
desktop/           WPF Visual Studio
db/ddl/            DDL completo de ambos motores
db/dml/            Datos de demostración para ambos motores
db/queries/        Ocho consultas de los informes
reports/ssrs/      Ocho RDL y fuente compartida
documentacion/     Manuales, diccionario de datos y accesos de demostración
scripts/           Build, bases, Tomcat, SSRS, calidad
tests/integration/ Pruebas reales y limpieza de fixtures
config/            Ejemplos públicos; privados ignorados