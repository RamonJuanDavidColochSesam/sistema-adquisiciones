IF OBJECT_ID('dbo.SucursalTelefono') IS NULL
CREATE TABLE dbo.SucursalTelefono (id_sucursal INT NOT NULL REFERENCES Sucursal(id_sucursal), telefono VARCHAR(20) NOT NULL, PRIMARY KEY(id_sucursal,telefono));
IF OBJECT_ID('dbo.ProveedorRubro') IS NULL
CREATE TABLE dbo.ProveedorRubro (id_proveedor INT NOT NULL REFERENCES Proveedor(id_proveedor), rubro VARCHAR(80) NOT NULL, PRIMARY KEY(id_proveedor,rubro));
IF COL_LENGTH('Articulo','estado') IS NULL ALTER TABLE Articulo ADD estado VARCHAR(10) NOT NULL CONSTRAINT DF_Articulo_Estado DEFAULT 'Activo' CONSTRAINT CK_Articulo_Estado CHECK(estado IN ('Activo','Inactivo'));
IF COL_LENGTH('Proveedor','estado') IS NULL ALTER TABLE Proveedor ADD estado VARCHAR(10) NOT NULL CONSTRAINT DF_Proveedor_Estado DEFAULT 'Activo' CONSTRAINT CK_Proveedor_Estado CHECK(estado IN ('Activo','Inactivo'));
IF OBJECT_ID('dbo.Auditoria') IS NULL
CREATE TABLE dbo.Auditoria (id_auditoria BIGINT IDENTITY PRIMARY KEY, id_usuario INT NULL REFERENCES Usuario(id_usuario), accion VARCHAR(20) NOT NULL, entidad VARCHAR(60) NOT NULL, referencia VARCHAR(80) NULL, fecha DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME());
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Pedido_Orden') CREATE INDEX IX_Pedido_Orden ON Pedido(id_orden) INCLUDE(id_departamento,id_articulo,cantidad);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Oferta_PedidoProveedor') CREATE INDEX IX_Oferta_PedidoProveedor ON Oferta(id_pedido,id_proveedor,fecha_oferta) INCLUDE(precio_unitario);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Departamento_Sucursal') CREATE INDEX IX_Departamento_Sucursal ON Departamento(id_sucursal);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='IX_Adjudicacion_Fecha') CREATE INDEX IX_Adjudicacion_Fecha ON Adjudicacion(fecha_resolucion) INCLUDE(id_orden);
-- @batch
CREATE OR ALTER VIEW dbo.vw_OrdenesAbiertas AS
SELECT o.id_orden,o.descripcion,o.fecha_creacion,o.fecha_limite_oferta,t.nombre AS tipo,s.nombre AS subtipo
FROM OrdenCompra o JOIN TipoOrden t ON t.id_tipo=o.id_tipo LEFT JOIN SubtipoOrden s ON s.id_subtipo=o.id_subtipo
WHERE NOT EXISTS(SELECT 1 FROM Adjudicacion a WHERE a.id_orden=o.id_orden) AND o.fecha_limite_oferta>=CAST(GETDATE() AS DATE);
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_Oferta_Integridad ON dbo.Oferta AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_pedido=i.id_pedido LEFT JOIN OrdenCompra o ON o.id_orden=p.id_orden WHERE o.id_orden IS NULL OR i.fecha_oferta<o.fecha_creacion OR i.fecha_oferta>o.fecha_limite_oferta)
 THROW 51001,'La oferta requiere pedido asignado y fecha dentro del plazo de la orden.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_pedido=i.id_pedido WHERE NOT EXISTS(SELECT 1 FROM ProveedorArticulo pa WHERE pa.id_proveedor=i.id_proveedor AND pa.id_articulo=p.id_articulo))
 THROW 51002,'El proveedor debe incluir el artículo en su catálogo.',1;
END;
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_DetalleAdjudicacion_Integridad ON dbo.DetalleAdjudicacion AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN Adjudicacion a ON a.id_adjudicacion=i.id_adjudicacion JOIN Pedido p ON p.id_pedido=i.id_pedido JOIN Oferta f ON f.id_oferta=i.id_oferta WHERE p.id_orden IS NULL OR p.id_orden<>a.id_orden OR f.id_pedido<>p.id_pedido OR f.fecha_oferta>a.fecha_resolucion OR i.cantidad_final>p.cantidad)
 THROW 51003,'La oferta, pedido y adjudicación deben corresponder a la misma orden y cantidad solicitada.',1;
END;
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_OrdenCompra_Integridad ON dbo.OrdenCompra AFTER INSERT,UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_orden=i.id_orden WHERE i.fecha_creacion<p.fecha_solicitud)
 THROW 51004,'La creación de la orden no puede preceder sus solicitudes.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN Adjudicacion a ON a.id_orden=i.id_orden WHERE i.fecha_creacion>a.fecha_resolucion)
 THROW 51005,'La creación de la orden no puede ser posterior a su adjudicación.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN SubtipoOrden s ON s.id_subtipo=i.id_subtipo WHERE s.id_tipo<>i.id_tipo)
 THROW 51006,'El subtipo no pertenece al tipo seleccionado.',1;
END;
