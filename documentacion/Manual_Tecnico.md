---
output:
  pdf_document: default
  html_document: default
---
# Manual Técnico — GuateCompras

Sistema de Gestión de Adquisiciones y Proveedores

Universidad Mariano Gálvez de Guatemala, sede Salamá, Baja Verapaz

Ingeniería en Sistemas de Información

Curso: Bases de Datos I. Catedrático: Ing. Erwin Roberto Méndez


## 1. Introducción

### 1.1 Objetivo y alcance

Este manual describe la construcción interna de GuateCompras y explica cómo desplegarlo, poblarlo con datos y mantenerlo. Está dirigido a quien deba instalar, revisar, extender o evaluar el sistema: desarrolladores, administradores de bases de datos y el equipo docente. Complementa al Manual de Usuario, que explica el uso de cada pantalla, y al Diccionario de Datos, que detalla columna por columna las tablas del esquema.

### 1.2 Qué resuelve el sistema

GuateCompras administra el ciclo de adquisición de una institución con varias sucursales: las solicitudes de compra de los departamentos se agrupan en órdenes, los proveedores presentan ofertas durante un plazo, se adjudica a la mejor oferta de cada solicitud y se evalúa al proveedor. Incluye control de acceso por roles, auditoría de cambios, ocho informes de gestión y un cliente web y otro de escritorio.

### 1.3 Alcance de la revisión de este documento

El contenido se elaboró a partir de la lectura del código fuente, de los scripts de base de datos y de la documentación del repositorio. No se ejecutó el sistema para redactarlo. Los comandos y rutas citados provienen de los scripts del proyecto; cuando se propone una alternativa que el repositorio no incluye, se indica expresamente.

## 2. Arquitectura del sistema

### 2.1 Tecnologías

| Componente | Tecnología y versión |
|---|---|
| Lenguaje y compilación | Java 17, Maven 3.9, empaquetado WAR |
| Servidor de aplicaciones | Apache Tomcat 10.1 (Jakarta Servlet 6.0) |
| API REST | Jersey 3.1.5 (JAX-RS) con serialización JSON mediante Jackson |
| Acceso a datos | JDBC con HikariCP 5.1.0, sin ORM |
| Controladores | mssql-jdbc 12.8.1 (SQL Server) y postgresql 42.7.4 (PostgreSQL) |
| Seguridad | jBCrypt 0.4 para contraseñas, sesión HTTP con cookie HttpOnly, token CSRF |
| Registro de eventos | SLF4J 2.0.9 con slf4j-simple |
| Base de datos principal | Microsoft SQL Server |
| Segunda base de datos | PostgreSQL |
| Cliente web | HTML, CSS y JavaScript sin frameworks |
| Cliente de escritorio | WPF con .NET 10 (opcional) |
| Informes | Consultas SQL expuestas por la API y ocho informes RDL para SQL Server Reporting Services (opcional) |
| Pruebas | Suite de integración en Python contra el servidor en ejecución; PMD y CPD para calidad |

### 2.2 Vista general

```
  Navegador (portal web)        Cliente WPF (opcional)        SSRS (opcional)
          |                              |                           |
          +-------------- HTTP/JSON -----+                           |
                          |                                          |
                 Tomcat: /sistema-adquisiciones                      |
                          |                                          |
          Filtros:  RecursoFilter (estáticos)                        |
                    AuthFilter (/api/*): sesión, CSRF, permisos      |
                          |                                          |
          Recursos JAX-RS (paquete recurso)                          |
                          |                                          |
          Servicios (reglas de negocio y transacciones)              |
                          |                                          |
          DAO / SqlDAO (JDBC parametrizado)                          |
                          |                                          |
          ConexionManager (dos pools HikariCP)                       |
              |                          |                           |
        SQL Server (operativo)     PostgreSQL (verificación) <-------+ (SSRS lee SQL Server)
```

Toda la operación de negocio se ejecuta contra SQL Server. PostgreSQL mantiene el mismo esquema y se usa para demostrar y verificar el funcionamiento sobre un segundo motor: tiene su propio pool, se comprueba desde la pantalla "Conexión de bases" y es destino independiente de la semilla y de la verificación de esquema. No existe réplica automática entre ambos motores; los datos de uno no se copian al otro.

### 2.3 Estructura del proyecto

| Ruta | Contenido |
|---|---|
| `src/main/java/com/adquisiciones/conexion` | `ConexionManager` (pools), `AplicacionLifecycle` (cierre ordenado) y clases de prueba de conexión. |
| `.../filtro` | `AuthFilter` (autenticación, CSRF y autorización) y `RecursoFilter` (cabeceras de archivos estáticos). |
| `.../recurso` | Recursos REST: login, gestión genérica, órdenes, adjudicaciones, reportes, dashboard, auditoría, conexión y los CRUD específicos. `ErroresApi` traduce excepciones a respuestas. |
| `.../servicio` | Reglas de negocio: `AdquisicionServicio`, `CatalogoServicio`, `ReportesServicio`, `SeguridadServicio`, `LoginServicio` y servicios de los CRUD específicos. |
| `.../dao` | Acceso a datos: `SqlDAO` (utilidades genéricas) y DAO de las entidades con CRUD específico. |
| `.../modelo` | Entidades y `Modulo`, el esquema declarativo de los catálogos genéricos. |
| `.../util` | `SemillaDemo`, `VerificarBases`, `PasswordUtil` y utilidades de apoyo. |
| `src/main/webapp` | Páginas HTML, `js/portal.js`, `js/api.js`, `css/portal.css` y `WEB-INF/web.xml`. |
| `db/ddl` | `sqlserver.sql` y `postgres.sql`: estructura completa de cada motor. |
| `db/queries` | Las ocho consultas de los informes, en T-SQL. |
| `reports/ssrs` | Ocho informes RDL y la fuente de datos compartida. |
| `desktop` | Solución WPF (`GuateCompras.sln`). |
| `scripts` | Compilación, arranque de Tomcat, semilla y verificación, instalación de SSRS y calidad. |
| `tests/integration` | Cinco suites de prueba HTTP en Python. |
| `config` | Archivos de configuración. Los que contienen credenciales no se versionan. |

### 2.4 Componentes principales

