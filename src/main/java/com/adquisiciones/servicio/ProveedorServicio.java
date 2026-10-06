package com.adquisiciones.servicio;

import com.adquisiciones.dao.ProveedorDAO;
import com.adquisiciones.modelo.Proveedor;

import java.sql.SQLException;
import java.util.List;

public class ProveedorServicio {

    private final ProveedorDAO dao = new ProveedorDAO();

    public List<Proveedor> listar() throws SQLException {
        return dao.listarTodos();
    }

    public Proveedor obtener(int id) throws SQLException {
        return dao.obtenerPorId(id);
    }

    public Proveedor crear(Proveedor p) throws SQLException {
        validar(p);
        return dao.crear(p);
    }

    public boolean actualizar(Proveedor p) throws SQLException {
        validar(p);
        return dao.actualizar(p);
    }

    public boolean eliminar(int id) throws SQLException {
        return dao.eliminar(id);
    }

    private void validar(Proveedor p) {
        if (p == null) throw new IllegalArgumentException("Debe enviar los datos del registro");
        if (p.getCodigoProveedor() == null || p.getCodigoProveedor().isBlank())
            throw new IllegalArgumentException("El código de proveedor es obligatorio");
        if (p.getCodigoProveedor().length() > 20)
            throw new IllegalArgumentException("El código de proveedor no puede exceder 20 caracteres");
        if (p.getNombreComercial() == null || p.getNombreComercial().isBlank())
            throw new IllegalArgumentException("El nombre comercial es obligatorio");
        if (p.getNombreComercial().length() > 100)
            throw new IllegalArgumentException("El nombre comercial no puede exceder 100 caracteres");
        if (p.getDireccion() == null || p.getDireccion().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria");
        if (p.getDireccion().length() > 150)
            throw new IllegalArgumentException("La dirección no puede exceder 150 caracteres");
        if (p.getTelefono() == null || p.getTelefono().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio");
        if (p.getTelefono().length() > 20)
            throw new IllegalArgumentException("El teléfono no puede exceder 20 caracteres");
        if (p.getCategoria() == null || p.getCategoria().isBlank())
            throw new IllegalArgumentException("La categoría es obligatoria");
        if (p.getCategoria().length() > 80)
            throw new IllegalArgumentException("La categoría no puede exceder 80 caracteres");
    }
}
