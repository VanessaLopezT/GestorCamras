package com.example.gestorcamras.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ArchivoMultimediaDTO {
    private Long idArchivo;
    private String nombreArchivo;
    private String rutaArchivo;
    private String tipo;
    private LocalDateTime fechaCaptura;
    private LocalDateTime fechaSubida;
    private Long camaraId;
    private Long equipoId;
} 