# SSRS

## Publicar y verificar los ocho informes SSRS

> **Estado real, 6 de octubre de 2026:** SSRS configurado; ocho RDL publicados y ocho PDF reales verificados, 285 filas y 20 páginas. Consulte CIERRE_SSRS.md y la evidencia de cierre.

## Configuración reproducible (ya completada en este equipo)

1. Abrir Report Server Configuration Manager como administrador y conectar la instancia instalada.
2. En Database, crear el catálogo ReportServer y su base temporal; mantener GuateCompras sin recrearla.
3. Configurar Web Service URL y Web Portal URL, aplicar y probar los vínculos. Anotar las dos URLs reales. Inicialmente utilizar acceso local.
4. Verificar acceso al servicio ReportService2010.asmx?wsdl con la identidad Windows autorizada.

Windows denegó el acceso WMI administrativo desde la sesión de trabajo; no se modificó rsreportserver.config a mano. [Configuración oficial de URLs Microsoft](https://learn.microsoft.com/en-us/sql/reporting-services/install-windows/configure-a-url-ssrs-configuration-manager?view=sql-server-ver17).

## Fuente de datos e identidades

La fuente usa Windows Integrated y Data Source=localhost;Initial Catalog=GuateCompras. La identidad efectiva necesita SELECT sobre Articulo, Proveedor, Pedido, Departamento, Sucursal, OrdenCompra, Oferta, Adjudicacion, DetalleAdjudicacion y vw_OrdenesAbiertas. No dar lectura global a Usuario ni sus hashes. El publicador necesita Content Manager en /GuateCompras; lectores institucionales, Browser en esa carpeta. No conceder la carpeta a proveedores externos: su acceso permanece en la API con alcance obligatorio.

Si aparece rsErrorOpeningConnection, revisar identidad y permisos SQL; no quitar autenticación. Roles Java y roles Windows/SSRS son distintos.

## Publicación y renderizado

Desde Windows PowerShell 5.1 y raíz del proyecto, sustituir URL_SERVICIO_REAL e ID_ORDEN_CON_OFERTAS:

```powershell
./scripts/publicar_ssrs.ps1 -ReportServer 'URL_SERVICIO_REAL' -SqlServer localhost
./scripts/verificar_ssrs.ps1 -ReportServer 'URL_SERVICIO_REAL' -IdOrden ID_ORDEN_CON_OFERTAS -Salida reports/evidencia -Desde '2025-10-06' -Hasta '2026-10-06'
```

Si la política local bloquea archivos ps1, ejecutar desde una consola autorizada o pedir al administrador la forma aprobada. No se cambió la política del equipo.

El publicador crea/reutiliza carpeta y fuente antes de cargar los RDL. El verificador solicita PDF mediante URL Access y valida cabecera/tamaño; requiere una revisión adicional de contenido/presentación, ya completada para esta exportación. No bastan un nombre publicado o una cabecera válida.

## Aceptación por informe

1. Abrir en portal, seleccionar parámetros y ejecutar sin errores.
2. Comparar datos con la consulta del [inventario](inventario.md). Usar una orden adjudicada con ofertas para ver ganador y fechas que incluyan los datos históricos.
3. Cambiar filtros y conciliar filas/montos; confirmar Top5, vista abierta y promedio de días.
4. Descargar PDF real, extraer texto para comprobar información no vacía y revisar columnas, fechas, totales y páginas.
5. Conservar PDF, parámetros, hora, SHA-256 y log. Solo entonces actualizar la tabla.

| # | RDL | Publicado | Abre SSRS | Datos SSRS | PDF | Resultado |
|---|---|---|---|---|---|---|
| 1 | `01_HistorialCompra.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 2 | `02_TopProveedores.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 3 | `03_ComparacionOfertas.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 4 | `04_PedidosSinAsignar.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 5 | `05_OrdenesAbiertas.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 6 | `06_GastoDepartamento.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 7 | `07_PromedioAdjudicacion.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |
| 8 | `08_EvolucionPrecios.rdl` | Pendiente | Pendiente | No comprobados | No generado | PENDIENTE |

## Integración en web y WPF

Establecer GUATECOMPRAS_SSRS_URL con la URL real del servicio antes de iniciar Tomcat y reiniciar. El enlace abre SSRS con parámetros y autenticación Windows independiente. Un valor de variable no prueba disponibilidad. CSV y la impresión web complementan; no son PDF SSRS.


[Volver a Reportes](README.md)
