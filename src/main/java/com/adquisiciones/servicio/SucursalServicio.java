package com.adquisiciones.servicio;

import com.adquisiciones.dao.SucursalDAO;
import com.adquisiciones.modelo.Sucursal;

import java.sql.SQLException;
import java.util.List;

public class SucursalServicio {

    private final SucursalDAO dao = new SucursalDAO();

    public List<Sucursal> listar() throws SQLException {
        return dao.listarTodas();
    }

    public Sucursal obtener(int id) throws SQLException {
        return dao.obtenerPorId(id);
    }

    public Sucursal crear(Sucursal s) throws SQLException {
        validar(s);
        return dao.crear(s);
    }

    public boolean actualizar(Sucursal s) throws SQLException {
        validar(s);
        return dao.actualizar(s);
    }

    public boolean eliminar(int id) throws SQLException {
        return dao.eliminar(id);
    }

    private void validar(Sucursal s) {
        if (s.getCodigoSucursal() == null || s.getCodigoSucursal().isBlank())
            throw new IllegalArgumentException("El código de sucursal es obligatorio");
        if (s.getDireccion() == null || s.getDireccion().isBlank())
            throw new IllegalArgumentException("La dirección es obligatoria");
        if (s.getCiudad() == null || s.getCiudad().isBlank())
            throw new IllegalArgumentException("La ciudad es obligatoria");
        if (s.getRegion() == null || s.getRegion().isBlank())
            throw new IllegalArgumentException("La región es obligatoria");
        if (s.getTelefono() == null || s.getTelefono().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio");
    }
}