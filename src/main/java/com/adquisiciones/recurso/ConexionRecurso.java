package com.adquisiciones.recurso;

import com.adquisiciones.conexion.ConexionManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/conexion")
public class ConexionRecurso {

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> probarConexiones() {
        Map<String, Object> resultado = new LinkedHashMap<>();

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer()) {
            resultado.put("sqlServer", !conn.isClosed());
        } catch (Exception e) {
            resultado.put("sqlServer", false);
            resultado.put("sqlServerError", e.getMessage());
        }

        try (Connection conn = ConexionManager.getInstancia().getConexionPostgres()) {
            resultado.put("postgres", !conn.isClosed());
        } catch (Exception e) {
            resultado.put("postgres", false);
            resultado.put("postgresError", e.getMessage());
        }

        return resultado;
    }
}