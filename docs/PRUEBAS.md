# Pruebas: entradas, resultados y evidencia

Complemento del capítulo 19. Esta tarea documental revisó código y registros existentes; no volvió a ejecutar operaciones de escritura. PASS significa que el resultado aparece en la evidencia de cierre. No se presentan pruebas propuestas como si hubieran sido ejecutadas.

| Prueba | Entrada | Resultado esperado | Resultado registrado | Evidencia |
|---|---|---|---|---|
| Sesión anónima | GET login/me sin cookie | 401 | 401, PASS | http.txt, security |
| Autenticación | Cuatro perfiles con credenciales privadas | Identidad, permisos y CSRF; sin hash | 200 y cookie HttpOnly, PASS | http.txt, security |
| Clave incorrecta | POST login con contraseña inválida | 401 | 401, PASS | http.txt, security |
| Defensa CSRF | Mutación autenticada sin token | 403 | 403, PASS | http.txt, security |
| Roles sin escritura | Auditor o gestor intenta modificar artículos | 403 | 403, PASS | http.txt, security |
| Propiedad de proveedor | Catálogo de otro proveedor | 403 | 403, PASS | http.txt, security |
| FK de ruta original | Asociación con IDs inexistentes | Error sanitizado 409 | 409, PASS | http.txt, security |
| CRUD específico | Artículo temporal: alta, edición, lectura y baja | Persistir, leer y limpiar | Códigos esperados, PASS | http.txt, security |
| Catálogos genéricos | Operaciones temporales y claves compuestas | Persistencia, validación y limpieza | PASS CRUD faltantes | http.txt, crud |
| Orden programada | Creación mañana | Guardar sin aparecer abierta hoy | 201 y exclusión de la vista, PASS | http.txt, acquisitions |
| Fechas y asignación | Orden anterior a solicitud o pedido ya asignado | Rechazar sin reasignar | 400; pedidos conservados, PASS | http.txt, acquisitions |
| Empate de ofertas | Dos precios iguales | Identificar ambos empates | Dos marcas, PASS | http.txt, acquisitions |
| Protección de historial | Borrar orden con ofertas o editar oferta ganadora | Rechazar | 400, PASS | http.txt, acquisitions |
| Resolución inválida | Precio diferente, pedido omitido u oferta ajena | Rechazo sin encabezado adicional | 400 y conteos sin cambio, PASS | http.txt, acquisitions |
| Resolución completa | Todos los detalles válidos | Encabezado y detalles atómicos | 201, estado Adjudicada, PASS | http.txt, acquisitions |
| Resolución duplicada/evaluada | Segunda resolución o reversión con evaluación | Rechazo | 400, PASS | http.txt, acquisitions |
| Limpieza económica | Eliminar registros temporales de la prueba | Revertir y liberar pedidos | 204 y pedidos libres, PASS | http.txt, acquisitions |
| Ocho consultas | Informes 1–8 con filtros de la evidencia | Datos coherentes | 30/5/9/40/10/10/1/180 filas, PASS | http.txt, reports; SSRS |
| Filtros inválidos | Rango invertido o comparación sin orden | 400 | 400, PASS | http.txt, reports |
| Informes de proveedor | Consultar gasto institucional y precios propios | 403 y alcance propio | 403; un proveedor distinto en precios, PASS | http.txt, reports |
| Cuerpo nulo | PUT de catálogo o POST de asociación | 400 | 400, PASS | http.txt, edge_cases |
| Asignación concurrente | Dos sesiones asignan el mismo pedido | Una sola orden válida | [201,400], PASS | http.txt, edge_cases |
| Búsqueda con caracteres SQL | Texto con comillas y DELETE | Tratarlo como valor | 200, consulta parametrizada, PASS | http.txt, edge_cases |
| Lecturas concurrentes | Seis sesiones consultan dashboard | Todas 200 | Todas 200 y 100 pedidos, PASS | http.txt, edge_cases |
| Conexiones | SELECT 1 en ambos motores | Una fila con 1 | Pools disponibles, PASS | http.txt, bases.txt |
| Integridad en ambas bases | Precios inválidos, fechas incompatibles e historial | Rechazo y rollback | Negativos aprobados, PASS | bases.txt |
| Build Java | Compilación limpia con perfil del entorno | WAR Java 17 | BUILD SUCCESS, 52 clases major 61 | java-build.txt, artefactos.json |
| Build WPF | Restore y build Release | Sin errores | Sin errores ni advertencias | wpf-build.txt |
| Cliente WPF | CRUD, flujo, módulos y perfiles por API | Operaciones y permisos correctos | PASS y limpieza de IDs | wpf.txt |
| Cultura y orden WPF | 123.45, 123,45; precios 30 y 5 | Punto válido, coma rechazada, 5 antes de 30 | PASS | wpf.txt |
| CSV WPF | Ocho informes; texto con prefijo de fórmula | Exportar y neutralizar fórmula | Ocho exportaciones y PASS | wpf.txt, informe-*.csv |
| Publicación SSRS | Ocho definiciones mediante SOAP | Publicar sin alterar diseños | Ocho PUBLICADO; SHA256 iguales | ssrs/publicacion-real.json |
| PDF SSRS | Orden 10, rango 2025–2026, año 2026 | Ocho PDF con datos | Ocho PDF, 285 filas, 20 páginas | ssrs/ejecucion-ssrs.txt |
| Datos y aspecto PDF | Comparar todas las filas/campos y renderizar páginas | Coincidencia; sin recortes | Comparación y revisión aprobadas | ssrs/verificacion-datos.json |
| Integración SSRS | GET reportes | URL y estado configurado | true y localhost/ReportServer | ssrs/integracion-backend.json |
| PMD/CPD | Fuentes Java y umbral de 100 tokens | Análisis real | 5 advertencias, 0 errores de procesamiento, 1 grupo duplicado | CALIDAD_SOFTWARE.md |
| Unitarias JUnit | Revisar dependencias y suite | Declarar su ausencia | No tests to run; no suite encontrada | java-build.txt y POM |

La prueba genérica no acredita CRUD completo de los 17 módulos: tipos y subtipos se consultan, sin el ciclo completo de alta/edición/baja en esa suite. El inventario de módulos es una comprobación distinta de la cobertura de pruebas.

Fuentes: [HTTP](evidencias/cierre/http.txt), [bases](evidencias/cierre/bases.txt), [WPF](evidencias/cierre/wpf.txt), [build Java](evidencias/cierre/java-build.txt), [build WPF](evidencias/cierre/wpf-build.txt), [calidad](CALIDAD_SOFTWARE.md) y [cierre SSRS](CIERRE_SSRS.md). El ejecutor [run.py](../tests/integration/run.py) encadena security, crud, acquisitions, reports y edge_cases contra el servidor real, sin mocks.

## Cómo interpretar los límites

La concurrencia registrada comprende dos sesiones de asignación y seis lecturas; no es un benchmark de producción. Los rechazos SQL no constituyen un pentest completo. El self-test WPF utiliza API, ViewModels y renderizado XAML; no automatiza todos los clics nativos. Los ocho PDF cubren los parámetros registrados, no toda combinación posible.

Los resultados dependen de la semilla y de la fecha. Las pruebas limpian sus IDs, pero conservan eventos de Auditoria; no restauran la base byte a byte. No hay cobertura porcentual acreditada, pruebas unitarias inventadas ni Quality Gate de Sonar. Ver [VALIDACION_DOCUMENTACION.md](VALIDACION_DOCUMENTACION.md) para las comprobaciones específicas de esta documentación.
