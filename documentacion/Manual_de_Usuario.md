# Manual de Usuario — GuateCompras

Sistema de Gestión de Adquisiciones y Proveedores

Universidad Mariano Gálvez de Guatemala, sede Salamá, Baja Verapaz

Ingeniería en Sistemas de Información

Curso: Bases de Datos I. Catedrático: Ing. Erwin Roberto Méndez


## 1. Introducción

### 1.1 Propósito

Este manual explica cómo utilizar GuateCompras, el sistema con el que una institución registra sus solicitudes de compra, las agrupa en órdenes, recibe las ofertas de sus proveedores, adjudica la compra y evalúa el desempeño de cada proveedor. El documento está organizado para que cada persona encuentre lo que necesita según el rol que tenga asignado.

### 1.2 A quién va dirigido

El manual atiende a cuatro perfiles de usuario, que corresponden a los cuatro roles del sistema:

- Administrador del sistema (AdminSistema): configura catálogos, usuarios y permisos, y tiene acceso completo.
- Gestor de compras (GestorCompras): registra solicitudes, crea órdenes, compara ofertas, adjudica y evalúa proveedores.
- Administrador de proveedor (AdminProveedor): representa a un proveedor y mantiene su catálogo de precios y sus ofertas.
- Auditor (Auditor): consulta toda la información y los informes, sin posibilidad de modificarla.

### 1.3 Cómo leer este manual

Las secciones 2 a 5 son comunes a todos los usuarios. La sección 6 describe los datos de demostración con los que se entrega el sistema, y las secciones 7 y 8 contienen la guía detallada por pantalla y los recorridos paso a paso por rol, construidos sobre esos datos. La sección 9 documenta los ocho informes, la 10 los mensajes que puede mostrar el sistema y la 11 reúne recomendaciones de uso. Un último apartado describe el cliente de escritorio opcional.

### 1.4 Requisitos para usar el sistema

- Un navegador actual (Edge, Chrome o Firefox) con JavaScript habilitado.
- La dirección del sistema que le proporcione su administrador. En una instalación local con Tomcat suele ser `http://localhost:8080/sistema-adquisiciones/`.
- Un usuario y una contraseña asignados por el administrador del sistema.

## 2. Conceptos del proceso de adquisición

Antes de usar las pantallas conviene fijar el vocabulario que emplea el sistema.

| Término | Significado en GuateCompras |
|---|---|
| Solicitud de compra (pedido) | Necesidad de un departamento: qué artículo, cuántas unidades, cuándo se solicitó y para cuándo se necesita. |
| Orden de compra | Agrupación de varias solicitudes con un plazo común para recibir ofertas. Tiene un tipo (Grande o Chica) y, opcionalmente, un subtipo (Normal o Urgente). |
| Oferta | Precio unitario que un proveedor propone para una solicitud concreta de una orden abierta. |
| Adjudicación | Resolución de una orden: para cada solicitud se elige una oferta ganadora, con su cantidad y precio final. |
| Evaluación | Calificación de 1 a 5 que se asigna a un proveedor por una línea adjudicada. |
| Catálogo de proveedor | Lista de artículos que el proveedor puede suministrar y su precio de referencia. |
| Relación comercial | Vínculo registrado entre dos proveedores distintos. |

El proceso completo sigue este orden: un departamento registra una solicitud, el gestor la incluye en una orden, los proveedores presentan ofertas mientras la orden está abierta, el gestor adjudica y, una vez adjudicada, se evalúa a los proveedores.

Una orden puede encontrarse en cuatro estados, que el sistema calcula con las fechas:

| Estado | Condición |
|---|---|
| Programada | La fecha de creación todavía no ha llegado. |
| Abierta | Ya fue creada, no está adjudicada y la fecha límite de ofertas no ha vencido. Es el único estado en que se reciben ofertas. |
| Vencida | Pasó la fecha límite de ofertas y no tiene adjudicación. |
| Adjudicada | Tiene una adjudicación registrada. |

## 3. Acceso al sistema

### 3.1 Inicio de sesión

1. Abra en el navegador la dirección del sistema. Si no tiene una sesión activa, se mostrará la pantalla "Bienvenido a su espacio de trabajo".
2. Escriba su nombre de usuario (hasta 30 caracteres) y su contraseña.
3. Pulse "Iniciar sesión". Al validar sus datos, el sistema abre el portal con el menú que corresponde a su rol.

### 3.2 Credenciales de la instalación de demostración

