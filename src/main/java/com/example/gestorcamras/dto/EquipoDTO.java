package com.example.gestorcamras.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class EquipoDTO {
    private Long idEquipo;
    private String nombre;
    private String identificador;
    private String ip;
    private Integer puerto;
    private LocalDateTime ultimaConexion;
    private Boolean activo;
    private Set<CamaraDTO> camaras;
}
