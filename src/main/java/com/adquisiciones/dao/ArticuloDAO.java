package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.modelo.Articulo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticuloDAO {

    public List<Articulo> listarTodos() throws SQLException {
        String sql = "SELECT id_articulo, codigo_articulo, nombre, descripcion FROM Articulo ORDER BY id_articulo";
        List<Articulo> lista = new ArrayList<>();

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Articulo obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_articulo, codigo_articulo, nombre, descripcion FROM Articulo WHERE id_articulo = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Articulo crear(Articulo a) throws SQLException {
        String sql = "INSERT INTO Articulo (codigo_articulo, nombre, descripcion) VALUES (?, ?, ?)";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getCodigoArticulo());
            ps.setString(2, a.getNombre());
            ps.setString(3, a.getDescripcion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setIdArticulo(rs.getInt(1));
            }
            return a;
        }
    }

    public boolean actualizar(Articulo a) throws SQLException {
        String sql = "UPDATE Articulo SET codigo_articulo = ?, nombre = ?, descripcion = ? WHERE id_articulo = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getCodigoArticulo());
            ps.setString(2, a.getNombre());
            ps.setString(3, a.getDescripcion());
            ps.setInt(4, a.getIdArticulo());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Articulo WHERE id_articulo = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Articulo mapear(ResultSet rs) throws SQLException {
        Articulo a = new Articulo();
        a.setIdArticulo(rs.getInt("id_articulo"));
        a.setCodigoArticulo(rs.getString("codigo_articulo"));
        a.setNombre(rs.getString("nombre"));
        a.setDescripcion(rs.getString("descripcion"));
        return a;
    }
}