La instalación de demostración incluye cuatro usuarios, uno por rol: `admin`, `gestor`, `proveedor` y `auditor`. Sus contraseñas no son fijas. Se generan al azar la primera vez que se carga la semilla de datos y se guardan en el archivo `config/demo-access.properties` de la instalación, que no se comparte ni se sube al repositorio. Solicite esas contraseñas a la persona que instaló el sistema. Por seguridad, cada institución debe cambiarlas antes de usar el sistema con información real.

### 3.3 Cierre de sesión y tiempo de inactividad

El botón "Cerrar sesión" aparece en la esquina superior derecha, junto a su nombre y rol. La sesión se cierra sola tras 30 minutos sin actividad; en ese caso el sistema le devolverá a la pantalla de inicio de sesión y deberá ingresar de nuevo.

### 3.4 Problemas al ingresar

| Situación | Qué significa | Qué hacer |
|---|---|---|
| "Usuario o contraseña incorrectos" | Los datos no coinciden o el usuario está inactivo. | Verifique mayúsculas y minúsculas. Si persiste, pida al administrador que revise su cuenta. |
| "Demasiados intentos; espere un minuto" | Se superaron 10 intentos en un minuto desde el mismo equipo. | Espere un minuto y vuelva a intentarlo. |
| "No se pudo iniciar sesión" o error de configuración | La base de datos no está disponible. | Avise al administrador técnico. |

## 4. La interfaz del portal

### 4.1 Distribución general

El portal tiene tres zonas. A la izquierda está el menú lateral con los módulos disponibles para su rol; si un módulo no aparece, su rol no tiene permiso de lectura sobre él. En la parte superior se muestran la ruta de navegación, su nombre completo, su rol y el botón para cerrar sesión. En el centro se despliega el contenido del módulo seleccionado. En pantallas estrechas el menú se oculta y se abre con el botón de tres líneas.

Los módulos se agrupan así: Resumen institucional; Solicitudes de compra, Órdenes de compra, Ofertas de proveedores y Adjudicaciones; los catálogos de Artículos, Proveedores, Catálogo de proveedores, Sucursales, Departamentos, Teléfonos de sucursal, Rubros de proveedores, Relaciones comerciales, Tipos de orden y Subtipos de orden; Evaluación de proveedores; Informes y consultas; la administración de seguridad (Usuarios, Roles, Pantallas y Matriz de permisos); y Conexión de bases e Historial de cambios.

### 4.2 Listados de catálogos

Los módulos de catálogo comparten la misma disposición:

- Un cuadro de búsqueda. Escriba el texto y pulse Intro o el botón "Buscar"; la búsqueda se aplica a los campos de texto del módulo.
- El botón "+ Nuevo registro", visible solo si su rol puede crear.
- Una tabla con los datos. Si hay acciones permitidas, la última columna muestra "Editar" y "Borrar".
- Paginación de 20 registros por página con los botones "Anterior" y "Siguiente".

### 4.3 Formulario de edición

Al crear o editar se abre un cuadro de diálogo con los campos del registro. Los campos obligatorios no pueden quedar vacíos, los límites de longitud se aplican mientras escribe, las referencias a otros registros (por ejemplo, la sucursal de un departamento) se eligen de una lista, y las claves de un registro existente aparecen bloqueadas porque no pueden cambiarse. Si el sistema rechaza el guardado, el motivo se muestra dentro del mismo diálogo para que pueda corregir y reintentar.

### 4.4 Avisos y confirmaciones

Los mensajes de resultado aparecen en una franja sobre el contenido. Antes de borrar se pide confirmación. Tenga presente que el sistema protege el historial: un registro con datos relacionados no se podrá borrar, y esto es un comportamiento esperado, no un fallo.

### 4.5 Impresión y exportación

La pantalla de informes ofrece los botones "Imprimir" y "Exportar CSV". El archivo CSV se abre con Excel o con cualquier hoja de cálculo.

## 5. Roles y permisos

Cada rol tiene permisos de crear, leer, actualizar y borrar por pantalla. La tabla siguiente resume la configuración con la que se entrega el sistema. Un administrador puede modificarla desde "Matriz de permisos".

