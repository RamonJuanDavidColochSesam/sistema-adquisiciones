package com.adquisiciones.recurso;
import com.adquisiciones.servicio.ReportesServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Path("/reportes") @Produces(MediaType.APPLICATION_JSON)
public final class ReportesRecurso {
 private final ReportesServicio service=new ReportesServicio();
 @SuppressWarnings("unchecked") private static Map<String,Object> user(HttpServletRequest r){return (Map<String,Object>)r.getAttribute("usuario");}
 @GET public Map<String,Object> catalogo(){
  String url=System.getenv("GUATECOMPRAS_SSRS_URL");
  return Map.of("informes",ReportesServicio.CATALOGO,"ssrsConfigurado",url!=null&&!url.isBlank(),"ssrsUrl",url==null?"":url);
 }
 @GET @Path("/{id}") public Map<String,Object> informe(@PathParam("id")int id,@QueryParam("articulo")@DefaultValue("0")int article,@QueryParam("proveedor")@DefaultValue("0")int provider,@QueryParam("orden")@DefaultValue("0")int order,@QueryParam("sucursal")@DefaultValue("0")int branch,@QueryParam("anio")int year,@QueryParam("desde")String from,@QueryParam("hasta")String to,@Context HttpServletRequest r)throws SQLException{
  try{return service.ejecutar(id,article,provider,order,branch,year==0?LocalDate.now().getYear():year,from==null?LocalDate.now().minusYears(1):LocalDate.parse(from),to==null?LocalDate.now():LocalDate.parse(to),user(r));}
  catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Use fechas con formato AAAA-MM-DD");}
 }
}
