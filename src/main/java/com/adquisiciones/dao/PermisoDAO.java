package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;

import java.sql.*;

public class PermisoDAO {

    public boolean tienePermiso(int idRol, String nombrePantalla, String accion) throws SQLException {
        String columna = switch (accion) {
            case "crear" -> "permite_crear";
            case "leer" -> "permite_leer";
            case "actualizar" -> "permite_actualizar";
            case "borrar" -> "permite_borrar";
            default -> throw new IllegalArgumentException("Acción desconocida: " + accion);
        };

        String sql = "SELECT p." + columna + " AS permiso " +
                     "FROM Permiso p " +
                     "INNER JOIN Pantalla pa ON pa.id_pantalla = p.id_pantalla " +
                     "WHERE p.id_rol = ? AND pa.nombre_pantalla = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ps.setString(2, nombrePantalla);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("permiso");
                return false; // sin fila en Permiso = sin acceso
            }
        }
    }
}