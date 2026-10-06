package com.adquisiciones.recurso;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.LoggerFactory;

@Provider
public final class ErroresApi implements ExceptionMapper<Exception> {
 @Override public Response toResponse(Exception e) {
  int code=500;String message="No se pudo completar la operación";
  if(e instanceof IllegalArgumentException){code=400;message=e.getMessage();}
  else if(e instanceof WebApplicationException w){code=w.getResponse().getStatus();message=code==404?"Registro no encontrado":code==403?"No tiene permiso para esta operación":"Solicitud no válida";}
  else if(e instanceof SQLException s) {
   if(s.getErrorCode()==2627||s.getErrorCode()==2601||s.getErrorCode()==547){code=409;message="El registro ya existe o está relacionado con otros datos";}
   else if(s.getErrorCode()>=51000&&s.getErrorCode()<51100){code=400;message="Los datos no respetan las reglas de integridad de adquisiciones";}
   else {code=503;message="No se pudo completar la consulta";}
  }
  String reference=UUID.randomUUID().toString();
  if(code>=500)LoggerFactory.getLogger(ErroresApi.class).error("Fallo API referencia={} tipo={}",reference,e.getClass().getSimpleName());
  return Response.status(code).type(MediaType.APPLICATION_JSON).entity(Map.of("error",message==null?"Solicitud no válida":message,"referencia",reference)).build();
 }
}
