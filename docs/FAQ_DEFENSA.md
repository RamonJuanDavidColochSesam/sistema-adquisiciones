# Preguntas y respuestas para la defensa

Sesenta respuestas basadas en las fuentes actuales. La evidencia enlazada permite ampliar cada explicación; los límites se declaran sin atribuir historia inexistente.

### 1. ¿Qué problema resuelve GuateCompras?

Organiza pedidos de departamentos, órdenes, ofertas y adjudicaciones con referencias e historial. Permite consultar quién solicitó, qué se ofreció y qué se resolvió; no integra el portal estatal ni factura compras.

Evidencia: [`README.md`](../README.md).

### 2. ¿Por qué dos interfaces?

Web y WPF permiten acceder al mismo negocio desde navegador o escritorio Windows. Comparten el backend y sus reglas, aunque no muestran exactamente los mismos controles ni tienen idénticas limitaciones.

Evidencia: [`desktop/GuateCompras.Desktop/Services/ApiClient.cs`](../desktop/GuateCompras.Desktop/Services/ApiClient.cs).

### 3. ¿Qué responsabilidad tiene un Resource?

Un Resource recibe método, ruta, parámetros y JSON mediante Jersey. Delega operaciones y devuelve una respuesta HTTP; la identidad ya ha pasado por AuthFilter.

Evidencia: [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java).

### 4. ¿Qué diferencia hay entre DAO y servicio?

El DAO ejecuta consultas y transforma resultados JDBC. El servicio coordina validaciones y operaciones; en los flujos nuevos también administra transacciones y contiene parte del SQL.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 5. ¿Hay un modelo por cada tabla?

No. Los recorridos originales tienen POJO y DAO específicos, mientras el catálogo genérico usa Map y Modulo. Inventar una clase Java por cada tabla describiría una arquitectura distinta.

Evidencia: [`src/main/java/com/adquisiciones/modelo/Modulo.java`](../src/main/java/com/adquisiciones/modelo/Modulo.java).

### 6. ¿Por qué no acceder directamente a SQL desde JavaScript?

El navegador utiliza HTTP, no JDBC ni credenciales de base. El backend centraliza permisos, validación y transacciones; permitir SQL libre desde la interfaz rompería esa frontera.

Evidencia: [`src/main/java/com/adquisiciones/filtro/AuthFilter.java`](../src/main/java/com/adquisiciones/filtro/AuthFilter.java).

### 7. ¿Se usó Spring Boot o microservicios?

El proyecto usa Java, Jersey y Servlet en un WAR desplegado en Tomcat. No se encontró Spring Boot ni separación en microservicios; los clientes comparten una aplicación backend.

Evidencia: [`pom.xml`](../pom.xml).

### 8. ¿Cuál es el contrato de CRUD del portal actual?

El portal consulta /api/gestion/{modulo} y su esquema de campos. Usa nombres snake_case, paginación y _key; los recursos originales conservan contratos camelCase y otras formas de respuesta.

Evidencia: [`src/main/webapp/js/portal.js`](../src/main/webapp/js/portal.js).

### 9. ¿Por qué se utilizan dos motores?

SQL Server es el motor operativo y PostgreSQL tiene DDL, migraciones y verificación propios. La presencia de ambos acredita esos recorridos, pero no una réplica automática ni el motivo histórico exacto de cada elección.

Evidencia: [`src/main/java/com/adquisiciones/util/MigrarBases.java`](../src/main/java/com/adquisiciones/util/MigrarBases.java).

### 10. ¿Cada alta se escribe en PostgreSQL también?

No. El CRUD y las compras operan en SQL Server. Las utilidades recorren ambos motores explícitamente, sin transacción distribuida ni sincronización de cada alta.

Evidencia: [`src/main/java/com/adquisiciones/servicio/CatalogoServicio.java`](../src/main/java/com/adquisiciones/servicio/CatalogoServicio.java).

### 11. ¿Para qué sirve HikariCP?

Reutiliza conexiones JDBC para evitar abrir una sesión física por cada operación. Hay pools independientes por motor, con máximo cinco conexiones y mínimo una; una falla de PostgreSQL no debe impedir crear el pool SQL.

Evidencia: [`src/main/java/com/adquisiciones/conexion/ConexionManager.java`](../src/main/java/com/adquisiciones/conexion/ConexionManager.java).

### 12. ¿Dónde se guardan las credenciales JDBC?

Se leen desde un archivo externo o variables de entorno con precedencia definida. El archivo privado se excluye del WAR y de esta entrega; una contraseña JDBC no es el hash de un usuario de aplicación.

