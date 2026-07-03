package com.example.gestorcamaras.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombre;
    private String correo;
    private LocalDateTime fechaRegistro;
}