Gestión de conexiones. `ConexionManager` es un singleton que crea de forma diferida un pool HikariCP por motor (máximo 5 conexiones, mínimo 1 inactiva, 10 segundos de espera). La configuración se lee de un archivo externo, nunca del WAR, y puede sustituirse con variables de entorno. `AplicacionLifecycle` cierra los pools al detener la aplicación.

Filtro de autenticación y autorización. `AuthFilter` intercepta todo lo que llega a `/api/*`. Es el único punto donde se decide si una petición puede continuar, por lo que ningún recurso verifica permisos por su cuenta. Se detalla en la sección 5.

API genérica de catálogos. `GestionRecurso` expone el CRUD de casi todos los catálogos bajo `/api/gestion/{modulo}`. El comportamiento de cada módulo lo define `Modulo.java`: tabla, clave, campos, tipos, longitudes y referencias. Ningún nombre de tabla o columna procede del cliente, lo que elimina la inyección por identificadores. `CatalogoServicio` valida, aplica reglas por módulo, escribe y registra la auditoría dentro de una transacción.

API de procesos. `OrdenRecurso` y `AdjudicacionRecurso`, apoyados en `AdquisicionServicio`, gestionan las operaciones con varias tablas y transacciones de mayor aislamiento: crear o editar órdenes, adjudicar y revertir.

Informes. `ReportesServicio` define el catálogo de los ocho informes y su SQL parametrizado; `ReportesRecurso` los publica bajo `/api/reportes/{1..8}`.

CRUD específicos. Sucursales, departamentos, artículos, proveedores y catálogo de proveedores conservan además un CRUD propio (modelo, DAO, servicio y recurso) en `/api/sucursales`, `/api/departamentos`, `/api/articulos`, `/api/proveedores` y `/api/proveedores/{id}/articulos`. Conviven con la API genérica y respetan los mismos permisos. El portal principal (`portal.html`) usa la genérica; las páginas individuales conservadas (`sucursales.html`, `departamentos.html`, `articulos.html`, `proveedores.html` y `proveedorarticulos.html`) usan las específicas.

### 2.5 Decisiones de diseño

- Llave sustituta (`id`) como clave primaria y foránea, con código de negocio único aparte, para que los cambios de código no se propaguen.
- Reglas que comparan datos entre tablas implementadas como triggers, porque un CHECK no puede consultar otras tablas.
- Las reglas se validan en dos niveles: primero en la aplicación, para dar mensajes claros, y después en la base de datos, que es la garantía final aunque alguien escriba directamente en las tablas.
- Historial inmutable: lo ya ofertado o adjudicado no se modifica ni se borra; se retira con el estado Inactivo.
- SQL parametrizado en todo el código y listas blancas de campos, tipos y valores.
- Dos motores con el mismo esquema, para verificar la portabilidad del modelo.

## 3. Modelo de datos

El esquema de ambos motores tiene 21 tablas, una vista, cuatro índices y diez triggers. Las columnas, tipos y restricciones de cada tabla están en el Diccionario de Datos; aquí se presenta la visión de conjunto.

### 3.1 Tablas por módulo

| Módulo | Tabla | Función | Clave primaria |
|---|---|---|---|
| Estructura | Sucursal | Sedes de la institución | id_sucursal |
| | SucursalTelefono | Teléfonos adicionales de una sucursal | (id_sucursal, telefono) |
| | Departamento | Unidades internas; nombre único por sucursal | id_departamento |
| Catálogos | Articulo | Bienes que se pueden solicitar, con estado | id_articulo |
| | Proveedor | Empresas proveedoras, con estado | id_proveedor |
| | ProveedorRubro | Rubros de actividad de un proveedor | (id_proveedor, rubro) |
| | ProveedorArticulo | Catálogo y precio de referencia por proveedor | (id_proveedor, id_articulo) |
| | RelacionComercial | Vínculo entre dos proveedores en orden canónico | id_relacion_comercial |
| Ciclo de adquisición | TipoOrden | Grande o Chica | id_tipo |
| | SubtipoOrden | Normal o Urgente, dependiente del tipo | id_subtipo |
| | OrdenCompra | Agrupación de solicitudes con plazo de ofertas | id_orden |
| | Pedido | Solicitud de un departamento | id_pedido |
| | Oferta | Precio de un proveedor para una solicitud | id_oferta |
| | Adjudicacion | Resolución de una orden (una por orden) | id_adjudicacion |
| | DetalleAdjudicacion | Oferta ganadora por solicitud | id_detalle_adjudicacion |
| | EvaluacionProveedor | Calificación de 1 a 5 de una línea adjudicada | id_evaluacion |
| Seguridad | Rol | Cuatro roles base | id_rol |
| | Pantalla | Pantallas sujetas a permisos | id_pantalla |
| | Permiso | Matriz rol por pantalla con cuatro indicadores | (id_rol, id_pantalla) |
| | Usuario | Cuentas, con contraseña cifrada y proveedor asociado opcional | id_usuario |
| | Auditoria | Historial de cambios | id_auditoria |

### 3.2 Relaciones principales

La cadena central del proceso es: Departamento genera Pedido; varios Pedido forman una OrdenCompra (la relación es opcional, un pedido nace sin orden); cada proveedor presenta Oferta sobre un Pedido; una OrdenCompra recibe a lo sumo una Adjudicacion, que contiene un DetalleAdjudicacion por cada Pedido y apunta a la Oferta ganadora; cada DetalleAdjudicacion puede recibir EvaluacionProveedor. Alrededor se encuentran los catálogos: Articulo y Proveedor se vinculan por ProveedorArticulo, y Usuario referencia a Rol y, para el rol AdminProveedor, a Proveedor.

### 3.3 Restricciones relevantes

- Nombre de departamento único dentro de su sucursal.
- SubtipoOrden con dos restricciones únicas: (tipo, nombre) y (tipo, subtipo).
- OrdenCompra referencia (tipo, subtipo) con una clave foránea compuesta, que garantiza que el subtipo pertenezca al tipo.
- RelacionComercial exige `id_proveedor_a < id_proveedor_b`, de modo que cada par se guarda en un único orden, además de la unicidad del par.
- Adjudicacion tiene `id_orden` único: una adjudicación por orden.
- DetalleAdjudicacion es único por (adjudicación, pedido).
- Precios y cantidades positivos; calificación entre 1 y 5; estado limitado a Activo o Inactivo; fechas coherentes mediante CHECK.

