# Diccionario real: sqlserver

Extraído mediante DatabaseMetaData. Sin datos de usuarios ni credenciales.

## Adjudicacion

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_adjudicacion | int identity | 10 | No | PK |
| id_orden | int | 10 | No |  |
| fecha_resolucion | date | 10 | No |  |
| observaciones | varchar | 300 | Sí |  |

Claves foráneas:

- id_orden → OrdenCompra.id_orden (`FK_Adjudicacion_OrdenCompra`)

Índices:

- `PK__Adjudica__D106B4DE76E85852`: id_adjudicacion
- `UQ__Adjudica__DD5B8F3241FF1B36`: id_orden
- `IX_Adjudicacion_Fecha`: fecha_resolucion

## Articulo

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_articulo | int identity | 10 | No | PK |
| codigo_articulo | varchar | 20 | No |  |
| nombre | varchar | 100 | No |  |
| descripcion | varchar | 300 | Sí |  |
| estado | varchar | 10 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__Articulo__3F6E828829278533`: id_articulo
- `UQ__Articulo__01435262A3CDC524`: codigo_articulo

## Auditoria

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_auditoria | bigint identity | 19 | No | PK |
| id_usuario | int | 10 | Sí |  |
| accion | varchar | 20 | No |  |
| entidad | varchar | 60 | No |  |
| referencia | varchar | 80 | Sí |  |
| fecha | datetime2 | 27 | No |  |

Claves foráneas:

- id_usuario → Usuario.id_usuario (`FK__Auditoria__id_us__3F115E1A`)

Índices:

- `PK__Auditori__9644A3CEADED50E2`: id_auditoria

## Departamento

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_departamento | int identity | 10 | No | PK |
| id_sucursal | int | 10 | No |  |
| nombre | varchar | 60 | No |  |
| descripcion | varchar | 200 | Sí |  |

Claves foráneas:

- id_sucursal → Sucursal.id_sucursal (`FK_Departamento_Sucursal`)

Índices:

- `PK__Departam__64F37A161C9C1027`: id_departamento
- `UQ_Departamento_SucursalNombre`: id_sucursal, nombre
- `IX_Departamento_Sucursal`: id_sucursal

## DetalleAdjudicacion

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_detalle_adjudicacion | int identity | 10 | No | PK |
| id_adjudicacion | int | 10 | No |  |
| id_pedido | int | 10 | No |  |
| id_oferta | int | 10 | No |  |
| cantidad_final | int | 10 | No |  |
| precio_acordado | decimal | 10 | No |  |

Claves foráneas:

- id_adjudicacion → Adjudicacion.id_adjudicacion (`FK_DetalleAdjudicacion_Adjudicacion`)
- id_oferta → Oferta.id_oferta (`FK_DetalleAdjudicacion_Oferta`)
- id_pedido → Pedido.id_pedido (`FK_DetalleAdjudicacion_Pedido`)

Índices:

- `PK__DetalleA__F2768D90FAD3AF18`: id_detalle_adjudicacion
- `UQ_DetalleAdjudicacion_AdjPedido`: id_adjudicacion, id_pedido

## EvaluacionProveedor

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_evaluacion | int identity | 10 | No | PK |
| id_detalle_adjudicacion | int | 10 | No |  |
| calificacion | tinyint | 3 | No |  |
| comentario | varchar | 300 | Sí |  |
| fecha | date | 10 | No |  |

Claves foráneas:

- id_detalle_adjudicacion → DetalleAdjudicacion.id_detalle_adjudicacion (`FK_EvaluacionProveedor_Detalle`)

Índices:

- `PK__Evaluaci__65DE60C56F539BE9`: id_evaluacion

## Oferta

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_oferta | int identity | 10 | No | PK |
| id_proveedor | int | 10 | No |  |
| id_pedido | int | 10 | No |  |
| precio_unitario | decimal | 10 | No |  |
| fecha_oferta | date | 10 | No |  |
| observaciones | varchar | 300 | Sí |  |

Claves foráneas:

- id_pedido → Pedido.id_pedido (`FK_Oferta_Pedido`)
- id_proveedor → Proveedor.id_proveedor (`FK_Oferta_Proveedor`)

Índices:

- `PK__Oferta__2B7BF92FB97BDCE4`: id_oferta
- `IX_Oferta_PedidoProveedor`: id_pedido, id_proveedor, fecha_oferta

## OrdenCompra

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_orden | int identity | 10 | No | PK |
| descripcion | varchar | 200 | No |  |
| fecha_creacion | date | 10 | No |  |
| fecha_limite_oferta | date | 10 | No |  |
| id_tipo | int | 10 | No |  |
| id_subtipo | int | 10 | Sí |  |
| observaciones | varchar | 300 | Sí |  |

Claves foráneas:

- id_subtipo → SubtipoOrden.id_subtipo (`FK_OrdenCompra_SubtipoOrden`)
- id_tipo → SubtipoOrden.id_tipo (`FK_OrdenCompra_TipoSubtipo`)
- id_subtipo → SubtipoOrden.id_subtipo (`FK_OrdenCompra_TipoSubtipo`)
- id_tipo → TipoOrden.id_tipo (`FK_OrdenCompra_TipoOrden`)

Índices:

- `PK__OrdenCom__DD5B8F33C1F6ED28`: id_orden

## Pantalla

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_pantalla | int identity | 10 | No | PK |
| nombre_pantalla | varchar | 60 | No |  |
| descripcion | varchar | 200 | Sí |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__Pantalla__DF1DF97D0F0445B0`: id_pantalla
- `UQ__Pantalla__7BB29319F90EBE75`: nombre_pantalla

