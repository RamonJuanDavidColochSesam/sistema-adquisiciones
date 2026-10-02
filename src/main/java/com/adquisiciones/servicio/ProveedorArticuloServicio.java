package com.adquisiciones.servicio;

import com.adquisiciones.dao.ProveedorArticuloDAO;
import com.adquisiciones.modelo.ProveedorArticulo;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProveedorArticuloServicio {

    // DECIMAL(10,2): 8 dígitos enteros y 2 decimales
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    private final ProveedorArticuloDAO dao = new ProveedorArticuloDAO();

    public List<ProveedorArticulo> listarPorProveedor(int idProveedor) throws SQLException {
        return dao.listarPorProveedor(idProveedor);
    }

    public ProveedorArticulo crear(ProveedorArticulo pa) throws SQLException {
        validar(pa);
        if (pa.getIdArticulo() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un artículo");
        return dao.crear(pa);
    }

    public boolean actualizarPrecio(ProveedorArticulo pa) throws SQLException {
        validar(pa);
        return dao.actualizarPrecio(pa);
    }

    public boolean eliminar(int idProveedor, int idArticulo) throws SQLException {
        return dao.eliminar(idProveedor, idArticulo);
    }

    private void validar(ProveedorArticulo pa) {
        if (pa.getIdProveedor() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un proveedor");
        if (pa.getPrecio() == null)
            throw new IllegalArgumentException("El precio es obligatorio");
        if (pa.getPrecio().signum() <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        if (pa.getPrecio().stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("El precio no puede tener más de 2 decimales");
        if (pa.getPrecio().compareTo(PRECIO_MAXIMO) > 0)
            throw new IllegalArgumentException("El precio no puede ser mayor que 99,999,999.99");
    }
}
