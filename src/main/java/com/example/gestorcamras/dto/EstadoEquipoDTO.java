package com.example.gestorcamras.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EstadoEquipoDTO {
    private String ip;
    private LocalDateTime timestamp;
    private List<EstadoCamaraDTO> estadoCamaras;
}