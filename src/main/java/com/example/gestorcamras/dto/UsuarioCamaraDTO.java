package com.example.gestorcamras.dto;

import lombok.Data;

@Data
public class UsuarioCamaraDTO {
    private Long usuarioId;
    private Long camaraId;
    private String permisos; // O cualquier otro campo relevante
}
