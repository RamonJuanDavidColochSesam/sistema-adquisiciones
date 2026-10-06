# Seguridad: controles y límites comprobados

Complemento de capítulos 12 y 25. Fuentes: [AuthFilter](../src/main/java/com/adquisiciones/filtro/AuthFilter.java), [LoginRecurso](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java), [SeguridadServicio](../src/main/java/com/adquisiciones/servicio/SeguridadServicio.java) y [web.xml](../src/main/webapp/WEB-INF/web.xml).

## Identidad y sesión

UsuarioDAO lee hash/rol/estado con un parámetro de nombre. LoginServicio exige Activo y verifica BCrypt. LoginRecurso invalida la sesión anterior, crea HttpSession y guarda IDs/rol/proveedor/UUID csrf. La identidad se conserva con cookie JSESSIONID HttpOnly y timeout 30 minutos; no hay JWT. me devuelve datos públicos/permisos/CSRF; POST logout exige token e invalida la sesión.

SeguridadServicio relee usuario vigente y permiso por pantalla/método. AuthFilter reconoce módulos estáticos, incluido gestion, y niega los desconocidos. GET/HEAD corresponde a leer, POST a crear, PUT/PATCH a actualizar y DELETE a borrar. La política no crea métodos HTTP que ningún recurso declare. Cambiar permisos/estado afecta peticiones posteriores aunque el menú siga en caché.

## Matriz insertada por la semilla

| Rol | Lectura | Escritura | Alcance |
|---|---|---|---|
| AdminSistema | Todas las 20 pantallas | CRUD según endpoints existentes | Administración; reglas de integridad aún se aplican |
| GestorCompras | Todas las 20 pantallas | Pedidos, órdenes, ofertas, adjudicaciones y evaluaciones | Sin editar catálogos administrativos por la matriz inicial |
| AdminProveedor | Artículos, catálogo, órdenes, pedidos, ofertas, dashboard y reportes | Catálogo y ofertas | Propiedad propia; artículos activos/pedidos permitidos; informes limitados |
| Auditor | Todas las 20 pantallas | Ninguna | Consulta institucional |

SemillaDemo inserta un permiso si no existía, no sobrescribe permisos previamente modificados. La autorización efectiva está en las filas actuales, no en botones ni un rol enviado por JSON. El scope de catálogo/ofertas usa idProveedor vigente; las consultas de órdenes/reportes aplican filtros particulares. Se protege cuenta propia, último administrador y roles académicos.

## Contraseñas y secretos

Usuario contiene contrasena_hash. Su getter tiene JsonIgnore y el catálogo no selecciona esa columna. CatalogoServicio y SemillaDemo generan BCrypt con coste 12; PasswordUtil.hashear conserva gensalt() sin parámetro explícito. Alta genérica exige 12–72 bytes UTF-8, mientras login tiene su propio límite de entrada. No hay MFA ni recuperación de clave por correo.

JDBC se configura por archivo externo/variables y db.properties se excluye del WAR. .gitignore excluye local-db.properties, demo-access.properties y .env. La hoja privada demo contiene claves iniciales locales y no es recurso público; aquí no se publica ningún valor de acceso o hash de usuario.

## Datos, SQL y errores

PreparedStatement enlaza valores y Modulo limita los identificadores SQL. Validaciones de servicio y FK/CHECK/UNIQUE/triggers se complementan. Las transacciones mantienen auditoría/operación en los recorridos genéricos y compras; no se certifica auditoría idéntica para todas las rutas originales. Los errores se sanitizan; el mapper añade UUID de referencia cuando se usa, pero login/filtro tienen algunas respuestas propias.

Los clientes usan textContent o tablas de datos; CSV neutraliza fórmulas. Esos controles no acreditan ausencia de toda vulnerabilidad. Las pruebas reales de rechazo están en [http.txt](evidencias/cierre/http.txt) y [test_security](../tests/integration/test_security.py).

## Límites honestos

- Demo HTTP loopback; Secure/SameSite no fijados explícitamente en web.xml y no se certificó TLS productivo.
- Login limita intentos por IP/memoria del proceso, más de 10 en 60 segundos; no es rate limit distribuido ni control exhaustivo de abuso.
- SSRS autentica Windows y administra permisos propios. Ocultar el botón no reemplaza su política de acceso.
- No hay pentest integral, comprobación documentada de todos los GRANT efectivos ni alta disponibilidad certificada.
- Los contratos originales y genéricos tienen validaciones/auditoría diferentes; esta documentación reconoce la coexistencia sin refactorizar.