## Pedido

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_pedido | int identity | 10 | No | PK |
| id_departamento | int | 10 | No |  |
| id_orden | int | 10 | Sí |  |
| id_articulo | int | 10 | No |  |
| cantidad | int | 10 | No |  |
| fecha_solicitud | date | 10 | No |  |
| fecha_necesaria | date | 10 | No |  |

Claves foráneas:

- id_articulo → Articulo.id_articulo (`FK_Pedido_Articulo`)
- id_departamento → Departamento.id_departamento (`FK_Pedido_Departamento`)
- id_orden → OrdenCompra.id_orden (`FK_Pedido_OrdenCompra`)

Índices:

- `PK__Pedido__6FF014898924AD03`: id_pedido
- `IX_Pedido_Orden`: id_orden

## Permiso

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_rol | int | 10 | No | PK |
| id_pantalla | int | 10 | No | PK |
| permite_crear | bit | 1 | No |  |
| permite_leer | bit | 1 | No |  |
| permite_actualizar | bit | 1 | No |  |
| permite_borrar | bit | 1 | No |  |

Claves foráneas:

- id_pantalla → Pantalla.id_pantalla (`FK_Permiso_Pantalla`)
- id_rol → Rol.id_rol (`FK_Permiso_Rol`)

Índices:

- `PK_Permiso`: id_rol, id_pantalla

