package com.example.gestorcamaras.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EstadoEquipoDTO {
    private LocalDateTime timestamp;
    private Boolean activo;
    private String estado;
    private String mensaje;
}