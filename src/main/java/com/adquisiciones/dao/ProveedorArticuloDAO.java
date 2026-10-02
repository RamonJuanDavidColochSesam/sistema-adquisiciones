package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.modelo.ProveedorArticulo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorArticuloDAO {

    public List<ProveedorArticulo> listarPorProveedor(int idProveedor) throws SQLException {
        String sql = "SELECT pa.id_proveedor, pa.id_articulo, pa.precio, a.codigo_articulo, a.nombre " +
                     "FROM ProveedorArticulo pa " +
                     "INNER JOIN Articulo a ON a.id_articulo = pa.id_articulo " +
                     "WHERE pa.id_proveedor = ? " +
                     "ORDER BY a.codigo_articulo";
        List<ProveedorArticulo> lista = new ArrayList<>();

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public ProveedorArticulo crear(ProveedorArticulo pa) throws SQLException {
        String sql = "INSERT INTO ProveedorArticulo (id_proveedor, id_articulo, precio) VALUES (?, ?, ?)";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pa.getIdProveedor());
            ps.setInt(2, pa.getIdArticulo());
            ps.setBigDecimal(3, pa.getPrecio());
            ps.executeUpdate();
            return pa;
        }
    }

    public boolean actualizarPrecio(ProveedorArticulo pa) throws SQLException {
        String sql = "UPDATE ProveedorArticulo SET precio = ? WHERE id_proveedor = ? AND id_articulo = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, pa.getPrecio());
            ps.setInt(2, pa.getIdProveedor());
            ps.setInt(3, pa.getIdArticulo());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idProveedor, int idArticulo) throws SQLException {
        String sql = "DELETE FROM ProveedorArticulo WHERE id_proveedor = ? AND id_articulo = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            ps.setInt(2, idArticulo);
            return ps.executeUpdate() > 0;
        }
    }

    private ProveedorArticulo mapear(ResultSet rs) throws SQLException {
        ProveedorArticulo pa = new ProveedorArticulo();
        pa.setIdProveedor(rs.getInt("id_proveedor"));
        pa.setIdArticulo(rs.getInt("id_articulo"));
        pa.setPrecio(rs.getBigDecimal("precio"));
        pa.setCodigoArticulo(rs.getString("codigo_articulo"));
        pa.setNombreArticulo(rs.getString("nombre"));
        return pa;
    }
}
