package com.example.gestorcamras.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InformeDTO {
    private Long idInfo;
    private String titulo;
    private LocalDateTime fechaGeneracion;
    private double tamaño;
    private String contenido;
    private Long usuarioId; // solo referencia por id para simplificar DTO
}
