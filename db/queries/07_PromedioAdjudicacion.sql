-- Parámetros: Desde, Hasta
SELECT COUNT(*) AS ordenes_adjudicadas,AVG(CAST(DATEDIFF(DAY,o.fecha_creacion,a.fecha_resolucion) AS DECIMAL(10,2))) AS promedio_dias FROM Adjudicacion a JOIN OrdenCompra o ON o.id_orden=a.id_orden WHERE a.fecha_resolucion BETWEEN @Desde AND @Hasta;
