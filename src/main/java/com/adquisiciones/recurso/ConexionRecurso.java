package com.adquisiciones.recurso;
import com.adquisiciones.conexion.ConexionManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.Connection;
import java.util.*;

@Path("/conexion") @Produces(MediaType.APPLICATION_JSON)
public final class ConexionRecurso {
 private static final java.util.logging.Logger LOG=java.util.logging.Logger.getLogger(ConexionRecurso.class.getName());
 @GET @Path("/test")
 public Map<String,Object> probarConexiones() {
  Map<String,Object> result=new LinkedHashMap<>();
  var manager=ConexionManager.getInstancia();
  for(String db:List.of("sqlServer","postgres")){
   long start=System.nanoTime();boolean ok=false;
   try(Connection c=db.equals("sqlServer")?manager.getConexionSqlServer():manager.getConexionPostgres();var s=c.prepareStatement("SELECT 1")) {
    s.setQueryTimeout(5);try(var rows=s.executeQuery()){if(rows.next()){ok=rows.getInt(1)==1;}}
   }catch(Exception e){LOG.warning("Diagnóstico de "+db+" falló: "+e.getClass().getSimpleName());}
   result.put(db,ok);result.put(db+"Ms",(System.nanoTime()-start)/1000000);
  }return result;
 }
}
