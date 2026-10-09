# Reporte 05 — OrdenesAbiertas

**Definición:** `reports/ssrs/05_OrdenesAbiertas.rdl`. Dataset `Datos`, fuente `/GuateCompras/GuateComprasSQL`.

**Estado:** consulta API/WPF comprobada; publicación y ejecución SSRS comprobadas y PDF real de 1 página verificado el 9 de octubre de 2026 (evidencia `../assets/evidencias/cierre/ssrs/local-20261009.txt`).

| Parámetro | Tipo | Predeterminado |
|---|---|---|


## Dataset SQL

```sql
SELECT o.* FROM vw_OrdenesAbiertas o WHERE 1=1  ORDER BY o.fecha_limite_oferta,o.id_orden
```

## Verificar

Elegir filtros con datos reales, ejecutar y comparar con esta consulta. Exportar PDF desde SSRS, comprobar filas/totales y legibilidad, registrar parámetros y hash. [Tabla de aceptación](ssrs.md).

[Volver a Reportes](README.md)
