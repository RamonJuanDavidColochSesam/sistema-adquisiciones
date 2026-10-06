# Reporte 06 — GastoDepartamento

**Definición:** `reports/ssrs/06_GastoDepartamento.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación/ejecución SSRS y PDF pendientes.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Sucursal | Integer | `0` |
| Anio | Integer | `=Year(Today())` |

## Dataset SQL

```sql
SELECT s.codigo_sucursal,s.ciudad,de.nombre AS departamento,SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))) AS gasto FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Departamento de ON de.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=de.id_sucursal WHERE (@Sucursal=0 OR s.id_sucursal=@Sucursal) AND a.fecha_resolucion>=@InicioAnio AND a.fecha_resolucion<@FinAnio GROUP BY s.codigo_sucursal,s.ciudad,de.nombre ORDER BY s.codigo_sucursal,de.nombre
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
