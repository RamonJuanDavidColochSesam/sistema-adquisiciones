package com.adquisiciones.servicio;

import com.adquisiciones.dao.UsuarioDAO;
import com.adquisiciones.modelo.Usuario;
import com.adquisiciones.util.PasswordUtil;

import java.sql.SQLException;

public class LoginServicio {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario autenticar(String nombreUsuario, String contrasena) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(nombreUsuario);

        if (usuario == null) return null;
        if (!"Activo".equals(usuario.getEstado())) return null;
        if (!PasswordUtil.verificar(contrasena, usuario.getContrasenaHash())) return null;

        return usuario;
    }
}