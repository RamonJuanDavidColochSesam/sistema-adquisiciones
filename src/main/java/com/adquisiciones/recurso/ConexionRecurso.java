package com.adquisiciones.recurso;
import com.adquisiciones.conexion.ConexionManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Path("/conexion") @Produces(MediaType.APPLICATION_JSON)
public final class ConexionRecurso {
 private static final java.util.logging.Logger LOG=java.util.logging.Logger.getLogger(ConexionRecurso.class.getName());
 private static final int TIMEOUT_SEGUNDOS=5;
 // Metadatos reales por motor: la prueba viva es SELECT 1 y la segunda consulta entrega versión, base y servidor.
 private record Motor(String clave,String nombre,boolean sqlServer,String consultaMetadatos,String[] columnas){}
 private static final List<Motor> MOTORES=List.of(
  new Motor("sqlServer","Microsoft SQL Server",true,
   "SELECT CAST(SERVERPROPERTY('ProductVersion') AS nvarchar(128)),CAST(SERVERPROPERTY('ProductLevel') AS nvarchar(128)),CAST(SERVERPROPERTY('Edition') AS nvarchar(128)),DB_NAME(),@@SERVERNAME,CONVERT(nvarchar(30),CAST(SYSDATETIME() AS datetime),126)",
   new String[]{"version","nivel","edicion","base","servidor","horaServidor"}),
  new Motor("postgres","PostgreSQL",false,
   "SELECT current_database(),current_setting('server_version'),NULL::text,COALESCE(host(inet_server_addr()),'local'),to_char(now(),'YYYY-MM-DD\"T\"HH24:MI:SS.MS'),version()",
   new String[]{"base","version","edicion","servidor","horaServidor","compilacion"}));
 @GET @Path("/test")
 public Map<String,Object> probarConexiones() {
  Map<String,Object> result=new LinkedHashMap<>();
  List<Map<String,Object>> motores=new ArrayList<>();
  result.put("fecha",LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
  for(Motor motor:MOTORES){
   Map<String,Object> info=new LinkedHashMap<>();
   info.put("clave",motor.clave());info.put("motor",motor.nombre());
   long start=System.nanoTime();boolean ok=false;Integer prueba=null;String error=null;
   try(Connection c=motor.sqlServer()?ConexionManager.getInstancia().getConexionSqlServer():ConexionManager.getInstancia().getConexionPostgres()){
    try(var s=c.prepareStatement("SELECT 1")){s.setQueryTimeout(TIMEOUT_SEGUNDOS);try(var rows=s.executeQuery()){if(rows.next())prueba=rows.getInt(1);}}
    try(var s=c.prepareStatement(motor.consultaMetadatos())){s.setQueryTimeout(TIMEOUT_SEGUNDOS);try(var rows=s.executeQuery()){if(rows.next())for(int i=0;i<motor.columnas().length;i++)info.put(motor.columnas()[i],rows.getString(i+1));}}
    ok=prueba!=null&&prueba==1;
   }catch(Exception e){LOG.warning("Diagnóstico de "+motor.clave()+" falló: "+e.getClass().getSimpleName());error=tipoDeError(e);}
   Map<String,Object> consulta=new LinkedHashMap<>();consulta.put("consulta","SELECT 1");consulta.put("valor",prueba);
   info.put("disponible",ok);info.put("ms",(System.nanoTime()-start)/1000000);info.put("prueba",consulta);info.put("error",error);
   motores.add(info);result.put(motor.clave(),ok);result.put(motor.clave()+"Ms",info.get("ms"));
  }
  result.put("motores",motores);
  return result;
 }
 // Solo tipo de fallo y estado SQL: nunca la cadena de conexión ni credenciales.
 private static String tipoDeError(Exception e){
  if(e instanceof SQLException sq){String estado=sq.getSQLState();return sq.getClass().getSimpleName()+(estado==null?"":" · SQLState "+estado);}
  return e.getClass().getSimpleName();
 }
}
