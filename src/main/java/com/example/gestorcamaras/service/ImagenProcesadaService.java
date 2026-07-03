package com.example.gestorcamaras.service;

import com.example.gestorcamaras.dto.ImagenProcesadaDTO;
import java.util.List;
import java.util.Optional;

public interface ImagenProcesadaService {
    List<ImagenProcesadaDTO> obtenerTodas();
    Optional<ImagenProcesadaDTO> obtenerPorId(Long id);
    ImagenProcesadaDTO guardarImagen(ImagenProcesadaDTO imagenProcesadaDTO);
    void eliminarImagen(Long id);
}
