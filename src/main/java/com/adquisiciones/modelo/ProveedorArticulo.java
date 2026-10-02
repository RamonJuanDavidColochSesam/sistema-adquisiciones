package com.adquisiciones.modelo;

import java.math.BigDecimal;

public class ProveedorArticulo {
    private int idProveedor;
    private int idArticulo;
    private BigDecimal precio;
    private String codigoArticulo; // solo para mostrar en listados, viene del JOIN
    private String nombreArticulo; // solo para mostrar en listados, viene del JOIN

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public int getIdArticulo() { return idArticulo; }
    public void setIdArticulo(int idArticulo) { this.idArticulo = idArticulo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getCodigoArticulo() { return codigoArticulo; }
    public void setCodigoArticulo(String codigoArticulo) { this.codigoArticulo = codigoArticulo; }

    public String getNombreArticulo() { return nombreArticulo; }
    public void setNombreArticulo(String nombreArticulo) { this.nombreArticulo = nombreArticulo; }
}
