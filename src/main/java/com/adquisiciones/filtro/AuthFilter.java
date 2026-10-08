package com.adquisiciones.filtro;

import com.adquisiciones.dao.PermisoDAO;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthFilter implements Filter {

    private static final String[] RUTAS_PUBLICAS = {
        "/api/login",
        "/api/conexion/test"
    };

    private static final Map<String, String> RUTA_A_PANTALLA = new LinkedHashMap<>();
    static {
        RUTA_A_PANTALLA.put("/api/sucursales", "Sucursales");
        RUTA_A_PANTALLA.put("/api/departamentos", "Departamentos");
        RUTA_A_PANTALLA.put("/api/articulos", "Articulos");
        RUTA_A_PANTALLA.put("/api/proveedores", "Proveedores");
        RUTA_A_PANTALLA.put("/api/pedidos", "Pedidos");
        RUTA_A_PANTALLA.put("/api/ordenes-compra", "OrdenesCompra");
        RUTA_A_PANTALLA.put("/api/ofertas", "Ofertas");
        RUTA_A_PANTALLA.put("/api/adjudicaciones", "Adjudicaciones");
        RUTA_A_PANTALLA.put("/api/reportes", "Reportes");
    }

    private final PermisoDAO permisoDAO = new PermisoDAO();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ruta = request.getRequestURI().substring(request.getContextPath().length());

        for (String publica : RUTAS_PUBLICAS) {
            if (ruta.equals(publica)) {
                chain.doFilter(req, res);
                return;
            }
        }

        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("idUsuario") == null) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");
            return;
        }

        String nombrePantalla = resolverPantalla(ruta);

        if (nombrePantalla != null) {
            int idRol = (int) sesion.getAttribute("idRol");
            String accion = resolverAccion(request.getMethod());

            try {
                if (!permisoDAO.tienePermiso(idRol, nombrePantalla, accion)) {
                    enviarError(response, HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para esta acción");
                    return;
                }
            } catch (Exception e) {
                enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al verificar permisos");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private String resolverPantalla(String ruta) {
        for (Map.Entry<String, String> entry : RUTA_A_PANTALLA.entrySet()) {
            if (ruta.startsWith(entry.getKey())) return entry.getValue();
        }
        return null; // ruta sin pantalla asociada (ej. /api/hello) - no se restringe por permisos
    }

    private String resolverAccion(String metodoHttp) {
        return switch (metodoHttp) {
            case "GET" -> "leer";
            case "POST" -> "crear";
            case "PUT" -> "actualizar";
            case "DELETE" -> "borrar";
            default -> "leer";
        };
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}