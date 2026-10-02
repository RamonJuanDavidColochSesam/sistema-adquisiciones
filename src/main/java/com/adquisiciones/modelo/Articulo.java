package com.adquisiciones.modelo;

public class Articulo {
    private int idArticulo;
    private String codigoArticulo;
    private String nombre;
    private String descripcion;

    public int getIdArticulo() { return idArticulo; }
    public void setIdArticulo(int idArticulo) { this.idArticulo = idArticulo; }

    public String getCodigoArticulo() { return codigoArticulo; }
    public void setCodigoArticulo(String codigoArticulo) { this.codigoArticulo = codigoArticulo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
