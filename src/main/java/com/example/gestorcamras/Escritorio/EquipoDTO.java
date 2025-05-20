package com.example.gestorcamras.Escritorio;

public class EquipoDTO {
    private Long idEquipo;
    private String nombreEquipo;
    private String ip;

    // constructor
    public EquipoDTO(Long idEquipo, String nombreEquipo, String ip) {
        this.idEquipo = idEquipo;
        this.nombreEquipo = nombreEquipo;
        this.ip = ip;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return nombreEquipo + " (" + ip + ")";
    }
}