| Pantalla | AdminSistema | GestorCompras | AdminProveedor | Auditor |
|---|---|---|---|---|
| Resumen institucional | Lectura | Lectura | Lectura (solo su proveedor) | Lectura |
| Solicitudes de compra | Completo | Completo | Lectura (solo las abiertas que puede ofertar) | Lectura |
| Órdenes de compra | Completo | Completo | Lectura (solo órdenes que le conciernen) | Lectura |
| Ofertas de proveedores | Completo | Completo | Completo (solo las propias) | Lectura |
| Adjudicaciones | Completo | Completo | Sin acceso | Lectura |
| Evaluación de proveedores | Completo | Completo | Sin acceso | Lectura |
| Artículos | Completo | Lectura | Lectura (solo activos) | Lectura |
| Proveedores y rubros | Completo | Lectura | Sin acceso | Lectura |
| Catálogo de proveedores | Completo | Lectura | Completo (solo el propio) | Lectura |
| Sucursales, teléfonos y departamentos | Completo | Lectura | Sin acceso | Lectura |
| Relaciones comerciales | Completo | Lectura | Sin acceso | Lectura |
| Tipos y subtipos de orden | Completo | Lectura | Sin acceso | Lectura |
| Usuarios, roles, pantallas y permisos | Completo | Lectura | Sin acceso | Lectura |
| Informes y consultas | Completo | Lectura | Lectura (informes 1, 2, 3, 5 y 8, solo sus datos) | Lectura |
| Conexión de bases | Completo | Lectura | Sin acceso | Lectura |
| Historial de cambios | Completo | Lectura | Sin acceso | Lectura |

En la tabla, "Completo" significa crear, leer, actualizar y borrar. Los informes y el resumen son de consulta para todos los roles, de modo que "Lectura" es su máximo alcance. Si intenta una operación para la que no tiene permiso, el sistema responde "No tiene permiso para esta operación".

## 6. Datos de demostración

La instalación se entrega con datos ficticios cargados por la semilla, de manera que cada pantalla tenga contenido desde el primer ingreso. Esos datos son los que se usan en los ejemplos de este manual. La semilla genera:

| Elemento | Cantidad | Detalle |
|---|---|---|
| Sucursales | 5 | Códigos GC1 a GC5, en Guatemala, Salamá, Cobán, Quetzaltenango y Flores. Cada una con dos teléfonos. |
| Departamentos | 10 | Dos por sucursal: Administración y Operaciones. |
| Artículos | 50 | Códigos ART-001 a ART-050: papelería, equipo de cómputo, mobiliario y limpieza. Todos activos. |
| Proveedores | 20 | Códigos PRV-001 a PRV-020, cada uno con dos rubros entre Papelería, Tecnología, Mobiliario, Limpieza y Seguridad. |
| Catálogo de proveedores | 1000 | Cada proveedor ofrece los 50 artículos. |
| Relaciones comerciales | 5 | PRV-001 con PRV-011, PRV-002 con PRV-012, y así hasta PRV-005 con PRV-015. |
| Tipos y subtipos de orden | 2 y 4 | Grande y Chica; cada uno con subtipos Normal y Urgente. |
| Órdenes de compra | 20 | Con descripción "DEMO Orden 01" a "DEMO Orden 20". Las órdenes 01 a 10 están adjudicadas y las 11 a 20 están abiertas. |
| Solicitudes de compra | 100 | 60 pertenecen a las 20 órdenes (tres por orden) y 40 están sin asignar. |
| Ofertas | 180 | Tres ofertas de proveedores distintos por cada solicitud de las 20 órdenes. |
| Adjudicaciones | 10 | Las de las órdenes 01 a 10, con tres líneas cada una (30 líneas). |
| Usuarios | 4 | admin, gestor, proveedor y auditor. El usuario "proveedor" está asociado a PRV-001. |

Las fechas de la demostración son relativas al día en que se cargó la semilla: las órdenes adjudicadas se crearon 45 días antes y las abiertas 7 días antes, con un plazo de ofertas que vence 10 días después de la carga. Por eso los informes y la lista de órdenes abiertas tienen datos coherentes durante ese período. La semilla no carga evaluaciones de proveedores ni registros del historial de cambios; estos se generan al usar el sistema.

## 7. Guía por pantalla

En esta sección se describe cada pantalla: para qué sirve, qué roles pueden usarla y qué reglas aplica. Los nombres coinciden con los del menú lateral.

### 7.1 Resumen institucional

Es la pantalla inicial. Muestra cuatro indicadores: monto adjudicado en quetzales, número de órdenes abiertas, solicitudes de compra y número de proveedores. Debajo aparece un gráfico de barras con los cinco proveedores de mayor monto adjudicado en los últimos doce meses y un panel que resume cuántas ofertas y adjudicaciones hay registradas. Para el administrador de proveedor, todos los valores se limitan a su propio proveedor y el alcance se indica como "Su proveedor".

### 7.2 Sucursales