### 3.4 Vista e índices

La vista `vw_OrdenesAbiertas` lista las órdenes sin adjudicación cuya fecha de creación ya llegó y cuyo límite de ofertas no ha vencido. La usan el informe 5, el indicador del resumen y la columna "Disponible para oferta" de las solicitudes.

| Índice | Tabla | Columnas |
|---|---|---|
| IX_Pedido_Orden | Pedido | id_orden, con id_departamento, id_articulo y cantidad incluidos |
| IX_Oferta_PedidoProveedor | Oferta | id_pedido, id_proveedor, fecha_oferta, con precio_unitario incluido |
| IX_Departamento_Sucursal | Departamento | id_sucursal |
| IX_Adjudicacion_Fecha | Adjudicacion | fecha_resolucion, con id_orden incluido |

### 3.5 Triggers de reglas de negocio

| Trigger | Tabla y evento | Regla que protege |
|---|---|---|
| TRG_Pedido_ValidaFechaOrden | Pedido, alta y cambio | La creación de la orden no puede ser anterior a la solicitud del pedido. |
| TRG_Adjudicacion_ValidaFecha | Adjudicacion, alta y cambio | La resolución no puede ser anterior a la creación de la orden. |
| tr_Oferta_Integridad | Oferta, alta y cambio | La oferta requiere pedido con orden y fecha dentro del plazo (51001); el artículo debe estar en el catálogo del proveedor (51002). |
| tr_DetalleAdjudicacion_Integridad | DetalleAdjudicacion, alta y cambio | Oferta, pedido y adjudicación deben corresponder a la misma orden y a una cantidad válida (51003). |
| tr_OrdenCompra_Integridad | OrdenCompra, alta y cambio | La creación no puede preceder a sus solicitudes (51004) ni ser posterior a la adjudicación (51005); el subtipo pertenece al tipo (51006). |
| tr_Pedido_Historial | Pedido, cambio | No reasignar ni cambiar el artículo de un pedido con ofertas (51007); la cantidad no baja de la adjudicada (51008). |
| tr_Adjudicacion_Historial | Adjudicacion, cambio | La resolución no contradice pedidos u ofertas ya adjudicados (51009). |
| tr_Oferta_Historial | Oferta, cambio | Una oferta adjudicada no se modifica (51010). |
| tr_OrdenCompra_Ofertas | OrdenCompra, cambio | Las nuevas fechas no pueden dejar ofertas fuera de plazo (51011). |
| tr_ProveedorArticulo_Historial | ProveedorArticulo, baja | No eliminar una línea de catálogo con ofertas históricas (51012). |

### 3.6 Diferencias entre SQL Server y PostgreSQL

| Aspecto | SQL Server | PostgreSQL |
|---|---|---|
| Identidad | `INT IDENTITY(1,1)` | `INT GENERATED BY DEFAULT AS IDENTITY` |
| Booleanos | `BIT` | `BOOLEAN` |
| Calificación | `TINYINT` | `SMALLINT` |
| Fecha actual | `GETDATE()` | `CURRENT_DATE` |
| Auditoría: fecha | `DATETIME2` con `SYSUTCDATETIME()` (hora UTC) | `TIMESTAMP` con `CURRENT_TIMESTAMP` (hora del servidor) |
| Triggers | Uno por regla, con `inserted` y `deleted`, que procesan el lote completo | Una función PL/pgSQL más un `CREATE TRIGGER`, que procesan fila por fila |
| Errores de regla | `THROW` con códigos 51001 a 51012 y, en los dos triggers de fechas, `RAISERROR` con reversión | `RAISE EXCEPTION` con el mismo mensaje |
| Vista | `CREATE OR ALTER VIEW` | `CREATE OR REPLACE VIEW` |

En PostgreSQL los identificadores sin comillas se almacenan en minúsculas, por lo que `Sucursal` y `sucursal` designan la misma tabla.

## 4. Flujos principales de procesos

### 4.1 Ciclo de adquisición

1. Registro de la solicitud. Un usuario con permiso crea un Pedido con departamento, artículo activo, cantidad y fechas. Nace sin orden.
2. Creación de la orden. `AdquisicionServicio.guardarOrden` abre una transacción con aislamiento SERIALIZABLE, valida tipo y subtipo, bloquea cada pedido con `UPDLOCK` y `HOLDLOCK`, comprueba que no pertenezca a otra orden, que su artículo esté activo y que la fecha de creación no preceda a la solicitud, inserta la orden, asigna los pedidos y registra la auditoría. Si algo falla se revierte todo.
3. Recepción de ofertas. Mientras la orden esté abierta, `CatalogoServicio` valida que el proveedor esté activo, que el artículo también y que la fecha de oferta caiga en el plazo. Un proveedor mantiene una sola oferta por pedido: una nueva sobre el mismo pedido actualiza la anterior. El trigger `tr_Oferta_Integridad` comprueba en la base que el artículo figure en el catálogo del proveedor.
4. Comparación. `AdquisicionServicio.orden` calcula para cada oferta el precio mínimo, la diferencia respecto a él, si es la mejor y si hay empate, y señala la ganadora cuando existe.
5. Adjudicación. `guardarAdjudicacion` bloquea la orden, verifica que no esté adjudicada, que la fecha no sea futura ni anterior a la orden o a la oferta ganadora, y valida cada línea: la oferta debe pertenecer al pedido de la orden, el precio acordado debe igualar el de la oferta, la cantidad final no puede superar la solicitada, y el proveedor debe estar activo. Debe cubrirse todos los pedidos de la orden. Todo ocurre en una sola transacción.
6. Evaluación. Se registra una calificación de 1 a 5 sobre una línea adjudicada, con fecha no anterior a la resolución. El promedio por proveedor se calcula al consultar el catálogo.
7. Reversión. Una adjudicación sin evaluaciones puede revertirse; se eliminan sus líneas y la cabecera y queda registrada como acción REVERTIR.

### 4.2 Ciclo de vida de una orden

```
 Programada --(llega la fecha de creación)--> Abierta
 Abierta --(se registra la adjudicación)--> Adjudicada
 Abierta --(vence el límite de ofertas)--> Vencida
 Vencida --(se registra la adjudicación)--> Adjudicada
 Adjudicada --(se revierte la adjudicación)--> Abierta o Vencida, según la fecha
```

