# Manual de usuario

## Acceso

Iniciar Tomcat y abrir el portal web o el ejecutable WPF. Introducir usuario y contraseña de la hoja privada local de la demo. La sesión dura 30 minutos de inactividad. «Cerrar sesión» invalida la sesión en el servidor. Una cuenta inactiva pierde acceso también en sesiones existentes.

| Usuario demo | Rol | Uso |
| --- | --- | --- |
| admin | AdminSistema | Configuración, usuarios, catálogos y operaciones |
| gestor | GestorCompras | Pedidos, órdenes, ofertas, adjudicaciones y evaluaciones |
| proveedor | AdminProveedor | Su catálogo y sus ofertas; consultas permitidas con alcance propio |
| auditor | Auditor | Lectura institucional, reportes y seguimiento; no modifica |

Los permisos CRUD se comprueban en backend. El menú y los botones reflejan esos permisos. No modificar los roles académicos ni quitar la última cuenta administradora activa. «Matriz de permisos» permite administrar accesos por rol y pantalla.

## Catálogos y organización

En Artículos, Proveedores, Sucursales o Departamentos, buscar, crear, editar o borrar según los permisos. Los listados ofrecen paginación. Los códigos son únicos; un departamento puede compartir nombre con otro en una sucursal distinta, pero no duplicarlo dentro de la misma. Teléfonos de sucursal y Rubros de proveedores permiten varios valores. Catálogo de proveedores relaciona proveedor/artículo y precio. Relaciones comerciales registran pares sin duplicar el sentido inverso.

En los campos numéricos de WPF use punto decimal y no introduzca separadores de miles. Al editar una cuenta, dejar la contraseña nueva vacía conserva su hash; al crearla se exige una contraseña de 12 a 72 bytes UTF-8. Las contraseñas nunca se muestran en tablas. «Estado Inactivo» permite conservar artículos/proveedores con referencias. Una eliminación con dependencias se rechaza con un mensaje; no borra el historial por cascada.

## Proceso de adquisición

1. **Solicitudes de compra:** registrar departamento, artículo activo, cantidad positiva, fecha de solicitud y fecha necesaria. Todavía no tiene orden.
2. **Órdenes:** crear descripción, fechas, tipo Grande/Chica y subtipo Normal/Urgente si corresponde. Seleccionar uno o varios pedidos libres. El alta y sus asignaciones se confirman juntos. No repetir un pedido en otra orden ni crear la orden antes de la solicitud.
3. **Ofertas:** proveedor habilitado, pedido de una orden abierta, artículo de su catálogo, precio positivo y fecha dentro del período. AdminProveedor opera únicamente con su proveedor asociado. No se admiten ofertas nuevas en órdenes vencidas o adjudicadas.
4. **Ver / comparar:** abrir una orden. Se muestran solicitudes y ofertas por pedido con proveedor, cantidad, precio mínimo, diferencia, mejor precio, empate y selección final. Verde suave indica precio mínimo; verde más intenso indica oferta adjudicada en web. Un empate no adjudica automáticamente.
5. **Adjudicar:** elegir una oferta por cada pedido, cantidad final hasta la solicitada y fecha de resolución. El precio acordado corresponde a la oferta seleccionada. La resolución no puede preceder la orden ni la oferta ni estar en el futuro. Todos los detalles y la cabecera se confirman en una transacción.
6. **Adjudicaciones:** consultar el detalle y monto. Solo se puede editar/revertir una resolución sin evaluaciones; la acción queda auditada. Una orden con ofertas no se borra ni se reasigna para preservar su historial.
7. **Evaluación:** seleccionar el ID de un detalle adjudicado, calificar de 1 a 5, fecha y comentario. En proveedores se muestra el promedio histórico cuando existen evaluaciones; sin evaluaciones aparece vacío.

El documento oficial no define inventario, entradas de almacén ni ventas. No se muestran existencias ficticias ni se descuentan cantidades de una tabla inventada.

## Dashboard y trazabilidad

«Resumen institucional» muestra monto adjudicado, órdenes abiertas, solicitudes y proveedores, con ranking y seguimiento de adquisiciones. Los valores provienen de consultas reales. Para AdminProveedor el alcance es su proveedor. «Historial de cambios» conserva usuario, fecha, operación, entidad y referencia; nunca contraseñas.

## Ocho informes

Elegir el informe en «Informes y consultas». Solo se muestran los filtros que corresponden. Consultar devuelve resultados reales y encabezados claros. En web exportar CSV o imprimir la vista consultada; en WPF consultar y exportar CSV. La comparación requiere ID de orden. Los informes de gasto/promedio institucional no están disponibles para AdminProveedor.

SSRS está configurado en este equipo y los ocho informes tienen PDF reales verificados. El servidor también ofrece sus formatos de exportación y la impresión; la evidencia de cierre corresponde a PDF. Los botones SSRS permanecen inhabilitados/ocultos cuando falta esa configuración. AdminProveedor usa exclusivamente la API con filtros obligatorios de alcance; no se le concede acceso institucional directo a SSRS.

## Mensajes y ayuda

Una operación inválida devuelve la regla que debe corregirse. Error 403 significa falta de permiso; 409 indica duplicidad/referencias; 503 indica un problema de consulta/servicio. Ante fallo, conservar la referencia mostrada y consultar al administrador; no insistir enviando el formulario repetidamente. Los botones de envío se bloquean mientras una operación está en curso.

## Demostración sugerida (15 minutos)

Mostrar cuatro perfiles, búsqueda y CRUD reversible de los cuatro catálogos, un pedido y orden nuevos, ofertas, comparación, adjudicación, evaluación y auditoría. Consultar ocho reportes, especialmente vista de órdenes abiertas y top 5. Mostrar WPF y diagnóstico autenticado de ambos motores. SSRS está configurado y sus ocho informes están publicados: finalizar con los ocho PDF reales de docs/evidencias/cierre/ssrs. No usar registros históricos de la semilla como datos desechables de la presentación.


## Referencia académica y observaciones de órdenes

El proceso interno se explica en [GUIA_COMPLETA_DESARROLLO.md](GUIA_COMPLETA_DESARROLLO.md) y la defensa en [FAQ_DEFENSA.md](FAQ_DEFENSA.md). El backend y WPF conservan observaciones de órdenes; el formulario web actual muestra el campo pero no lo incluye al guardar. No se modificó esa lógica en esta tarea documental.
