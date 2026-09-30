package com.adquisiciones.modelo;

public class Departamento {
    private int idDepartamento;
    private int idSucursal;
    private String nombre;
    private String descripcion;
    private String codigoSucursal; // solo para mostrar en listados, viene del JOIN

    public int getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(int idDepartamento) { this.idDepartamento = idDepartamento; }

    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCodigoSucursal() { return codigoSucursal; }
    public void setCodigoSucursal(String codigoSucursal) { this.codigoSucursal = codigoSucursal; }
}