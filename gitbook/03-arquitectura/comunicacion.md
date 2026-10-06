# Comunicación entre componentes

Web usa fetch; WPF HttpClient/JsonNode con CookieContainer. Login devuelve permisos/CSRF y las mutaciones envían X-CSRF-Token. SSRS usa identidad Windows independiente; el cliente no recibe credenciales JDBC.

```mermaid
sequenceDiagram
 participant C as Web o WPF
 participant A as API
 participant S as SQL Server
 C->>A: Login
 A->>S: Usuario activo y permisos
 A-->>C: Sesión y CSRF
 C->>A: Mutación con cookie y CSRF
 A->>S: Validar y confirmar transacción
 S-->>A: Commit o rollback
 A-->>C: Datos o error JSON
```

[Volver a Arquitectura](README.md)
