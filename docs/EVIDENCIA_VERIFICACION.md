# Evidencia de verificación de cierre

6 de octubre de 2026, America/Guatemala. Pruebas reales sobre el proyecto existente, sin recrear bases ni sustituir datos de negocio.

## SQL Server

Conexión mediante ConexionManager al motor operativo. Suite HTTP verifica SELECT 1 protegido, roles y operaciones reales. [Bases](evidencias/cierre/bases.txt), [HTTP](evidencias/cierre/http.txt), [diccionario de 22 tablas](DICCIONARIO_SQLSERVER.md). Demo: 5 sucursales, 10 departamentos, 50 artículos, 20 proveedores, 100 pedidos y 5 usuarios, uno inactivo de prueba. Ocho consultas en db/queries verificadas por API/WPF.

## PostgreSQL

Conexión independiente mediante el mismo manager, 22 tablas, migraciones V001–V004 y mínimos 5/10/50/20/100; cuatro usuarios. [Bases](evidencias/cierre/bases.txt), [diccionario](DICCIONARIO_POSTGRES.md). Vista e integridad con nueve rechazos/rollback por motor. No replica automáticamente SQL Server.

## Java: compilación, WAR y despliegue

```powershell
./scripts/build.ps1 -Maven RUTA_MAVEN -RestrictedWindows -Repository RUTA_REPOSITORIO_LOCAL
```

Ejecuta clean verify, que incluye package. [Build](evidencias/cierre/java-build.txt): BUILD SUCCESS. WAR de 52 clases Java17/major61 inspeccionado sin configuración privada. Desplegado en Tomcat10.1.60, http://127.0.0.1:18080/sistema-adquisiciones/ . Suite HTTP posterior pasó.

## WPF: ejecución, navegación y funciones

[Build](evidencias/cierre/wpf-build.txt): cero errores/advertencias. [Self-test](evidencias/cierre/wpf.txt): 27 marcas PASS, incluidos resúmenes. Clientes/viewmodels contra API real: login, cookies/CSRF, carga de módulos, CRUD reversible, agrupación/liberación de pedidos, precio decimal, ordenamiento, ocho CSV, mensajes y cuatro perfiles. No certifica cada clic nativo.

![Dashboard WPF renderizado](evidencias/cierre/wpf-dashboard.png)

## Backend, compras y seguridad

```powershell
python tests/integration/run.py
./scripts/bases.ps1 -Accion Verificar -Java RUTA_JAVA17
```

[HTTP](evidencias/cierre/http.txt): 164 marcas PASS, incluidos resúmenes; suite completa. CRUD simples/compuestos, pedidos, órdenes, ofertas, adjudicación completa y evaluación/reversión. Casos negativos de cantidad/precio/oferta/fecha e invariantes, rollback y conservación del historial. Roles, sesiones, CSRF y proveedor propio comprobados. Dos solicitudes por un pedido producen una asignación; seis dashboards concurrentes completan. Limpieza de los fixtures propios.

Maven no encontró tests JUnit. El build no es una prueba unitaria; Python y WPF son integración real.

## SSRS

SSRS configurado; publicados **8/8**, ejecutados **8/8**, PDF reales **8/8**. [Publicación](evidencias/cierre/ssrs/publicacion-real.json), [comparación](evidencias/cierre/ssrs/verificacion-datos.json) y [cierre](CIERRE_SSRS.md) acreditan hashes iguales,285filas y20páginas. Los diagnósticos anteriores de catálogo/URLs vacíos se conservan como historia superada.

## Calidad

[PMD/CPD](CALIDAD_SOFTWARE.md): 10→5 advertencias, cero errores de procesamiento; un grupo CPD de 101 tokens, 17 líneas y tres ocurrencias. XML antes/después conservados. Sin Quality Gate Sonar: el requisito oficial exige entrega Sonat, no Sonar.

## Navegador y documentación

[Registro visual anterior](evidencias/final/final-browser.txt): alta/edición/búsqueda real, comparación y revisión responsive. Descarga CSV web no verificada por interrupción de herramienta; ocho CSV WPF sí generados. Documentación GitBook preparada y validada por enlaces; el espacio externo pide inicio de sesión. Sin sincronización/publicación externa.

## Pendientes y límites

SSRS completado y verificado. GitBook: conexión/sincronización/publicación. Sonat: entrega autenticada. Demo HTTP local sin certificación de penetración, alta disponibilidad o rendimiento productivo. Se recomienda recorrido humano WPF antes de defender el proyecto.


## Actualización SSRS, 6 de octubre de 2026

Ocho informes publicados y ocho PDF reales comprobados: 285 filas coincidentes y 20 páginas revisadas. El diagnóstico anterior de catálogo/URLs vacíos queda superado por la publicación real. SSRS ya no es un pendiente. URL: http://localhost/ReportServer. Evidencia en SSRS/ o docs/evidencias/cierre/ssrs/; resumen CIERRE_SSRS.md.
