package com.example.gestorcamras.Escritorio.dto;

import java.time.LocalDateTime;

public class ArchivoMultimediaDTO {
    private Long id;
    private String nombre;
    private String ruta;
    private String tipo; // "foto" o "video"
    private LocalDateTime fechaCaptura;
    private LocalDateTime fechaSubida;
    private Long idCamara;
    private Long idEquipo;

    // Constructor vacío
    public ArchivoMultimediaDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }


    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }


    public LocalDateTime getFechaCaptura() {
        return fechaCaptura;
    }

    public void setFechaCaptura(LocalDateTime fechaCaptura) {
        this.fechaCaptura = fechaCaptura;
    }


    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }


    public Long getIdCamara() {
        return idCamara;
    }

    public void setIdCamara(Long idCamara) {
        this.idCamara = idCamara;
    }


    public Long getIdEquipo() {
        return idEquipo;
    }
    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }


    @Override
    public String toString() {
        return "ArchivoMultimediaDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ruta='" + ruta + '\'' +
                ", tipo='" + tipo + '\'' +
                ", fechaCaptura=" + fechaCaptura +
                ", fechaSubida=" + fechaSubida +
                ", idCamara=" + idCamara +
                ", idEquipo=" + idEquipo +
                '}';
    }
}