El estado no se almacena: se calcula con la fecha actual y la existencia de adjudicación, tanto en el listado de órdenes como en el detalle.

### 4.3 Atención de una petición

1. El navegador envía la petición a `/api/...` con la cookie de sesión y, si modifica datos, la cabecera `X-CSRF-Token`.
2. `AuthFilter` agrega las cabeceras de seguridad y deja pasar solo el inicio de sesión sin sesión previa.
3. Comprueba que la sesión tenga un usuario y que este siga activo; en caso contrario responde 401.
4. Si el método modifica datos, exige que el token CSRF de la sesión coincida con la cabecera; si no, responde 403.
5. Determina la pantalla a partir del segmento de la ruta y consulta la tabla Permiso para el rol y el método HTTP. Sin fila de permiso, la respuesta es 403.
6. Si el usuario es AdminProveedor y accede a `/api/proveedores/{id}`, verifica que el identificador sea el suyo.
7. El recurso invoca al servicio, que aplica las reglas, ejecuta SQL parametrizado y, en operaciones de escritura, registra la auditoría en la misma transacción.
8. `ErroresApi` convierte cualquier excepción en una respuesta JSON con un mensaje y una referencia para el registro del servidor.

### 4.4 Inicio de sesión

El cliente envía `POST /api/login` con usuario y contraseña. `LoginServicio` busca el usuario, rechaza los inactivos y compara la contraseña con el hash BCrypt. Si es válida, se invalida cualquier sesión previa, se crea una nueva con inactividad máxima de 30 minutos, se guardan el usuario, el rol y el proveedor, y se genera un token CSRF aleatorio. La respuesta devuelve los datos del usuario, el token y su lista de permisos, con los que el portal construye el menú. Cada dirección IP puede intentar 10 inicios de sesión por minuto; el undécimo recibe 429.

### 4.5 Informes

| N.º | Informe | Origen |
|---|---|---|
| 1 | Historial de compra por artículo y período | `db/queries/01_HistorialCompra.sql` |
| 2 | Top 5 de proveedores por monto adjudicado | `02_TopProveedores.sql` |
| 3 | Comparación de ofertas y ganadora de una orden | `03_ComparacionOfertas.sql` |
| 4 | Pedidos sin asignación | `04_PedidosSinAsignar.sql` |
| 5 | Órdenes abiertas (vista) | `05_OrdenesAbiertas.sql` |
| 6 | Gasto por departamento, sucursal y año | `06_GastoDepartamento.sql` |
| 7 | Promedio de días entre creación y adjudicación | `07_PromedioAdjudicacion.sql` |
| 8 | Evolución de precios por artículo y proveedor | `08_EvolucionPrecios.sql` |

Los archivos de `db/queries` usan parámetros con nombre (`@Articulo`, `@Desde`, etcétera) y se pueden ejecutar directamente en SQL Server Management Studio. `ReportesServicio` contiene la misma consulta con parámetros posicionales y, cuando el usuario es AdminProveedor, agrega un filtro por su proveedor. Los informes 4, 6 y 7 se niegan a ese rol. Los mismos ocho informes existen como archivos RDL en `reports/ssrs`.

### 4.6 Prueba de conexión a los dos motores

`GET /api/conexion/test` abre una conexión desde cada pool, ejecuta `SELECT 1`, consulta metadatos (versión, base, servidor, hora) y devuelve para cada motor su disponibilidad y su latencia. Los errores se reportan solo con su tipo y estado SQL, nunca con la cadena de conexión ni las credenciales. La pantalla "Conexión de bases" presenta ese resultado y es la forma más directa de demostrar que el sistema opera con ambos motores de manera simultánea.

## 5. Seguridad

### 5.1 Dos capas independientes

La seguridad se compone de dos mecanismos que no deben confundirse. El primero es la cuenta de base de datos `app_adquisiciones`, que usa la aplicación para conectarse a los motores; es una credencial de infraestructura, única para toda la aplicación. El segundo son las tablas Usuario, Rol, Pantalla y Permiso, que identifican a cada persona y deciden lo que puede hacer. Las personas nunca se conectan a las bases de datos: lo hace la aplicación en su nombre, y la aplicación decide qué se permite.

### 5.2 Autenticación

- Contraseñas almacenadas con BCrypt (costo 12 en los usuarios de la semilla). La contraseña nunca se devuelve en la API.
- Longitud de contraseña de 12 a 72 bytes al crear o cambiar un usuario.
- Sesión HTTP en cookie HttpOnly, con modo de seguimiento por cookie y expiración a los 30 minutos de inactividad.
- Al iniciar sesión se invalida la sesión anterior, lo que evita la fijación de sesión.
- Límite de 10 intentos por minuto y por dirección IP.

### 5.3 Autorización

`AuthFilter` traduce el segmento de la ruta a una pantalla (por ejemplo `articulos` a `Articulos`, `ordenes` a `Ordenes`) y el método HTTP a un permiso: GET es leer, POST es crear, PUT es actualizar y DELETE es borrar. La consulta se hace en cada petición contra la tabla Permiso, así que un cambio de la matriz surte efecto de inmediato. Si la ruta no está asociada a una pantalla o no existe una fila de permiso, se deniega.

Las rutas `/api/login/me` y `/api/login/logout` solo exigen sesión. Las rutas bajo `/api/gestion/{modulo}` se asignan por el nombre del módulo, y los teléfonos de sucursal y los rubros heredan la pantalla de Sucursales y Proveedores respectivamente.

### 5.4 Matriz de permisos entregada por la semilla

La semilla crea los cuatro roles, las veinte pantallas y una fila de permiso para cada combinación, con estas reglas:

| Rol | Lectura | Escritura (crear, actualizar y borrar) |
|---|---|---|
| AdminSistema | Todas las pantallas | Todas las pantallas |
| GestorCompras | Todas las pantallas | Pedidos, Ordenes, Ofertas, Adjudicaciones y Evaluaciones |
| AdminProveedor | Articulos, ProveedorArticulos, Ordenes, Pedidos, Ofertas, Dashboard y Reportes | ProveedorArticulos y Ofertas |
| Auditor | Todas las pantallas | Ninguna |

