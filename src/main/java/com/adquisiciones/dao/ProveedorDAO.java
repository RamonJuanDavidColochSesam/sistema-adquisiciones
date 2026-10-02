package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.modelo.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = "SELECT id_proveedor, codigo_proveedor, nombre_comercial, direccion, telefono, categoria FROM Proveedor ORDER BY id_proveedor";
        List<Proveedor> lista = new ArrayList<>();

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Proveedor obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_proveedor, codigo_proveedor, nombre_comercial, direccion, telefono, categoria FROM Proveedor WHERE id_proveedor = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Proveedor crear(Proveedor p) throws SQLException {
        String sql = "INSERT INTO Proveedor (codigo_proveedor, nombre_comercial, direccion, telefono, categoria) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCodigoProveedor());
            ps.setString(2, p.getNombreComercial());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getCategoria());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setIdProveedor(rs.getInt(1));
            }
            return p;
        }
    }

    public boolean actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE Proveedor SET codigo_proveedor = ?, nombre_comercial = ?, direccion = ?, telefono = ?, categoria = ? WHERE id_proveedor = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getCodigoProveedor());
            ps.setString(2, p.getNombreComercial());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getCategoria());
            ps.setInt(6, p.getIdProveedor());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Proveedor WHERE id_proveedor = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setCodigoProveedor(rs.getString("codigo_proveedor"));
        p.setNombreComercial(rs.getString("nombre_comercial"));
        p.setDireccion(rs.getString("direccion"));
        p.setTelefono(rs.getString("telefono"));
        p.setCategoria(rs.getString("categoria"));
        return p;
    }
}
