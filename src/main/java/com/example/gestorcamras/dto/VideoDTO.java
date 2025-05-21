package com.example.gestorcamras.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VideoDTO {
    private Long idVideo;
    private String nombre;
    private double tamano;
    private LocalDateTime fechaCaptura;
    private Duration duracion;
    private String rutaAlmacenamiento;
    private Long camaraId;
}
