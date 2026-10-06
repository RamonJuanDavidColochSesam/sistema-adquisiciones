# MATRIZ DE REQUISITOS

5 de octubre de 2026. U = PDF universitario local con página; S = solicitud del usuario. Referencias locales no se confunden con requisitos oficiales. COMPLETO exige evidencia del alcance de la fila; PARCIAL implementación/verificación incompleta; AUSENTE sin implementación; DEFECTUOSO incumplimiento observable. Ningún CRUD es COMPLETO. La entrega documental COMPLETO no certifica sistema.

Java abreviado dentro de src/main/java/com/adquisiciones; HTML dentro de src/main/webapp; ../ materiales fuera del repositorio en ProyectoBD. Ver auditoría para conflictos web/escritorio, SSRS/Jasper y plan definitivo.

| Requisito | Evidencia actual | Estado | Archivos relacionados | Trabajo necesario |
| --- | --- | --- | --- | --- |
| U p.1–2: modelo 3FN | 18 entidades; rubros/teléfonos pendientes | PARCIAL | schema.sql; diccionario/drawio externos | Documentar DF/claves; normalizar multivaluados |
| U p.2: Java/C# | Java y bytecode61; error clean | PARCIAL | pom.xml; evidencias | Build limpio y ejecución JDK17 |
| U p.2: escritorio/Visual Studio | Web/Eclipse por solicitud usuario | PARCIAL | pom.xml; web.xml; Eclipse | Resolver aceptación académica sin migrar silenciosamente |
| U p.2: SQL Server | JDBC SELECT1; base sin tablas | PARCIAL | ConexionManager; schema.sql | Instalar esquema y validar manager/DAO |
| U p.2,5: dos DBMS | PostgreSQL28P01; pools acoplados | DEFECTUOSO | pom; ConexionManager; ConexionRecurso | Corregir config/desacoplar; prueba de ambos |
| U p.2: entrega Sonat | Sin entrega realizada | AUSENTE | README | Preparar entrega académica posterior |
| U p.3,5: CRUD Sucursal | Modelo/DAO/servicio/REST/HTML; sin BD | PARCIAL | Sucursal*.java; HTML | Operaciones reales/permisos/validación/UX |
| U p.3,5: CRUD Departamento | Modelo/DAO/servicio/REST/HTML; sin BD | PARCIAL | Departamento*.java; HTML | Operaciones reales/permisos/validación/UX |
| U p.3,5: CRUD Articulo | Modelo/DAO/servicio/REST/HTML; sin BD | PARCIAL | Articulo*.java; HTML | Operaciones reales/permisos/validación/UX |
| U p.3,5: CRUD Proveedor | Modelo/DAO/servicio/REST/HTML; sin BD | PARCIAL | Proveedor*.java; HTML | Operaciones reales/permisos/validación/UX |
| U p.3: Códigos únicos | UNIQUE DDL | PARCIAL | schema.sql; fuentes catálogos | Instalar/probar duplicados |
| U p.3: Departamento único por sucursal | UNIQUE compuesto | PARCIAL | schema.sql; fuentes catálogos | Probar repetición entre sucursales y rechazo dentro |
| U p.3: Teléfonos sucursal | Un VARCHAR20 | PARCIAL | schema.sql; fuentes catálogos | Normalizar varios teléfonos |
| U p.3: Rubros múltiples | categoria singular | PARCIAL | schema.sql; fuentes catálogos | Rubro/ProveedorRubro y migración |
| U p.3: Proveedor-artículo N:M y precio | PK compuesta y CRUD fuente | PARCIAL | schema.sql; fuentes catálogos | Probar precio/duplicados/scope |
| U p.3: Relación comercial proveedores | Solo tabla/drawio | PARCIAL | schema.sql; fuentes catálogos | Aclarar simetría y crear capas funcionales |
| U p.4: Pedido artículo/cantidad/fechas/departamento | Solo DDL Pedido | PARCIAL | schema.sql | Capas completas y pantalla |
| U p.4: Cantidad>0/fecha necesaria válida | CHECK DDL | PARCIAL | schema.sql | SQL negativo y servicio |
| U p.4: Orden agrupa pedidos | FK nullable Pedido.id_orden | PARCIAL | schema.sql | Servicio transaccional/selección pendientes |
| U p.4: Fecha orden>=solicitud | Trigger solo Pedido | PARCIAL | schema.sql | Cubrir actualizaciones de padres/lotes |
| U p.4: Tipos Grande/Chica/Subtipos | FK independientes | PARCIAL | schema.sql | Compatibilidad/nulidad/UNICIDAD |
| U p.4: Oferta proveedor/pedido/precio/fecha | Solo DDL Oferta | PARCIAL | schema.sql | Capas completas/período/duplicados |
| U p.4: Adjudicación por orden/fecha | Tabla/UNIQUE/trigger | PARCIAL | schema.sql | Servicio transaccional/protección padre |
| U p.4: Ganador/cantidad/precio por pedido | Detalle con FK independientes | PARCIAL | schema.sql | Correspondencia oferta-pedido-orden/justificación |
| U p.4: rol Administrador del Sistema | Tabla Rol sin seed/autorización | PARCIAL | schema.sql; AuthFilter | Seed permisos/pruebas por rol/alcance |
| U p.4: rol Gestor de Compras | Tabla Rol sin seed/autorización | PARCIAL | schema.sql; AuthFilter | Seed permisos/pruebas por rol/alcance |
| U p.4: rol Administrador de Proveedor | Tabla Rol sin seed/autorización | PARCIAL | schema.sql; AuthFilter | Seed permisos/pruebas por rol/alcance |
| U p.4: rol Auditor/Reportes | Tabla Rol sin seed/autorización | PARCIAL | schema.sql; AuthFilter | Seed permisos/pruebas por rol/alcance |
| U p.5: permisos CRUD por pantalla | Permiso solo DDL; filtro solo sesión | DEFECTUOSO | AuthFilter; schema.sql | Filtro Jersey/política/PermisoDAO/deny default |
| U p.5: login/roles funcionales | Sesión/DAO/BCrypt; Usuario ausente en BD | PARCIAL | LoginRecurso/Servicio; UsuarioDAO; login.html | Pruebas login correcto/incorrecto/inactivo/rol |
| U p.5: MER/cardinalidades | Drawio18 entidades con discrepancias | PARCIAL | ../Diagrama Proyecto BD I.drawio | Sincronizar atributos/cardinalidades/3FN |
| U p.5: diccionario | 18 tablas en DOCX | PARCIAL | ../DICCIONARIO DE DATOS.docx | Actualizar reglas/modelo final |
| U p.5: DDL PK/FK/UNIQUE/NOT NULL/CHECK | schema y Word DDL | PARCIAL | schema.sql; ../SCRIPTS DDL PROYECTO | Instalar/probar y completar integridad |
| U p.5: código documentado/funcional | Comentarios parciales; módulos ausentes | PARCIAL | src/main; README | Completar funcionamiento/comentarios útiles |
| U p.5: seed 5 sucursales | Sin seed; BD vacía | AUSENTE | schema.sql | DML coherente y conteos demostrables |
| U p.5: seed 10 departamentos | Sin seed; BD vacía | AUSENTE | schema.sql | DML coherente y conteos demostrables |
| U p.5: seed 50 artículos | Sin seed; BD vacía | AUSENTE | schema.sql | DML coherente y conteos demostrables |
| U p.5: seed 20 proveedores | Sin seed; BD vacía | AUSENTE | schema.sql | DML coherente y conteos demostrables |
| U p.5: seed 100 pedidos | Sin seed; BD vacía | AUSENTE | schema.sql | DML coherente y conteos demostrables |
| U p.6: reporte 1 Historial por artículo | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 2 Top5 proveedores por monto/rango fechas | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 3 Comparativo ofertas/orden/ganador | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 4 Pedidos no asignados | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 5 VIEW órdenes abiertas a ofertas | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 6 Gasto departamento/sucursal/año | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 7 Promedio días creación-adjudicación | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: reporte 8 Precio artículo/proveedor/fecha | Sin consulta/API/UI | AUSENTE | src/main; schema.sql | Consulta parametrizada/conciliación/interfaz real |
| U p.6: Manual usuario | No existe | AUSENTE | docs; README | Documentar sistema final |
| U p.6: Manual técnico | No existe | AUSENTE | docs; README | Documentar sistema final |
| U p.6 incentivo: SSRS/escritorio | Sin integración; Jasper solicitado | AUSENTE | pom; docs | Resolver objetivo incentivo; Jasper no sustituye SSRS |
| S: Java17/Maven/WAR reproducible | Build limpio falla; fork emite error | PARCIAL | pom/evidencias | Resolver ejecución limpia y plugins reproducibles |
| S: BCrypt sin texto plano | PasswordUtil+jBCrypt | PARCIAL | PasswordUtil/LoginServicio | Prueba real/política/hash inválido |
| S: Logout/sesión/me | Invalidate implementado; falta me/rotación | PARCIAL | LoginRecurso/AuthFilter | Sesión segura/revocación/me |
| S: Autorización backend/IDOR | Solo idUsuario; proveedor libre | DEFECTUOSO | AuthFilter/ProveedorArticuloRecurso | Permisos por endpoint/CRUD y scope |
| S: Validación/HTTP/errores uniformes | Catches/e.getMessage heterogéneos | DEFECTUOSO | recursos/servicios | DTO/mapper/null/IDs/longitudes/409 |
| S: XSS | Dos páginas sin escape | DEFECTUOSO | sucursales/departamentos.html | DOM seguro y eventos externos |
| S: SQL Injection | DAO parametrizados; sin pentest | PARCIAL | dao/*.java | Mantener parámetros/probar entradas |
| S: Credenciales externas/.env.example | db.properties entra en WAR | DEFECTUOSO | ConexionManager/pom/.gitignore | Loader externo/ignores/WAR sin secretos |
| S: Estados/observaciones | Solo Usuario.estado | AUSENTE | schema.sql | Modelo/transiciones de negocio |
| S: CRUD usuarios/roles/rubros | Sin capas/UI | AUSENTE | UsuarioDAO solo login | Administración con permisos |
| S: Búsqueda/filtros/paginación | Listados completos | AUSENTE | HTML/DAO | Consulta y UI funcionales |
| S: Comparativo diferencias/empates/selección | Sin pantalla/servicio | AUSENTE | src/main | Comparativo específico y selección trazable |
| S: Evaluación1–5/promedio | Tabla adicional | PARCIAL | schema.sql | CRUD asociado/promedio derivado |
| S: Dashboard | index solo enlaces | AUSENTE | index.html | Métricas reales y visualizaciones útiles |
| S: health/databases/tiempos | test usa !isClosed/público | PARCIAL | ConexionRecurso | SELECT1/pools independientes/permiso |
| S: Jasper ocho PDF | Sin librería/JRXML/exportación | AUSENTE | pom/reports ausente | Plantillas/parámetros/PDF reales |
| S: Frontend empresarial responsive | CSS/JS embebidos/sin viewport | PARCIAL | 7HTML | Sidebar/shared CSS/formularios/loading/empty/error |
| S: SQL organizado01–09 | Solo schema.sql | AUSENTE | database ausente | Instalación/migraciones/seed/views/reportes |
| S: Pruebas conexión/login/permisos/CRUD/reglas/reportes | No tests to run | AUSENTE | src/test/tests ausentes | Casos positivos/negativos/integración real |
| S: E2E navegador-API-SQLServer | Sin arranque/schema funcional | AUSENTE | tests ausente | Flujo real con evidencia |
| S: README/guía instalación | Solo título | AUSENTE | README | Guía/config/usuarios/troubleshooting |
| S: Documentación arquitectura/modelo/endpoints/seguridad/reportes | Auditoría/materiales externos | PARCIAL | docs/materiales | Documentar implementación futura |
| S: Auditoría inicial/matriz esta fase | Documentos entregados con evidencia | COMPLETO | docs/AUDITORIA_INICIAL; MATRIZ | Solo entrega documental, no sistema completo |
| S: Auditoría final/checklist | No corresponde todavía | AUSENTE | AUDITORIA_FINAL ausente | Crear al completar sistema |
| S: Sin mocks de negocio | DAO reales; hello diagnóstico fijo | PARCIAL | DAO/HelloRecurso | No contar hello como función de negocio |

## Inventario por archivo

| Archivo | Estado | Diagnóstico |
| --- | --- | --- |
| src/main/java/com/adquisiciones/conexion/ConexionManager.java | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/dao/ArticuloDAO.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/dao/DepartamentoDAO.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/dao/ProveedorArticuloDAO.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/dao/ProveedorDAO.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/dao/SucursalDAO.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/dao/UsuarioDAO.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/filtro/AuthFilter.java | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/modelo/Articulo.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/modelo/Departamento.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/modelo/Proveedor.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/modelo/ProveedorArticulo.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/modelo/Sucursal.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/modelo/Usuario.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/ConexionRecurso.java | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/HelloRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/LoginRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/recurso/SucursalRecurso.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/servicio/ArticuloServicio.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/servicio/DepartamentoServicio.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/servicio/LoginServicio.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/servicio/ProveedorArticuloServicio.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/servicio/ProveedorServicio.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/servicio/SucursalServicio.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/util/GenerarHash.java | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/java/com/adquisiciones/util/PasswordUtil.java | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/resources/db.properties | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/resources/db.properties.example | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/articulos.html | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/departamentos.html | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/index.html | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/login.html | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/proveedorarticulos.html | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/proveedores.html | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/sucursales.html | DEFECTUOSO | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |
| src/main/webapp/WEB-INF/web.xml | PARCIAL | Fuente/configuración presente; ver diagnóstico por archivo en auditoría, sin ejecución integral certificada |

## Inventario de capas y SQL

- 7 páginas HTML, cada una con JS/CSS embebidos; no archivos JS/CSS independientes.
- 8 recursos REST, 6 DAO, 6 servicios, 6 modelos, 1 conexión, 1 filtro de autenticación, 2 utilidades. Todos detallados arriba.
- DTO/mapper/security/report ausentes. Autorización defectuosa (filtro solo sesión).
- pom: parcial, 8 dependencias directas/Java17/WAR; plugins sin fijar, sin Jasper/pruebas.
- schema: parcial, 18 tablas y 2 triggers; cobertura cruzada defectuosa; ninguna tabla en GuateCompras verificada.
- Tablas: Sucursal, Departamento, Articulo, Proveedor, ProveedorArticulo, RelacionComercial, TipoOrden, SubtipoOrden, OrdenCompra, Pedido, Oferta, Adjudicacion, DetalleAdjudicacion, EvaluacionProveedor, Rol, Pantalla, Permiso, Usuario. Cada una PARCIAL: DDL presente, instalación/pruebas faltantes.
- Vistas/consultas de reportes/seed/índices explícitos/JRXML/Jasper/SSRS/tests ausentes.
- README: ausente como guía, solo título. docs: auditoría/matriz/evidencias, resto pendiente.
- PDF/diccionario/DDL Word/drawio/seguridad: disponibles fuera de repositorio, leídos; clasificación en auditoría. Drive completo pendiente.

Decisiones de subtipo/revisión ofertas/cantidades/simetría se documentan antes del modelo definitivo. No se implementan módulos grandes en esta fase.
