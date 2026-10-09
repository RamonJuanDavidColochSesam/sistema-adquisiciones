# Inventario de RDL

## Inventario y verificación SSRS

6 de octubre de 2026. El servicio SQLServerReportingServices está Running, pero rsreportserver.config no tiene DSN de catálogo, directorios virtuales ni reservas de URL. El WSDL local no es accesible. Instalación comprobada, configuración funcional pendiente. Evidencia: `evidencias/cierre/ssrs-diagnostico.json` y `ssrs-intento.txt`.

9 de octubre de 2026 (equipo local): SSRS 2022 16.0.9760.42787 configurado por `scripts/instalar_ssrs.ps1`; WSDL y portal responden HTTP 200, catálogo `ReportServer` creado y los ocho RDL publicados en `/GuateCompras`. Evidencia: `evidencias/cierre/ssrs/local-20261009.txt`.

Carpeta prevista `/GuateCompras`; fuente compartida `/GuateCompras/GuateComprasSQL`; extensión SQL; conexión `Data Source=localhost;Initial Catalog=GuateCompras`; Windows Integrated. La URL destino debe confirmarse después de configurar SSRS. No se atribuye al ejemplo localhost una publicación real.

| # | RDL | Publicado | Abre SSRS | Datos SSRS | PDF | Resultado |
|---|---|---|---|---|---|---|
| 1 | `01_HistorialCompra.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 2 | `02_TopProveedores.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 3 | `03_ComparacionOfertas.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 4 | `04_PedidosSinAsignar.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 5 | `05_OrdenesAbiertas.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 6 | `06_GastoDepartamento.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 7 | `07_PromedioAdjudicacion.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |
| 8 | `08_EvolucionPrecios.rdl` | Sí | Sí (URL Access) | Verificados contra SQL | Generado, SHA-256 en evidencia | COMPLETADO |

## Definiciones existentes

### 1. 01_HistorialCompra.rdl

Ubicación: `reports/ssrs/01_HistorialCompra.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `43b99244bb0d0b15c78a51d7cd162760e6930a527c6d1c2b30d2137b24005e16`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Articulo | Integer | `0` |
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

```sql
SELECT ar.codigo_articulo,ar.nombre AS articulo,v.nombre_comercial AS proveedor,a.fecha_resolucion,d.cantidad_final,d.precio_acordado,CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2)) AS monto FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE (@Articulo=0 OR ar.id_articulo=@Articulo) AND a.fecha_resolucion BETWEEN @Desde AND @Hasta  ORDER BY ar.nombre,a.fecha_resolucion,v.nombre_comercial
```

### 2. 02_TopProveedores.rdl

Ubicación: `reports/ssrs/02_TopProveedores.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `2da5f2555cdcf9f732a623ad3ec6d5134da217c44c6e7a51acedfcfa2757a1f1`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

```sql
SELECT TOP(5) v.codigo_proveedor,v.nombre_comercial AS proveedor,SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))) AS monto_adjudicado,COUNT(DISTINCT a.id_orden) AS ordenes FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Oferta f ON f.id_oferta=d.id_oferta JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE a.fecha_resolucion BETWEEN @Desde AND @Hasta  GROUP BY v.codigo_proveedor,v.nombre_comercial ORDER BY monto_adjudicado DESC,v.codigo_proveedor
```

### 3. 03_ComparacionOfertas.rdl

Ubicación: `reports/ssrs/03_ComparacionOfertas.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `1b00b813126c14ce932ec48974b708b8cb3a758989f823770864781f832f02e1`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Orden | Integer | `None` |

```sql
SELECT p.id_pedido,ar.nombre AS articulo,p.cantidad,v.nombre_comercial AS proveedor,f.precio_unitario,f.fecha_oferta,CAST(CASE WHEN d.id_oferta IS NULL THEN 0 ELSE 1 END AS BIT) AS ganadora,d.cantidad_final,d.precio_acordado FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor LEFT JOIN DetalleAdjudicacion d ON d.id_oferta=f.id_oferta WHERE p.id_orden=@Orden  ORDER BY p.id_pedido,f.precio_unitario,f.id_oferta
```

### 4. 04_PedidosSinAsignar.rdl

