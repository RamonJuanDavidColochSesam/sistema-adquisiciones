# Reporte 04 — PedidosSinAsignar

**Definición:** `reports/ssrs/04_PedidosSinAsignar.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación y ejecución SSRS comprobadas y PDF real de 3 páginas verificado el 9 de octubre de 2026 (evidencia `../assets/evidencias/cierre/ssrs/local-20261009.txt`).

| Parámetro | Tipo | Predeterminado |
|---|---|---|


## Dataset SQL

```sql
SELECT p.id_pedido,s.codigo_sucursal,d.nombre AS departamento,a.nombre AS articulo,p.cantidad,p.fecha_solicitud,p.fecha_necesaria FROM Pedido p JOIN Departamento d ON d.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=d.id_sucursal JOIN Articulo a ON a.id_articulo=p.id_articulo WHERE p.id_orden IS NULL  ORDER BY p.fecha_necesaria,p.id_pedido
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