Evidencia: [`src/main/java/com/adquisiciones/conexion/ConexionManager.java`](../src/main/java/com/adquisiciones/conexion/ConexionManager.java).

### 13. ¿Por qué 22 tablas si schema.sql tiene 18?

El DDL inicial aporta 18 tablas. V001 añade tres y el ejecutor administra SchemaVersion, dando 22; V002–V004 refuerzan reglas, observaciones y la vista.

Evidencia: [`db/migrations/sqlserver/V001__integridad.sql`](../db/migrations/sqlserver/V001__integridad.sql).

### 14. ¿Qué hace una FK en este proyecto?

Exige que una referencia apunte a una fila válida, por ejemplo un Pedido a su Articulo. No comprueba por sí sola todas las fechas o la compatibilidad entre ofertas y resoluciones: esas reglas requieren servicios y triggers.

Evidencia: [`schema.sql`](../schema.sql).

### 15. ¿Qué sucede si borro un proveedor con referencias?

Las referencias y reglas históricas pueden rechazar la baja con un error de integridad. Cuando procede conservar historial se utiliza el estado Inactivo; no se borra toda la historia en cascada.

Evidencia: [`db/migrations/sqlserver/V002__historial.sql`](../db/migrations/sqlserver/V002__historial.sql).

### 16. ¿Por qué el precio está en varias tablas?

El catálogo expresa un precio de referencia, la oferta conserva la propuesta y el detalle registra el precio acordado. Son hechos de momentos distintos; el precio final debe coincidir exactamente con la oferta seleccionada.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 17. ¿Cómo evita duplicados una clave compuesta?

Una PK proveedor/artículo permite una sola asociación de esa pareja sin impedir que ambos aparezcan en otras parejas. La clave _key representa sus componentes con ~ para editar o borrar la fila correcta.

Evidencia: [`src/main/java/com/adquisiciones/modelo/Modulo.java`](../src/main/java/com/adquisiciones/modelo/Modulo.java).

### 18. ¿Cómo se justificó normalización?

Teléfonos y rubros múltiples se separan en tablas de asociación en lugar de repetirse en columnas. Los diccionarios y FK permiten explicar el diseño; no se inventa una demostración formal de normalización que no quedó registrada.

Evidencia: [`db/migrations/sqlserver/V001__integridad.sql`](../db/migrations/sqlserver/V001__integridad.sql).

### 19. ¿Para qué sirve SchemaVersion?

Registra las migraciones aplicadas para evitar ejecutar una versión nuevamente. No almacena checksums ni convierte dos motores en una transacción única; una evolución futura requiere una versión nueva.

Evidencia: [`src/main/java/com/adquisiciones/util/MigrarBases.java`](../src/main/java/com/adquisiciones/util/MigrarBases.java).

### 20. ¿Hay procedimientos almacenados de negocio?

No se encontraron procedimientos almacenados de negocio en los scripts inspeccionados. Las operaciones usan SQL JDBC y los RDL tienen consultas propias; vistas y triggers son objetos diferentes.

Evidencia: [`db/schema_postgres.sql`](../db/schema_postgres.sql).

### 21. ¿Qué pasa cuando llega un POST de proveedor?

En el portal actual pasa por AuthFilter, GestionRecurso, CatalogoServicio y SqlDAO. Se validan campos y permisos, se inserta y se registra auditoría dentro de la transacción; el recurso original sigue otro recorrido.

Evidencia: [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java).

### 22. ¿Por qué un nombre de tabla concatenado no permite cualquier SQL?

Los identificadores se obtienen de Modulo, una lista estática permitida. Los valores se enlazan mediante PreparedStatement; el usuario no puede elegir un nombre arbitrario de tabla o una sentencia libre.

Evidencia: [`src/main/java/com/adquisiciones/dao/SqlDAO.java`](../src/main/java/com/adquisiciones/dao/SqlDAO.java).

### 23. ¿Cómo obtiene el ID después de insertar?

SqlDAO pide claves generadas a JDBC al insertar y lee el ResultSet correspondiente. Debe avanzar y comprobar la fila antes de leerla; las utilidades corregidas rechazan resultados vacíos.

Evidencia: [`src/main/java/com/adquisiciones/dao/SqlDAO.java`](../src/main/java/com/adquisiciones/dao/SqlDAO.java).

### 24. ¿Cómo se valida cantidad/precio?

