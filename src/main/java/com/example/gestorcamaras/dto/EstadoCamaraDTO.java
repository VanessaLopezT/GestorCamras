package com.example.gestorcamaras.dto;

import lombok.Data;

@Data
public class EstadoCamaraDTO {
    private String nombre;
    private String estado; // "OK", "OFFLINE", etc.
}