package com.adquisiciones.recurso;

import com.adquisiciones.modelo.Departamento;
import com.adquisiciones.servicio.DepartamentoServicio;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Map;

@Path("/departamentos")
public class DepartamentoRecurso {

    private final DepartamentoServicio servicio = new DepartamentoServicio();

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
            Departamento d = servicio.obtener(id);
            if (d == null) return notFound();
            return Response.ok(d).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Departamento departamento) {
        try {
            return Response.status(Response.Status.CREATED).entity(servicio.crear(departamento)).build();
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
    public Response actualizar(@PathParam("id") int id, Departamento departamento) {
        try {
            if (departamento == null) throw new IllegalArgumentException("Se requiere un cuerpo JSON");
            departamento.setIdDepartamento(id);
            if (!servicio.actualizar(departamento)) return notFound();
            return Response.ok(departamento).build();
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
            return Response.ok(Map.of("mensaje", "Departamento eliminado")).build();
        } catch (Exception e) {
            return error(e);
        }
    }

    private Response manejarErrorSql(SQLException e) {
        if (e.getMessage() != null && e.getMessage().contains("UQ_Departamento_SucursalNombre")) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Ya existe un departamento con ese nombre en esta sucursal"))
                    .build();
        }
        return error(e);
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Departamento no encontrado")).build();
    }

    private Response badRequest(String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", mensaje)).build();
    }

    private Response error(Exception e) {
        return new ErroresApi().toResponse(e);
    }
}