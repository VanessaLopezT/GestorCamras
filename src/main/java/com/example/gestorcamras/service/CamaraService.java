package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.CamaraDTO;

import java.util.List;
import java.util.Optional;

public interface CamaraService {
    List<CamaraDTO> obtenerTodas();
    Optional<CamaraDTO> obtenerPorId(Long id);
    CamaraDTO guardarCamara(CamaraDTO camaraDTO);
    void eliminarCamara(Long id);

    List<CamaraDTO> obtenerPorPropietario(Long idUsuario);
    List<CamaraDTO> obtenerPorUbicacion(Long idUbicacion);
    List<CamaraDTO> obtenerPorActiva(boolean activa);
    List<CamaraDTO> obtenerPorTipo(String tipo);
}
