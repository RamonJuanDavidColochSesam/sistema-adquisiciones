# Verificación consolidada

## Evidencia de verificación de cierre

6 de octubre de 2026, America/Guatemala. Pruebas reales sobre el proyecto existente, sin recrear bases ni sustituir datos de negocio.

## SQL Server

Conexión mediante ConexionManager al motor operativo. Suite HTTP verifica SELECT 1 protegido, roles y operaciones reales. [Bases](../assets/evidencias/cierre/bases.txt), [HTTP](../assets/evidencias/cierre/http.txt), [diccionario de 22 tablas](../anexos/diccionario-sqlserver.md). Demo: 5 sucursales, 10 departamentos, 50 artículos, 20 proveedores, 100 pedidos y 5 usuarios, uno inactivo de prueba. Ocho consultas en db/queries verificadas por API/WPF.

## PostgreSQL

Conexión independiente mediante el mismo manager, 22 tablas, migraciones V001–V004 y mínimos 5/10/50/20/100; cuatro usuarios. [Bases](../assets/evidencias/cierre/bases.txt), [diccionario](../anexos/diccionario-postgres.md). Vista e integridad con nueve rechazos/rollback por motor. No replica automáticamente SQL Server.

## Java: compilación, WAR y despliegue

```powershell
./scripts/build.ps1 -Maven RUTA_MAVEN -RestrictedWindows -Repository RUTA_REPOSITORIO_LOCAL
```

Ejecuta clean verify, que incluye package. [Build](../assets/evidencias/cierre/java-build.txt): BUILD SUCCESS. WAR de 52 clases Java17/major61 inspeccionado sin configuración privada. Desplegado en Tomcat10.1.60, http://127.0.0.1:18080/sistema-adquisiciones/ . Suite HTTP posterior pasó.

## WPF: ejecución, navegación y funciones

[Build](../assets/evidencias/cierre/wpf-build.txt): cero errores/advertencias. [Self-test](../assets/evidencias/cierre/wpf.txt): 27 marcas PASS, incluidos resúmenes. Clientes/viewmodels contra API real: login, cookies/CSRF, carga de módulos, CRUD reversible, agrupación/liberación de pedidos, precio decimal, ordenamiento, ocho CSV, mensajes y cuatro perfiles. No certifica cada clic nativo.

![Dashboard WPF renderizado](../assets/evidencias/cierre/wpf-dashboard.png)

## Backend, compras y seguridad

```powershell
python tests/integration/run.py
./scripts/bases.ps1 -Accion Verificar -Java RUTA_JAVA17
```

[HTTP](../assets/evidencias/cierre/http.txt): 164 marcas PASS, incluidos resúmenes; suite completa. CRUD simples/compuestos, pedidos, órdenes, ofertas, adjudicación completa y evaluación/reversión. Casos negativos de cantidad/precio/oferta/fecha e invariantes, rollback y conservación del historial. Roles, sesiones, CSRF y proveedor propio comprobados. Dos solicitudes por un pedido producen una asignación; seis dashboards concurrentes completan. Limpieza de los fixtures propios.

Maven no encontró tests JUnit. El build no es una prueba unitaria; Python y WPF son integración real.

## SSRS

[Diagnóstico](../assets/evidencias/cierre/ssrs-diagnostico.json): instalado/Running, catálogo/URLs vacíos. [SOAP](../assets/evidencias/cierre/ssrs-intento.txt): fallo exit1. Ocho RDL XSD válidos, datasets/parámetros/SQL en [inventario](../08-reportes/inventario.md). Publicados **0/8**, ejecutados SSRS **0/8**, PDF SSRS **0/8** comprobados. No se realizaron ocho ejecuciones fallidas: falta el servidor configurado.

## Calidad

[PMD/CPD](../12-calidad/analisis.md): 10→5 advertencias, cero errores de procesamiento; un grupo CPD de 101 tokens, 17 líneas y tres ocurrencias. XML antes/después conservados. Sin Quality Gate Sonar: el requisito oficial exige entrega Sonat, no Sonar.

## Navegador y documentación

[Registro visual anterior](../assets/evidencias/final/final-browser.txt): alta/edición/búsqueda real, comparación y revisión responsive. Descarga CSV web no verificada por interrupción de herramienta; ocho CSV WPF sí generados. Documentación GitBook preparada y validada por enlaces; el espacio externo pide inicio de sesión. Sin sincronización/publicación externa.

## Pendientes y límites

SSRS: catálogo/URLs, permisos, publicación y revisión de ocho PDF. GitBook: conexión/sincronización/publicación. Sonat: entrega autenticada. Demo HTTP local sin certificación de penetración, alta disponibilidad o rendimiento productivo. Se recomienda recorrido humano WPF antes de defender el proyecto.


[Volver a Evidencias](README.md)


## Actualización SSRS, 6 de octubre de 2026

Ocho informes publicados y ocho PDF reales comprobados: 285 filas coincidentes y 20 páginas revisadas. El diagnóstico anterior de catálogo/URLs vacíos queda superado por la publicación real. SSRS ya no es un pendiente. URL: http://localhost/ReportServer. Evidencia en SSRS/ o docs/evidencias/cierre/ssrs/; resumen CIERRE_SSRS.md.
