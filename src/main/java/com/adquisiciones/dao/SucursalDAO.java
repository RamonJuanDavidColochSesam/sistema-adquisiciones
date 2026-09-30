package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.modelo.Sucursal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SucursalDAO {

    public List<Sucursal> listarTodas() throws SQLException {
        String sql = "SELECT id_sucursal, codigo_sucursal, direccion, ciudad, region, telefono FROM Sucursal ORDER BY id_sucursal";
        List<Sucursal> lista = new ArrayList<>();

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Sucursal obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_sucursal, codigo_sucursal, direccion, ciudad, region, telefono FROM Sucursal WHERE id_sucursal = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Sucursal crear(Sucursal s) throws SQLException {
        String sql = "INSERT INTO Sucursal (codigo_sucursal, direccion, ciudad, region, telefono) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getCodigoSucursal());
            ps.setString(2, s.getDireccion());
            ps.setString(3, s.getCiudad());
            ps.setString(4, s.getRegion());
            ps.setString(5, s.getTelefono());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setIdSucursal(rs.getInt(1));
            }
            return s;
        }
    }

    public boolean actualizar(Sucursal s) throws SQLException {
        String sql = "UPDATE Sucursal SET codigo_sucursal = ?, direccion = ?, ciudad = ?, region = ?, telefono = ? WHERE id_sucursal = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getCodigoSucursal());
            ps.setString(2, s.getDireccion());
            ps.setString(3, s.getCiudad());
            ps.setString(4, s.getRegion());
            ps.setString(5, s.getTelefono());
            ps.setInt(6, s.getIdSucursal());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Sucursal WHERE id_sucursal = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Sucursal mapear(ResultSet rs) throws SQLException {
        Sucursal s = new Sucursal();
        s.setIdSucursal(rs.getInt("id_sucursal"));
        s.setCodigoSucursal(rs.getString("codigo_sucursal"));
        s.setDireccion(rs.getString("direccion"));
        s.setCiudad(rs.getString("ciudad"));
        s.setRegion(rs.getString("region"));
        s.setTelefono(rs.getString("telefono"));
        return s;
    }
}