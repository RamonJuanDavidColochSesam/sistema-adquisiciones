# Backend: contratos, CRUD y transacciones

Complemento de los capítulos 7,10,11,13,23 y 29. Prefijo de demo: `http://127.0.0.1:18080/sistema-adquisiciones`; las rutas siguientes empiezan en `/api`.

## Inventario de endpoints extraído de anotaciones

Esta tabla indica declaraciones existentes, no que todas estén autorizadas para todos los roles. `/api/hello` se deniega por el mapa de autorización actual. Las rutas paramétricas de gestion se restringen a 17 módulos. No se inventan endpoints planos para todas las tablas.

| Método | Ruta | Recurso fuente |
|---|---|---|
| GET | `/api/adjudicaciones` | [`src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java`](../src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java) |
| GET | `/api/adjudicaciones/{id}` | [`src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java`](../src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java) |
| POST | `/api/adjudicaciones` | [`src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java`](../src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java) |
| PUT | `/api/adjudicaciones/{id}` | [`src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java`](../src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java) |
| DELETE | `/api/adjudicaciones/{id}` | [`src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java`](../src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java) |
| GET | `/api/articulos` | [`src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java) |
| GET | `/api/articulos/{id}` | [`src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java) |
| POST | `/api/articulos` | [`src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java) |
| PUT | `/api/articulos/{id}` | [`src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java) |
| DELETE | `/api/articulos/{id}` | [`src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java) |
| GET | `/api/auditoria` | [`src/main/java/com/adquisiciones/recurso/AuditoriaRecurso.java`](../src/main/java/com/adquisiciones/recurso/AuditoriaRecurso.java) |
| GET | `/api/conexion/test` | [`src/main/java/com/adquisiciones/recurso/ConexionRecurso.java`](../src/main/java/com/adquisiciones/recurso/ConexionRecurso.java) |
| GET | `/api/dashboard` | [`src/main/java/com/adquisiciones/recurso/DashboardRecurso.java`](../src/main/java/com/adquisiciones/recurso/DashboardRecurso.java) |
| GET | `/api/departamentos` | [`src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java`](../src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java) |
| GET | `/api/departamentos/{id}` | [`src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java`](../src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java) |
| POST | `/api/departamentos` | [`src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java`](../src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java) |
| PUT | `/api/departamentos/{id}` | [`src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java`](../src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java) |
| DELETE | `/api/departamentos/{id}` | [`src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java`](../src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java) |
| GET | `/api/gestion/{modulo}/schema` | [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java) |
| GET | `/api/gestion/{modulo}` | [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java) |
| GET | `/api/gestion/{modulo}/{key}` | [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java) |
| POST | `/api/gestion/{modulo}` | [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java) |
| PUT | `/api/gestion/{modulo}/{key}` | [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java) |
| DELETE | `/api/gestion/{modulo}/{key}` | [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java) |
| GET | `/api/hello` | [`src/main/java/com/adquisiciones/recurso/HelloRecurso.java`](../src/main/java/com/adquisiciones/recurso/HelloRecurso.java) |
| POST | `/api/login` | [`src/main/java/com/adquisiciones/recurso/LoginRecurso.java`](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java) |
| GET | `/api/login/me` | [`src/main/java/com/adquisiciones/recurso/LoginRecurso.java`](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java) |
| POST | `/api/login/logout` | [`src/main/java/com/adquisiciones/recurso/LoginRecurso.java`](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java) |
| GET | `/api/ordenes` | [`src/main/java/com/adquisiciones/recurso/OrdenRecurso.java`](../src/main/java/com/adquisiciones/recurso/OrdenRecurso.java) |
| GET | `/api/ordenes/{id}` | [`src/main/java/com/adquisiciones/recurso/OrdenRecurso.java`](../src/main/java/com/adquisiciones/recurso/OrdenRecurso.java) |
| POST | `/api/ordenes` | [`src/main/java/com/adquisiciones/recurso/OrdenRecurso.java`](../src/main/java/com/adquisiciones/recurso/OrdenRecurso.java) |
| PUT | `/api/ordenes/{id}` | [`src/main/java/com/adquisiciones/recurso/OrdenRecurso.java`](../src/main/java/com/adquisiciones/recurso/OrdenRecurso.java) |
| DELETE | `/api/ordenes/{id}` | [`src/main/java/com/adquisiciones/recurso/OrdenRecurso.java`](../src/main/java/com/adquisiciones/recurso/OrdenRecurso.java) |
| GET | `/api/proveedores/{idProveedor}/articulos` | [`src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java) |
| POST | `/api/proveedores/{idProveedor}/articulos` | [`src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java) |
| PUT | `/api/proveedores/{idProveedor}/articulos/{idArticulo}` | [`src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java) |
| DELETE | `/api/proveedores/{idProveedor}/articulos/{idArticulo}` | [`src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java) |
| GET | `/api/proveedores` | [`src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java) |
| GET | `/api/proveedores/{id}` | [`src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java) |
| POST | `/api/proveedores` | [`src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java) |
| PUT | `/api/proveedores/{id}` | [`src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java) |
| DELETE | `/api/proveedores/{id}` | [`src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java) |
| GET | `/api/reportes` | [`src/main/java/com/adquisiciones/recurso/ReportesRecurso.java`](../src/main/java/com/adquisiciones/recurso/ReportesRecurso.java) |
| GET | `/api/reportes/{id}` | [`src/main/java/com/adquisiciones/recurso/ReportesRecurso.java`](../src/main/java/com/adquisiciones/recurso/ReportesRecurso.java) |
| GET | `/api/sucursales` | [`src/main/java/com/adquisiciones/recurso/SucursalRecurso.java`](../src/main/java/com/adquisiciones/recurso/SucursalRecurso.java) |
| GET | `/api/sucursales/{id}` | [`src/main/java/com/adquisiciones/recurso/SucursalRecurso.java`](../src/main/java/com/adquisiciones/recurso/SucursalRecurso.java) |
| POST | `/api/sucursales` | [`src/main/java/com/adquisiciones/recurso/SucursalRecurso.java`](../src/main/java/com/adquisiciones/recurso/SucursalRecurso.java) |
| PUT | `/api/sucursales/{id}` | [`src/main/java/com/adquisiciones/recurso/SucursalRecurso.java`](../src/main/java/com/adquisiciones/recurso/SucursalRecurso.java) |
| DELETE | `/api/sucursales/{id}` | [`src/main/java/com/adquisiciones/recurso/SucursalRecurso.java`](../src/main/java/com/adquisiciones/recurso/SucursalRecurso.java) |