Las cantidades e IDs usan conversión entera exacta y límites de negocio. Los importes se tratan como BigDecimal con dos decimales sin redondeo silencioso; la cantidad adjudicada no supera la solicitada.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 25. ¿Por qué 401 y 403 son diferentes?

401 indica que falta una identidad autenticada válida. 403 indica que una petición autenticada no cumple permiso, alcance o token; ocultar un botón no reemplaza ese rechazo del servidor.

Evidencia: [`src/main/java/com/adquisiciones/filtro/AuthFilter.java`](../src/main/java/com/adquisiciones/filtro/AuthFilter.java).

### 26. ¿Cuándo devuelve 409?

Los fallos de integridad SQL identificados por el mapper pueden convertirse en conflicto 409. Las validaciones previas suelen producir 400; no toda excepción de base recibe automáticamente el mismo código.

Evidencia: [`src/main/java/com/adquisiciones/recurso/ErroresApi.java`](../src/main/java/com/adquisiciones/recurso/ErroresApi.java).

### 27. ¿Existe PATCH implementado para editar catálogo?

No se declaró PATCH de edición genérica en los recursos inspeccionados. Que el filtro clasifique PATCH como actualización no crea un endpoint: la edición disponible utiliza PUT.

Evidencia: [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java).

### 28. ¿Para qué sirve el diagnóstico de conexión?

Comprueba acceso básico mediante SELECT 1 y permite distinguir un problema de conexión de un esquema incompleto. Una conexión exitosa no demuestra que existan tablas, permisos completos o datos de negocio.

Evidencia: [`src/main/java/com/adquisiciones/recurso/ConexionRecurso.java`](../src/main/java/com/adquisiciones/recurso/ConexionRecurso.java).

### 29. ¿Pedido y orden son lo mismo?

Pedido conserva una necesidad de un departamento, con artículo, cantidad y fechas. OrdenCompra agrupa pedidos y fija un periodo/tipo para recibir ofertas; el pedido puede existir sin una orden asignada.

Evidencia: [`schema.sql`](../schema.sql).

### 30. ¿Cómo se impide doble asignación concurrente?

AdquisicionServicio usa SERIALIZABLE y bloqueos UPDLOCK/HOLDLOCK al validar y asignar pedidos. La prueba de dos sesiones obtuvo una respuesta 201 y otra 400, con una sola asignación.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 31. ¿El sistema elige siempre el menor precio?

No. El detalle calcula mínimo, diferencia y empate para comparar, pero el usuario elige la oferta. El precio de resolución debe igualar la oferta elegida, aunque no sea el menor de todas.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 32. ¿Puede haber dos adjudicaciones para una orden?

La base impone UNIQUE sobre id_orden en Adjudicacion. El servicio también valida que no exista una resolución; una segunda adjudicación se rechaza.

Evidencia: [`schema.sql`](../schema.sql).

### 33. ¿Se puede adjudicar un solo pedido de una orden con tres?

No puede omitir los otros pedidos: debe incluir cada pedido de la orden una vez. Sí puede elegir una cantidad final menor que la solicitada si cumple los límites; eso no crea un módulo de entregas parciales.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 34. ¿Qué ocurre si falla un detalle después del encabezado?

Encabezado, detalles y auditoría comparten una Connection y transacción. Ante una excepción se ejecuta rollback, evitando conservar una resolución incompleta.

