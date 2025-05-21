package com.example.gestorcamras.dto;

import lombok.Data;

@Data
public class EstadoCamaraDTO {
    private String nombre;
    private String estado; // "OK", "OFFLINE", etc.
}