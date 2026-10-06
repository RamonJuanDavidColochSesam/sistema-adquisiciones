# Diccionario real: postgres

Extraído mediante DatabaseMetaData. Sin datos de usuarios ni credenciales.

## adjudicacion

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_adjudicacion | int4 | 10 | No | PK |
| id_orden | int4 | 10 | No |  |
| fecha_resolucion | date | 13 | No |  |
| observaciones | varchar | 300 | Sí |  |

Claves foráneas:

- id_orden → ordencompra.id_orden (`adjudicacion_id_orden_fkey`)

Índices:

- `adjudicacion_id_orden_key`: id_orden
- `adjudicacion_pkey`: id_adjudicacion
- `ix_adjudicacion_fecha`: fecha_resolucion

## articulo

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_articulo | int4 | 10 | No | PK |
| codigo_articulo | varchar | 20 | No |  |
| nombre | varchar | 100 | No |  |
| descripcion | varchar | 300 | Sí |  |
| estado | varchar | 10 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `articulo_codigo_articulo_key`: codigo_articulo
- `articulo_pkey`: id_articulo

## auditoria

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_auditoria | int8 | 19 | No | PK |
| id_usuario | int4 | 10 | Sí |  |
| accion | varchar | 20 | No |  |
| entidad | varchar | 60 | No |  |
| referencia | varchar | 80 | Sí |  |
| fecha | timestamp | 29 | No |  |

Claves foráneas:

- id_usuario → usuario.id_usuario (`auditoria_id_usuario_fkey`)

Índices:

- `auditoria_pkey`: id_auditoria

## departamento

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_departamento | int4 | 10 | No | PK |
| id_sucursal | int4 | 10 | No |  |
| nombre | varchar | 60 | No |  |
| descripcion | varchar | 200 | Sí |  |

Claves foráneas:

- id_sucursal → sucursal.id_sucursal (`departamento_id_sucursal_fkey`)

Índices:

- `departamento_pkey`: id_departamento
- `uq_departamento_sucursalnombre`: id_sucursal, nombre
- `ix_departamento_sucursal`: id_sucursal

## detalleadjudicacion

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_detalle_adjudicacion | int4 | 10 | No | PK |
| id_adjudicacion | int4 | 10 | No |  |
| id_pedido | int4 | 10 | No |  |
| id_oferta | int4 | 10 | No |  |
| cantidad_final | int4 | 10 | No |  |
| precio_acordado | numeric | 10 | No |  |

Claves foráneas:

- id_adjudicacion → adjudicacion.id_adjudicacion (`detalleadjudicacion_id_adjudicacion_fkey`)
- id_oferta → oferta.id_oferta (`detalleadjudicacion_id_oferta_fkey`)
- id_pedido → pedido.id_pedido (`detalleadjudicacion_id_pedido_fkey`)

Índices:

- `detalleadjudicacion_pkey`: id_detalle_adjudicacion
- `uq_detalleadjudicacion_adjpedido`: id_adjudicacion, id_pedido

## evaluacionproveedor

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_evaluacion | int4 | 10 | No | PK |
| id_detalle_adjudicacion | int4 | 10 | No |  |
| calificacion | int2 | 5 | No |  |
| comentario | varchar | 300 | Sí |  |
| fecha | date | 13 | No |  |

Claves foráneas:

- id_detalle_adjudicacion → detalleadjudicacion.id_detalle_adjudicacion (`evaluacionproveedor_id_detalle_adjudicacion_fkey`)

Índices:

- `evaluacionproveedor_pkey`: id_evaluacion

## oferta

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_oferta | int4 | 10 | No | PK |
| id_proveedor | int4 | 10 | No |  |
| id_pedido | int4 | 10 | No |  |
| precio_unitario | numeric | 10 | No |  |
| fecha_oferta | date | 13 | No |  |
| observaciones | varchar | 300 | Sí |  |

Claves foráneas:

- id_pedido → pedido.id_pedido (`oferta_id_pedido_fkey`)
- id_proveedor → proveedor.id_proveedor (`oferta_id_proveedor_fkey`)

Índices:

- `oferta_pkey`: id_oferta
- `ix_oferta_pedidoproveedor`: id_pedido, id_proveedor, fecha_oferta

## ordencompra

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_orden | int4 | 10 | No | PK |
| descripcion | varchar | 200 | No |  |
| fecha_creacion | date | 13 | No |  |
| fecha_limite_oferta | date | 13 | No |  |
| id_tipo | int4 | 10 | No |  |
| id_subtipo | int4 | 10 | Sí |  |
| observaciones | varchar | 300 | Sí |  |

Claves foráneas:

- id_tipo → subtipoorden.id_tipo (`fk_ordencompra_tiposubtipo`)
- id_subtipo → subtipoorden.id_subtipo (`fk_ordencompra_tiposubtipo`)
- id_subtipo → subtipoorden.id_subtipo (`ordencompra_id_subtipo_fkey`)
- id_tipo → tipoorden.id_tipo (`ordencompra_id_tipo_fkey`)

Índices:

- `ordencompra_pkey`: id_orden

## pantalla

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_pantalla | int4 | 10 | No | PK |
| nombre_pantalla | varchar | 60 | No |  |
| descripcion | varchar | 200 | Sí |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `pantalla_nombre_pantalla_key`: nombre_pantalla
- `pantalla_pkey`: id_pantalla

## pedido

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_pedido | int4 | 10 | No | PK |
| id_departamento | int4 | 10 | No |  |
| id_orden | int4 | 10 | Sí |  |
| id_articulo | int4 | 10 | No |  |
| cantidad | int4 | 10 | No |  |
| fecha_solicitud | date | 13 | No |  |
| fecha_necesaria | date | 13 | No |  |

