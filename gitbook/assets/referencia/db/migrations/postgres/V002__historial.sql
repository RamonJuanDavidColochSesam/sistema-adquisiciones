ALTER TABLE SubtipoOrden ADD CONSTRAINT UQ_SubtipoOrden_TipoNombre UNIQUE(id_tipo,nombre);
ALTER TABLE SubtipoOrden ADD CONSTRAINT UQ_SubtipoOrden_TipoId UNIQUE(id_tipo,id_subtipo);
ALTER TABLE OrdenCompra ADD CONSTRAINT FK_OrdenCompra_TipoSubtipo FOREIGN KEY(id_tipo,id_subtipo) REFERENCES SubtipoOrden(id_tipo,id_subtipo);
ALTER TABLE RelacionComercial ADD CONSTRAINT CK_RelacionComercial_Canonica CHECK(id_proveedor_a<id_proveedor_b);
-- @batch
CREATE OR REPLACE FUNCTION validar_pedido_historial() RETURNS TRIGGER AS $$
BEGIN
 IF (NEW.id_orden IS DISTINCT FROM OLD.id_orden OR NEW.id_articulo<>OLD.id_articulo) AND EXISTS(SELECT 1 FROM Oferta f WHERE f.id_pedido=NEW.id_pedido) THEN RAISE EXCEPTION 'No se puede reasignar ni cambiar el artículo de un pedido con ofertas'; END IF;
 IF EXISTS(SELECT 1 FROM DetalleAdjudicacion d WHERE d.id_pedido=NEW.id_pedido AND d.cantidad_final>NEW.cantidad) THEN RAISE EXCEPTION 'La cantidad solicitada es menor que la adjudicada'; END IF;
 RETURN NEW;
END; $$ LANGUAGE plpgsql;
-- @batch
CREATE TRIGGER tr_Pedido_Historial BEFORE UPDATE ON Pedido FOR EACH ROW EXECUTE FUNCTION validar_pedido_historial();
-- @batch
CREATE OR REPLACE FUNCTION validar_adjudicacion_historial() RETURNS TRIGGER AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM DetalleAdjudicacion d JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Oferta f ON f.id_oferta=d.id_oferta WHERE d.id_adjudicacion=NEW.id_adjudicacion AND (p.id_orden<>NEW.id_orden OR f.fecha_oferta>NEW.fecha_resolucion)) THEN RAISE EXCEPTION 'La resolución contradice pedidos u ofertas adjudicados'; END IF;
 RETURN NEW;
END; $$ LANGUAGE plpgsql;
-- @batch
CREATE TRIGGER tr_Adjudicacion_Historial BEFORE UPDATE ON Adjudicacion FOR EACH ROW EXECUTE FUNCTION validar_adjudicacion_historial();
-- @batch
CREATE OR REPLACE FUNCTION validar_oferta_historial() RETURNS TRIGGER AS $$
BEGIN
 IF (NEW.id_proveedor<>OLD.id_proveedor OR NEW.id_pedido<>OLD.id_pedido OR NEW.precio_unitario<>OLD.precio_unitario OR NEW.fecha_oferta<>OLD.fecha_oferta) AND EXISTS(SELECT 1 FROM DetalleAdjudicacion d WHERE d.id_oferta=NEW.id_oferta) THEN RAISE EXCEPTION 'La oferta adjudicada no puede modificarse'; END IF;
 RETURN NEW;
END; $$ LANGUAGE plpgsql;
-- @batch
CREATE TRIGGER tr_Oferta_Historial BEFORE UPDATE ON Oferta FOR EACH ROW EXECUTE FUNCTION validar_oferta_historial();
-- @batch
CREATE OR REPLACE FUNCTION validar_orden_ofertas() RETURNS TRIGGER AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM Pedido p JOIN Oferta f ON f.id_pedido=p.id_pedido WHERE p.id_orden=NEW.id_orden AND (f.fecha_oferta<NEW.fecha_creacion OR f.fecha_oferta>NEW.fecha_limite_oferta)) THEN RAISE EXCEPTION 'Las nuevas fechas dejan ofertas fuera del plazo'; END IF;
 RETURN NEW;
END; $$ LANGUAGE plpgsql;
-- @batch
CREATE TRIGGER tr_OrdenCompra_Ofertas BEFORE UPDATE ON OrdenCompra FOR EACH ROW EXECUTE FUNCTION validar_orden_ofertas();
-- @batch
CREATE OR REPLACE FUNCTION validar_catalogo_historial() RETURNS TRIGGER AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido WHERE f.id_proveedor=OLD.id_proveedor AND p.id_articulo=OLD.id_articulo) THEN RAISE EXCEPTION 'No puede eliminar catálogo con ofertas históricas'; END IF;
 RETURN OLD;
END; $$ LANGUAGE plpgsql;
-- @batch
CREATE TRIGGER tr_ProveedorArticulo_Historial BEFORE DELETE ON ProveedorArticulo FOR EACH ROW EXECUTE FUNCTION validar_catalogo_historial();
