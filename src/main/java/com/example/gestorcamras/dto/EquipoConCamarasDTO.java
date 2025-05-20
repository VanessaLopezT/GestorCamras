package com.example.gestorcamras.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EquipoConCamarasDTO {
    private Long idEquipo;
    private String nombreEquipo;
    private String ipAsignada;
    private LocalDateTime fechaRegistro;
    private List<CamaraDTO> camaras;
}
