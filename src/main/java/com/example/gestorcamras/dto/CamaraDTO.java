package com.example.gestorcamras.dto;

import java.time.LocalDateTime;

public class CamaraDTO {
    private Long idCamara;
    private String nombre;
    private String ip;
    private boolean activa;
    private String tipo;
    private LocalDateTime fechaRegistro;

    private Long ubicacionId;
    private Long propietarioId;
    private Long equipoId;

    // Getters y Setters
    public Long getIdCamara() { return idCamara; }
    public void setIdCamara(Long idCamara) { this.idCamara = idCamara; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Long getUbicacionId() { return ubicacionId; }
    public void setUbicacionId(Long ubicacionId) { this.ubicacionId = ubicacionId; }

    public Long getPropietarioId() { return propietarioId; }
    public void setPropietarioId(Long propietarioId) { this.propietarioId = propietarioId; }

    public Long getEquipoId() { return equipoId; }
    public void setEquipoId(Long equipoId) { this.equipoId = equipoId; }
}