Registra las sedes de la institución. Campos: código (obligatorio, hasta 10 caracteres y único), dirección (150), ciudad (60), departamento geográfico (60) y teléfono principal (20); todos son obligatorios. No se puede borrar una sucursal que tenga departamentos.

### 7.3 Teléfonos de sucursal

Permite registrar teléfonos adicionales de una sucursal. Se elige la sucursal y se escribe el teléfono (hasta 20 caracteres). Un mismo teléfono no puede repetirse en la misma sucursal.

### 7.4 Departamentos

Unidades internas de una sucursal que generan solicitudes. Campos: sucursal (se elige de la lista), nombre (obligatorio, hasta 60) y descripción (opcional, hasta 200). El nombre no puede repetirse dentro de la misma sucursal. El listado muestra además el código de la sucursal.

### 7.5 Artículos

Catálogo de bienes que se pueden solicitar. Campos: código (hasta 20, único), nombre (hasta 100), descripción (opcional, hasta 300) y estado (Activo o Inactivo). Un artículo inactivo no puede incluirse en nuevas solicitudes, órdenes ni ofertas; es la forma correcta de retirar un artículo del uso sin perder su historial. El administrador de proveedor solo ve los artículos activos.

### 7.6 Proveedores

Registro de las empresas que suministran bienes. Campos: código (hasta 20, único), nombre comercial (100), dirección (150), teléfono (20), rubro principal (80) y estado. La tabla incluye la columna "Evaluación promedio (1–5)", calculada con las evaluaciones recibidas. Solo un proveedor activo puede presentar ofertas y solo uno activo puede resultar adjudicado.

### 7.7 Rubros de proveedores

Permite asociar a un proveedor varios rubros de actividad además del principal. Se elige el proveedor y se escribe el rubro (hasta 80). La combinación proveedor y rubro no puede repetirse.

### 7.8 Catálogo de proveedores

Relaciona cada proveedor con los artículos que puede suministrar y el precio de referencia. Campos: proveedor, artículo y precio de catálogo en quetzales, mayor que cero y con dos decimales como máximo. Es un requisito del sistema que un proveedor solo pueda ofertar artículos que figuren en su catálogo. No se puede borrar una línea de catálogo si el proveedor ya presentó ofertas sobre ese artículo. El administrador de proveedor ve y gestiona únicamente su propio catálogo.

### 7.9 Relaciones comerciales

Registra un vínculo entre dos proveedores. Se eligen el primer y el segundo proveedor, que deben ser distintos. El sistema guarda siempre el par en un orden fijo, de modo que registrar PRV-002 con PRV-001 equivale a PRV-001 con PRV-002 y no se puede duplicar.

### 7.10 Tipos de orden y Subtipos de orden

Los tipos de orden son Grande y Chica; los subtipos son Normal y Urgente y siempre dependen de un tipo. Un tipo no puede tener dos subtipos con el mismo nombre. Estos catálogos rara vez cambian y solo el administrador puede modificarlos.

### 7.11 Solicitudes de compra

Es el punto de partida del proceso. Campos: departamento, artículo, cantidad (entero mayor que cero), fecha de solicitud y fecha necesaria. La fecha necesaria no puede ser anterior a la de solicitud y el artículo debe estar activo. El listado agrega las columnas "Orden asignada", departamento, sucursal, artículo y "Disponible para oferta", que indica con Sí o No si la solicitud pertenece a una orden abierta. Una solicitud que ya forma parte de una orden no se edita desde esta pantalla: se gestiona desde la orden. No se puede borrar una solicitud asignada.

### 7.12 Órdenes de compra

El listado muestra identificador, descripción, fechas, tipo, subtipo, estado y número de solicitudes. Cada fila ofrece "Ver / comparar" y, según el permiso, "Editar" y "Borrar".

Para crear o editar una orden se completan la descripción, la fecha de creación, el límite de ofertas, observaciones opcionales, el tipo, el subtipo (opcional) y la lista de solicitudes incluidas. La lista solo muestra solicitudes sin orden y las que ya pertenecen a la orden que se edita. El sistema aplica estas reglas:

- Debe seleccionarse al menos una solicitud y ninguna puede repetirse ni pertenecer a otra orden.
- La fecha límite no puede ser anterior a la fecha de creación.
- La fecha de creación no puede ser anterior a la fecha de solicitud de ninguna de sus solicitudes.
- El subtipo debe corresponder al tipo elegido.
- Los artículos de las solicitudes deben estar activos.

