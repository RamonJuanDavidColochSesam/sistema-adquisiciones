package com.adquisiciones.dao;

import com.adquisiciones.conexion.ConexionManager;
import com.adquisiciones.modelo.Departamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoDAO {

    public List<Departamento> listarTodos() throws SQLException {
        String sql = "SELECT d.id_departamento, d.id_sucursal, d.nombre, d.descripcion, s.codigo_sucursal " +
                     "FROM Departamento d " +
                     "INNER JOIN Sucursal s ON s.id_sucursal = d.id_sucursal " +
                     "ORDER BY d.id_departamento";
        List<Departamento> lista = new ArrayList<>();

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Departamento obtenerPorId(int id) throws SQLException {
        String sql = "SELECT d.id_departamento, d.id_sucursal, d.nombre, d.descripcion, s.codigo_sucursal " +
                     "FROM Departamento d " +
                     "INNER JOIN Sucursal s ON s.id_sucursal = d.id_sucursal " +
                     "WHERE d.id_departamento = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Departamento crear(Departamento d) throws SQLException {
        String sql = "INSERT INTO Departamento (id_sucursal, nombre, descripcion) VALUES (?, ?, ?)";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getIdSucursal());
            ps.setString(2, d.getNombre());
            ps.setString(3, d.getDescripcion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.setIdDepartamento(rs.getInt(1));
            }
            return d;
        }
    }

    public boolean actualizar(Departamento d) throws SQLException {
        String sql = "UPDATE Departamento SET id_sucursal = ?, nombre = ?, descripcion = ? WHERE id_departamento = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, d.getIdSucursal());
            ps.setString(2, d.getNombre());
            ps.setString(3, d.getDescripcion());
            ps.setInt(4, d.getIdDepartamento());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Departamento WHERE id_departamento = ?";

        try (Connection conn = ConexionManager.getInstancia().getConexionSqlServer();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Departamento mapear(ResultSet rs) throws SQLException {
        Departamento d = new Departamento();
        d.setIdDepartamento(rs.getInt("id_departamento"));
        d.setIdSucursal(rs.getInt("id_sucursal"));
        d.setNombre(rs.getString("nombre"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setCodigoSucursal(rs.getString("codigo_sucursal"));
        return d;
    }
}