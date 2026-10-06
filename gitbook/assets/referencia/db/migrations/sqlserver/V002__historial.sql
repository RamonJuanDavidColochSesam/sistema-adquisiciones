IF NOT EXISTS(SELECT 1 FROM sys.key_constraints WHERE name='UQ_SubtipoOrden_TipoNombre') ALTER TABLE SubtipoOrden ADD CONSTRAINT UQ_SubtipoOrden_TipoNombre UNIQUE(id_tipo,nombre);
IF NOT EXISTS(SELECT 1 FROM sys.key_constraints WHERE name='UQ_SubtipoOrden_TipoId') ALTER TABLE SubtipoOrden ADD CONSTRAINT UQ_SubtipoOrden_TipoId UNIQUE(id_tipo,id_subtipo);
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_OrdenCompra_TipoSubtipo') ALTER TABLE OrdenCompra ADD CONSTRAINT FK_OrdenCompra_TipoSubtipo FOREIGN KEY(id_tipo,id_subtipo) REFERENCES SubtipoOrden(id_tipo,id_subtipo);
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='CK_RelacionComercial_Canonica') ALTER TABLE RelacionComercial ADD CONSTRAINT CK_RelacionComercial_Canonica CHECK(id_proveedor_a<id_proveedor_b);
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_Pedido_Historial ON dbo.Pedido AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.id_pedido=i.id_pedido WHERE (ISNULL(i.id_orden,0)<>ISNULL(d.id_orden,0) OR i.id_articulo<>d.id_articulo) AND EXISTS(SELECT 1 FROM Oferta f WHERE f.id_pedido=i.id_pedido))
 THROW 51007,'No se puede reasignar ni cambiar el artículo de un pedido con ofertas.',1;
 IF EXISTS(SELECT 1 FROM inserted i JOIN DetalleAdjudicacion d ON d.id_pedido=i.id_pedido WHERE i.cantidad<d.cantidad_final)
 THROW 51008,'La cantidad solicitada no puede ser menor que la adjudicada.',1;
END;
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_Adjudicacion_Historial ON dbo.Adjudicacion AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN DetalleAdjudicacion d ON d.id_adjudicacion=i.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Oferta f ON f.id_oferta=d.id_oferta WHERE p.id_orden<>i.id_orden OR f.fecha_oferta>i.fecha_resolucion)
 THROW 51009,'La resolución contradice los pedidos u ofertas ya adjudicados.',1;
END;
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_Oferta_Historial ON dbo.Oferta AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.id_oferta=i.id_oferta JOIN DetalleAdjudicacion a ON a.id_oferta=i.id_oferta WHERE i.id_proveedor<>d.id_proveedor OR i.id_pedido<>d.id_pedido OR i.precio_unitario<>d.precio_unitario OR i.fecha_oferta<>d.fecha_oferta)
 THROW 51010,'La oferta adjudicada forma parte del historial y no puede modificarse.',1;
END;
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_OrdenCompra_Ofertas ON dbo.OrdenCompra AFTER UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM inserted i JOIN Pedido p ON p.id_orden=i.id_orden JOIN Oferta f ON f.id_pedido=p.id_pedido WHERE f.fecha_oferta<i.fecha_creacion OR f.fecha_oferta>i.fecha_limite_oferta)
 THROW 51011,'Las nuevas fechas dejan ofertas existentes fuera del plazo.',1;
END;
-- @batch
CREATE OR ALTER TRIGGER dbo.tr_ProveedorArticulo_Historial ON dbo.ProveedorArticulo AFTER DELETE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS(SELECT 1 FROM deleted d JOIN Oferta f ON f.id_proveedor=d.id_proveedor JOIN Pedido p ON p.id_pedido=f.id_pedido AND p.id_articulo=d.id_articulo)
 THROW 51012,'No puede eliminar una relación de catálogo con ofertas históricas.',1;
END;