Las pantallas son: Sucursales, Departamentos, Articulos, Proveedores, ProveedorArticulos, Relaciones, TiposOrden, Pedidos, Ordenes, Ofertas, Adjudicaciones, Evaluaciones, Usuarios, Roles, Pantallas, Permisos, Dashboard, Reportes, Conexion y Auditoria. La semilla solo inserta filas que no existen, de modo que los cambios que un administrador haga luego en la matriz no se sobrescriben al repetirla.

### 5.5 Alcance por proveedor

Para el rol AdminProveedor el filtro y los servicios aplican un ámbito adicional que no depende de la matriz: solo ve sus propias ofertas y su catálogo, en las solicitudes solo aquellas de órdenes abiertas con artículos de su catálogo, en las órdenes solo las que le conciernen, y en los informes únicamente sus datos. Si en una escritura indica otro proveedor, la petición se rechaza con 403, y el campo de proveedor se fuerza al suyo.

### 5.6 Protección de las peticiones

- Token CSRF: toda petición que no sea GET, HEAD u OPTIONS debe llevar en `X-CSRF-Token` el valor entregado al iniciar sesión. `js/api.js` lo agrega automáticamente a las peticiones del portal.
- Cabeceras: `X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN` y `Cache-Control: no-store` en la API.
- SQL parametrizado con `PreparedStatement` en todo el código. Los nombres de tablas y columnas vienen de `Modulo.java` y nunca del cliente.
- Lista blanca de campos: el CRUD genérico rechaza cualquier campo que no esté definido en el esquema del módulo.
- Validación de tipos, rangos y longitudes en el servidor, independiente de la validación del navegador.
- Las respuestas de error no revelan detalles internos; incluyen una referencia para ubicar el evento en el registro del servidor.
- El portal construye el contenido con `textContent`, no con HTML, y el CSV neutraliza fórmulas al exportar.

### 5.7 Protecciones de integridad administrativa

El sistema impide que un administrador se borre, se desactive o cambie su propio rol, exige conservar al menos un AdminSistema activo y no permite borrar ni renombrar los cuatro roles base. Un usuario con rol AdminProveedor debe tener proveedor asociado, y los demás no pueden tenerlo.

### 5.8 Auditoría

`SqlDAO.audit` inserta en la tabla Auditoria, dentro de la misma transacción de la operación, el usuario, la acción (CREAR, ACTUALIZAR, BORRAR, ADJUDICAR o REVERTIR), la entidad y la referencia del registro. Como forma parte de la transacción, un cambio que se revierte no deja rastro, y un cambio confirmado siempre queda registrado. El inicio de sesión no genera auditoría.

### 5.9 Gestión de secretos

El WAR no incluye credenciales: `pom.xml` excluye `db.properties` de los recursos empaquetados. `ConexionManager` busca la configuración en este orden:

1. La propiedad de sistema `-Dguatecompras.config` o la variable de entorno `GUATECOMPRAS_CONFIG`, con la ruta del archivo.
2. `config/local-db.properties` en el directorio de trabajo y en sus directorios padres.
3. `config/local-db.properties` dentro de subcarpetas del directorio de trabajo, para cuando Tomcat arranca desde el espacio de trabajo de Eclipse.
4. `db.properties` en el classpath, que solo existe en el despliegue de desarrollo de Eclipse.

Cada valor puede sustituirse con una variable de entorno cuyo nombre es `GUATECOMPRAS_` seguido de la clave en mayúsculas y con guion bajo, por ejemplo `GUATECOMPRAS_SQLSERVER_PASSWORD` o `GUATECOMPRAS_POSTGRES_URL`. El `.gitignore` excluye `src/main/resources/db.properties`, `config/local-db.properties` y `config/demo-access.properties`.

### 5.10 Recomendaciones para un entorno real

- Cambiar las contraseñas de la cuenta de base de datos y de los usuarios de demostración, y no reutilizar claves compartidas durante el desarrollo.
- Otorgar a `app_adquisiciones` solo los permisos necesarios (lectura, escritura y uso de secuencias), no propiedad de la base.
- Publicar solo por HTTPS y marcar la cookie de sesión como Secure.
- Sustituir `trustServerCertificate=true` por un certificado de confianza en SQL Server.
- No distribuir el proyecto con archivos de credenciales; revisar el contenido de cualquier archivo comprimido antes de entregarlo.

## 6. API REST

Todas las rutas cuelgan de `/sistema-adquisiciones/api`. Las respuestas son JSON; los errores incluyen los campos `error` y, cuando aplica, `referencia`.

| Ruta | Métodos | Descripción |
|---|---|---|
| `/login` | POST | Inicio de sesión. Entrada: `nombreUsuario`, `contrasena`. |
| `/login/me` | GET | Usuario actual, token CSRF y permisos. |
| `/login/logout` | POST | Cierra la sesión. |
| `/gestion/{modulo}` | GET, POST | Listado paginado con búsqueda (`page`, `pageSize`, `q`) y alta. |
| `/gestion/{modulo}/{clave}` | GET, PUT, DELETE | Consulta, actualización y borrado. Las claves compuestas se unen con `~`. |
| `/gestion/{modulo}/schema` | GET | Definición de campos del módulo, que usa el portal para dibujar los formularios. |
| `/ordenes` y `/ordenes/{id}` | GET, POST, PUT, DELETE | Órdenes de compra con sus pedidos y ofertas. |
| `/adjudicaciones` y `/adjudicaciones/{id}` | GET, POST, PUT, DELETE | Adjudicaciones y sus líneas. |
| `/reportes` y `/reportes/{1..8}` | GET | Catálogo de informes y ejecución con parámetros. |
| `/dashboard` | GET | Indicadores del resumen institucional. |
| `/auditoria` | GET | Historial de cambios, paginado. |
| `/conexion/test` | GET | Estado de los dos motores. |
| `/sucursales`, `/departamentos`, `/articulos`, `/proveedores`, `/proveedores/{id}/articulos` | GET, POST, PUT, DELETE | CRUD específicos conservados. |

Códigos de respuesta usados: 200 y 201 en éxito, 204 al borrar, 400 por datos o reglas no válidas, 401 sin sesión, 403 sin permiso o sin token CSRF, 404 registro inexistente, 409 duplicado o relación existente, 429 exceso de intentos de inicio de sesión y 503 cuando no se puede completar la consulta en la base de datos.

