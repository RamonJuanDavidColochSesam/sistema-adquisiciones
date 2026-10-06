CREATE OR ALTER VIEW dbo.vw_OrdenesAbiertas AS
SELECT o.id_orden,o.descripcion,o.fecha_creacion,o.fecha_limite_oferta,t.nombre AS tipo,s.nombre AS subtipo
FROM OrdenCompra o JOIN TipoOrden t ON t.id_tipo=o.id_tipo LEFT JOIN SubtipoOrden s ON s.id_subtipo=o.id_subtipo
WHERE NOT EXISTS(SELECT 1 FROM Adjudicacion a WHERE a.id_orden=o.id_orden)
AND o.fecha_creacion<=CAST(GETDATE() AS DATE) AND o.fecha_limite_oferta>=CAST(GETDATE() AS DATE);