La opción "Ver / comparar" abre un panel con las solicitudes de la orden y la comparación de ofertas. En ella se resalta el mejor precio de cada solicitud, se indica la diferencia de cada oferta frente al mínimo y se señalan los empates. Si la orden aún no está adjudicada y usted puede adjudicar, el panel incluye el botón "Adjudicar orden".

Una orden que ya tiene ofertas o una adjudicación no se puede editar ni borrar; esto conserva el historial de compra. Al borrar una orden sin ofertas, sus solicitudes quedan libres para incluirse en otra.

### 7.13 Ofertas de proveedores

Registra el precio que un proveedor propone para una solicitud. Campos: proveedor, solicitud, precio unitario, fecha de la oferta y observaciones opcionales. Solo se pueden elegir solicitudes que estén disponibles para oferta. Las reglas son:

- La oferta debe presentarse mientras la orden está abierta, es decir, entre su fecha de creación y su fecha límite.
- El proveedor y el artículo deben estar activos y el artículo debe figurar en el catálogo del proveedor.
- Cada proveedor mantiene una sola oferta por solicitud. Si registra otra para la misma solicitud, el sistema actualiza la existente en lugar de duplicarla.
- Una oferta adjudicada, o de una orden ya adjudicada, forma parte del historial y no puede modificarse ni borrarse. Tampoco puede modificarse una oferta cuya orden ya venció o aún no se ha abierto.

El listado agrega la columna "Estado": Vigente, Vencida o Adjudicada. Para el administrador de proveedor, el campo proveedor se completa con el propio y no puede cambiarse.

### 7.14 Adjudicaciones

El listado muestra cada adjudicación con su orden, descripción, fecha de resolución y monto total. "Ver detalle" presenta las líneas adjudicadas con solicitud, artículo, proveedor, cantidad final y precio acordado.

Para adjudicar se parte de la opción "Adjudicar orden" en el panel de la orden. El formulario pide la fecha de resolución, observaciones opcionales y, para cada solicitud, la oferta ganadora, la cantidad final y el precio acordado. Reglas aplicables:

- Debe adjudicarse todas las solicitudes de la orden, una sola vez cada una.
- La oferta elegida debe pertenecer a esa solicitud y el precio acordado debe coincidir con el de la oferta.
- La cantidad final no puede exceder la solicitada.
- La fecha de resolución no puede estar en el futuro, ni ser anterior a la creación de la orden ni a la fecha de la oferta ganadora.
- El proveedor ganador debe estar activo.
- Una orden solo admite una adjudicación.

La operación es atómica: si alguna validación falla, no se guarda nada. La opción "Revertir" elimina por completo la adjudicación, pero solo es posible si ninguna de sus líneas tiene evaluaciones; la reversión queda registrada en el historial de cambios.

### 7.15 Evaluación de proveedores

Permite calificar a un proveedor por una línea adjudicada. Campos: detalle adjudicado (identificador numérico de la línea), calificación de 1 a 5, comentario opcional (hasta 300) y fecha. La fecha de la evaluación no puede ser anterior a la fecha de resolución de la adjudicación. Las calificaciones alimentan la columna "Evaluación promedio" del módulo Proveedores.

El identificador de la línea adjudicada se solicita como número. En una base recién sembrada, las líneas de las adjudicaciones de las órdenes DEMO 01 a 10 tienen los identificadores 1 a 30, a razón de tres por orden y en el orden de sus solicitudes. Para adjudicaciones creadas posteriormente, consulte el identificador con el administrador técnico.

### 7.16 Usuarios

Administra las cuentas de acceso. Campos: usuario (hasta 30, único), contraseña (de 12 a 72 caracteres; al editar se deja vacía para conservar la actual), nombre completo, correo electrónico, rol, proveedor asociado y estado. Las reglas de seguridad son:

- El proveedor asociado es obligatorio solo para el rol AdminProveedor y no se permite en otros roles; el campo aparece únicamente cuando se elige ese rol.
- Debe existir siempre al menos un AdminSistema activo.
- Nadie puede borrar su propia cuenta, desactivarla ni cambiar su propio rol.
- Un usuario inactivo no puede iniciar sesión.

### 7.17 Roles, Pantallas y Matriz de permisos

Los roles y las pantallas son catálogos de nombre y descripción. Los cuatro roles base no pueden borrarse ni cambiar de nombre. La Matriz de permisos relaciona cada rol con cada pantalla mediante cuatro casillas: crear, leer, actualizar y borrar. Un cambio de permisos se aplica en la siguiente petición del usuario afectado.

### 7.18 Informes y consultas

Contiene los ocho informes del sistema. Se describe en la sección 9.

### 7.19 Conexión de bases

