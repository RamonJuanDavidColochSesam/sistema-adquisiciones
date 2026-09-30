package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena_hash, u.nombre_completo, " +
                     "       u.email, u.id_rol, r.nombre_rol, u.id_proveedor, u.estado " +
                     "FROM Usuario u " +
                     "INNER JOIN Rol r ON r.id_rol = u.id_rol " +
                     "WHERE u.nombre_usuario = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setContrasenaHash(rs.getString("contrasena_hash"));
                    u.setNombreCompleto(rs.getString("nombre_completo"));
                    u.setEmail(rs.getString("email"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setNombreRol(rs.getString("nombre_rol"));
                    int idProveedor = rs.getInt("id_proveedor");
                    u.setIdProveedor(rs.wasNull() ? null : idProveedor);
                    u.setEstado(rs.getString("estado"));
                    return u;
                }
                return null;
            }
        }
    }
}