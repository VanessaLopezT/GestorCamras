package com.example.gestorcamras.Escritorio.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArchivoMultimediaDTO {
    private Long idArchivo;
    private String nombreArchivo;
    private String rutaArchivo;
    private String tipo;
    private String fechaCaptura;
    private String fechaSubida;
    
    @JsonProperty("camaraId")
    private Long camaraId;
    
    @JsonProperty("equipoId")
    private Long equipoId;

    // Getters and setters
    public Long getIdArchivo() { return idArchivo; }
    public void setIdArchivo(Long idArchivo) { this.idArchivo = idArchivo; }
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getFechaCaptura() { return fechaCaptura; }
    public void setFechaCaptura(String fechaCaptura) { this.fechaCaptura = fechaCaptura; }
    
    public String getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(String fechaSubida) { this.fechaSubida = fechaSubida; }
    
    public Long getCamaraId() { return camaraId; }
    public void setCamaraId(Long camaraId) { this.camaraId = camaraId; }
    
    public Long getEquipoId() { return equipoId; }
    public void setEquipoId(Long equipoId) { this.equipoId = equipoId; }
}