## Los 17 esquemas del catálogo

Todos usan Modulo → GestionRecurso → CatalogoServicio → SqlDAO → ConexionManager/SQL Server. schema es GET; listado GET; detalle GET; alta POST; edición PUT; baja DELETE. La tabla muestra las claves utilizadas por `_key`.

| Módulo | Tabla | Clave | Regla/presentación particular |
|---|---|---|---|
| `sucursales` | Sucursal | `id_sucursal` | Código único; teléfono principal y módulos adicionales de teléfonos |
| `departamentos` | Departamento | `id_departamento` | Sucursal existente; nombre único por sucursal; código sucursal en consulta |
| `articulos` | Articulo | `id_articulo` | Código único y estado Activo/Inactivo |
| `proveedores` | Proveedor | `id_proveedor` | Código único; promedio calculado de evaluaciones; estado |
| `proveedorarticulos` | ProveedorArticulo | `id_proveedor~id_articulo` | Clave proveedor/artículo; precio positivo; scope e historial |
| `telefonos` | SucursalTelefono | `id_sucursal~telefono` | Sucursal/telefono como clave compuesta; no lista en VARCHAR |
| `rubros` | ProveedorRubro | `id_proveedor~rubro` | Proveedor/rubro como clave compuesta; no tabla Rubro independiente |
| `relaciones` | RelacionComercial | `id_relacion_comercial` | Dos proveedores; orden canónico a<b |
| `tiposorden` | TipoOrden | `id_tipo` | Grande/Chica; enum validado y CHECK |
| `subtiposorden` | SubtipoOrden | `id_subtipo` | Urgente/Normal; nombre único por tipo |
| `roles` | Rol | `id_rol` | Conservar nombres y existencia de cuatro roles académicos |
| `pantallas` | Pantalla | `id_pantalla` | Nombre único; matriz de autorización depende del nombre |
| `permisos` | Permiso | `id_rol~id_pantalla` | Rol/pantalla; cuatro booleanos; valores cliente no deciden identidad |
| `usuarios` | Usuario | `id_usuario` | Alta con hash; asociación proveedor según rol; conservar último admin |
| `pedidos` | Pedido | `id_pedido` | Artículo activo, fechas y pedido no asignado al editar/borrar |
| `ofertas` | Oferta | `id_oferta` | Catálogo, proveedor activo, plazo, propiedad e historial |
| `evaluaciones` | EvaluacionProveedor | `id_evaluacion` | Detalle resuelto, fecha de evaluación y calificación 1–5 |

