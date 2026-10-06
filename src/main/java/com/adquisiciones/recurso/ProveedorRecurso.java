package com.adquisiciones.recurso;

import com.adquisiciones.modelo.Proveedor;
import com.adquisiciones.servicio.ProveedorServicio;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Map;

@Path("/proveedores")
public class ProveedorRecurso {

    // Códigos de error de SQL Server: 2627/2601 = clave duplicada, 547 = conflicto con llave foránea
    private static final int ERROR_CLAVE_DUPLICADA = 2627;
    private static final int ERROR_INDICE_DUPLICADO = 2601;
    private static final int ERROR_LLAVE_FORANEA = 547;

    private final ProveedorServicio servicio = new ProveedorServicio();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar() {
        try {
            return Response.ok(servicio.listar()).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtener(@PathParam("id") int id) {
        try {
            Proveedor p = servicio.obtener(id);
            if (p == null) return notFound();
            return Response.ok(p).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Proveedor proveedor) {
        try {
            return Response.status(Response.Status.CREATED).entity(servicio.crear(proveedor)).build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (SQLException e) {
            return manejarErrorSql(e);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, Proveedor proveedor) {
        try {
            if (proveedor == null) throw new IllegalArgumentException("Se requiere un cuerpo JSON");
            proveedor.setIdProveedor(id);
            if (!servicio.actualizar(proveedor)) return notFound();
            return Response.ok(proveedor).build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (SQLException e) {
            return manejarErrorSql(e);
        } catch (Exception e) {
            return error(e);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("id") int id) {
        try {
            if (!servicio.eliminar(id)) return notFound();
            return Response.ok(Map.of("mensaje", "Proveedor eliminado")).build();
        } catch (SQLException e) {
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "No se puede eliminar el proveedor porque tiene artículos, ofertas o usuarios asociados"))
                        .build();
            }
            return error(e);
        } catch (Exception e) {
            return error(e);
        }
    }

    private Response manejarErrorSql(SQLException e) {
        boolean claveDuplicada = e.getErrorCode() == ERROR_CLAVE_DUPLICADA || e.getErrorCode() == ERROR_INDICE_DUPLICADO;
        if (claveDuplicada && e.getMessage() != null && e.getMessage().contains("'dbo.Proveedor'")) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Ya existe un proveedor con ese código"))
                    .build();
        }
        return error(e);
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Proveedor no encontrado")).build();
    }

    private Response badRequest(String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", mensaje)).build();
    }

    private Response error(Exception e) {
        return new ErroresApi().toResponse(e);
    }
}