Claves foráneas:

- id_articulo → articulo.id_articulo (`pedido_id_articulo_fkey`)
- id_departamento → departamento.id_departamento (`pedido_id_departamento_fkey`)
- id_orden → ordencompra.id_orden (`pedido_id_orden_fkey`)

Índices:

- `pedido_pkey`: id_pedido
- `ix_pedido_orden`: id_orden

## permiso

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_rol | int4 | 10 | No | PK |
| id_pantalla | int4 | 10 | No | PK |
| permite_crear | bool | 1 | No |  |
| permite_leer | bool | 1 | No |  |
| permite_actualizar | bool | 1 | No |  |
| permite_borrar | bool | 1 | No |  |

Claves foráneas:

- id_pantalla → pantalla.id_pantalla (`permiso_id_pantalla_fkey`)
- id_rol → rol.id_rol (`permiso_id_rol_fkey`)

Índices:

- `pk_permiso`: id_rol, id_pantalla

## proveedor

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_proveedor | int4 | 10 | No | PK |
| codigo_proveedor | varchar | 20 | No |  |
| nombre_comercial | varchar | 100 | No |  |
| direccion | varchar | 150 | No |  |
| telefono | varchar | 20 | No |  |
| categoria | varchar | 80 | No |  |
| estado | varchar | 10 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `proveedor_codigo_proveedor_key`: codigo_proveedor
- `proveedor_pkey`: id_proveedor

## proveedorarticulo

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_proveedor | int4 | 10 | No | PK |
| id_articulo | int4 | 10 | No | PK |
| precio | numeric | 10 | No |  |

Claves foráneas:

- id_articulo → articulo.id_articulo (`proveedorarticulo_id_articulo_fkey`)
- id_proveedor → proveedor.id_proveedor (`proveedorarticulo_id_proveedor_fkey`)

Índices:

- `pk_proveedorarticulo`: id_proveedor, id_articulo

## proveedorrubro

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_proveedor | int4 | 10 | No | PK |
| rubro | varchar | 80 | No | PK |

Claves foráneas:

- id_proveedor → proveedor.id_proveedor (`proveedorrubro_id_proveedor_fkey`)

Índices:

- `proveedorrubro_pkey`: id_proveedor, rubro

## relacioncomercial

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_relacion_comercial | int4 | 10 | No | PK |
| id_proveedor_a | int4 | 10 | No |  |
| id_proveedor_b | int4 | 10 | No |  |

Claves foráneas:

- id_proveedor_a → proveedor.id_proveedor (`relacioncomercial_id_proveedor_a_fkey`)
- id_proveedor_b → proveedor.id_proveedor (`relacioncomercial_id_proveedor_b_fkey`)

Índices:

- `relacioncomercial_pkey`: id_relacion_comercial
- `uq_relacioncomercial_par`: id_proveedor_a, id_proveedor_b

## rol

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_rol | int4 | 10 | No | PK |
| nombre_rol | varchar | 40 | No |  |
| descripcion | varchar | 200 | Sí |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `rol_nombre_rol_key`: nombre_rol
- `rol_pkey`: id_rol

## schemaversion

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| version_id | varchar | 20 | No | PK |
| nombre | varchar | 150 | No |  |
| fecha | timestamp | 29 | Sí |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `schemaversion_pkey`: version_id

## subtipoorden

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_subtipo | int4 | 10 | No | PK |
| id_tipo | int4 | 10 | No |  |
| nombre | varchar | 20 | No |  |

Claves foráneas:

- id_tipo → tipoorden.id_tipo (`subtipoorden_id_tipo_fkey`)

Índices:

- `subtipoorden_pkey`: id_subtipo
- `uq_subtipoorden_tipoid`: id_tipo, id_subtipo
- `uq_subtipoorden_tiponombre`: id_tipo, nombre

## sucursal

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_sucursal | int4 | 10 | No | PK |
| codigo_sucursal | varchar | 10 | No |  |
| direccion | varchar | 150 | No |  |
| ciudad | varchar | 60 | No |  |
| region | varchar | 60 | No |  |
| telefono | varchar | 20 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `sucursal_codigo_sucursal_key`: codigo_sucursal
- `sucursal_pkey`: id_sucursal

## sucursaltelefono

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_sucursal | int4 | 10 | No | PK |
| telefono | varchar | 20 | No | PK |

Claves foráneas:

- id_sucursal → sucursal.id_sucursal (`sucursaltelefono_id_sucursal_fkey`)

Índices:

- `sucursaltelefono_pkey`: id_sucursal, telefono

## tipoorden

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_tipo | int4 | 10 | No | PK |
| nombre | varchar | 20 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `tipoorden_nombre_key`: nombre
- `tipoorden_pkey`: id_tipo

## usuario

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_usuario | int4 | 10 | No | PK |
| nombre_usuario | varchar | 30 | No |  |
| contrasena_hash | varchar | 255 | No |  |
| nombre_completo | varchar | 100 | No |  |
| email | varchar | 100 | No |  |
| id_rol | int4 | 10 | No |  |
| id_proveedor | int4 | 10 | Sí |  |
| estado | varchar | 10 | No |  |
| fecha_creacion | date | 13 | No |  |

Claves foráneas:

- id_proveedor → proveedor.id_proveedor (`usuario_id_proveedor_fkey`)
- id_rol → rol.id_rol (`usuario_id_rol_fkey`)

Índices:

- `usuario_email_key`: email
- `usuario_nombre_usuario_key`: nombre_usuario
- `usuario_pkey`: id_usuario