Muestra el estado en vivo de los dos motores de base de datos, SQL Server y PostgreSQL. Cada tarjeta indica si el motor está disponible, el nombre de la base y del servidor, la versión, el resultado de una prueba `SELECT 1`, la hora del servidor y la latencia. El botón "Comprobar conexiones ahora" repite la prueba. Es útil para confirmar que ambos motores responden, por ejemplo antes de una presentación.

### 7.20 Historial de cambios

Es el registro de auditoría. Cada creación, actualización, borrado, adjudicación o reversión realizada desde el portal deja una línea con el usuario, la acción, la entidad afectada, la referencia del registro y la fecha. Se muestra paginado de 50 en 50 y es de solo lectura. Las fechas se guardan en hora universal (UTC).

## 8. Recorridos por rol

Cada recorrido usa los datos de demostración descritos en la sección 6. Los ejercicios que modifican datos están pensados para repetirse sin riesgo, pero conviene realizarlos en una instalación de prueba.

### 8.1 Administrador del sistema (admin)

Objetivo: conocer la configuración y ejecutar el ciclo completo de una compra.

1. Ingrese como `admin` y revise el Resumen institucional. Debe ver el monto adjudicado de las diez órdenes adjudicadas y cinco proveedores en el gráfico.
2. Abra Sucursales y confirme las cinco sedes GC1 a GC5. Entre a Departamentos y observe que cada sucursal tiene Administración y Operaciones.
3. En Artículos, busque "Resma". Edite uno de ellos, cambie su estado a Inactivo, guarde y verifique que ya no puede seleccionarse al crear una solicitud. Vuelva a dejarlo Activo.
4. En Solicitudes de compra, localice las que tienen la columna "Orden asignada" vacía: son las 40 solicitudes sin asignar. Cree una nueva con un departamento, un artículo activo, una cantidad y fechas coherentes.
5. En Órdenes de compra, pulse "+ Crear orden". Escriba una descripción, deje la fecha de creación de hoy, fije un límite de ofertas posterior, elija tipo Chica y subtipo Normal, y marque dos o tres solicitudes sin asignar. Guarde: la orden aparecerá como Abierta.
6. Cierre sesión e ingrese como `proveedor` para registrar ofertas (recorrido 8.3) y regrese como `admin`.
7. Abra la orden con "Ver / comparar", revise el mejor precio resaltado y pulse "Adjudicar orden". Elija la oferta ganadora de cada solicitud, confirme cantidades y precios y guarde.
8. En Evaluación de proveedores, cree una evaluación para una de las líneas adjudicadas con una calificación de 1 a 5. Consulte luego la columna "Evaluación promedio" del proveedor.
9. En Usuarios, cree un usuario con rol Auditor y una contraseña de al menos 12 caracteres. Revise en Historial de cambios que todas las operaciones anteriores quedaron registradas.
10. En Matriz de permisos, verifique la configuración de cada rol antes de entregar el sistema a otras personas.

### 8.2 Gestor de compras (gestor)

Objetivo: gestionar el ciclo de adquisición sin modificar catálogos.

1. Ingrese como `gestor`. El menú muestra todos los módulos, pero solo podrá modificar solicitudes, órdenes, ofertas, adjudicaciones y evaluaciones.
2. En Solicitudes de compra, revise las 40 pendientes. El informe 4 (Pedidos sin asignación) las presenta ordenadas por fecha necesaria, lo que ayuda a priorizar.
3. Cree una orden agrupando solicitudes del mismo tipo de necesidad, por ejemplo artículos de papelería. Asigne tipo Grande y subtipo Urgente si el caso lo amerita.
4. Mientras la orden esté abierta, vigile en "Ver / comparar" la llegada de ofertas. Un empate de precio se señala en la tabla; en ese caso, decida con el criterio de la institución y deje constancia en las observaciones.
5. Una vez vencido el plazo o cuando disponga de las ofertas, adjudique la orden. Si se equivoca y la adjudicación aún no tiene evaluaciones, use "Revertir" y adjudique de nuevo.
6. Evalúe a los proveedores adjudicados.
7. Para practicar sin modificar datos, revise las órdenes DEMO 01 a 10: ya están adjudicadas y muestran en el detalle qué oferta ganó cada solicitud.

### 8.3 Administrador de proveedor (proveedor)

Objetivo: mantener el catálogo propio y ofertar en órdenes abiertas. El usuario de demostración pertenece al proveedor PRV-001.

