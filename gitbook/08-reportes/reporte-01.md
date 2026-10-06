# Reporte 01 — HistorialCompra

**Definición:** `reports/ssrs/01_HistorialCompra.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación/ejecución SSRS y PDF pendientes.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Articulo | Integer | `0` |
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

## Dataset SQL

```sql
SELECT ar.codigo_articulo,ar.nombre AS articulo,v.nombre_comercial AS proveedor,a.fecha_resolucion,d.cantidad_final,d.precio_acordado,CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2)) AS monto FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE (@Articulo=0 OR ar.id_articulo=@Articulo) AND a.fecha_resolucion BETWEEN @Desde AND @Hasta  ORDER BY ar.nombre,a.fecha_resolucion,v.nombre_comercial
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
