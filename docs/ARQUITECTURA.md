# Arquitectura de GuateCompras

Complemento de los capítulos 2,5 y 6 de [la guía](GUIA_COMPLETA_DESARROLLO.md).

## Responsabilidades y llamadas

| Capa | Recibe | Devuelve | Llamadores reales |
|---|---|---|---|
| modelo | Valores/identificadores; Modulo define metadatos fijos | POJO o schema de campos | Recursos, servicios, DAO y serialización |
| recurso | HTTP, query/path params y JSON | Response o objeto serializable | Jersey en Tomcat |
| filtro | ServletRequest y HttpSession | Petición autorizada o error | Contenedor antes del recurso |
| servicio | Entidad/Map y, en nuevos flujos, identidad vigente | Fila, lista o resultado de negocio | Recursos |
| DAO | Parámetros, entidad o conexión compartida | Clave, conteo, fila o lista | Servicios y algunas consultas directas |
| conexión | Motor y configuración externa | Connection prestada | DAO/servicios/utilidades |
| clientes | Acción del usuario y JSON | Formularios, tablas, enlaces | Navegador o WPF |

Los servicios económicos reciben una Connection dentro de transaction; SqlDAO opera sobre esa misma conexión. Abrir una conexión nueva por cada detalle rompería la atomicidad porque cada sesión JDBC tendría su propia transacción. El objeto modelo no establece una conexión ni verifica un permiso por sí mismo.

## Clases Java reales por paquete

### com.adquisiciones.conexion

[`src/main/java/com/adquisiciones/conexion/AplicacionLifecycle.java`](../src/main/java/com/adquisiciones/conexion/AplicacionLifecycle.java), [`src/main/java/com/adquisiciones/conexion/ConexionManager.java`](../src/main/java/com/adquisiciones/conexion/ConexionManager.java), [`src/main/java/com/adquisiciones/conexion/PruebaConexion2.java`](../src/main/java/com/adquisiciones/conexion/PruebaConexion2.java), [`src/main/java/com/adquisiciones/conexion/PruebasConexion.java`](../src/main/java/com/adquisiciones/conexion/PruebasConexion.java).

### com.adquisiciones.dao

[`src/main/java/com/adquisiciones/dao/ArticuloDAO.java`](../src/main/java/com/adquisiciones/dao/ArticuloDAO.java), [`src/main/java/com/adquisiciones/dao/DepartamentoDAO.java`](../src/main/java/com/adquisiciones/dao/DepartamentoDAO.java), [`src/main/java/com/adquisiciones/dao/ProveedorArticuloDAO.java`](../src/main/java/com/adquisiciones/dao/ProveedorArticuloDAO.java), [`src/main/java/com/adquisiciones/dao/ProveedorDAO.java`](../src/main/java/com/adquisiciones/dao/ProveedorDAO.java), [`src/main/java/com/adquisiciones/dao/SqlDAO.java`](../src/main/java/com/adquisiciones/dao/SqlDAO.java), [`src/main/java/com/adquisiciones/dao/SucursalDAO.java`](../src/main/java/com/adquisiciones/dao/SucursalDAO.java), [`src/main/java/com/adquisiciones/dao/UsuarioDAO.java`](../src/main/java/com/adquisiciones/dao/UsuarioDAO.java).

### com.adquisiciones.filtro

[`src/main/java/com/adquisiciones/filtro/AuthFilter.java`](../src/main/java/com/adquisiciones/filtro/AuthFilter.java).

### com.adquisiciones.modelo

[`src/main/java/com/adquisiciones/modelo/Articulo.java`](../src/main/java/com/adquisiciones/modelo/Articulo.java), [`src/main/java/com/adquisiciones/modelo/Departamento.java`](../src/main/java/com/adquisiciones/modelo/Departamento.java), [`src/main/java/com/adquisiciones/modelo/Modulo.java`](../src/main/java/com/adquisiciones/modelo/Modulo.java), [`src/main/java/com/adquisiciones/modelo/Proveedor.java`](../src/main/java/com/adquisiciones/modelo/Proveedor.java), [`src/main/java/com/adquisiciones/modelo/ProveedorArticulo.java`](../src/main/java/com/adquisiciones/modelo/ProveedorArticulo.java), [`src/main/java/com/adquisiciones/modelo/Sucursal.java`](../src/main/java/com/adquisiciones/modelo/Sucursal.java), [`src/main/java/com/adquisiciones/modelo/Usuario.java`](../src/main/java/com/adquisiciones/modelo/Usuario.java).

### com.adquisiciones.recurso