Ubicación: `reports/ssrs/04_PedidosSinAsignar.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `38e39771a69f7903bb0115e32690a4020e73cb9090a3d8fedb7baf8df61107b8`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|


```sql
SELECT p.id_pedido,s.codigo_sucursal,d.nombre AS departamento,a.nombre AS articulo,p.cantidad,p.fecha_solicitud,p.fecha_necesaria FROM Pedido p JOIN Departamento d ON d.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=d.id_sucursal JOIN Articulo a ON a.id_articulo=p.id_articulo WHERE p.id_orden IS NULL  ORDER BY p.fecha_necesaria,p.id_pedido
```

### 5. 05_OrdenesAbiertas.rdl

Ubicación: `reports/ssrs/05_OrdenesAbiertas.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `2f4eaf32dc9463491cf94479343f7fa31bd568e6e21b327e48ed833042f02975`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|


```sql
SELECT o.* FROM vw_OrdenesAbiertas o WHERE 1=1  ORDER BY o.fecha_limite_oferta,o.id_orden
```

### 6. 06_GastoDepartamento.rdl

Ubicación: `reports/ssrs/06_GastoDepartamento.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `691c8ce9664baa0fba00f6f9d1d0dd6ab187555c14d26bdc3918fb8314047fb1`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Sucursal | Integer | `0` |
| Anio | Integer | `=Year(Today())` |

```sql
SELECT s.codigo_sucursal,s.ciudad,de.nombre AS departamento,SUM(CAST(d.cantidad_final*d.precio_acordado AS DECIMAL(18,2))) AS gasto FROM DetalleAdjudicacion d JOIN Adjudicacion a ON a.id_adjudicacion=d.id_adjudicacion JOIN Pedido p ON p.id_pedido=d.id_pedido JOIN Departamento de ON de.id_departamento=p.id_departamento JOIN Sucursal s ON s.id_sucursal=de.id_sucursal WHERE (@Sucursal=0 OR s.id_sucursal=@Sucursal) AND a.fecha_resolucion>=@InicioAnio AND a.fecha_resolucion<@FinAnio GROUP BY s.codigo_sucursal,s.ciudad,de.nombre ORDER BY s.codigo_sucursal,de.nombre
```

### 7. 07_PromedioAdjudicacion.rdl

Ubicación: `reports/ssrs/07_PromedioAdjudicacion.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `c2b2142800467eaee3dc386496d1726611ab7970661b627e68d5fcd6e3304162`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

```sql
SELECT COUNT(*) AS ordenes_adjudicadas,AVG(CAST(DATEDIFF(DAY,o.fecha_creacion,a.fecha_resolucion) AS DECIMAL(10,2))) AS promedio_dias FROM Adjudicacion a JOIN OrdenCompra o ON o.id_orden=a.id_orden WHERE a.fecha_resolucion BETWEEN @Desde AND @Hasta
```

### 8. 08_EvolucionPrecios.rdl

Ubicación: `reports/ssrs/08_EvolucionPrecios.rdl`. Dataset: `Datos`. Fuente: `GuateComprasSQL`. Referencia: `/GuateCompras/GuateComprasSQL`. SHA-256: `bc702d276b6ad41e6800639d3a697209dcc4b0f639556e66e0ac175c501f801c`.

| Parámetro | Tipo | Predeterminado |
|---|---|---|
| Articulo | Integer | `0` |
| Proveedor | Integer | `0` |
| Desde | DateTime | `=DateAdd("yyyy",-1,Today())` |
| Hasta | DateTime | `=Today()` |

```sql
SELECT ar.codigo_articulo,ar.nombre AS articulo,v.nombre_comercial AS proveedor,f.fecha_oferta,f.precio_unitario,p.id_orden FROM Oferta f JOIN Pedido p ON p.id_pedido=f.id_pedido JOIN Articulo ar ON ar.id_articulo=p.id_articulo JOIN Proveedor v ON v.id_proveedor=f.id_proveedor WHERE (@Articulo=0 OR ar.id_articulo=@Articulo) AND (@Proveedor=0 OR f.id_proveedor=@Proveedor) AND f.fecha_oferta BETWEEN @Desde AND @Hasta  ORDER BY ar.nombre,v.nombre_comercial,f.fecha_oferta,f.id_oferta
```


[Volver a Reportes](README.md)
