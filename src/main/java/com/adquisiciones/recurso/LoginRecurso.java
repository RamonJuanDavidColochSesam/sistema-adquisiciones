package com.adquisiciones.recurso;
import com.adquisiciones.modelo.Usuario;
import com.adquisiciones.servicio.LoginServicio;
import com.adquisiciones.servicio.SeguridadServicio;
import jakarta.servlet.http.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/login") @Produces(MediaType.APPLICATION_JSON)
public final class LoginRecurso {
 private static final Logger LOGGER=LoggerFactory.getLogger(LoginRecurso.class);
 private final LoginServicio loginServicio=new LoginServicio();
 private static final Map<String,Attempt> ATTEMPTS=new ConcurrentHashMap<>();
 private record Attempt(long started,int count){}
 @POST @Consumes(MediaType.APPLICATION_JSON)
 public Response login(Map<String,String> credentials,@Context HttpServletRequest request) {
  if(credentials==null)return bad("Debe enviar las credenciales");
  String username=credentials.get("nombreUsuario"),password=credentials.get("contrasena");
  if(username==null||username.isBlank()||username.length()>30||password==null||password.length()>128)return bad("Credenciales no válidas");
  String ip=request.getRemoteAddr();long now=System.currentTimeMillis();
  ATTEMPTS.entrySet().removeIf(e->now-e.getValue().started()>60000);
  Attempt attempt=ATTEMPTS.compute(ip,(k,a)->a==null?new Attempt(now,1):new Attempt(a.started(),a.count()+1));
  if(attempt.count()>10||ATTEMPTS.size()>10000)return Response.status(429).header("Retry-After","60").entity(Map.of("error","Demasiados intentos; espere un minuto")).build();
  try {
   Usuario user=loginServicio.autenticar(username,password);
   if(user==null)return Response.status(401).entity(Map.of("error","Usuario o contraseña incorrectos")).build();
   var previous=request.getSession(false);if(previous!=null)previous.invalidate();
   var session=request.getSession(true);session.setMaxInactiveInterval(1800);
   session.setAttribute("idUsuario",user.getIdUsuario());session.setAttribute("idRol",user.getIdRol());session.setAttribute("rol",user.getNombreRol());session.setAttribute("idProveedor",user.getIdProveedor());
   session.setAttribute("csrf",UUID.randomUUID().toString());ATTEMPTS.remove(ip);
   return me(request);
  }catch(IllegalStateException e){
   LOGGER.error("Base de datos no disponible al autenticar a '{}'", username, e);
   return Response.status(503).entity(Map.of("error", e.getMessage()==null?"Configuración de base de datos no disponible":e.getMessage())).build();
  }catch(Exception e){
   LOGGER.error("Error inesperado al autenticar a '{}'", username, e);
   return Response.status(503).entity(Map.of("error","No se pudo iniciar sesión")).build();
  }
 }
 @GET @Path("/me")
 public Response me(@Context HttpServletRequest request)throws java.sql.SQLException {
  var session=request.getSession(false);
  if(session==null||!(session.getAttribute("idUsuario") instanceof Integer id))return Response.status(401).build();
  var security=new SeguridadServicio();var user=security.usuarioActivo(id);
  if(user==null)return Response.status(401).build();
  user.put("csrf",session.getAttribute("csrf"));user.put("permisos",security.permisos((Integer)user.get("idRol")));
  return Response.ok(user).build();
 }
 @POST @Path("/logout")
 public Response logout(@Context HttpServletRequest request){var session=request.getSession(false);if(session!=null)session.invalidate();return Response.ok(Map.of("mensaje","Sesión cerrada")).build();}
 private static Response bad(String message){return Response.status(400).entity(Map.of("error",message)).build();}
}
