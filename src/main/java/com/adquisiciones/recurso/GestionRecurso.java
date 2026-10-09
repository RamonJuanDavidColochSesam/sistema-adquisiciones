package com.adquisiciones.recurso;
import com.adquisiciones.modelo.Modulo;
import com.adquisiciones.servicio.CatalogoServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.util.Map;

@Path("/gestion/{modulo}") @Produces(MediaType.APPLICATION_JSON)
public final class GestionRecurso {
 private final CatalogoServicio service=new CatalogoServicio();
 @SuppressWarnings("unchecked") private static Map<String,Object> user(HttpServletRequest r){return (Map<String,Object>)r.getAttribute("usuario");}
 @GET @Path("/schema") public Modulo schema(@PathParam("modulo")String name){return Modulo.obtener(name);}
 @GET public Map<String,Object> list(@PathParam("modulo")String name,@QueryParam("page")@DefaultValue("1")int page,@QueryParam("pageSize")@DefaultValue("20")int size,@QueryParam("q")String q,@Context HttpServletRequest r)throws SQLException {return service.listar(Modulo.obtener(name),page,size,q,user(r));}
 @GET @Path("/{key}") public Map<String,Object> get(@PathParam("modulo")String name,@PathParam("key")String key,@Context HttpServletRequest r)throws SQLException {return service.obtener(Modulo.obtener(name),key,user(r));}
 @POST @Consumes(MediaType.APPLICATION_JSON) public Response create(@PathParam("modulo")String name,Map<String,Object> body,@Context HttpServletRequest r)throws SQLException {var g=service.guardar(Modulo.obtener(name),null,body,user(r));return Response.status(g.creado()?201:200).entity(g.fila()).build();} // 201 al crear, 200 si el upsert actualizó una fila existente
 @PUT @Path("/{key}") @Consumes(MediaType.APPLICATION_JSON) public Map<String,Object> update(@PathParam("modulo")String name,@PathParam("key")String key,Map<String,Object> body,@Context HttpServletRequest r)throws SQLException {return service.guardar(Modulo.obtener(name),key,body,user(r)).fila();}
 @DELETE @Path("/{key}") public Response delete(@PathParam("modulo")String name,@PathParam("key")String key,@Context HttpServletRequest r)throws SQLException {service.borrar(Modulo.obtener(name),key,user(r));return Response.noContent().build();}
}
