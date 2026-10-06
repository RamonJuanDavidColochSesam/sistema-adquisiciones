package com.adquisiciones.recurso;
import com.adquisiciones.servicio.ReportesServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.util.Map;

@Path("/dashboard") @Produces(MediaType.APPLICATION_JSON)
public final class DashboardRecurso {
 @GET @SuppressWarnings("unchecked") public Map<String,Object> dashboard(@Context HttpServletRequest r)throws SQLException{return new ReportesServicio().dashboard((Map<String,Object>)r.getAttribute("usuario"));}
}
