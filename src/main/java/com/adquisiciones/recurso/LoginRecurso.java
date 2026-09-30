package com.adquisiciones.recurso;

import com.adquisiciones.modelo.Usuario;
import com.adquisiciones.servicio.LoginServicio;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/login")
public class LoginRecurso {

    private final LoginServicio loginServicio = new LoginServicio();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> credenciales, @Context HttpServletRequest request) {
        String nombreUsuario = credenciales.get("nombreUsuario");
        String contrasena = credenciales.get("contrasena");

        if (nombreUsuario == null || contrasena == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Debe enviar nombreUsuario y contrasena"))
                    .build();
        }

        try {
            Usuario usuario = loginServicio.autenticar(nombreUsuario, contrasena);

            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Usuario o contraseña incorrectos"))
                        .build();
            }

            HttpSession sesion = request.getSession(true);
            sesion.setAttribute("idUsuario", usuario.getIdUsuario());
            sesion.setAttribute("rol", usuario.getNombreRol());
            sesion.setAttribute("idRol", usuario.getIdRol());
            sesion.setAttribute("idProveedor", usuario.getIdProveedor());

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("nombreCompleto", usuario.getNombreCompleto());
            respuesta.put("rol", usuario.getNombreRol());

            return Response.ok(respuesta).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
        return Response.ok(Map.of("mensaje", "Sesión cerrada")).build();
    }
}