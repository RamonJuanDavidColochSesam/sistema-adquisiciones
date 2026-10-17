# Datos de demostración GuateCompras

Registros ficticios adaptados a cinco sedes en Guatemala: Guatemala, Salamá, Cobán, Quetzaltenango y Flores. Proveedores especializados en papelería, tecnología, mobiliario, limpieza y seguridad; catálogo de productos con precios plausibles en quetzales, sin afirmar que sean cotizaciones reales. Los códigos PRV son identificadores internos, no NIT. Los nombres, direcciones y teléfonos no representan contactos verificados; correos usan example.test.

20 órdenes: 10 adjudicadas y 10 sin adjudicación, vigentes al 7/10/2026. 100 pedidos: 60 asociados a órdenes y 40 sin asignar. Tres ofertas por pedido asociado, proveedores con el artículo en catálogo y fechas dentro del plazo. Las 30 adjudicaciones de detalle coinciden con pedido, orden y oferta ganadora; cantidades y precios corresponden a la oferta seleccionada. Hay 30 evaluaciones posteriores a la resolución. Auditoria contiene 40 entradas marcadas DEMO/SIMULADO, no eventos que realmente ocurrieron. Fechas fijas reproducibles: las órdenes dejan de estar abiertas cuando pase su fecha límite.

Los cuatro roles y las veinte pantallas usan los nombres exactos esperados por la aplicación. Los permisos conservan el criterio de SemillaDemo.java: administrador administra, gestor gestiona compras, proveedor limita lectura y escritura a sus módulos, auditor consulta. El usuario proveedor se vincula a PRV-001. No se cambia la autorización de SSRS mediante estos registros.



Al haber poblado las bases de datos con los DML del proyecto o usando la semilla puede iniciar sesión en el sistema usando estas credenciales:

## Accesos de demostración

|Usuario|Contraseña de demostración|
|-|-|
|admin|DemoAdmin2026!|
|gestor|DemoGestor2026!|
|proveedor|DemoProveedor2026!|
|auditor|DemoAuditor2026!|

Los INSERT contienen hashes BCrypt de coste 12 compatibles con jBCrypt, no las contraseñas en texto plano. Estas claves públicas pertenecen a la semilla nueva; no son las contraseñas del equipo original. Las pruebas automáticas que leen config/demo-access.properties requieren configurar allí estos cuatro valores en el equipo donde se cargue la nueva semilla.

## Conteos

|Tabla|Registros|
|-|-:|
|Sucursal|5|
|Departamento|10|
|Articulo|50|
|Proveedor|20|
|ProveedorArticulo|200|
|RelacionComercial|5|
|TipoOrden|2|
|SubtipoOrden|2|
|Rol|4|
|Pantalla|20|
|Permiso|80|
|Usuario|4|
|SucursalTelefono|10|
|ProveedorRubro|20|
|OrdenCompra|20|
|Pedido|100|
|Oferta|180|
|Adjudicacion|10|
|DetalleAdjudicacion|30|
|EvaluacionProveedor|30|
|Auditoria|40|



