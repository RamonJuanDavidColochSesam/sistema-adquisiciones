# REVALIDACIÓN DE PRERREQUISITOS

> Documento histórico. Las condiciones iniciales descritas aquí fueron actualizadas durante la implementación. Consulte AUDITORIA_FINAL.md, MATRIZ_REQUISITOS.md y EVIDENCIA_VERIFICACION.md para el estado verificado actual; las conexiones del usuario no están pendientes de instalación.

Revisión del 5 de octubre de 2026. Esta sección sustituye las conclusiones provisionales del informe inicial conservado al final como historial. Rama main, HEAD e5a0b0c, siete commits. Sin cambios de código funcional, pom, credenciales, esquema o datos. Se conserva el cambio previo en articulos.html.

## Resultado y correcciones

Se releyó íntegra la auditoría y se contrastó con 30 Java, siete HTML (CSS/JS embebidos), pom.xml, schema.sql, web.xml, configuración, historial y materiales locales. Se confirman cuatro CRUD parciales, login/logout, proveedor-artículo, 18 tablas DDL, dos triggers, ausencia de autorización, XSS en sucursales/departamentos y pools acoplados. **Ningún módulo está certificado de extremo a extremo.**

Correcciones: docs ahora existe; se encontraron PDF universitario, diccionario, DDL Word y drawio fuera del repositorio. SQL Server JDBC sí conecta, aunque la base efectiva no tiene tablas. Maven genera WAR, pero no se certifica build limpio sin excepción del compilador.

## Documento oficial y materiales disponibles

Se leyó y renderizó el PDF completo, seis páginas: `C:\Users\dinae\Desktop\ProyectoBD\Proyecto_1_Sistema_VentasProductos -V2 (2).pdf`. Encabezado UMG/Salamá/Bases de Datos I; título interno Sistema de Gestión de Adquisiciones y Proveedores.

| Material | Clasificación / resultado |
| --- | --- |
| PDF universitario | Autoridad académica: p.1–2 modelo/3FN/SQL/tecnología, p.3 catálogos, p.4 ciclo/roles, p.5 permisos/entregables/seed, p.6 ocho reportes/manuales/SSRS adicional |
| DICCIONARIO DE DATOS.docx | Reutilizable con revisión: 18 tablas; evaluación adicional; categoría y teléfono únicos; actualizar normalización/reglas |
| SCRIPTS DDL PROYECTO/DDL SQL SERVER.docx | Referencia reutilizable/duplicación de schema.sql, dos triggers; Word no es instalador SQL |
| Diagrama Proyecto BD I.drawio | Parcial: 18 entidades; id_ususario mal escrito, Permisos vs Permiso y fecha_creacion Usuario omitida; verificar cardinalidad y 3FN |
| Security_SQL_Server.docx | Referencia didáctica de seguridad del motor/criptografía y ejemplos de credenciales; no implementa autorización de aplicación |
| Drive completo | Pendiente: apertura anterior fallida; materiales locales no prueban revisión de toda la carpeta |

Los originales no se modificaron. DOCX leídos por extracción de contenido; su presentación editorial no fue auditada. Drawio se inspeccionó estructuralmente, no se certificó diseño visual.

### Conflictos documentados

- PDF p.2 pide Visual Studio y aplicación de escritorio; el usuario pide Eclipse/web. Conservar arquitectura web por instrucción del usuario; aceptación académica de modalidad requiere confirmación docente, sin afirmar equivalencia.
- SSRS integrado en escritorio es criterio adicional de exoneración (p.6). Ocho reportes obligatorios no dependen de ese incentivo. Jasper/PDF es ampliación del usuario y no sustituye SSRS para obtenerlo.
- Mínimo oficial: **100 pedidos**, no pedidos/órdenes indistintamente.
- Teléfonos y rubros en plural: proponer SucursalTelefono y Rubro/ProveedorRubro; no almacenar listas multivaluadas en VARCHAR.
- Estados/dashboard/BCrypt/evaluaciones/UX ampliada no se atribuyen al PDF donde no aparecen.
- Pendientes de política: revisiones de ofertas, entregas parciales, simetría comercial, límite de cantidad final y obligatoriedad de subtipo Chica. No inventar reglas oficiales.

## Compilación real y WAR

Herramientas portables comprobadas: Microsoft OpenJDK **17.0.20.1**, Apache Maven **3.9.9**. Descargas HTTPS de Microsoft (`https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip`) y Maven Central (`https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip`). No se completó cotejo independiente de firma/checksum. Sin instalación global ni PATH permanente.

