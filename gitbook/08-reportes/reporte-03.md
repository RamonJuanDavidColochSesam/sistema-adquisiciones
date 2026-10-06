# Reporte 03 — ComparacionOfertas

**Definición:** `reports/ssrs/03_ComparacionOfertas.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación/ejecución SSRS y PDF pendientes.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Orden | Integer | `None` |

## Dataset SQL

```sql
SELECT p.id_pedido,ar.nombre AS articulo,p.cantidad,v.nombre_comercial AS proveedor,f.precio_unitario,f.fecha_oferta,CAST(CASE WHEN d.id_oferta IS NULL THEN 0 ELSE 1 END AS BIT) AS ganadora,d.cantidad_final,d.precio_acordado FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor LEFT JOIN DetalleAdjudicacion d ON d.id_oferta=f.id_oferta WHERE p.id_orden=@Orden  ORDER BY p.id_pedido,f.precio_unitario,f.id_oferta
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
