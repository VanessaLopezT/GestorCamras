package com.example.gestorcamras.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroEquipoRequest {
    private String nombreEquipo;
    private String ip;
    private double latitud;
    private double longitud;
    private String direccion; // opcional

    private List<CamaraDTO> camaras;

}