Los módulos de `/gestion` son: sucursales, departamentos, articulos, proveedores, proveedorarticulos, telefonos, rubros, relaciones, tiposorden, subtiposorden, roles, pantallas, permisos, usuarios, pedidos, ofertas y evaluaciones.

## 7. Despliegue y siembra de datos

### 7.1 Requisitos

| Elemento | Requisito |
|---|---|
| JDK | Java 17 |
| Compilación | Maven 3.9 o, sin Maven, el script `scripts/compilar_backend.ps1` |
| Servidor | Apache Tomcat 10.1 |
| Base principal | SQL Server con autenticación mixta y TCP/IP habilitado en el puerto 1433 |
| Segunda base | PostgreSQL con acceso en el puerto 5432 |
| Cliente de escritorio (opcional) | Windows con .NET Desktop Runtime 10 |
| Informes SSRS (opcional) | SQL Server Reporting Services y la variable `GUATECOMPRAS_SSRS_URL` |
| Pruebas (opcional) | Python 3 |

### 7.2 Preparar las bases de datos

Se crean las bases vacías y se ejecuta una sola vez el script de estructura de cada motor. Los nombres deben coincidir con los de las URL del archivo de configuración.

SQL Server (SSMS o `sqlcmd`):

```sql
CREATE DATABASE GuateCompras;
```

```
sqlcmd -S localhost -E -d GuateCompras -i db\ddl\sqlserver.sql
```

PostgreSQL:

```
createdb -U postgres guatecompras
psql -U postgres -d guatecompras -v ON_ERROR_STOP=1 -f db/ddl/postgres.sql
```

Los scripts de estructura no insertan datos ni fijan la base de datos: se ejecutan contra la que se elija. No deben ejecutarse sobre bases que ya contengan información.

### 7.3 Crear la cuenta de la aplicación

Se recomienda una cuenta propia, sin privilegios de administración. Los siguientes comandos son un ejemplo que se debe adaptar; sustituya la contraseña por una propia.

SQL Server:

```sql
CREATE LOGIN app_adquisiciones WITH PASSWORD = '<contraseña>';
USE GuateCompras;
CREATE USER app_adquisiciones FOR LOGIN app_adquisiciones;
ALTER ROLE db_datareader ADD MEMBER app_adquisiciones;
ALTER ROLE db_datawriter ADD MEMBER app_adquisiciones;
```

PostgreSQL, ejecutado después de crear el esquema:

```sql
CREATE ROLE app_adquisiciones LOGIN PASSWORD '<contraseña>';
GRANT CONNECT ON DATABASE guatecompras TO app_adquisiciones;
GRANT USAGE ON SCHEMA public TO app_adquisiciones;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_adquisiciones;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_adquisiciones;
```

### 7.4 Configurar la conexión

Modifique sus credenciales en local-db.properties, ruta: `config/local-db.properties` a partir de `src/main/resources/db.properties.example`. El archivo contiene seis claves:

```
sqlserver.url=jdbc:sqlserver://localhost:1433;databaseName=GuateCompras;encrypt=true;trustServerCertificate=true
sqlserver.user=app_adquisiciones
sqlserver.password=<contraseña>

postgres.url=jdbc:postgresql://localhost:5432/guatecompras
postgres.user=app_adquisiciones
postgres.password=<contraseña>
```

Este archivo nunca debe subirse al repositorio ni incluirse en un paquete de entrega. Si se despliega el WAR en otro Tomcat, indique su ubicación con `-Dguatecompras.config=<ruta>` o con la variable `GUATECOMPRAS_CONFIG`, porque el WAR no contiene credenciales.

### 7.5 Compilar y ejecutar

Con Maven instalado:

```
./scripts/build.ps1 -Maven mvn
./scripts/iniciar_tomcat.ps1 -Java <ruta-jdk17> -TomcatHome <ruta-tomcat10>
```

El primer comando ejecuta `mvn clean verify` y deja `target/sistema-adquisiciones.war` y el directorio expandido `target/sistema-adquisiciones`. Sin Maven, `scripts/compilar_backend.ps1` compila con `javac` usando las librerías del repositorio local de Maven. El segundo comando prepara una base de Tomcat en `target/tomcat-base` y arranca el servidor.

El proyecto también puede ejecutarse desde Eclipse mediante "Run on Server" con el servidor Apache Tomcat 10.1 registrado, o desde VS Code con las tareas definidas en `.vscode`. En ambos casos el puerto habitual es 8080 y el portal queda en:

```
http://localhost:8080/sistema-adquisiciones/portal.html
```

El README del proyecto, el archivo de ejemplo de variables y las pruebas de integración usan 18080 como puerto de ejemplo; utilice el que corresponda a su Tomcat.

### 7.6 Carga automática de datos con la semilla

Nota importante. Al poner en marcha el sistema por primera vez, las tablas de las bases de datos de GuateCompras se pueblan de forma totalmente automática en el sentido de que no requiere intervención manual sobre los datos, pero hay que lanzarla una vez, después de crear la estructura. Arrancar Tomcat por sí solo no la ejecuta.
Ejecución con los scripts del proyecto, una vez compilado el backend:
./scripts/bases.ps1 -Accion Semilla -Java java
./scripts/bases.ps1 -Accion Verificar -Java java
El primer comando ejecuta com.adquisiciones.util.SemillaDemo. El segundo ejecuta VerificarBases. Como alternativa propia de quien usa Eclipse, la clase SemillaDemo tiene un método main y puede lanzarse con “Run As, Java Application” desde la raíz del proyecto, de modo que encuentre config/local-db.properties; esta forma no está incluida en los scripts del repositorio.

 No hay que escribir ni importar los datos a mano: un solo comando crea roles, pantallas, permisos, sucursales, departamentos, artículos, proveedores, catálogos, usuarios, órdenes, solicitudes, ofertas y adjudicaciones, y lo hace en SQL Server y en PostgreSQL. Si, en cambio, se prefiere poblar las bases con scripts DML propios de la carpeta “db” (instrucciones INSERT) desde su gestor preferido, las bases de datos deben estar vacías, es decir, solo con la estructura creada por los scripts de db/ddl, y no debe ejecutarse la semilla ni antes ni después. 


Comportamiento de la semilla:

