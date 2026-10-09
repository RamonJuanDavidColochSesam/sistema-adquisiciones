# Cierre verificado de SSRS

6 de octubre de 2026. **Ocho informes publicados y ocho PDF reales verificados.** Servicio: http://localhost/ReportServer. Carpeta del portal: http://localhost/Reports/browse/GuateCompras.

La publicación y exportación se ejecutaron con la sesión Windows autorizada. Los ocho RDL recuperados por SOAP tienen SHA-256 idéntico a los archivos fuente. Los PDF proceden de SSRS con conexión SQL integrada a GuateCompras.

Se comparó cada campo de cada fila extraída de las tablas PDF contra las ocho consultas reales del backend: **285 filas coincidentes**. Orden adjudicada 10; rango 2025-01-01 a 2026-12-31; año 2026; artículo/proveedor/sucursal 0 (todos).

| Informe | Filas | Páginas |
|---|---:|---:|
| Historial de compra | 30 | 2 |
| Top proveedores | 5 | 1 |
| Comparación de ofertas | 9 | 1 |
| Pedidos sin asignar | 40 | 3 |
| Órdenes abiertas | 10 | 1 |
| Gasto por departamento | 10 | 1 |
| Promedio de adjudicación | 1 | 1 |
| Evolución de precios | 180 | 10 |

Revisión visual de las 20 páginas: tablas legibles, encabezados repetidos en continuación, sin columnas recortadas ni solapamientos. Comparación: tres ganadoras con cantidad/precio acordado. Promedio: diez órdenes adjudicadas, doce días. Esta evidencia acredita los parámetros registrados; no todas las combinaciones posibles de filtros.

Evidencia: `publicacion-real.json`, `verificacion-datos.json`, `baseline-consultas.json`, `ejecucion-ssrs.txt`, ocho PDF y ocho definiciones recuperadas del servidor. La comparación reproducible está en `verificar_datos_pdf.py` (requiere Python y pdfplumber).

El backend fue iniciado con GUATECOMPRAS_SSRS_URL=http://localhost/ReportServer para habilitar «Abrir SSRS»; el acceso continúa sujeto a permisos Windows/SSRS. Para otros equipos configurar su URL real. GitBook y Sonat conservan sus estados independientes.

## Repetición en este equipo (9 de octubre de 2026)

El ciclo completo se repitió en el equipo local (BMO) sobre SSRS 2022 16.0.9760.42787 (edición Developer) instalado y configurado con `scripts/instalar_ssrs.ps1`; publicación con `scripts/publicar_ssrs.ps1` y verificación con `scripts/verificar_ssrs.ps1`, parámetros orden 11 y rango 2025-10-09 a 2026-10-09.

| Informe | Líneas con datos | Páginas |
|---|---:|---:|
| Historial de compra | 81 | 3 |
| Top proveedores | 7 | 1 |
| Comparación de ofertas | 22 | 1 |
| Pedidos sin asignación | 64 | 3 |
| Órdenes abiertas | 19 | 1 |
| Gasto por departamento | 12 | 1 |
| Promedio de adjudicación | 3 | 1 |
| Evolución de precios | 239 | 11 |
| **Total** | **447** | **22** |

Los cruces con SQL Server coinciden: informe 07 devuelve 15 órdenes con promedio 8.6 días y el Top 5 del informe 02 es idéntico fila por fila. Evidencia con parámetros, hora, bytes y SHA-256 de los ocho PDF: [local-20261009.txt](evidencias/cierre/ssrs/local-20261009.txt); PDF en `reports/evidencia`.
