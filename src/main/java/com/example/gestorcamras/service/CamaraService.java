package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.CamaraDTO;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface CamaraService {
    List<CamaraDTO> obtenerTodas();
    Optional<CamaraDTO> obtenerPorId(Long id);
    CamaraDTO guardarCamara(CamaraDTO camaraDTO);
    void eliminarCamara(Long id);
    Camara toEntity(CamaraDTO dto);
    Optional<Camara> obtenerPorNombreYEquipo(String nombre, Equipo equipo);

    List<CamaraDTO> obtenerPorPropietario(Long idUsuario);
    List<CamaraDTO> obtenerPorUbicacion(Long idUbicacion);
    List<CamaraDTO> obtenerPorActiva(boolean activa);
    List<CamaraDTO> obtenerPorTipo(String tipo);
    List<CamaraDTO> obtenerPorEquipo(Long idEquipo);
    
    /**
     * Obtiene todas las cámaras de un equipo específico
     * @param equipoId ID del equipo
     * @return Lista de cámaras del equipo
     */
    List<Camara> obtenerCamarasPorEquipo(Long equipoId);
}
