package com.example.gestorcamras.dto;

import java.time.LocalDateTime;

public class ImagenProcesadaDTO {
    private Long idImgProcesada;
    private String nombre;
    private LocalDateTime fechaProcesamiento;
    private double tamaño;
    private String rutaImagen;
    private Long imagenOriginalId; // solo el id para no cargar toda la entidad
    private Long filtroId;

    // Getters y Setters
    public Long getIdImgProcesada() {
        return idImgProcesada;
    }

    public void setIdImgProcesada(Long idImgProcesada) {
        this.idImgProcesada = idImgProcesada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public double getTamaño() {
        return tamaño;
    }

    public void setTamaño(double tamaño) {
        this.tamaño = tamaño;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public Long getImagenOriginalId() {
        return imagenOriginalId;
    }

    public void setImagenOriginalId(Long imagenOriginalId) {
        this.imagenOriginalId = imagenOriginalId;
    }

    public Long getFiltroId() {
        return filtroId;
    }

    public void setFiltroId(Long filtroId) {
        this.filtroId = filtroId;
    }
}
