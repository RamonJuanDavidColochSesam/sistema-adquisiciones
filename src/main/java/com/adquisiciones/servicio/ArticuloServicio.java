package com.adquisiciones.servicio;

import com.adquisiciones.dao.ArticuloDAO;
import com.adquisiciones.modelo.Articulo;

import java.sql.SQLException;
import java.util.List;

public class ArticuloServicio {

    private final ArticuloDAO dao = new ArticuloDAO();

    public List<Articulo> listar() throws SQLException {
        return dao.listarTodos();
    }

    public Articulo obtener(int id) throws SQLException {
        return dao.obtenerPorId(id);
    }

    public Articulo crear(Articulo a) throws SQLException {
        validar(a);
        return dao.crear(a);
    }

    public boolean actualizar(Articulo a) throws SQLException {
        validar(a);
        return dao.actualizar(a);
    }

    public boolean eliminar(int id) throws SQLException {
        return dao.eliminar(id);
    }

    private void validar(Articulo a) {
        if (a.getCodigoArticulo() == null || a.getCodigoArticulo().isBlank())
            throw new IllegalArgumentException("El código de artículo es obligatorio");
        if (a.getCodigoArticulo().length() > 20)
            throw new IllegalArgumentException("El código de artículo no puede exceder 20 caracteres");
        if (a.getNombre() == null || a.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del artículo es obligatorio");
        if (a.getNombre().length() > 100)
            throw new IllegalArgumentException("El nombre del artículo no puede exceder 100 caracteres");
        if (a.getDescripcion() != null && a.getDescripcion().length() > 300)
            throw new IllegalArgumentException("La descripción no puede exceder 300 caracteres");
    }
}
