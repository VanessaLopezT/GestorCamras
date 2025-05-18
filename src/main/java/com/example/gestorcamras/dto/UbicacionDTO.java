package com.example.gestorcamras.dto;

import java.util.List;

public class UbicacionDTO {
    private Long id;
    private double latitud;
    private double longitud;
    private String direccion;
    private List<Long> camaraIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public List<Long> getCamaraIds() { return camaraIds; }
    public void setCamaraIds(List<Long> camaraIds) { this.camaraIds = camaraIds; }
}
