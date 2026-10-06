package com.adquisiciones.recurso;

import com.adquisiciones.modelo.Sucursal;
import com.adquisiciones.servicio.SucursalServicio;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import java.util.Map;

@Path("/sucursales")
public class SucursalRecurso {

    private final SucursalServicio servicio = new SucursalServicio();

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
            Sucursal s = servicio.obtener(id);
            if (s == null) return notFound();
            return Response.ok(s).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Sucursal sucursal) {
        try {
            return Response.status(Response.Status.CREATED).entity(servicio.crear(sucursal)).build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return error(e);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, Sucursal sucursal) {
        try {
            if (sucursal == null) throw new IllegalArgumentException("Se requiere un cuerpo JSON");
            sucursal.setIdSucursal(id);
            if (!servicio.actualizar(sucursal)) return notFound();
            return Response.ok(sucursal).build();
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
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
            return Response.ok(Map.of("mensaje", "Sucursal eliminada")).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Sucursal no encontrada")).build();
    }

    private Response badRequest(String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", mensaje)).build();
    }

    private Response error(Exception e) {
        return new ErroresApi().toResponse(e);
    }
}