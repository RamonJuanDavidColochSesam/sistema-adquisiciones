# Reporte 08 — EvolucionPrecios

**Definición:** `reports/ssrs/08_EvolucionPrecios.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación y ejecución SSRS comprobadas y PDF real de 11 páginas verificado el 9 de octubre de 2026 (evidencia `../assets/evidencias/cierre/ssrs/local-20261009.txt`).

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Articulo | Integer | `0` |
| Proveedor | Integer | `0` |
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

## Dataset SQL

```sql
SELECT ar.codigo_articulo,ar.nombre AS articulo,v.nombre_comercial AS proveedor,f.fecha_oferta,f.precio_unitario,p.id_orden FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE (@Articulo=0 OR ar.id_articulo=@Articulo) AND (@Proveedor=0 OR f.id_proveedor=@Proveedor) AND f.fecha_oferta BETWEEN @Desde AND @Hasta  ORDER BY ar.nombre,v.nombre_comercial,f.fecha_oferta,f.id_oferta
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
