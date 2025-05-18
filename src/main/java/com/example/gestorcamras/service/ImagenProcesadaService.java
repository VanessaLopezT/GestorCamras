package com.example.gestorcamras.service;

import com.example.gestorcamras.model.ImagenProcesada;
import java.util.List;
import java.util.Optional;

public interface ImagenProcesadaService {
    List<ImagenProcesada> obtenerTodas();
    Optional<ImagenProcesada> obtenerPorId(Long id);
    ImagenProcesada guardarImagenProcesada(ImagenProcesada imagenProcesada);
    void eliminarImagenProcesada(Long id);
}
