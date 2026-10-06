# Inventario de inspección para la documentación

6 de octubre de 2026. Se inspeccionaron fuentes, configuración pública, DDL, migraciones, consultas, RDL, clientes, pruebas y registros. Se consultó Git para el origen y los registros locales para el cierre. No se publicaron credenciales ni archivos de acceso privados.

| Elemento | Encontrado y examinado |
|---|---:|
| Archivos públicos de texto/configuración/documentación inventariados al inicio | 318 |
| Fuentes Java | 49 |
| Declaraciones REST extraídas | 49 |
| Esquemas de CRUD genérico | 17 |
| Tablas de cada diccionario | 22 |
| Diseños RDL fuente | 8 |

Los diccionarios son exportaciones obtenidas previamente mediante JDBC; esta tarea no ejecutó nuevamente metadata SQL. No se encontraron JRXML/JASPER, GitHub Actions, Docusaurus ni configuración GitHub Pages. La configuración del conector Tomcat pertenece al runtime externo; sus valores de demo constan en manuales y registros.

El inventario público registra rutas, tamaños y SHA256. Excluye .git, productos target/bin/obj, configuraciones privadas y accesos demo. Los hashes de los ocho RDL y ocho PDF se comprobaron contra la evidencia de publicación, sin reejecutar SSRS. Los comandos se revisaron frente a los parámetros de sus scripts, sin aplicar DDL ni pruebas con mutaciones. Ver [VALIDACION_DOCUMENTACION.md](VALIDACION_DOCUMENTACION.md).
