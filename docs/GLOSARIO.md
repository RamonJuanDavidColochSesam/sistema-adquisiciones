# Glosario de GuateCompras

| Término | Significado aplicado al proyecto |
|---|---|
| API | Contrato HTTP que usan web y WPF para acceder al backend. |
| REST | Organización de recursos y métodos HTTP. |
| Endpoint | Método y ruta, por ejemplo GET /api/ordenes/{id}. |
| JSON | Representación de los cuerpos de datos intercambiados por HTTP. |
| CRUD | Crear, leer, editar y borrar registros. |
| POJO | Objeto Java de datos; no implica acceso automático a la base. |
| DTO | Objeto de transferencia; aquí predominan POJO, Map y metadatos, sin un paquete DTO completo. |
| Resource | Clase Jersey que recibe peticiones y produce respuestas. |
| Service | Reglas y operaciones; algunos servicios también contienen SQL. |
| DAO | Acceso a datos mediante JDBC; SqlDAO admite una conexión compartida. |
| JAX-RS/Jersey | Anotaciones REST y su implementación usada por el backend. |
| Servlet/Tomcat | Contenedor del WAR, sesiones, filtro y ciclo de vida. |
| WAR | Archivo desplegable con clases, bibliotecas y web; requiere Tomcat aparte. |
| Maven/POM | Herramienta de construcción y definición de dependencias. |
| JDBC | API Java para conexiones, sentencias, resultados y transacciones. |
| PreparedStatement | Sentencia que recibe valores mediante parámetros. |
| ResultSet | Resultado JDBC que exige avanzar a una fila antes de leer. |
| HikariCP/pool | Conexiones reutilizadas con límites por motor. |
| PK | Clave primaria que identifica una fila o combinación. |
| FK | Referencia obligatoria u opcional a una fila existente. |
| UNIQUE/CHECK | Unicidad y restricciones sobre valores. |
| Trigger | Regla de base ejecutada ante INSERT, UPDATE o DELETE. |
| View | Consulta con nombre; vw_OrdenesAbiertas refleja el periodo actual. |
| Migración | Cambio incremental V001–V004 registrado en SchemaVersion. |
| Commit/rollback | Confirmación o reversión de una transacción. |
| SERIALIZABLE | Aislamiento usado para evitar asignaciones concurrentes incompatibles. |
| BCrypt | Hash adaptativo que permite verificar una clave sin almacenarla en texto. |
| Sesión/JSESSIONID | Identidad conservada en el servidor y cookie del cliente. |
| CSRF | Defensa de mutaciones con token de sesión y X-CSRF-Token. |
| Autenticación | Comprobación de usuario, contraseña y estado. |
| Autorización/scope | Permiso de acción y alcance de filas según la identidad vigente. |
| WPF/XAML | Cliente Windows y declaración de su interfaz. |
| MVVM/binding | ViewModels, propiedades notificables y enlaces; conviven con code-behind. |
| HttpClient/CookieContainer | Cliente HTTP de C# y persistencia de cookies en su instancia. |
| SSRS/RDL | Servicio Microsoft y diseños XML reales de los informes. |
| Dataset/DataSource | Consulta/campos y conexión de SSRS; aquí Datos y GuateComprasSQL. |
| URL Access/SOAP | Mecanismos de renderizado/exportación y administración SSRS. |
| JRXML/JasperReports | Formato y motor alternativos explicados para comparar; no implementados. |
| CSV/PDF | Tabla exportable por el cliente y documento renderizado por SSRS. |
| PMD/CPD | Análisis de advertencias y duplicación; no un Quality Gate de Sonar. |
| Sonat/GitBook | Entrega académica y publicación documental, independientes del negocio. |
| Git/commit | Historial versionado parcial; las ampliaciones locales no tienen todos sus commits. |
