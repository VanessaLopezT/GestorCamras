package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public interface UbicacionService {
    List<Ubicacion> obtenerTodas();
    Optional<Ubicacion> obtenerPorId(Long id);
    Ubicacion guardarUbicacion(Ubicacion ubicacion);
    void eliminarUbicacion(Long id);
}
