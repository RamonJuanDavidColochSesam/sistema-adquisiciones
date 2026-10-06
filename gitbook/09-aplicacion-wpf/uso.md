# Inicio, navegación y funciones

1. Compilar en Visual Studio o build_desktop.ps1.
2. Iniciar EXE Release e introducir URL de API/credenciales locales.
3. Elegir módulo permitido, filtrar/paginar y operar CRUD.
4. Usar flujo de orden/adjudicación; importes con punto decimal.
5. Consultar informes/CSV y abrir SSRS solo con URL/permisos efectivos.

`ApiClient` mantiene CookieContainer y CSRF; `MainViewModel` controla módulos, consultas y tablas; `EditorViewModel` controla formularios de catálogo; `WorkflowViewModel` prepara órdenes y adjudicaciones. Las ventanas enlazan datos y muestran errores. La vista de reportes consulta la misma API y exporta CSV; SSRS se abre por URL con autorización Windows independiente. El modo `--self-test` usa estos clientes/viewmodels contra DB real y renderiza el dashboard a PNG. No equivale a una prueba manual de cada interacción de ventana.

![Dashboard](../assets/evidencias/cierre/wpf-dashboard.png)

[Volver a Aplicación WPF](README.md)
