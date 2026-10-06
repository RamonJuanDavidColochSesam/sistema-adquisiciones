package com.adquisiciones.conexion;

/** Prueba JDBC con la misma configuración externa de la aplicación. */
public class PruebaConexion2 {
    public static void main(String[] args) throws Exception {
        try (var connection = ConexionManager.getInstancia().getConexionPostgres();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT 1")) {
            if (!result.next()) throw new java.sql.SQLException("La consulta de diagnóstico no devolvió filas");
            System.out.println("postgres: SELECT 1=" + result.getInt(1));
        } finally {
            ConexionManager.getInstancia().close();
        }
    }
}