- Escribe en SQL Server y luego en PostgreSQL, cada uno en una transacción independiente. Si falla un motor, ese motor queda como estaba; el otro puede haberse confirmado ya.
- Es repetible: antes de insertar busca cada registro por su clave natural (códigos, nombres, descripciones) y solo crea lo que falta. No borra información.
- No hay réplica: cada motor recibe su propia carga, y como ambos se pueblan en el mismo orden sobre bases vacías, los identificadores coinciden.
- Las fechas son relativas al día de la primera ejecución, que se guarda en la clave `_seedDate` de `config/demo-access.properties`.
- Genera contraseñas aleatorias de 18 bytes, codificadas en Base64 URL, para los cuatro usuarios y las guarda en `config/demo-access.properties`. Ese archivo no se versiona y es la única fuente de esas contraseñas, porque en la base solo se guarda su hash.

Advertencia sobre el archivo de accesos. Si `config/demo-access.properties` se pierde y la semilla se vuelve a ejecutar, el sistema generará contraseñas nuevas en el archivo pero no modificará a los usuarios que ya existen, por lo que las contraseñas del archivo no coincidirán con las de la base. Además, la fecha base cambiaría y se crearían solicitudes sin asignar adicionales. Conserve ese archivo o, si se perdió, restablezca las contraseñas desde la pantalla Usuarios.

### 7.7 Datos que carga la semilla

| Tabla | Registros | Origen de los datos |
|---|---|---|
| Rol | 4 | AdminSistema, GestorCompras, AdminProveedor y Auditor |
| Pantalla | 20 | Las veinte pantallas listadas en la sección 5.4 |
| Permiso | 80 | Una fila por rol y pantalla, según la matriz de la sección 5.4 |
| Sucursal | 5 | Códigos GC1 a GC5 en Guatemala, Salamá, Cobán, Quetzaltenango y Flores |
| SucursalTelefono | 10 | Dos teléfonos por sucursal |
| Departamento | 10 | Administración y Operaciones en cada sucursal |
| Articulo | 50 | ART-001 a ART-050 con nombres de papelería, cómputo, mobiliario y limpieza |
| Proveedor | 20 | PRV-001 a PRV-020 con rubro principal que rota entre cinco rubros |
| ProveedorRubro | 40 | El rubro principal y el siguiente de la lista |
| ProveedorArticulo | 1000 | Cada proveedor con los 50 artículos |
| Usuario | 4 | admin, gestor, proveedor (asociado a PRV-001) y auditor |
| TipoOrden | 2 | Grande y Chica |
| SubtipoOrden | 4 | Normal y Urgente por cada tipo |
| OrdenCompra | 20 | "DEMO Orden 01" a "DEMO Orden 20", tipos alternados |
| Pedido | 100 | 60 dentro de las órdenes y 40 sin asignar |
| Oferta | 180 | Tres por cada pedido de orden |
| Adjudicacion | 10 | Órdenes 01 a 10 |
| DetalleAdjudicacion | 30 | Tres por adjudicación; la ganadora es la primera oferta de cada pedido |
| RelacionComercial | 5 | PRV-001 con PRV-011 hasta PRV-005 con PRV-015 |
| EvaluacionProveedor | 0 | No se siembra |
| Auditoria | 0 | No se siembra |

Los precios se calculan con una fórmula determinista: depende del artículo (posiciones 1 a 20: 5 más 2 por posición; 21 a 35: 250 más 175 por posición desde la 21; 36 a 50: 25 más 30 por posición desde la 36) y suma 3 quetzales por el residuo del número de proveedor entre 5. Las órdenes 01 a 10 se crean 45 días antes de la fecha base, con un plazo de ofertas de 10 días y adjudicación al día 12; las órdenes 11 a 20 se crean 7 días antes y su límite vence 10 días después de la fecha base. Las solicitudes sin asignar se registran entre 3 y 5 días antes y se necesitan 15 días después.

Los mínimos exigidos por el enunciado del curso (5 sucursales, 10 departamentos, 50 artículos, 20 proveedores y 100 pedidos) quedan cubiertos con estos volúmenes.

### 7.8 Verificar la instalación

1. `./scripts/bases.ps1 -Accion Verificar` comprueba, en ambos motores, los mínimos de registros, que la vista de órdenes abiertas coincida con su definición, que no haya ofertas sobre pedidos sin orden y que los triggers rechacen casos inválidos (precio no positivo, oferta fuera de plazo, oferta sobre pedido sin asignar, modificación de una oferta ganadora, entre otros). Los casos negativos se revierten.
2. Abrir el portal e iniciar sesión con `admin`. Entrar en "Conexión de bases" y confirmar que ambos motores aparecen como disponibles.
3. Verificar que el Resumen institucional muestra monto adjudicado y cinco proveedores.
4. Opcional: ejecutar la suite de integración, con el servidor y la semilla activos:

```
python tests/integration/run.py
```

Por defecto la suite apunta a `http://127.0.0.1:18080/sistema-adquisiciones/`; para otro puerto defina `GUATECOMPRAS_TEST_URL`. Las cinco suites cubren seguridad, CRUD, flujo de adquisición, informes y casos límite, y limpian los datos temporales que crean. Es importante saber que las pruebas se ejecutan contra el servidor real y leen las contraseñas de `config/demo-access.properties`.

### 7.9 Empaquetado para otro servidor

1. Ejecutar `./scripts/build.ps1` para generar `target/sistema-adquisiciones.war`.
2. Copiar el WAR a la carpeta `webapps` del Tomcat de destino.
3. Colocar el archivo de configuración fuera del WAR y declararlo con `-Dguatecompras.config` o `GUATECOMPRAS_CONFIG` en el arranque de Tomcat.
4. Crear las bases con los scripts de `db/ddl`, la cuenta de la aplicación y, si se desea, ejecutar la semilla.

### 7.10 Componentes opcionales

Cliente WPF. Se compila con `./scripts/build_desktop.ps1` (Visual Studio o el SDK de .NET 10). Solicita la dirección del servidor al iniciar sesión; la dirección predeterminada se define con `GUATECOMPRAS_API_URL`.

SSRS. `scripts/instalar_ssrs.ps1` instala y configura el servidor de informes y `scripts/publicar_ssrs.ps1` publica los ocho RDL. Con la variable `GUATECOMPRAS_SSRS_URL` definida, el portal muestra el botón "Abrir SSRS" en la pantalla de informes. SSRS lee SQL Server con una identidad de Windows distinta de la cuenta de la aplicación.

