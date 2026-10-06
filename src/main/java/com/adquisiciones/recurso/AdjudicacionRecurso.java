package com.adquisiciones.recurso;
import com.adquisiciones.servicio.AdquisicionServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.util.Map;

@Path("/adjudicaciones") @Produces(MediaType.APPLICATION_JSON)
public final class AdjudicacionRecurso {
 private final AdquisicionServicio service=new AdquisicionServicio();
 @SuppressWarnings("unchecked") private static Map<String,Object> user(HttpServletRequest r){return (Map<String,Object>)r.getAttribute("usuario");}
 @GET public Map<String,Object> list()throws SQLException{return service.adjudicaciones();}
 @GET @Path("/{id}") public Map<String,Object> get(@PathParam("id")int id)throws SQLException{return service.adjudicacion(id);}
 @POST @Consumes(MediaType.APPLICATION_JSON) public Response create(Map<String,Object> body,@Context HttpServletRequest r)throws SQLException{return Response.status(201).entity(service.guardarAdjudicacion(null,body,user(r))).build();}
 @PUT @Path("/{id}") @Consumes(MediaType.APPLICATION_JSON) public Map<String,Object> update(@PathParam("id")int id,Map<String,Object> body,@Context HttpServletRequest r)throws SQLException{return service.guardarAdjudicacion(id,body,user(r));}
 @DELETE @Path("/{id}") public Response delete(@PathParam("id")int id,@Context HttpServletRequest r)throws SQLException{service.borrarAdjudicacion(id,user(r));return Response.noContent().build();}
}