## CRUD que conviven

| Entidad | Modelo/DAO específico | Servicio/recurso original | Ruta original | Ruta de clientes actuales |
|---|---|---|---|---|
| Sucursal | Sucursal/SucursalDAO | SucursalServicio/SucursalRecurso | /api/sucursales | /api/gestion/sucursales |
| Departamento | Departamento/DepartamentoDAO | DepartamentoServicio/DepartamentoRecurso | /api/departamentos | /api/gestion/departamentos |
| Articulo | Articulo/ArticuloDAO | ArticuloServicio/ArticuloRecurso | /api/articulos | /api/gestion/articulos |
| Proveedor | Proveedor/ProveedorDAO | ProveedorServicio/ProveedorRecurso | /api/proveedores | /api/gestion/proveedores |
| ProveedorArticulo | ProveedorArticulo/ProveedorArticuloDAO | ProveedorArticuloServicio/ProveedorArticuloRecurso | /api/proveedores/{idProveedor}/articulos | /api/gestion/proveedorarticulos |

Un ejemplo didáctico de cuerpo para el catálogo actual (no ejecutado por esta tarea) es:

```json
{"codigo_proveedor":"EJEMPLO-01","nombre_comercial":"Proveedor de ejemplo","direccion":"Dirección de ejemplo","telefono":"2200-0000","categoria":"Papelería","estado":"Activo"}
```

El contrato original espera codigoProveedor/nombreComercial y no expone el estado del mismo modo. La lectura genérica devuelve paginación y `_key`; la original suele devolver directamente la lista. El servicio genérico agrega Auditoria en la misma transacción; no adjudicar esa propiedad a todos los DAO originales.

## Transacción real y dinero

AdquisicionServicio.transaction obtiene Connection, setAutoCommit(false), setTransactionIsolation(SERIALIZABLE), ejecuta el trabajo y commit; su catch hace rollback. CatalogoServicio implementa un patrón semejante para alta/edición/baja. UPDLOCK/HOLDLOCK se usa en orden/pedidos relevantes para guardar una agrupación; no se usa en absolutamente todas las consultas.

Una adjudicación recibe id_orden, fecha_resolucion, observaciones y detalles[id_pedido,id_oferta,cantidad_final,precio_acordado]. La validación usa intValueExact para cantidades e IDs y BigDecimal con escala 2 UNNECESSARY. Debe incluir todos los pedidos de la orden; una cantidad final menor puede ser válida. El precio debe igualar la oferta, no el catálogo ni el mínimo calculado. SQL Server calcula montos de informes con DECIMAL18,2 y no hay columna total en la resolución.

## Identidad y errores

AuthFilter inserta atributo request.usuario después de comprobar usuario activo. Los recursos nuevos toman esa identidad, no el idRol que pudiera enviar JSON. Los servicios de proveedor aplican restricción adicional. ErroresApi convierte IllegalArgumentException 400,WebApplicationException según código y SQLException orientada a SQL Server; otros fallos 500 con referencia. Login/filtro construyen algunas respuestas sin mapper. Consultar [SEGURIDAD](SEGURIDAD.md) y [PRUEBAS](PRUEBAS.md).
