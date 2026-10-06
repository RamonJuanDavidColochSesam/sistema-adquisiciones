# WPF

`ApiClient` mantiene CookieContainer y CSRF; `MainViewModel` controla módulos, consultas y tablas; `EditorViewModel` controla formularios de catálogo; `WorkflowViewModel` prepara órdenes y adjudicaciones. Las ventanas enlazan datos y muestran errores. La vista de reportes consulta la misma API y exporta CSV; SSRS se abre por URL con autorización Windows independiente. El modo `--self-test` usa estos clientes/viewmodels contra DB real y renderiza el dashboard a PNG. No equivale a una prueba manual de cada interacción de ventana.

[Volver a Arquitectura](README.md)