Todos los builds ejecutados desde raíz. Dependencias resueltas desde Central en caché aislada. Plugins efectivos: resources3.3.1, compiler3.13.0, surefire3.2.5, war3.4.0. Pom no fija plugins.

| Ejecución | Resultado | Interpretación |
| --- | --- | --- |
| package Java21 | Código1 | AccessDeniedException en compile |
| package Java17 | Código0 BUILD SUCCESS | Nothing to compile: solo empaquetado incremental |
| clean verify Java17 | Código1 | 30 fuentes intentadas; acceso denegado a jakarta.activation-api al cerrar JAR |
| verify fork sin limpiar | Código0 | Incremental, no demuestra recompilación |
| clean verify fork Java17 | Código0 BUILD SUCCESS | 30 clases/WAR generados, pero excepción javac persiste; no se acepta como build sin incidencias |

WAR inspeccionado: 30 clases propias, todas major61 (Java17), 30 JAR y siete HTML. **Contiene WEB-INF/classes/db.properties**; no se distribuye porque incluye configuración privada. No se mostraron sus valores. SHA256 en evidencias/war-inspection.json. Surefire: **No tests to run**.

No se corrigió código/pom: el fallo observado es acceso de archivos del entorno, no error de fuente demostrado. No ocultar excepción ni usar build incremental como sustituto de compilación limpia. Repetir `mvn -B -e clean verify` con JDK17 fuera de esa restricción. Comandos exactos y logs en docs/evidencias.

## SQL Server, JDBC, Hikari y DAO

Prueba de solo lectura con Java17/driver12.8.1.jre11/configuración local: **SELECT1=1**; 9747ms incluyendo carga/conexión/consulta, no benchmark aislado. Base efectiva **GuateCompras**, esquema dbo, VIEW DEFINITION=1. INFORMATION_SCHEMA.TABLES devuelve cero tablas; consultas a once tablas principales fallan SQLStateS0002/código208. El esquema requerido no existe en esa base comprobada. No se ejecutó DDL/DML.

Servicios MSSQLSERVER/PostgreSQL17 activos no certifican aplicación. sqlcmd anterior usó Windows, distinto de autenticación JDBC SQL.

Configuración necesaria según código actual:

1. db.properties en classpath, claves sqlserver.url/user/password y postgres.url/user/password. No se leen variables, .env ni archivo externo.
2. Ejemplo SQL Server: `jdbc:sqlserver://localhost:1433;databaseName=GuateCompras;encrypt=true;trustServerCertificate=true`. No publicar configuración privada; certificado válido al desplegar, trustServerCertificate solo local controlado.
3. TCP/puerto accesible, login SQL habilitado, usuario mapeado y permisos DML; identidad de instalación distinta, sin sysadmin para aplicación.
4. Crear base/aplicar schema.sql únicamente sobre base vacía, con SSMS/sqlcmd que procesen GO. No enviar GO como SQL JDBC.
5. DDL no crea seed/roles/cuentas; posteriores datos con hashes BCrypt.
6. DAO: columnas compatibles estáticamente con DDL, PreparedStatement, try-with-resources; nombres sin esquema requieren resolución dbo.
7. Hikari máximo5 por pool, sin cierre gestionado/límites propios, setJdbcUrl duplicados. Constructor crea ambos: rechazo secundario impide retornar getInstancia y bloquea DAO principal.

Prueba local: conexión directa por gestor → manager real → DAO/login cuando existan tablas/datos. Sondas Java de evidencia usan SELECT1/metadatos/conteos, sin credenciales incrustadas y sin crear módulos.



### Evidencia adicional del manager
La sonda final del ConexionManager real falló por PSQLException, SQLState 28P01: el rechazo PostgreSQL bloquea el manager principal. SLF4J informó falta de implementación de logging. Ver docs/evidencias/README.md y hikari-probe.log.

## Segundo DBMS

Recomendación **PostgreSQL**: driver42.7.4, pool/config/método existentes y servicio17 instalado. Migrar a MySQL no resuelve el rechazo actual.

Prueba real: PSQLException, **SQLState28P01**, 295ms. Revisar usuario/contraseña de la instancia/base y pg_hba.conf conforme a autenticación definida. No se cambiaron credenciales ni reglas; existencia de base destino no certificada porque falla autenticación primero.

PDF p.5 permite conexión secuencial o simultánea, sin exigir duplicación de negocio. Propuesta posterior: GET /api/health/databases protegido, SELECT1 independiente, estados/tiempos y errores sanitizados; desacoplar pools, configurar timeouts/cierre y probar fallo secundario. No se implementó integración nueva.

## Diagnóstico de seguridad

Rutas Java relativas a src/main/java/com/adquisiciones.

