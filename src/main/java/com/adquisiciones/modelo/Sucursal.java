package com.adquisiciones.modelo;

public class Sucursal {
    private int idSucursal;
    private String codigoSucursal;
    private String direccion;
    private String ciudad;
    private String region;
    private String telefono;

    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }

    public String getCodigoSucursal() { return codigoSucursal; }
    public void setCodigoSucursal(String codigoSucursal) { this.codigoSucursal = codigoSucursal; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}