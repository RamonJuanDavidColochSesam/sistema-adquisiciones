# Validación de la documentación del desarrollo

Fecha: 6 de octubre de 2026. Alcance: guía de 29 capítulos, anexos, manual de usuario y correcciones documentales del cierre. Esta revisión inspeccionó fuentes y evidencia existente; no reejecutó pruebas funcionales, migraciones, publicaciones ni operaciones de negocio.

## Correspondencia con el repositorio

| Elemento | Verificado | Evidencia y significado |
|---|---|---|
| Arquitectura | Sí | ARQUITECTURA.md y 49 archivos Java enlazados por paquete; coexistencia de contratos originales y genéricos. |
| Base de datos | Sí | BASE_DATOS.md, ambos diccionarios, schema.sql y migraciones V001–V004; 22 tablas, claves, restricciones, índices y relaciones. |
| API | Sí | BACKEND.md y anotaciones de recursos; 49 declaraciones REST y 17 esquemas genéricos, sin inventar rutas por tabla. |
| Frontend | Sí | FRONTEND.md y HTML/CSS/JS; funciones, formularios, permisos y omisión real de observaciones de orden. |
| WPF | Sí | WPF.md, csproj, XAML/C#, build y self-test; .NET 10, API, cookies, cultura decimal y límites de automatización. |
| JasperReports | No como implementación; ausencia verificada | No hay dependencia, JRXML/JASPER ni recorrido Jasper. La explicación comparativa no lo presenta instalado. |
| SSRS | Sí | REPORTES.md y ocho RDL; consultas y parámetros literales, fuente compartida, publicación y PDF reales. |
| Seguridad | Sí | SEGURIDAD.md, filtro, login y servicios; sesión, BCrypt, permisos, CSRF y alcance; límites TLS/Windows declarados. |
| Pruebas | Sí en el alcance registrado | PRUEBAS.md y registros HTTP/bases/WPF/SSRS. No se atribuye CRUD completo de los 17 módulos ni suite JUnit inexistente. |
| Despliegue | Sí como procedimiento y evidencia local | INSTALACION.md, scripts y registros; instalación vacía distinta del equipo existente. No se certifica producción HTTPS. |
| Cronología | Sí, parcial | Siete commits y auditorías/registros fechados. No se inventa autor ni fecha exacta para cada cambio local. |
| Defensa y glosario | Sí | FAQ_DEFENSA.md contiene 60 respuestas con evidencia; GLOSARIO.md define conceptos aplicados. |

## Comprobaciones documentales

- Se comprobaron los 29 capítulos y el resumen final «Resumen para comprender GuateCompras en 30 minutos».
- Se revisaron referencias locales, clases, rutas, nombres de tablas/módulos, versiones declaradas y parámetros de scripts.
- Se comparó el SQL literal de los ocho RDL y sus parámetros/defaults con REPORTES.md.
- Se comprobaron los SHA256 actuales de ocho RDL y ocho PDF contra la publicación registrada; suman 285 filas y 20 páginas. Esta comprobación no representa una nueva consulta al servidor SSRS.
- Se revisaron bloques Markdown y diagramas Mermaid: tipo, participantes, relaciones y correspondencia física/de negocio. No se atribuye renderizado gráfico mediante una herramienta Mermaid que no se ejecutó.
- Los comandos PowerShell se revisan sintácticamente y frente a sus scripts; contienen rutas de ejemplo que deben adaptarse. No se ejecutan por esta tarea.
- Se compararon hashes de fuentes, configuración pública, scripts y pruebas con el inventario tomado al iniciar. Las modificaciones realizadas están en documentación.
- Se buscaron valores de contraseñas privadas en los documentos entregados, sin imprimir ni incluir esos valores.

El detalle medible se conserva en [validacion.json](evidencias/desarrollo/validacion.json) y el inventario de inspección en [inventario.json](evidencias/desarrollo/inventario.json). Los nombres y rutas se vinculan a archivos reales; los fragmentos explicativos se distinguen de consultas literales y de comandos no ejecutados.

## Correcciones de documentación anterior

Se actualizaron inventario SSRS, avance, evidencia general, instrucciones SSRS y conteo de requisitos que aún describían un servidor sin publicación. Se conservaron los diagnósticos históricos fallidos y AUDITORIA_INICIAL: reflejan el punto de partida, no el estado final. La matriz original conserva 73 CUMPLIDO, 1 PENDIENTE y 1 NO APLICA; las ampliaciones tienen su propio alcance.

La entrega Sonat y la publicación de esta nueva guía en el sitio GitBook independiente no están acreditadas por un recibo del repositorio. No se publicaron automáticamente estos documentos ni se modificó el sitio. Para la historia no registrada se mantiene: «No se encontró evidencia suficiente en el repositorio para confirmar este punto».

## Resultado medible de la revisión

**PASS:** 16 documentos revisados, 29 capítulos, 60 preguntas, 291 enlaces locales comprobados y ninguno roto. Correspondencia con 49 fuentes Java, 49 declaraciones REST, 17 módulos y 22 tablas. Siete bloques PowerShell analizados sintácticamente, sin errores. Ocho RDL y ocho PDF mantienen los hashes registrados; 285 filas y 20 páginas. Los 133 archivos públicos protegidos de fuentes/configuración/scripts/pruebas mantienen su SHA256; no se detectaron valores de contraseñas privadas en los documentos revisados.
