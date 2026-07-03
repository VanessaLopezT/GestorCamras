package com.example.gestorcamaras.service;

import com.example.gestorcamaras.dto.UbicacionDTO;

import java.util.List;
import java.util.Optional;

public interface UbicacionService {
    List<UbicacionDTO> obtenerTodas();
    Optional<UbicacionDTO> obtenerPorId(Long id);
    UbicacionDTO guardarUbicacion(UbicacionDTO ubicacionDTO);
    void eliminarUbicacion(Long id);
}
