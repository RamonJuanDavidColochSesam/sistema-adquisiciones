# Autorización

BCrypt coste 12 en usuarios nuevos/semilla, contraseñas no expuestas en DTO. Sesión renovada al autenticar, cookie HttpOnly y seguimiento exclusivamente por cookie. Roles y estado se vuelven a comprobar en DB para cada solicitud; permisos por pantalla/método y denegación por defecto. Mutaciones exigen `X-CSRF-Token`. Login limita intentos por IP. AdminProveedor obtiene filtro obligatorio por su ID incluso en consultas y reportes. La autorización depende del servidor, no del menú.

Validaciones de campo/tipo/longitud, claves inmutables, cuentas protegidas, fechas, cantidades y precio. Mapper central devuelve JSON: 400 validación, 401 sin sesión, 403 denegación, 404 inexistente, 409 restricciones, 503 SQL no disponible, 500 otros errores con referencia. No envía SQL, stack traces ni credenciales. Auditoría SQL Server guarda su fecha en UTC; las fechas de pedido/oferta/resolución son DATE locales. Frontend construye DOM con textContent y eventos; las páginas anteriores se endurecieron sin eliminar funcionalidades. CSV neutraliza fórmulas al exportar.

El alcance comprobado es una demo local. No se certificaron TLS, pruebas de penetración exhaustivas, alta disponibilidad ni recuperación ante desastre. Para despliegue en red adaptar HTTPS, cookies Secure y cuentas SQL con mínimos privilegios. La sesión de API no autentica Windows ante SSRS: deben configurarse permisos separados.

[Volver a Seguridad](README.md)
