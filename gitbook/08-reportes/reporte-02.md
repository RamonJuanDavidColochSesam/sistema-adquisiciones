# Reporte 02 — TopProveedores

**Definición:** `reports/ssrs/02_TopProveedores.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación/ejecución SSRS y PDF pendientes.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

## Dataset SQL

```sql
SELECT TOP(5) v.codigo_proveedor,v.nombre_comercial AS proveedor,SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))) AS monto_adjudicado,COUNT(DISTINCT a.id_orden) AS ordenes FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE a.fecha_resolucion BETWEEN @Desde AND @Hasta  GROUP BY v.codigo_proveedor,v.nombre_comercial ORDER BY monto_adjudicado DESC,v.codigo_proveedor
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
