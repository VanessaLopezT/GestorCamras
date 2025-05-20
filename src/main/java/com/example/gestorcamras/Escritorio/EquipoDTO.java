package com.example.gestorcamras.Escritorio;

public class EquipoDTO {
    private Long idEquipo;
    private String nombreEquipo;
    private String ipAsignada;

    // constructor
    public EquipoDTO(Long idEquipo, String nombreEquipo, String ipAsignada) {
        this.idEquipo = idEquipo;
        this.nombreEquipo = nombreEquipo;
        this.ipAsignada = ipAsignada;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public String getIpAsignada() {
        return ipAsignada;
    }

    @Override
    public String toString() {
        return nombreEquipo + " (" + ipAsignada + ")";
    }
}