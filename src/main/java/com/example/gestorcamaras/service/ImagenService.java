package com.example.gestorcamaras.service;

import com.example.gestorcamaras.model.Imagen;
import java.util.List;
import java.util.Optional;

public interface ImagenService {
    List<Imagen> obtenerTodas();
    Optional<Imagen> obtenerPorId(Long id);
    Optional<Imagen> obtenerPorIdYEquipo(Long id, Long equipoId);
    Imagen guardarImagen(Imagen imagen);
    void eliminarImagen(Long id);
    List<Imagen> obtenerPorCamara(Long camaraId);
}