Evidencia: [`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java).

### 35. ¿Por qué una orden programada no está abierta?

Su fecha de creación todavía no ha llegado. V004 exige creación menor o igual a hoy, hoy menor o igual al límite y ausencia de adjudicación para aparecer abierta.

Evidencia: [`db/migrations/sqlserver/V004__periodo_orden_abierta.sql`](../db/migrations/sqlserver/V004__periodo_orden_abierta.sql).

### 36. ¿Para qué se conserva una evaluación?

Registra calificación y observaciones vinculadas a un detalle resuelto. Esa referencia también protege historial: una adjudicación evaluada no puede revertirse o editarse libremente.

Evidencia: [`src/main/java/com/adquisiciones/servicio/CatalogoServicio.java`](../src/main/java/com/adquisiciones/servicio/CatalogoServicio.java).

### 37. ¿Se guarda la contraseña en texto en Usuario?

Usuario almacena contrasena_hash y la verificación usa BCrypt. El getter se excluye de JSON; las altas genéricas y la semilla usan coste 12, mientras el helper hashear conserva gensalt sin parámetro explícito.

Evidencia: [`src/main/java/com/adquisiciones/servicio/LoginServicio.java`](../src/main/java/com/adquisiciones/servicio/LoginServicio.java).

### 38. ¿El sistema usa JWT?

No. Usa HttpSession y cookie JSESSIONID para web y WPF. La identidad y el token se guardan en memoria de sesión; no se encontraron JWT ni refresh tokens.

Evidencia: [`src/main/java/com/adquisiciones/recurso/LoginRecurso.java`](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java).

### 39. ¿Cómo se evita fijación de sesión?

Tras un login válido se invalida la sesión anterior y se crea otra. Así se renueva la identidad de sesión en lugar de conservar sin cambios un ID previo.

Evidencia: [`src/main/java/com/adquisiciones/recurso/LoginRecurso.java`](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java).

### 40. ¿Por qué revalidar permisos en cada petición?

Los permisos y el estado pueden cambiar después del login. SeguridadServicio relee la identidad vigente en cada petición; el menú en caché del cliente no decide la autorización efectiva.

Evidencia: [`src/main/java/com/adquisiciones/servicio/SeguridadServicio.java`](../src/main/java/com/adquisiciones/servicio/SeguridadServicio.java).

### 41. ¿Cómo se restringe al proveedor propio?

La identidad vigente aporta idProveedor y el servicio lo usa para limitar filas y operaciones. Enviar el ID de otro proveedor no convierte al usuario en su propietario.

Evidencia: [`src/main/java/com/adquisiciones/servicio/CatalogoServicio.java`](../src/main/java/com/adquisiciones/servicio/CatalogoServicio.java).

### 42. ¿Qué hace CSRF en el recorrido web/WPF?

Login entrega un UUID de sesión y los clientes envían X-CSRF-Token en mutaciones. AuthFilter compara ese token antes de permitir la operación; una sesión válida sin token recibe 403.

Evidencia: [`src/main/java/com/adquisiciones/filtro/AuthFilter.java`](../src/main/java/com/adquisiciones/filtro/AuthFilter.java).

### 43. ¿La demo está lista para Internet público?

La evidencia acredita una demo HTTP en loopback. No certifica TLS productivo, Secure/SameSite explícitos, pentest completo ni alta disponibilidad; esos controles requieren un despliegue y pruebas propios.

Evidencia: [`src/main/webapp/WEB-INF/web.xml`](../src/main/webapp/WEB-INF/web.xml).

### 44. ¿Qué tecnología usa el portal?

HTML, CSS y JavaScript propios, sin framework SPA externo. portal.js construye navegación, tablas y formularios; el dashboard utiliza barras CSS y no una biblioteca de gráficos.

Evidencia: [`src/main/webapp/js/portal.js`](../src/main/webapp/js/portal.js).

### 45. ¿Cómo reduce XSS al mostrar datos?

Los helpers crean elementos y asignan textContent en lugar de interpolar datos como HTML o handlers ejecutables. Esa medida corrige el problema registrado, pero una revisión dirigida no equivale a un pentest completo.

Evidencia: [`src/main/webapp/js/api.js`](../src/main/webapp/js/api.js).

### 46. ¿Qué hace el schema del CRUD?

Expone los campos, tipos, referencias y claves permitidos por Modulo para generar formularios. No descubre cualquier tabla JDBC ni da permiso para SQL arbitrario; el servicio vuelve a validar los datos.

Evidencia: [`src/main/java/com/adquisiciones/modelo/Modulo.java`](../src/main/java/com/adquisiciones/modelo/Modulo.java).

### 47. ¿Cómo se comunica C# con Java?

ApiClient utiliza HttpClient y CookieContainer para HTTP/JSON hacia Java. Conserva identidad y CSRF en memoria, aplica timeout y presenta errores; no abre una conexión SQL directa desde WPF.

Evidencia: [`desktop/GuateCompras.Desktop/Services/ApiClient.cs`](../desktop/GuateCompras.Desktop/Services/ApiClient.cs).

### 48. ¿Cómo se usó MVVM?

Los ViewModels administran datos, propiedades notificables y comandos, mientras XAML enlaza controles. Las ventanas conservan handlers, por lo que la separación es práctica y no una ausencia total de code-behind.

Evidencia: [`desktop/GuateCompras.Desktop/ViewModels/MainViewModel.cs`](../desktop/GuateCompras.Desktop/ViewModels/MainViewModel.cs).

### 49. ¿Por qué probar números/cultura en WPF?

Una cultura puede interpretar el separador decimal de forma diferente y una cadena ordena 30 antes de 5. EditorViewModel usa InvariantCulture y DataTable conserva columnas decimal; el self-test registró ambos casos.

Evidencia: [`desktop/GuateCompras.Desktop/ViewModels/EditorViewModel.cs`](../desktop/GuateCompras.Desktop/ViewModels/EditorViewModel.cs).

### 50. ¿Persisten las observaciones de una orden igual en ambas interfaces?

El backend admite observaciones y WorkflowViewModel las envía. editOrder del portal muestra el campo pero omite incluirlo al guardar; esta limitación se documentó sin modificar el código.

Evidencia: [`src/main/webapp/js/portal.js`](../src/main/webapp/js/portal.js).

### 51. ¿Qué diferencia hay entre RDL y JRXML?

RDL es el diseño XML que SSRS publica y renderiza en este proyecto. JRXML sería un diseño Jasper, pero no hay esa integración; un CSV no sustituye ninguno de esos motores de informes.

Evidencia: [`reports/ssrs/01_HistorialCompra.rdl`](../reports/ssrs/01_HistorialCompra.rdl).

### 52. ¿SSRS llama a ReportesServicio para obtener datos?

No. El RDL ejecuta su consulta con una fuente SQL compartida en SSRS. El API devuelve catálogo/URL y ejecuta consultas equivalentes para clientes, de modo que ambos SQL deben mantenerse coherentes.

Evidencia: [`reports/ssrs/03_ComparacionOfertas.rdl`](../reports/ssrs/03_ComparacionOfertas.rdl).

### 53. ¿Cómo se identificó la oferta ganadora?

El informe 3 enlaza DetalleAdjudicacion por id_oferta y marca la correspondencia GANADORA. Solo las ofertas elegidas muestran condiciones finales; la orden 10 produjo nueve ofertas y tres ganadoras.

Evidencia: [`reports/ssrs/03_ComparacionOfertas.rdl`](../reports/ssrs/03_ComparacionOfertas.rdl).

### 54. ¿Qué significa el promedio 12 días?

El informe 7 calcula AVG de días entre creación de orden y resolución en el rango pedido. La evidencia contiene diez órdenes y promedio 12; son días de calendario, sin descontar feriados.

Evidencia: [`reports/ssrs/07_PromedioAdjudicacion.rdl`](../reports/ssrs/07_PromedioAdjudicacion.rdl).

### 55. ¿Cómo se demostró que los PDF son reales?

El transcript registra publicación SOAP y exportación URL Access a PDF. Los hashes de definiciones coincidieron y se compararon todos los campos de 285 filas, con revisión visual de 20 páginas reales.

Evidencia: [`scripts/verificar_ssrs.ps1`](../scripts/verificar_ssrs.ps1).

### 56. ¿El login Java sirve para entrar a SSRS?

No. SSRS autentica la identidad Windows del navegador y su fuente SQL tiene permisos propios. El proveedor no recibe el botón institucional, pero la política SSRS debe administrarse aparte de Java.

Evidencia: [`desktop/GuateCompras.Desktop/Views/ReportWindow.xaml.cs`](../desktop/GuateCompras.Desktop/Views/ReportWindow.xaml.cs).

### 57. ¿BUILD SUCCESS significa que toda la aplicación fue probada?

BUILD SUCCESS acredita compilación y empaquetado. Las pruebas funcionales aparecen en registros Python, base, WPF y SSRS; Surefire no encontró suite JUnit y no acredita cobertura.

Evidencia: [`tests/integration/run.py`](../tests/integration/run.py).

### 58. ¿Qué se sabe de la cronología?

Hay siete commits entre el 10 de septiembre y el 1 de octubre. Las mejoras de cierre están en cambios locales y registros del 5–6 de octubre; no se puede atribuir fecha y autor exactos a cada ampliación.

Evidencia: [`pom.xml`](../pom.xml).

### 59. ¿Qué falta fuera del producto local?

La evidencia acredita el producto local y SSRS. La publicación del contenido GitBook y la entrega Sonat requieren confirmación externa; tener un sitio público no prueba que contenga esta nueva guía.

Evidencia: [`.gitbook.yaml`](../.gitbook.yaml).

### 60. ¿Cuál es el orden correcto para reinstalar?

En bases vacías: DDL, accesos, build, migraciones, semilla opcional y verificación; después Tomcat, WPF y SSRS. En el equipo existente no repetir el DDL base; INSTALACION.md conserva esa distinción.

Evidencia: [`scripts/bases.ps1`](../scripts/bases.ps1).
