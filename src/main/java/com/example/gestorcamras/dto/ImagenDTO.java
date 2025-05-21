package com.example.gestorcamras.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenDTO {
    private Long idImagen;
    private String nombre;
    private double tamaño;
    private LocalDateTime fechaCaptura;
    private String rutaAlmacenamiento;
    private Long camaraId;  // solo el id de la cámara para simplificar
}