1. Ingrese como `proveedor`. Verá solo los módulos que le corresponden: Resumen, Solicitudes de compra, Órdenes de compra, Ofertas, Artículos, Catálogo de proveedores e Informes.
2. En Catálogo de proveedores, observe sus 50 artículos y precios. Cambie el precio de uno y guarde. Recuerde que no podrá borrar una línea sobre la que ya tenga ofertas.
3. En Órdenes de compra, localice las órdenes abiertas que le conciernen. Solo aparecen aquellas con solicitudes de artículos de su catálogo.
4. En Ofertas, pulse "+ Nuevo registro". El proveedor ya está completado y bloqueado. Elija una solicitud disponible, escriba un precio unitario menor o igual al que desee competir, deje la fecha de hoy y guarde.
5. Si necesita corregir el precio mientras la orden siga abierta, edite su oferta o registre una nueva para la misma solicitud: el sistema actualiza la existente.
6. Consulte la columna "Estado" de sus ofertas: Vigente mientras la orden está abierta, Vencida si pasó el plazo y Adjudicada si resultó ganadora.
7. En Informes, ejecute el historial de compra, el top de proveedores, la comparación de ofertas, las órdenes abiertas o la evolución de precios; todos mostrarán únicamente datos suyos.

### 8.4 Auditor (auditor)

Objetivo: verificar la información sin alterarla.

1. Ingrese como `auditor`. Todos los módulos son visibles y ninguno muestra botones de creación, edición o borrado.
2. En Historial de cambios, revise quién modificó qué y cuándo. Úselo para contrastar con los movimientos de compra.
3. En Informes, ejecute el informe 7 (promedio de días entre creación y adjudicación) y el informe 6 (gasto por departamento) para el año en curso. Exporte los resultados en CSV para su papel de trabajo.
4. En Matriz de permisos, confirme que ningún rol de operación tenga más permisos de los necesarios.
5. En Conexión de bases, deje constancia de que ambos motores respondieron.

## 9. Informes y consultas

La pantalla "Informes y consultas" permite elegir uno de los ocho informes, indicar sus parámetros y pulsar "Consultar". Solo se piden los parámetros del informe elegido. Los resultados se muestran en una tabla con el total de registros y los botones "Imprimir" y "Exportar CSV". Si la instalación tiene configurado el servidor de informes SSRS, aparece además el botón "Abrir SSRS", que abre la versión de ese informe en el servidor de informes.

| N.º | Informe | Parámetros | Para qué sirve |
|---|---|---|---|
| 1 | Historial de compra de artículos | Artículo (0 = todos), desde, hasta | Ver qué se compró, a quién, cuándo, en qué cantidad y a qué precio. |
| 2 | Top 5 de proveedores por monto adjudicado | Desde, hasta | Identificar a los proveedores con mayor volumen adjudicado. |
| 3 | Comparación de ofertas y ganadora por orden | Orden | Revisar todas las ofertas de una orden y cuál ganó. |
| 4 | Pedidos sin asignación | Ninguno | Listar las solicitudes que aún no forman parte de una orden. |
| 5 | Órdenes abiertas para ofertas | Ninguno | Mostrar las órdenes que hoy reciben ofertas. |
| 6 | Gasto por departamento, sucursal y año | Sucursal (0 = todas), año | Conocer cuánto gastó cada departamento. |
| 7 | Promedio de días entre creación y adjudicación | Desde, hasta | Medir la rapidez del proceso de compra. |
| 8 | Evolución de precios por artículo y proveedor | Artículo, proveedor (0 = todos), desde, hasta | Seguir cómo cambian los precios ofrecidos. |

Los identificadores de artículo, proveedor, orden y sucursal se escriben como números. Se obtienen de la columna "ID" de cada listado. Los informes 4, 6 y 7 no están disponibles para el administrador de proveedor, y en los demás solo recibe sus propios datos. Todos los importes se expresan en quetzales (GTQ).

Ejemplos con los datos de demostración:

- Informe 4 sin parámetros: devuelve las 40 solicitudes sin asignar, ordenadas por fecha necesaria.
- Informe 3 con la orden 1 (DEMO Orden 01 en una base recién sembrada): devuelve las 9 ofertas de las tres solicitudes de esa orden, con la ganadora marcada.
- Informe 5: devuelve las órdenes abiertas y vigentes, que en la demostración son las órdenes DEMO 11 a 20 durante los diez días posteriores a la carga.
- Informe 2 con un rango que incluya los últimos meses: muestra el ranking de los cinco proveedores con mayor monto adjudicado.

## 10. Mensajes del sistema y cómo resolverlos

