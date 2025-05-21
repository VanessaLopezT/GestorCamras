package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Imagen;
import java.util.List;
import java.util.Optional;

public interface ImagenService {
    List<Imagen> obtenerTodas();
    Optional<Imagen> obtenerPorId(Long id);
    Imagen guardarImagen(Imagen imagen);
    void eliminarImagen(Long id);
    List<Imagen> obtenerPorCamara(Long camaraId);

}
