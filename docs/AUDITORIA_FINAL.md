# Auditoría final de cierre

6 de octubre de 2026. Se conservan bases, backend, módulos y funcionalidades previas. Correcciones pequeñas respaldadas por PMD, sin refactor amplio ni recreación de datos.

| Área | Estado real | Evidencia |
|---|---|---|
| Java / WAR / despliegue | CUMPLIDO | Build y HTTP posterior |
| SQL Server y PostgreSQL | CUMPLIDO | Conexiones, mínimos e integridad |
| Backend / CRUD / compras | CUMPLIDO | Suite real, transacciones y concurrencia |
| Seguridad | CUMPLIDO en alcance demo | Roles/CSRF/alcance; no penetración exhaustiva |
| Dashboard / frontend | CUMPLIDO | Datos reales y revisión visual anterior |
| WPF | CUMPLIDO en alcance probado | Build/self-test; recorrido humano recomendado |
| SSRS instalado | CUMPLIDO | Servicio Windows Running |
| Catálogo / URLs SSRS | CUMPLIDO | ReportServer accesible desde sesión Windows autorizada |
| Ocho RDL | CUMPLIDO como definición | XSD, inventario y consultas API |
| Ocho publicados / PDF | CUMPLIDO | SOAP, huellas idénticas, 285 filas y 20 páginas revisadas |
| Pruebas / documentación | CUMPLIDO | Registros trazables |
| GitBook preparado | CUMPLIDO | 16 capítulos y Git Sync |
| GitBook publicado | PENDIENTE | Sitio independiente creado; publicación de documentación pendiente por autorización del conector |
| PMD / CPD local | CUMPLIDO como análisis | 10→5 advertencias; cero errores de procesamiento |
| Quality Gate Sonar | NO APLICA al requisito oficial | Documento exige Sonat, no Sonar |
| Entrega Sonat | PENDIENTE | No hubo envío externo |

## Los 75 requisitos

CUMPLIDO: **73**, PENDIENTE: **1**, PARCIAL: **0**, NO APLICA: **1**. Total **75**. R076–R082/C001–C003 separados del denominador. R040–R047 cubren consultas API/WPF; R050 CUMPLIDO tras publicación y revisión de ocho PDF reales. Jasper NO APLICA por solicitud posterior.

## Correcciones y pruebas repetidas

Tres utilidades JDBC ahora validan next() antes de leer; diagnóstico registra tipo de fallo sin secretos. Se aclaró un cortocircuito correcto que PMD señalaba. Se repitieron compilación, despliegue, HTTP, ambos motores y WPF. Quedan cinco recomendaciones de mantenimiento y un grupo CPD documentados, sin ocultarlos ni llamarlos métricas Sonar.

## Conclusión

**Proyecto funcional y comprobado localmente; cierre externo incompleto.** No se declara FINALIZADO ni 100 % mientras falten publicación de la documentación en GitBook y entrega Sonat. SSRS está configurado, publicado y verificado.

SQL Server es operativo; PostgreSQL independiente, sin réplica. No se añadió inventario. La cuenta de prueba visual quedó inactiva para conservar auditoría. No se cambiaron contraseñas existentes ni se empaquetaron secretos.

[Matriz](MATRIZ_REQUISITOS.md) · [evidencia](EVIDENCIA_VERIFICACION.md) · [SSRS](REPORTES_SSRS.md) · [calidad](CALIDAD_SOFTWARE.md) · [entrega](ENTREGA_SONAT_GITBOOK.md).
