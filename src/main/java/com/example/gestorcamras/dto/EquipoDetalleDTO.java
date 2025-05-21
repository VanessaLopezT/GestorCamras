package com.example.gestorcamras.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EquipoDetalleDTO {
    private Long idEquipo;
    private String nombreEquipo;
    private String ip;
    private LocalDateTime fechaRegistro;

    private UbicacionDTO ubicacion;
    private List<CamaraDTO> camaras;
}

