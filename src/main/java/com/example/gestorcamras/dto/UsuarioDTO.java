package com.example.gestorcamras.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioDTO {
    private Long idUsuario;
    private String nombre;
    private String correo;
    private String contrasena; // Puedes omitir en respuestas o cifrar
    private LocalDateTime fechaRegistro;
    private Long rolId;
}