[`src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java`](../src/main/java/com/adquisiciones/recurso/AdjudicacionRecurso.java), [`src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ArticuloRecurso.java), [`src/main/java/com/adquisiciones/recurso/AuditoriaRecurso.java`](../src/main/java/com/adquisiciones/recurso/AuditoriaRecurso.java), [`src/main/java/com/adquisiciones/recurso/ConexionRecurso.java`](../src/main/java/com/adquisiciones/recurso/ConexionRecurso.java), [`src/main/java/com/adquisiciones/recurso/DashboardRecurso.java`](../src/main/java/com/adquisiciones/recurso/DashboardRecurso.java), [`src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java`](../src/main/java/com/adquisiciones/recurso/DepartamentoRecurso.java), [`src/main/java/com/adquisiciones/recurso/ErroresApi.java`](../src/main/java/com/adquisiciones/recurso/ErroresApi.java), [`src/main/java/com/adquisiciones/recurso/GestionRecurso.java`](../src/main/java/com/adquisiciones/recurso/GestionRecurso.java), [`src/main/java/com/adquisiciones/recurso/HelloRecurso.java`](../src/main/java/com/adquisiciones/recurso/HelloRecurso.java), [`src/main/java/com/adquisiciones/recurso/LoginRecurso.java`](../src/main/java/com/adquisiciones/recurso/LoginRecurso.java), [`src/main/java/com/adquisiciones/recurso/OrdenRecurso.java`](../src/main/java/com/adquisiciones/recurso/OrdenRecurso.java), [`src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorArticuloRecurso.java), [`src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java`](../src/main/java/com/adquisiciones/recurso/ProveedorRecurso.java), [`src/main/java/com/adquisiciones/recurso/ReportesRecurso.java`](../src/main/java/com/adquisiciones/recurso/ReportesRecurso.java), [`src/main/java/com/adquisiciones/recurso/SucursalRecurso.java`](../src/main/java/com/adquisiciones/recurso/SucursalRecurso.java).

### com.adquisiciones.servicio

[`src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java`](../src/main/java/com/adquisiciones/servicio/AdquisicionServicio.java), [`src/main/java/com/adquisiciones/servicio/ArticuloServicio.java`](../src/main/java/com/adquisiciones/servicio/ArticuloServicio.java), [`src/main/java/com/adquisiciones/servicio/CatalogoServicio.java`](../src/main/java/com/adquisiciones/servicio/CatalogoServicio.java), [`src/main/java/com/adquisiciones/servicio/DepartamentoServicio.java`](../src/main/java/com/adquisiciones/servicio/DepartamentoServicio.java), [`src/main/java/com/adquisiciones/servicio/LoginServicio.java`](../src/main/java/com/adquisiciones/servicio/LoginServicio.java), [`src/main/java/com/adquisiciones/servicio/ProveedorArticuloServicio.java`](../src/main/java/com/adquisiciones/servicio/ProveedorArticuloServicio.java), [`src/main/java/com/adquisiciones/servicio/ProveedorServicio.java`](../src/main/java/com/adquisiciones/servicio/ProveedorServicio.java), [`src/main/java/com/adquisiciones/servicio/ReportesServicio.java`](../src/main/java/com/adquisiciones/servicio/ReportesServicio.java), [`src/main/java/com/adquisiciones/servicio/SeguridadServicio.java`](../src/main/java/com/adquisiciones/servicio/SeguridadServicio.java), [`src/main/java/com/adquisiciones/servicio/SucursalServicio.java`](../src/main/java/com/adquisiciones/servicio/SucursalServicio.java).

### com.adquisiciones.util

[`src/main/java/com/adquisiciones/util/GenerarHash.java`](../src/main/java/com/adquisiciones/util/GenerarHash.java), [`src/main/java/com/adquisiciones/util/MigrarBases.java`](../src/main/java/com/adquisiciones/util/MigrarBases.java), [`src/main/java/com/adquisiciones/util/PasswordUtil.java`](../src/main/java/com/adquisiciones/util/PasswordUtil.java), [`src/main/java/com/adquisiciones/util/SemillaDemo.java`](../src/main/java/com/adquisiciones/util/SemillaDemo.java), [`src/main/java/com/adquisiciones/util/VerificarBases.java`](../src/main/java/com/adquisiciones/util/VerificarBases.java).

## Dependencias que deben explicarse honestamente

Hay DAO específicos para seis recorridos originales y un SqlDAO común; no 49 servicios ni 22 DAO. Los recursos suelen instanciar servicios directamente. El HK2 declarado proporciona infraestructura Jersey, pero el código no usa un contenedor de inyección para todas sus clases de negocio. CatalogoServicio/AdquisicionServicio contienen SQL y referencias JAX-RS: la frontera de capas es práctica, no libre de dependencias de transporte.

AuthFilter usa un mapa de módulos/pantallas. GestionRecurso devuelve el schema estático y los dos clientes usan ese contrato; la interfaz no descubre tablas JDBC ni permite ejecutar SQL. La ruta HelloRecurso permanece, pero el mapa del filtro no autoriza hello; se documenta como código de origen, no como health público.

Los diagramas generales representan SQL Server como motor operativo y PostgreSQL como destino de utilidades/diagnóstico. SSRS ejecuta RDL por otro proceso y autenticación; ReportesRecurso solo comunica URL y catálogo. Ver [BACKEND](BACKEND.md), [SEGURIDAD](SEGURIDAD.md) y [REPORTES](REPORTES.md).
