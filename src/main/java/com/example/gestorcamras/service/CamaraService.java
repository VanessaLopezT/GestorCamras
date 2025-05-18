package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Camara;

import java.util.List;
import java.util.Optional;

public interface CamaraService {
    List<Camara> obtenerTodas();
    Optional<Camara> obtenerPorId(Long id);
    Camara guardarCamara(Camara camara);
    void eliminarCamara(Long id);

    List<Camara> obtenerPorPropietario(Long idUsuario);
    List<Camara> obtenerPorUbicacion(Long idUbicacion);
    List<Camara> obtenerPorActiva(boolean activa);
    List<Camara> obtenerPorTipo(String tipo);
}
