package com.adquisiciones.servicio;

import com.adquisiciones.dao.DepartamentoDAO;
import com.adquisiciones.modelo.Departamento;

import java.sql.SQLException;
import java.util.List;

public class DepartamentoServicio {

    private final DepartamentoDAO dao = new DepartamentoDAO();

    public List<Departamento> listar() throws SQLException {
        return dao.listarTodos();
    }

    public Departamento obtener(int id) throws SQLException {
        return dao.obtenerPorId(id);
    }

    public Departamento crear(Departamento d) throws SQLException {
        validar(d);
        return dao.crear(d);
    }

    public boolean actualizar(Departamento d) throws SQLException {
        validar(d);
        return dao.actualizar(d);
    }

    public boolean eliminar(int id) throws SQLException {
        return dao.eliminar(id);
    }

    private void validar(Departamento d) {
        if (d.getIdSucursal() <= 0)
            throw new IllegalArgumentException("Debe seleccionar una sucursal");
        if (d.getNombre() == null || d.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del departamento es obligatorio");
    }
}