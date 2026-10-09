# Publicar y verificar los ocho informes SSRS

> **Estado real, 6 de octubre de 2026:** SSRS configurado; ocho RDL publicados y ocho PDF reales verificados, 285 filas y 20 páginas. Consulte CIERRE_SSRS.md y la evidencia de cierre.
>
> **Equipo local, 9 de octubre de 2026:** SSRS 2022 16.0.9760.42787 (edición Developer, modo Native) instalado y configurado por script; los ocho RDL publicados y los ocho PDF reales verificados (22 páginas, 447 líneas con datos) en `reports/evidencia`, con cruces contra SQL Server. Evidencia: [local-20261009.txt](../evidencias/cierre/ssrs/local-20261009.txt).

## Instalación y configuración automatizadas

Instalador oficial [SQL Server 2022 Reporting Services](https://www.microsoft.com/download/details.aspx?id=104502) (`SQLServerReportingServices.exe`, edición Developer: gratuita y sin caducidad). SSRS 2022 es la última versión de SSRS con soporte hasta el 11 de enero de 2033 y admite versiones posteriores del motor SQL Server para el catálogo, como el SQL Server 2025 de este equipo.

Desde una consola elevada (el script pide un solo aviso de Control de Cuenta de Usuario):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\instalar_ssrs.ps1 -Instalador "C:\ruta\SQLServerReportingServices.exe"
```

`instalar_ssrs.ps1` instala en silencio, localiza el proveedor WMI de configuración, crea las bases `ReportServer`/`ReportServerTempDB` con `sqlcmd`, otorga permisos a `NT SERVICE\SQLServerReportingServices`, conecta el catálogo, reserva las URLs `http://localhost/ReportServer` y `http://localhost/Reports`, inicializa el report server, reinicia el servicio y comprueba ambos HTTP. Es idempotente. Para empezar de cero existe `scripts\reinstalar_ssrs.ps1` (desinstala, borra solo los catálogos de SSRS, reinstala y vuelve a configurar; `GuateCompras` no se toca).

Detalles que conviene conocer:

- `sqlcmd` necesita `-C` contra `localhost`: el motor presenta certificado autofirmado y el ODBC 18 lo rechaza por defecto.
- La clase `MSReportServer_ConfigurationSetting` sólo responde desde una consola elevada.
- Tras `InitializeReportServer` el servicio requiere un reinicio limpio; hasta entonces el web service contesta 503 aunque el portal responda 200.
- No se modificó `rsreportserver.config` a mano. [Configuración oficial de URLs Microsoft](https://learn.microsoft.com/en-us/sql/reporting-services/install-windows/configure-a-url-ssrs-configuration-manager?view=sql-server-ver17).

## Configuración manual equivalente (otra máquina)

1. Abrir Report Server Configuration Manager como administrador y conectar la instancia instalada.
2. En Database, crear el catálogo ReportServer y su base temporal; mantener GuateCompras sin recrearla.
3. Configurar Web Service URL y Web Portal URL, aplicar y probar los vínculos. Anotar las dos URLs reales. Inicialmente utilizar acceso local.
4. Verificar acceso al servicio ReportService2010.asmx?wsdl con la identidad Windows autorizada.

Windows denegó el acceso WMI administrativo desde la sesión de trabajo; no se modificó rsreportserver.config a mano.

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
2. Comparar datos con la consulta del [inventario](SSRS_INVENTARIO.md). Usar una orden adjudicada con ofertas para ver ganador y fechas que incluyan los datos históricos.
3. Cambiar filtros y conciliar filas/montos; confirmar Top5, vista abierta y promedio de días.
4. Descargar PDF real, extraer texto para comprobar información no vacía y revisar columnas, fechas, totales y páginas.
5. Conservar PDF, parámetros, hora, SHA-256 y log. Solo entonces actualizar la tabla.

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

Parametrización de la verificación local: orden 11, fechas 2025-10-09 a 2026-10-09; páginas 3/1/1/3/1/1/1/11; cruces 07 (15 órdenes, promedio 8.6) y 02 (Top5) coinciden con las consultas de `db/queries`.

## Integración en web y WPF

Establecer GUATECOMPRAS_SSRS_URL con la URL real del servicio antes de iniciar Tomcat y reiniciar. En este equipo la variable está definida a nivel de usuario y `scripts\iniciar_tomcat.ps1` la toma del registro del usuario si la sesión no la trae (así funciona también al pulsar F5 sin reiniciar VS Code). El enlace abre SSRS con parámetros y autenticación Windows independiente: el navegador vuelve a autenticar con la identidad de Windows y, si el sistema pide credenciales, se acepta con la cuenta local. Un valor de variable no prueba disponibilidad. CSV y la impresión web complementan; no son PDF SSRS.
