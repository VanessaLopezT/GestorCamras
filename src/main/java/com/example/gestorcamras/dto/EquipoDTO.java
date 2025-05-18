package com.example.gestorcamras.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoDTO {
    private Long idEquipo;
    private String nombreEquipo;
    private String ipAsignada;
    private LocalDateTime fechaRegistro;
    // No incluimos las cámaras para evitar cargas innecesarias en la API.
    // Si quieres, luego puedes hacer un DTO específico con cámaras.
}