| Mensaje | Causa probable | Qué hacer |
|---|---|---|
| No autenticado / Sesión no válida | La sesión expiró o su usuario fue desactivado. | Inicie sesión nuevamente. |
| Token de seguridad no válido | La página quedó abierta con una sesión anterior. | Recargue la página (Ctrl+F5) e inténtelo de nuevo. |
| No tiene permiso para esta operación | Su rol no puede realizar esa acción. | Pida al administrador que revise la matriz de permisos si considera que debería poder hacerlo. |
| Solo puede gestionar su propio catálogo | Un administrador de proveedor intentó acceder a datos de otro proveedor. | Trabaje únicamente con su proveedor. |
| El registro ya existe o está relacionado con otros datos | Se duplicó un código o se intentó borrar algo con datos dependientes. | Revise códigos únicos; para retirar un registro con historial use el estado Inactivo. |
| Los datos no respetan las reglas de integridad de adquisiciones | Una regla de la base de datos rechazó la operación, por ejemplo fechas fuera de plazo o ofertas de un artículo que el proveedor no ofrece. | Revise fechas, artículo y catálogo del proveedor. |
| La orden ya tiene ofertas o adjudicación; conserve su historial | Se intentó editar o borrar una orden con movimientos. | No es posible; el historial se conserva. |
| La orden ya está adjudicada | Se intentó adjudicar dos veces. | Revise la adjudicación existente o revírtala primero. |
| La adjudicación tiene evaluaciones; conserve su historial | Se intentó revertir o cambiar una adjudicación evaluada. | No es posible. |
| Debe adjudicar todos los pedidos de la orden | Faltan líneas en la adjudicación. | Elija una oferta ganadora para cada solicitud. |
| No se pudo completar la consulta | Fallo temporal de la base de datos. | Reintente; si persiste, avise al administrador técnico con la referencia que muestra el sistema. |

## 11. Buenas prácticas

- Use el estado Inactivo para retirar artículos, proveedores y usuarios; no intente borrarlos, ya que el historial los referencia.
- Registre las observaciones en órdenes, ofertas y adjudicaciones cuando una decisión no sea obvia, por ejemplo al resolver un empate.
- Cierre sesión al terminar, sobre todo en equipos compartidos.
- Guarde una contraseña propia y no la comparta; cada acción queda registrada con el nombre de usuario.
- Antes de adjudicar, revise la comparación de ofertas y confirme que la cantidad final no exceda la solicitada.
- Exporte los informes en CSV cuando necesite conservar una fotografía de los datos a una fecha determinada.

## 12. Cliente de escritorio (opcional)

Además del portal web, el proyecto incluye un cliente de escritorio para Windows, construido con WPF y .NET 10. Se conecta a la misma API que el portal, por lo que respeta los mismos roles, permisos y reglas. En la pantalla de inicio se indica la dirección del servidor de la institución, el usuario y la contraseña. Permite consultar y gestionar los módulos de acuerdo con su rol y exportar resultados a CSV. Requiere el Desktop Runtime de .NET 10 instalado en el equipo y que el servidor web esté en ejecución.

## Anexo A. Resumen de reglas de validación

| Dato | Regla |
|---|---|
| Cantidades y precios | Mayores que cero. Los precios admiten dos decimales y un máximo de 99,999,999.99. |
| Contraseña de usuario | De 12 a 72 bytes. |
| Calificación de evaluación | Entre 1 y 5. |
| Fechas de solicitud | La fecha necesaria no puede preceder a la fecha de solicitud. |
| Fechas de orden | El límite de ofertas no puede preceder a la creación; la creación no puede preceder a las solicitudes que agrupa. |
| Fechas de adjudicación | No puede ser futura ni preceder a la orden ni a la oferta ganadora. |
| Registros activos | Solo artículos y proveedores activos participan en solicitudes, órdenes, ofertas y adjudicaciones. |
| Unicidad | Código de sucursal, de artículo y de proveedor; nombre de usuario, correo, nombre de rol y de pantalla; nombre de departamento por sucursal; una adjudicación por orden. |

## Anexo B. Glosario

Adjudicar: elegir, para cada solicitud de una orden, la oferta ganadora.

Catálogo: lista de artículos y precios de referencia de un proveedor.

CSV: archivo de texto con valores separados por comas, que abre cualquier hoja de cálculo.

GTQ: código de la moneda, quetzal guatemalteco.

Orden abierta: orden que recibe ofertas en este momento.

Semilla: proceso que carga los datos de demostración en las bases de datos.

Solicitud o pedido: necesidad de compra de un departamento.
