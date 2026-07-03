package com.example.gestorcamaras.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroDTO {
    private Long idFiltro;
    private String tipo;
    private String descripcion;
}
