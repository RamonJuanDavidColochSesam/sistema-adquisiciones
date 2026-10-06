# Arquitectura general

```mermaid
flowchart LR
 W[Portal HTML/CSS/JS] --> API[Jersey REST /api]
 D[WPF MVVM / HttpClient] --> API
 API --> F[AuthFilter: sesión, permiso, CSRF y alcance]
 F --> S[Servicios de catálogos y adquisiciones]
 S --> DAO[DAO / PreparedStatement]
 DAO --> H[ConexionManager / HikariCP]
 H --> SQL[(SQL Server operativo)]
 H --> PG[(PostgreSQL: modelo y verificación)]
 R[SSRS / RDL] --> SQL
```

Se conservaron modelos, DAO, servicios, recursos y páginas originales de los cuatro catálogos. Se añadieron módulos sobre esquemas explícitos (`Modulo`), DAO parametrizado, servicios y recursos separados. No se aceptan nombres de tablas/columnas del cliente. Los CRUD heredados mantienen camelCase; los nuevos recursos `/gestion` usan nombres de columnas y `_key` para claves compuestas.

[Volver a Arquitectura](README.md)