## Proveedor

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_proveedor | int identity | 10 | No | PK |
| codigo_proveedor | varchar | 20 | No |  |
| nombre_comercial | varchar | 100 | No |  |
| direccion | varchar | 150 | No |  |
| telefono | varchar | 20 | No |  |
| categoria | varchar | 80 | No |  |
| estado | varchar | 10 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__Proveedo__8D3DFE289989C6B5`: id_proveedor
- `UQ__Proveedo__0EF366B3176EFED9`: codigo_proveedor

## ProveedorArticulo

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_proveedor | int | 10 | No | PK |
| id_articulo | int | 10 | No | PK |
| precio | decimal | 10 | No |  |

Claves foráneas:

- id_articulo → Articulo.id_articulo (`FK_ProveedorArticulo_Articulo`)
- id_proveedor → Proveedor.id_proveedor (`FK_ProveedorArticulo_Proveedor`)

Índices:

- `PK_ProveedorArticulo`: id_proveedor, id_articulo

## ProveedorRubro

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_proveedor | int | 10 | No | PK |
| rubro | varchar | 80 | No | PK |

Claves foráneas:

- id_proveedor → Proveedor.id_proveedor (`FK__Proveedor__id_pr__3864608B`)

Índices:

- `PK__Proveedo__B740295A70629721`: id_proveedor, rubro

## RelacionComercial

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_relacion_comercial | int identity | 10 | No | PK |
| id_proveedor_a | int | 10 | No |  |
| id_proveedor_b | int | 10 | No |  |

Claves foráneas:

- id_proveedor_a → Proveedor.id_proveedor (`FK_RelacionComercial_ProveedorA`)
- id_proveedor_b → Proveedor.id_proveedor (`FK_RelacionComercial_ProveedorB`)

Índices:

- `PK__Relacion__E30BA06BDEDEF976`: id_relacion_comercial
- `UQ_RelacionComercial_Par`: id_proveedor_a, id_proveedor_b

## Rol

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_rol | int identity | 10 | No | PK |
| nombre_rol | varchar | 40 | No |  |
| descripcion | varchar | 200 | Sí |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__Rol__6ABCB5E02F47FA13`: id_rol
- `UQ__Rol__673CB435A63349CC`: nombre_rol

## SchemaVersion

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| version_id | varchar | 20 | No | PK |
| nombre | varchar | 150 | No |  |
| fecha | datetime2 | 27 | Sí |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__SchemaVe__07A58869556C024C`: version_id

## SubtipoOrden

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_subtipo | int identity | 10 | No | PK |
| id_tipo | int | 10 | No |  |
| nombre | varchar | 20 | No |  |

Claves foráneas:

- id_tipo → TipoOrden.id_tipo (`FK_SubtipoOrden_TipoOrden`)

Índices:

- `PK__SubtipoO__F4D34CA24A727319`: id_subtipo
- `UQ_SubtipoOrden_TipoId`: id_tipo, id_subtipo
- `UQ_SubtipoOrden_TipoNombre`: id_tipo, nombre

## Sucursal

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_sucursal | int identity | 10 | No | PK |
| codigo_sucursal | varchar | 10 | No |  |
| direccion | varchar | 150 | No |  |
| ciudad | varchar | 60 | No |  |
| region | varchar | 60 | No |  |
| telefono | varchar | 20 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__Sucursal__4C758013578612D2`: id_sucursal
- `UQ__Sucursal__2F19A22D7B7077C9`: codigo_sucursal

## SucursalTelefono

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_sucursal | int | 10 | No | PK |
| telefono | varchar | 20 | No | PK |

Claves foráneas:

- id_sucursal → Sucursal.id_sucursal (`FK__SucursalT__id_su__3587F3E0`)

Índices:

- `PK__Sucursal__1ED4ED87E592A204`: id_sucursal, telefono

## TipoOrden

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_tipo | int identity | 10 | No | PK |
| nombre | varchar | 20 | No |  |

Claves foráneas:

Sin claves foráneas.

Índices:

- `PK__TipoOrde__CF9010890366FECE`: id_tipo
- `UQ__TipoOrde__72AFBCC6B7D76DDA`: nombre

## Usuario

| Columna | Tipo | Tamaño | Nulo | Clave primaria |
| --- | --- | --- | --- | --- |
| id_usuario | int identity | 10 | No | PK |
| nombre_usuario | varchar | 30 | No |  |
| contrasena_hash | varchar | 255 | No |  |
| nombre_completo | varchar | 100 | No |  |
| email | varchar | 100 | No |  |
| id_rol | int | 10 | No |  |
| id_proveedor | int | 10 | Sí |  |
| estado | varchar | 10 | No |  |
| fecha_creacion | date | 10 | No |  |

Claves foráneas:

- id_proveedor → Proveedor.id_proveedor (`FK_Usuario_Proveedor`)
- id_rol → Rol.id_rol (`FK_Usuario_Rol`)

Índices:

- `PK__Usuario__4E3E04AD60C06245`: id_usuario
- `UQ__Usuario__AB6E6164CD1B4D97`: email
- `UQ__Usuario__D4D22D743E4934DE`: nombre_usuario