Calidad. `./scripts/calidad.ps1` ejecuta PMD y CPD y deja los informes en `target/pmd.xml` y `target/cpd.xml`.

### 7.11 Restablecer el entorno de demostración

Para volver a un estado limpio, se eliminan y recrean las dos bases (o se vacían las tablas respetando las claves foráneas), se ejecutan de nuevo los scripts de `db/ddl`, se borra `config/demo-access.properties` y se ejecuta la semilla. Es una operación destructiva que solo debe hacerse en entornos de prueba.

## 8. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| "Failed to get driver instance" o `Connection refused` al puerto 1433 | SQL Server sin TCP/IP habilitado o sin puerto fijo, o servicio detenido. | En SQL Server Configuration Manager, habilite TCP/IP, fije el puerto 1433, reinicie el servicio y revise el firewall. |
| La prueba de conexión muestra SQL Server en verdadero y PostgreSQL en falso | Base, usuario o contraseña de PostgreSQL incorrectos, o servicio detenido. | Revise `postgres.url` y las credenciales; el nombre de la base debe coincidir exactamente con el creado. |
| "Falta la configuración 'sqlserver.url'" | `ConexionManager` no encontró el archivo de configuración. | Cree `config/local-db.properties` o defina `GUATECOMPRAS_CONFIG` con su ruta absoluta. |
| `database ... is being accessed by other users` al recrear en PostgreSQL | Hay conexiones abiertas a la base. | Cierre las conexiones, por ejemplo con `pg_terminate_backend`, y repita. |
| La semilla falla con un error de trigger | La base contiene datos manuales que contradicen las reglas. | Use bases vacías o revise los datos; la semilla de cada motor se revierte por completo si falla. |
| 401 al abrir una URL de `/api` en el navegador | No hay sesión activa. | Inicie sesión en el portal y vuelva a consultar. |
| 403 en una operación de escritura con sesión válida | Falta el token CSRF o el rol no tiene permiso. | Use el portal, que envía el token, o agregue `X-CSRF-Token` a las pruebas manuales; revise la matriz de permisos. |
| 429 al iniciar sesión | Más de 10 intentos en un minuto. | Espere un minuto. |
| Un menú del portal no aparece | El rol no tiene permiso de lectura sobre esa pantalla. | Ajuste la matriz de permisos. |
| Las órdenes de la demostración ya no aparecen como abiertas | Pasó el plazo de ofertas, que se calcula desde la fecha de la primera carga. | Cree órdenes nuevas con fechas vigentes o restablezca el entorno. |
| Las contraseñas del archivo de accesos no funcionan | El archivo se regeneró después de existir los usuarios. | Restablezca las contraseñas desde la pantalla Usuarios. |

## 9. Limitaciones conocidas y mejoras sugeridas

- Los datos operativos residen únicamente en SQL Server; PostgreSQL recibe su propia carga de la semilla, pero las operaciones del portal no se replican en él.
- La pantalla de evaluaciones pide el identificador numérico de la línea adjudicada, que el detalle de la adjudicación no muestra. Una mejora natural es elegirla de una lista.
- Las infracciones de los dos triggers de fechas originales en SQL Server se reportan con `RAISERROR`, que produce el código 50000 y la API responde 503; la aplicación ya valida esas fechas antes, de modo que no se alcanzan desde el portal. Convertirlas a `THROW` con un código del rango 51000 uniformaría la respuesta.
- La conexión a SQL Server usa `trustServerCertificate=true`, adecuada para desarrollo local pero no para producción.
- El proyecto conserva dos caminos para algunos catálogos (la API específica y la genérica). Conviene retirar uno cuando termine la entrega.
- Los archivos `PruebaConexion2.java`, `PruebasConexion.java`, `GenerarHash.java` y `hashprueba.java` son de apoyo al desarrollo y no forman parte de la funcionalidad.
- La cobertura de pruebas es de integración; no hay pruebas unitarias con JUnit.

## Anexo A. Comandos de referencia

| Tarea | Comando |
|---|---|
| Compilar con Maven | `./scripts/build.ps1 -Maven mvn` |
| Compilar sin Maven | `./scripts/compilar_backend.ps1` |
| Iniciar Tomcat local | `./scripts/iniciar_tomcat.ps1 -Compilar` |
| Cargar la semilla | `./scripts/bases.ps1 -Accion Semilla -Java java` |
| Verificar las bases | `./scripts/bases.ps1 -Accion Verificar -Java java` |
| Pruebas de integración | `python tests/integration/run.py` |
| Análisis de calidad | `./scripts/calidad.ps1 -Maven mvn` |
| Compilar el cliente WPF | `./scripts/build_desktop.ps1` |

## Anexo B. Variables y propiedades de configuración

| Nombre | Uso |
|---|---|
| `guatecompras.config` (propiedad de Java) o `GUATECOMPRAS_CONFIG` | Ruta del archivo de configuración de bases de datos. |
| `GUATECOMPRAS_SQLSERVER_URL`, `_USER`, `_PASSWORD` | Sustituyen a `sqlserver.url`, `sqlserver.user` y `sqlserver.password`. |
| `GUATECOMPRAS_POSTGRES_URL`, `_USER`, `_PASSWORD` | Sustituyen a las claves `postgres.*`. |
| `GUATECOMPRAS_API_URL` | Dirección de la API que usa el cliente WPF. |
| `GUATECOMPRAS_SSRS_URL` | Dirección del servidor de informes; habilita el botón "Abrir SSRS". |
| `GUATECOMPRAS_TEST_URL` | Dirección base de las pruebas de integración. |

## Anexo C. Glosario técnico

BCrypt: algoritmo de hash de contraseñas con sal y costo configurable.

CSRF: ataque que aprovecha una sesión abierta para enviar peticiones no deseadas; se mitiga con un token por sesión.

DAO: objeto de acceso a datos, que concentra las consultas SQL.

HikariCP: biblioteca que administra un grupo de conexiones reutilizables.

JAX-RS: especificación de Java para servicios REST, implementada aquí con Jersey.

Llave sustituta: identificador numérico sin significado de negocio que se usa como clave primaria.

Semilla: proceso que carga los datos de demostración en las bases de datos.

Trigger: código que la base de datos ejecuta automáticamente al insertar, actualizar o borrar filas.

WAR: archivo empaquetado de una aplicación web Java.
