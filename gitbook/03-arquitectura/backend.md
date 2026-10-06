# Backend Java

`ConexionManager` crea pools independientes bajo demanda: máximo 5 conexiones y mínimo 1, espera de conexión 10 s, validación 3 s. `try-with-resources` devuelve cada conexión al pool. El listener cierra los pools al detener el contexto. El dashboard reutiliza una sola conexión para métricas y ranking, evitando bloqueos del pool por adquisiciones anidadas. La API operativa usa SQL Server; PostgreSQL se prueba con el mismo manager en migraciones/semilla/verificación y en diagnóstico protegido. No hay doble escritura ni transacción distribuida.

Migraciones V001/V002/V003/V004: multivaluados normalizados, estados activos, auditoría, índices, vista, compatibilidad tipo/subtipo, pares comerciales canónicos y triggers de protección temporal e histórica. `SchemaVersion` controla versiones. Las reglas de integridad ejecutadas en ambos motores rechazan precios/cantidades inválidos, oferta sin orden o fuera de período, detalles incompatibles, reasignación con ofertas, cambios de fechas que invalidan hijos y modificación de ofertas ganadoras.

Adquisiciones usa una conexión por operación, aislamiento SERIALIZABLE y bloqueos de actualización sobre órdenes/pedidos. Valida todos los detalles antes de persistir. Cabecera, detalle, asignaciones y evento de auditoría se confirman juntos o se revierten. Monto = SUM(cantidad_final × precio_acordado), DECIMAL; no se persiste un total duplicado. La fecha local de negocio no se convierte a UTC desde el navegador.

| Ruta relativa a /api | Método | Función |
| --- | --- | --- |
| /login | POST | nombreUsuario, contrasena → usuario, permisos, csrf |
| /login/me | GET | Sesión actual |
| /login/logout | POST | Invalidar sesión (CSRF) |
| /gestion/{modulo}/schema | GET | Metadatos de formulario |
| /gestion/{modulo}?page=1&pageSize=20&q=texto | GET | Listado, total y paginación (máximo 100) |
| /gestion/{modulo}/{key} | GET/PUT/DELETE | Consulta, edición y eliminación |
| /gestion/{modulo} | POST | Alta |
| /ordenes y /ordenes/{id} | GET/POST/PUT/DELETE | Orden agrupada; pedidoIds |
| /adjudicaciones y /adjudicaciones/{id} | GET/POST/PUT/DELETE | Cabecera + detalles atómicos |
| /reportes | GET | Ocho informes y estado de configuración SSRS |
| /reportes/{1..8} | GET | articulo, proveedor, orden, sucursal, anio, desde, hasta |
| /dashboard | GET | Métricas con alcance del rol |
| /conexion/test | GET | SELECT 1 real por motor y tiempos, protegido |
| /auditoria?page=1 | GET | Eventos paginados |
| /articulos, /sucursales, /departamentos, /proveedores | CRUD | Compatibilidad con páginas originales |
| /proveedores/{id}/articulos | CRUD | Relación heredada con alcance de proveedor |

Módulos: sucursales, departamentos, articulos, proveedores, proveedorarticulos, telefonos, rubros, relaciones, tiposorden, subtiposorden, roles, pantallas, permisos, usuarios, pedidos, ofertas, evaluaciones. Claves compuestas se codifican en URL y separan con `~`; no se cambian al editar. DELETE nuevo devuelve 204; algunas rutas heredadas devuelven 200 por compatibilidad.

Una orden recibe `descripcion`, `fecha_creacion`, `fecha_limite_oferta`, `id_tipo`, `id_subtipo` opcional y `pedidoIds`. Una adjudicación recibe `id_orden`, `fecha_resolucion` y `detalles`: `id_pedido`, `id_oferta`, `cantidad_final`, `precio_acordado` por pedido. No se acepta adjudicación parcial, duplicada ni precio diferente de la oferta seleccionada.

[Volver a Arquitectura](README.md)
