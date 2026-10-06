package com.adquisiciones.recurso;
import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.dao.SqlDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.util.Map;

@Path("/auditoria") @Produces(MediaType.APPLICATION_JSON)
public final class AuditoriaRecurso {
 @GET public Map<String,Object> list(@QueryParam("page")@DefaultValue("1")int page)throws SQLException {
  try(var c=ConexionManager.getInstancia().getConexionSqlServer()){return Map.of("items",SqlDAO.query(c,"SELECT a.id_auditoria,u.nombre_usuario,a.accion,a.entidad,a.referencia,a.fecha FROM Auditoria a LEFT JOIN Usuario u ON u.id_usuario=a.id_usuario ORDER BY a.id_auditoria DESC OFFSET ? ROWS FETCH NEXT 50 ROWS ONLY",(Math.max(1,page)-1)*50),"total",SqlDAO.scalar(c,"SELECT COUNT(*) FROM Auditoria"));}
 }
}
