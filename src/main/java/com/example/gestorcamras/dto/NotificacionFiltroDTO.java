package com.example.gestorcamras.dto;

public class NotificacionFiltroDTO {
    private Long idArchivo;
    private Long idEquipo;
    private Long idCamara;
    private String nombreFiltro;
    private String nombreArchivoOriginal;
    
    // Getters y Setters
    public Long getIdArchivo() {
        return idArchivo;
    }
    
    public void setIdArchivo(Long idArchivo) {
        this.idArchivo = idArchivo;
    }
    
    public Long getIdEquipo() {
        return idEquipo;
    }
    
    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }
    
    public Long getIdCamara() {
        return idCamara;
    }
    
    public void setIdCamara(Long idCamara) {
        this.idCamara = idCamara;
    }
    
    public String getNombreFiltro() {
        return nombreFiltro;
    }
    
    public void setNombreFiltro(String nombreFiltro) {
        this.nombreFiltro = nombreFiltro;
    }
    
    public String getNombreArchivoOriginal() {
        return nombreArchivoOriginal;
    }
    
    public void setNombreArchivoOriginal(String nombreArchivoOriginal) {
        this.nombreArchivoOriginal = nombreArchivoOriginal;
    }
    
    @Override
    public String toString() {
        return "NotificacionFiltroDTO{" +
                "idArchivo=" + idArchivo +
                ", idEquipo=" + idEquipo +
                ", idCamara=" + idCamara +
                ", nombreFiltro='" + nombreFiltro + '\'' +
                ", nombreArchivoOriginal='" + nombreArchivoOriginal + '\'' +
                '}';
    }
}
