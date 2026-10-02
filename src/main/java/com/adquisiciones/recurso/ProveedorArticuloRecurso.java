package com.adquisiciones.recurso;

import com.adquisiciones.modelo.ProveedorArticulo;
import com.adquisiciones.servicio.ProveedorArticuloServicio;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Map;

@Path("/proveedores/{idProveedor}/articulos")
public class ProveedorArticuloRecurso {

    // Códigos de error de SQL Server: 2627/2601 = clave duplicada, 547 = conflicto con llave foránea
    private static final int ERROR_CLAVE_DUPLICADA = 2627;
    private static final int ERROR_INDICE_DUPLICADO = 2601;
    private static final int ERROR_LLAVE_FORANEA = 547;

    private final ProveedorArticuloServicio servicio = new ProveedorArticuloServicio();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(@PathParam("idProveedor") int idProveedor) {
        try {
            return Response.ok(servicio.listarPorProveedor(idProveedor)).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(@PathParam("idProveedor") int idProveedor, ProveedorArticulo proveedorArticulo) {
        try {
            proveedorArticulo.setIdProveedor(idProveedor);
            return Response.status(Response.Status.CREATED).entity(servicio.crear(proveedorArticulo)).build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (SQLException e) {
            return manejarErrorSql(e);
        } catch (Exception e) {
            return error(e);
        }
    }

    @PUT
    @Path("/{idArticulo}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("idProveedor") int idProveedor,
                               @PathParam("idArticulo") int idArticulo,
                               ProveedorArticulo proveedorArticulo) {
        try {
            proveedorArticulo.setIdProveedor(idProveedor);
            proveedorArticulo.setIdArticulo(idArticulo);
            if (!servicio.actualizarPrecio(proveedorArticulo)) return notFound();
            return Response.ok(proveedorArticulo).build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return error(e);
        }
    }

    @DELETE
    @Path("/{idArticulo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@PathParam("idProveedor") int idProveedor, @PathParam("idArticulo") int idArticulo) {
        try {
            if (!servicio.eliminar(idProveedor, idArticulo)) return notFound();
            return Response.ok(Map.of("mensaje", "Artículo quitado del proveedor")).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    private Response manejarErrorSql(SQLException e) {
        String detalle = e.getMessage() != null ? e.getMessage() : "";
        boolean claveDuplicada = e.getErrorCode() == ERROR_CLAVE_DUPLICADA || e.getErrorCode() == ERROR_INDICE_DUPLICADO;

        if (claveDuplicada && detalle.contains("PK_ProveedorArticulo")) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Este artículo ya está asociado a este proveedor"))
                    .build();
        }
        if (e.getErrorCode() == ERROR_LLAVE_FORANEA && detalle.contains("FK_ProveedorArticulo_Proveedor")) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Proveedor no encontrado"))
                    .build();
        }
        if (e.getErrorCode() == ERROR_LLAVE_FORANEA && detalle.contains("FK_ProveedorArticulo_Articulo")) {
            return badRequest("El artículo seleccionado no existe");
        }
        return error(e);
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "El proveedor no tiene asociado ese artículo")).build();
    }

    private Response badRequest(String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", mensaje)).build();
    }

    private Response error(Exception e) {
        String mensaje = e.getMessage() != null ? e.getMessage() : "Error interno";
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", mensaje)).build();
    }
}