| Archivo → problema | Riesgo | Solución propuesta |
| --- | --- | --- |
| filtro/AuthFilter.java → solo idUsuario | CRUD por rol ajeno | Política módulo/acción + PermisoDAO, denegación por defecto |
| ProveedorArticuloRecurso → proveedor libre en ruta | Datos de terceros | Scope servidor por Usuario.id_proveedor en servicio/DAO |
| LoginRecurso → sesión reutilizada | Fijación de sesión | Rotar ID, timeout, HttpOnly/SameSite/Secure bajo HTTPS |
| LoginRecurso → null/sin límites | 500 y abuso | DTO validado, errores uniformes, rate limit |
| LoginServicio/AuthFilter → sin revalidar usuario/rol | Privilegios persistentes tras revocar | Identidad/permisos vigentes o caché invalidable |
| PasswordUtil → sin política/hash inválido | Errores/truncamiento | Límite BCrypt72 bytes, entradas/hash seguro |
| GenerarHash → literal1234 | Clave débil reutilizada | Parametrizar/retirar, no sembrarla |
| Usuario → getter hash | Fuga futura al serializar | DTO público sin hash |
| sucursales/departamentos.html → innerHTML/JSON en onclick | XSS persistente | textContent/createElement/addEventListener |
| recursos → e.getMessage/Map.of(null) | Fuga interna/error secundario | Mapper JSON uniforme/log sanitizado |
| AuthFilter/ConexionRecurso → diagnóstico público | Filtración sin sesión | Health con permiso explícito |
| servicios → null/IDs/longitudes dispares | Bypass/errores | Validación central respaldada por SQL |
| web.xml/login → sin CSRF propio | Mutaciones cookie sin política | Token/Origin y pruebas de cookies/CORS |
| ConexionManager/resources → propiedades empaquetadas | Secretos WAR | Loader externo y excluir configuración privada |
| .gitignore → solo db.properties | Futuras .env en Git | Ignores/.env.example/escáner |

SQL Injection: seis DAO parametrizados; concatenaciones observadas son SQL fijo, no entradas. Es conclusión estática, no pentest. db.properties ignorado; ningún archivo con ese nombre/.env en siete commits. No garantiza ausencia exhaustiva de secretos renombrados/codificados en contenido histórico. Claves didácticas externas no se copiaron.

### Estrategia contra llamadas directas

