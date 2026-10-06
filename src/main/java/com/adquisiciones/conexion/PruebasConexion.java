package com.adquisiciones.conexion;

/** Prueba JDBC con la misma configuración externa de la aplicación. */
public class PruebasConexion {
    public static void main(String[] args) throws Exception {
        try (var connection = ConexionManager.getInstancia().getConexionSqlServer();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT 1")) {
            if (!result.next()) throw new java.sql.SQLException("La consulta de diagnóstico no devolvió filas");
            System.out.println("sqlserver: SELECT 1=" + result.getInt(1));
        } finally {
            ConexionManager.getInstancia().close();
        }
    }
}