1. AuthFilter autentica /api/*; solo POST login público; logout/me como identidad.
2. ContainerRequestFilter Jersey después de resolver recurso consulta anotación módulo/acción. Método sin política denegado; no depender de URL/botón.
3. Identidad desde sesión/usuario activo, nunca idRol cliente. Consultar Permiso(id_rol,id_pantalla) y bit acción. Sin sesión401, sin permiso403, BD falla sin ejecutar operación.
4. GET leer, POST alta crear, PUT/PATCH actualizar, DELETE borrar; adjudicar/retirar/PDF con políticas explícitas.
5. Proveedor externo restringido adicionalmente por Usuario.id_proveedor en SQL y servicio, con filas afectadas comprobadas.
6. UI orienta, backend valida cada llamada. Pruebas de auditorPOST403, gestor permitido, proveedorA contraB, revocación, ruta desconocida y errorBD cerrado por defecto.

Estrategia no implementada; seguridad actual sigue defectuosa.

## Diagnóstico de integridad

| Regla | Actual | Protección necesaria |
| --- | --- | --- |
| Identidad/códigos/departamento por sucursal | PK/UNIQUE/FK | Conservar/probar collation/espacios |
| Pedido cantidad>0, necesaria>=solicitud | CHECK | Servicio y pruebas SQL negativas |
| Artículo/departamento válidos | FK | Activos/permisos en servicio |
| Pedido en una orden | FK nullable | Transacción/bloqueo; impedir reasignación tras oferta |
| Orden no vacía | No garantizado | Apertura valida conjunto transaccional |
| Fecha orden>=pedidos | Trigger Pedido | Proteger UPDATE OrdenCompra y lotes |
| Límite>=creación | CHECK | Conservar |
| Tipo/subtipo | FK independientes | UNIQUE tipo/nombre y FK compuesta/trigger; decidir nulidad Chica |
| Oferta en orden abierta/período | FK insuficiente | Trigger cruzado y proteger padres |
| Oferta no duplicada | Ausente | UNIQUE proveedor/pedido o versión histórica según política |
| Proveedor/artículo ofertado | FK separadas | Servicio; catálogo obligatorio solo si política confirmada |
| Precio oferta>0 | CHECK | BigDecimal/escala/precisión |
| Una adjudicación/orden | UNIQUE | Bloqueo y conflicto409 concurrente |
| Resolución>=creación | Trigger adjudicación | Proteger cambio fecha padre |
| Oferta del mismo pedido | FK separadas insuficientes | UNIQUE Oferta(id_oferta,id_pedido) + FK compuesta |
| Pedido en orden adjudicada | FK separadas insuficientes | Trigger set-based/prohibir reasignación histórica |
| Ganador | Derivado de Oferta | No duplicar proveedor; relación coherente |
| Cantidad/precio final positivos | CHECK | Conservar; límite cantidad según política |
| Precio igual oferta o justificado | Ausente | Justificación/servicio/trigger; proteger oferta histórica |
| Detalle único adjudicación/pedido | UNIQUE | Conservar/proteger reasignación |
| Pedidos/estados sincronizados | Ausente | Transacción cabecera+detalles+estados, rollback/concurrencia |
| Evaluación1–5 | FK/CHECK | Fecha/duplicación según política, promedio derivado |
| Relación comercial | FK/distintos/par UNIQUE | Resolver simetría y par canónico |

Índices para FK y consultas fecha/estado/orden/proveedor/departamento; SQL Server no indexa cada FK automáticamente. No sustituyen integridad. Triggers set-based solo para reglas cruzadas y cambios padre. Transacciones coordinan acciones/concurrencia. Normalizar rubros/teléfonos; precios catálogo/oferta/acuerdo son hechos distintos; ganador/totales/promedios derivados. Documentar DF/claves antes de certificar3FN.

## PLAN DE IMPLEMENTACIÓN DEFINITIVO

Sustituye plan previo: seguridad antes de ampliar CRUD; modelo/arquitectura antes de DDL. Pruebas/documentación acompañan cada etapa.

| Orden | Entrega | Criterio para avanzar |
| --- | --- | --- |
| 1 | Build/config/despliegue | clean verify sin excepción, WAR sin secretos, arranque Servlet6 |
| 2 | Decisiones/modelo/arquitectura | Web/SSRS explícitos; MER/diccionario/3FN/estados |
| 3 | SQL Server/migraciones/seed | Esquema/reglas/índices/triggers;5/10/50/20/100pedidos;rollback |
| 4 | Conexión/backend común | Pools externos/DTO/errores/validación/DAO reales |
| 5 | Autenticación/autorización | Login/logout/me,BCrypt/sesión/roles/scope,401/403 |
| 6 | CRUD/relaciones | SQL/API/UI reales por rol |
| 7 | Flujo adquisiciones/evaluación | Pedido→orden→ofertas→comparativo→adjudicación atómica |
| 8 | Dashboard | Métricas conciliadas |
| 9 | PostgreSQL/health | SELECT1 ambos,tiempos/fallo independiente |
| 10 | Ocho consultas/vista | Parámetros/resultados conciliados |
| 11 | Jasper/PDF;SSRS adicional si incentivo | Ocho PDF reales,SSRS no sustituido |
| 12 | Frontend final | Responsive/DOM seguro/filtros/estados/UX |
| 13 | Pruebas consolidadas | Unitarias/integración/SQL/seguridad/PDF/E2E |
| 14 | Documentación/entrega | README/manuales/MER/diccionario/Sonat |
| 15 | Auditoría final | Módulo/estado/evidencia y pendientes |

No avanzar sobre build fallido o base sin esquema. No implementar estas fases ahora.

## Alcance comprobado y pendiente

Verificado: fuentes/materiales/versiones/dependencias/intentos Maven/WAR/bytecode/JDBC SQLServer/base sin tablas/rechazo PostgreSQL/exclusión actual de secretos y nombres en siete commits.

No verificado: build estándar limpio sin excepción, arranqueHTTP,login/BCrypt funcional,CRUD/permisos,ejecución constraints/triggers,E2E,reportes/PDF/SSRS y totalidad Drive. Sin implementación grande ni commits automáticos. Evidencias y comandos en docs/evidencias.


---

# HISTORIAL DE AUDITORÍA ANTERIOR

Se conserva como registro; las conclusiones actualizadas anteriores tienen prioridad.

# AUDITORÍA INICIAL — Sistema de Gestión de Adquisiciones y Proveedores

Fecha: 5 de octubre de 2026. Curso: Bases de Datos I, Universidad Mariano Gálvez de Guatemala.
Repositorio local: C:\Users\dinae\Desktop\ProyectoBD\sistema-adquisiciones.
Estado de esta entrega: auditoría estática realizada; verificación operativa bloqueada. No certifica un sistema terminado.

## Alcance, fuentes y evidencia

Se revisaron pom.xml, schema.sql, README.md, los 30 archivos Java, los siete HTML con CSS/JavaScript embebidos, web.xml, configuración de ejemplo, exclusiones Git y configuración Eclipse. Se inspeccionaron rama, historial, archivos versionados y diferencia local. No se imprimieron credenciales de db.properties; se confirmó su presencia y exclusión de Git.

- Rama main; HEAD e5a0b0c; siete commits. Origin corresponde al repositorio proporcionado. GitHub público muestra la misma estructura principal; no se ejecutó fetch ni se comprobó igualdad exacta con HEAD remoto.
- Modificación previa: una línea de navegación añadida a articulos.html. Preservada. No se hicieron commits, reset, limpieza ni cambios de código en el original.
- Drive: apertura web fallida. No se inventarió su contenido y no puede clasificarse como obsoleto o reutilizable sin acceder a los archivos.
- Documento oficial: no encontrado en los adjuntos ni en el árbol del proyecto. La solicitud extensa es la referencia provisional; no sustituye el cotejo con el documento oficial.
- SQL Server MSSQLSERVER y PostgreSQL 17 aparecen Running. Esto no demuestra que JDBC conecte ni que exista el esquema desplegado.
- Java y Maven no están en PATH. Eclipse contiene Java 25.0.4.1 y runtime Maven 3.9.16. La configuración del proyecto declara Java 17 y Servlet 6.

## Estado actual

### Funciona

Elementos confirmados por inspección, sin atribuirles ejecución integral:

- Separación recurso → servicio → DAO → modelo para cuatro catálogos y proveedor-artículo.
- Consultas DAO parametrizadas mediante PreparedStatement y cierre de conexiones/resultados con try-with-resources.
- BCrypt: PasswordUtil llama hashpw/gensalt/checkpw. LoginServicio consulta usuario, exige estado Activo y verifica hash; no compara contraseñas en texto plano.
- DDL de 18 tablas, PK/FK, unicidad por sucursal/nombre de departamento, relación N:M ProveedorArticulo con precio propio, cantidades/precios positivos y calificación 1–5.
- Scripts de frontend hacen fetch a API real; no se encontró localStorage como sustituto de BD. Los arrays de artículos/asociaciones son cachés de respuestas, no datos de negocio fijos.
- Archivo db.properties.example y exclusión de configuración local.

Ningún módulo puede marcarse funcional de extremo a extremo todavía.

### Parcial

| Área | Evidencia | Brecha |
|---|---|---|
| Sucursales | Modelo/DAO/servicio/recurso/página | Sin estado; validación de longitudes incompleta; errores SQL expuestos |
| Departamentos | CRUD y selector de sucursal | Longitudes sin validar; HTML inseguro; borrado ignora error HTTP |
| Artículos | CRUD, límites de longitud y escape HTML | Sin estado, filtros/paginación; prueba operativa pendiente |
| Proveedores | CRUD, categoría única, NIT como código | Sin estado, rubros N:M, historial o perfil |
| Proveedor-artículo | PK compuesta, BigDecimal, precio validado | PUT valida menos que POST; falta aislamiento por proveedor y comprobación de borrado |
| Login/logout | Sesión HttpSession y BCrypt | Sin rotación explícita de sesión, usuario actual, permisos, limitación de intentos o pruebas |
| Roles/permisos | Tablas Rol/Pantalla/Permiso | No consultadas por AuthFilter; sin administración |
| Dos DBMS | Drivers y pools | Pools acoplados; prueba incompleta; falta tiempo de respuesta y endpoint solicitado |
| Adquisiciones | Tablas Pedido/Orden/Oferta/Adjudicacion/Detalle/Evaluacion | Ninguna capa Java ni pantalla correspondiente |
| Integridad | Dos triggers de fechas | Cobertura incompleta ante actualizaciones de orden y relaciones cruzadas |

### No funciona

Resultados observados y defectos determinables por código:

1. Autorización por módulo/operación: AuthFilter solo comprueba idUsuario y luego permite continuar. Todo usuario autenticado puede invocar operaciones de otros roles. idProveedor no se usa para limitar recursos.
2. DDL no impide que un detalle vincule una oferta de otro pedido o un pedido ajeno a la adjudicación. Las FK independientes no garantizan esa correspondencia.
3. DDL no impide ofertas fuera del período, duplicadas lógicamente o vinculadas a pedidos sin orden. No hay estado de orden que permita impedir ofertas al cierre.
4. Cambiar fecha_creacion de OrdenCompra puede invalidar pedidos/adjudicaciones existentes sin ejecutar los triggers de esas tablas.
5. sqlcmd -S localhost -E -C -l 5 falló con errores de cifrado/credenciales y conexión. No se atribuye el fallo a un defecto específico del sistema sin más evidencia.
6. javac --release 17 sobre copia aislada emitió 30 .class pero terminó con código 3 y AccessDeniedException al cerrar recursos JAR. No se acepta como compilación exitosa.
7. Invocación directa de MavenCli del runtime embebido de Eclipse terminó con código 1 y mensaje de componentes JavaFX faltantes. Ese intento no ejecutó el ciclo Maven; no demuestra fallo del pom.

### Faltante

Pedidos, órdenes, ofertas, comparativo, adjudicación transaccional, evaluaciones y promedios, dashboard, rubros, relaciones comerciales y perfiles, CRUD de usuarios/roles/permisos, health/databases, ocho consultas/reportes, JasperReports y PDF, seed suficiente, pruebas automatizadas y E2E, documentación de instalación, manuales, MER/diccionario, errores centralizados, configuración externa y sesión/permisos seguros.

README contiene solo el nombre. No existen src/test con pruebas de código, tests/, docs/, database/ ni reports/ en el inventario fuente. target/test-classes existente no constituye evidencia de pruebas.

### Riesgos

| Prioridad | Riesgo | Evidencia / efecto |
|---|---|---|
| Crítica | Ausencia de autorización | AuthFilter.doFilter no consulta Permiso ni limita proveedor |
| Alta | XSS persistente | sucursales.html y departamentos.html interpolan valores en innerHTML y JSON en onclick sin escape |
| Alta | Información interna expuesta | Recursos retornan e.getMessage; conexión de diagnóstico es pública |
| Alta | Dependencia innecesaria de PostgreSQL | ConexionManager construye ambos pools en el mismo constructor; fallo secundario bloquea instancia principal |
| Alta | Integridad de adjudicación insuficiente | Falta correspondencia oferta/pedido/orden y garantía de precio justificado |
| Alta | No hay evidencia de compilación, arranque o conexión JDBC | Los intentos de entorno fallaron; no existe prueba E2E |
| Media | Fijación de sesión / CSRF | Login reutiliza sesión sin changeSessionId; sin protección CSRF propia para operaciones con cookie |
| Media | Datos y materiales no cotejados | Documento oficial y Drive pendientes; contenido real de bases desconocido |
| Media | Credenciales dentro del WAR | db.properties en resources se empaqueta al compilar; debe externalizarse |

No se encontraron secretos reales en los archivos versionados revisados, pero NO se hizo auditoría de todo el historial Git. GenerarHash contiene la contraseña débil de ejemplo 1234; no se identifica como credencial real, pero debe retirarse o parametrizarse.

### Deuda técnica

- Manejo de errores repetido en recursos, contratos {error}/{mensaje} sin timestamp/path y uso de Map.of con e.getMessage potencialmente null.
- Ausencia de DTO: Usuario expone getter de hash; el login actual no serializa ese objeto, pero futuros recursos podrían hacerlo.
- Validación incompleta de cuerpos null, identificadores y longitudes en algunos servicios.
- Hikari sin cierre de pools al detener aplicación ni límites de diagnóstico explícitos; llamadas setJdbcUrl duplicadas.
- Estilos y lógica fetch repetidos; sin viewport, componentes accesibles comunes, carga/vacío/error uniforme ni experiencia profesional.
- Manejo por texto de errores SQL frágil y heterogéneo; tres pantallas ignoran respuesta de DELETE.
- Sin versiones explícitas de plugins Maven ni build reproducible comprobado. El compilador declara source/target, no release.
- HelloRecurso es una prueba de infraestructura; no confundir su respuesta fija con funcionalidad académica.

## Decisiones recomendadas / arquitectura propuesta

Conservar Java 17, WAR, Jersey 3/JAX-RS, Servlet 6, JDBC/HikariCP, BCrypt, SQL Server, PostgreSQL y frontend vanilla. La auditoría no evidencia necesidad de Spring Boot ni de framework frontend. Usar contenedor compatible con Servlet 6, previsto Tomcat 10.1, y verificar despliegue real antes de aprobar esa decisión.

Mantener paquetes españoles actuales para evitar renombrado masivo. Incorporar config, seguridad, dto, mapper y reporte. Recursos delgados; servicios con reglas y límites transaccionales; DAO reciben la misma Connection durante operaciones compuestas. Crear/asignar orden y adjudicar deben ser atómicos. Bloquear/validar estado para evitar adjudicación simultánea o asignación doble. La adjudicación conserva evidencia histórica y no se borra como un catálogo.

Separar pools por gestor y permitir diagnóstico independiente mediante SELECT 1 con timeout. Externalizar credenciales a archivo indicado por variable de entorno o propiedades de ejecución. Registro de errores interno sin secretos y respuesta JSON pública uniforme.

Sesión con rotación al login, timeout, cookie HttpOnly y SameSite; Secure según HTTPS del despliegue. Autorización desde Rol/Pantalla/Permiso, denegación por defecto y alcance del proveedor aplicado en servicio/DAO. DTO de usuario sin hash. Protección de solicitudes que modifican datos y pruebas de acceso denegado.

Frontend común con sidebar/header, CSS central, utilidades fetch y DOM seguro; tablas con filtros y formularios relacionales. Comparativo específico por pedido con empate, diferencia y selección de oferta. No adjudicar automáticamente solo por precio: permitir decisión trazable.

JasperReports con ocho JRXML, parámetros tipados y PDF desde consulta real; fuentes y numeración verificadas. No diseñar reportes antes de estabilizar modelo y consultas.

## Diferencias contra la especificación / modelo propuesto

- Agregar estado de sucursal/artículo/proveedor/pedido/orden/oferta y observaciones exigidas.
- Sustituir categoría única de proveedor por Rubro + ProveedorRubro N:M. Migrar cada categoría distinta sin perder texto original; no asumir que cadenas con comas representan varios rubros.
- Conservar ProveedorArticulo y su precio de catálogo. Precio de Oferta y precio_acordado representan eventos históricos distintos, no duplicación del mismo hecho.
- Oferta obtiene orden por Pedido.id_orden; no duplicar id_orden salvo que una FK compuesta justifique su uso. Pedido no puede reasignarse una vez ofertado. Oferta única por proveedor/pedido bajo regla explícita; si se requieren revisiones, tabla histórica/versiones documentada.
- Conservar proveedor ganador derivado de la oferta elegida, no repetirlo en DetalleAdjudicacion.
- RelacionComercial requiere decidir si es simétrica; propuesta inicial: relación simétrica con par canónico a < b, evitando A/B y B/A. Cotejar documento oficial antes de migrar.
- SubtipoOrden no impide subtipo incompatible ni duplicados por tipo. Exigir Grande sin subtipo y Chica con Urgente/Normal en aplicación y SQL; preservar IDs de catálogos.
- Respaldar correspondencia de detalle con oferta/pedido/orden mediante claves compuestas o triggers set-based; proteger también cambios de entidades padre. Exigir justificación al apartarse del precio seleccionado.
- EvaluacionProveedor ya referencia detalle adjudicado; conservar relación y derivar promedio por oferta/proveedor. Definir unicidad por detalle si se adopta una evaluación por entrega.
- No almacenar totales/promedios derivados. Documentar dependencias funcionales por tabla: PK → atributos; claves candidatas y asociaciones compuestas. Verificar 3FN después del modelo definitivo, no afirmar normalización total solo por tener FK.
- Estados propuestos: pedido PENDIENTE/ASIGNADO/ADJUDICADO/CANCELADO; orden BORRADOR/ABIERTA/ADJUDICADA/CANCELADA; oferta ACTIVA/RETIRADA. Validar transiciones, cierre y reglas de edición con docente/especificación.

## Plan de migración

1. Preservar HEAD y cambio local; crear rama de trabajo al iniciar implementación, sin incluir cambios ajenos en commits propios.
2. Inventariar BD efectiva con consultas de solo lectura; confirmar si existen datos útiles. No ejecutar schema.sql sobre una base existente.
3. Backup antes de DDL; ensayar migración en BD separada. Guardar mapa de IDs y conteos previos/posteriores.
4. Mantener tablas/campos útiles. Migraciones incrementales para estados, rubros y restricciones; detectar huérfanos, duplicados e inconsistencias antes de activar constraints.
5. Migrar categorías a rubros, resolver pares comerciales invertidos y fechas incompatibles con registro de incidencias. No corregir datos silenciosamente.
6. Separar instalación limpia database/01…09 de migraciones versionadas. Conservar schema.sql como referencia inicial mientras dure transición.
7. Seed explícitamente de demostración, separado de datos existentes; 5 sucursales, 10 departamentos, 50 artículos, 20 proveedores, al menos 100 pedidos y flujo con ofertas/adjudicaciones/evaluaciones. Fechas coherentes y escenarios de los ocho reportes.
8. Validar restauración, conteos, FK, CHECK, reglas cruzadas y consultas antes de cambiar aplicación. Commits por unidades revisables.

## Plan de implementación y orden exacto

Cada fase exige evidencia propia y prerrequisitos verificados. Los controles siguientes no autorizan saltarse una fase fallida.

| Fase | Entrega | Criterio para avanzar |
|---|---|---|
| 0 Auditoría | Este diagnóstico + fuentes disponibles | Cotejar materiales pendientes y resolver ejecución de herramientas |
| 1 Arquitectura/modelo | ADR, MER, relacional, dependencias y estados | Matriz de requisitos y compatibilidad del modelo revisadas |
| 2 Base definitiva | 01_create_database,02_tables,03_constraints,04_indexes,05_triggers | Instalación/migración en SQL Server; reglas negativas probadas |
| 3 Datos | 06_seed y 09_test_data | Conteos mínimos, integridad y escenarios de reporte |
| 4 Conexión | Configuración y pool SQL Server | JDBC real y arranque verificados |
| 5 Modelo/DAO | Entidades, DTO/mapper y persistencia | Lectura/escritura reales y recursos cerrados |
| 6 Servicios | Validaciones y transacciones | Reglas positivas/negativas y rollback |
| 7 API | Recursos, códigos HTTP y errores | Contratos REST reales sin exposición interna |
| 8 Autenticación | Login/logout/me y BCrypt | Casos correcto/incorrecto/inactivo y sesión |
| 9 Autorización | Roles/permisos y alcance proveedor | 401/403, CRUD por rol y aislamiento |
| 10 CRUD | Todos los módulos y UI progresiva | Operaciones permitidas y bloqueadas contra BD |
| 11 Adquisiciones | Pedido→orden→oferta→comparativo→adjudicación→evaluación | Flujo integrado, consistencia y concurrencia |
| 12 Dashboard | Métricas desde consultas | Conciliación de conteos/montos y próximos vencimientos |
| 13 Segundo DBMS | PostgreSQL + health/databases | SELECT 1 real en ambos y tiempos medidos |
| 14 Reportes SQL | 07_views,08_report_queries, ocho reportes | Filtros, resultados y conciliación |
| 15 Jasper | JRXML, compilación y exportación | Ocho PDF válidos con parámetros, totales y páginas |
| 16 Frontend final | Diseño sobrio y responsive | UX, DOM seguro, estados y permisos visibles |
| 17 Pruebas | Unitarias, integración y E2E | Navegador→API→servicio→DAO→SQL Server con evidencia |
| 18 Documentación | README, manuales, diccionario y diagramas | Instalación reproducible y recorrido académico |
| 19 Auditoría final | AUDITORIA_FINAL.md | Checklist con evidencia; pendientes visibles |

Aunque las pruebas consolidadas son fase 17, cada fase se verifica al implementarla. CRUD preliminar de adjudicación no permitirá edición/borrado destructivo de historia; se utilizarán acciones de negocio coherentes.

## Comandos y pruebas de esta fase

- rg --files, lecturas Get-Content y búsqueda de rutas/clases/validaciones/renderizado sobre el árbol fuente.
- git status --short; git branch --show-current; git log -7 --oneline; git remote -v; git ls-files; git diff -- articulos.html.
- Get-Command java,mvn,sqlcmd,psql; Get-Service para MSSQL/PostgreSQL/Tomcat; búsqueda acotada de runtime de Eclipse.
- sqlcmd -S localhost -E -C -l 5 con SELECT de sys.databases y @@VERSION: fallo, no se obtuvo catálogo ni versión.
- Copia de fuentes a work/audit-baseline sin db.properties. javac --release 17 -encoding UTF-8 contra cuatro bibliotecas de caché local: código 3, AccessDeniedException al cerrar JAR. Se generaron clases pero no se considera éxito.
- Java del Eclipse -version: 25.0.4.1. Intento MavenCli offline: código 1, mensaje JavaFX; build Maven no verificado.
- Una llamada posterior Git mostró discrepancia de propietario del usuario sandbox; no se cambió safe.directory global.

No se probaron login, CRUD, rollback, Jasper, PDF ni E2E porque no se validó entorno de ejecución. No se alteró esquema ni datos de ninguna base.

## Problemas y próximos pasos

La fase 0 queda con pendientes externos/operativos: obtener materiales Drive/documento oficial, permitir guardar informe en el proyecto fuera de los directorios autorizados y ejecutar Java/Maven/conexión sin restricciones que impiden la verificación. Solicitar únicamente permisos necesarios, sin imprimir secretos. No comenzar DDL o implementación masiva hasta resolver estos prerrequisitos. Esta entrega identifica arquitectura y plan, pero no declara aprobado ningún criterio de aceptación operativo.

