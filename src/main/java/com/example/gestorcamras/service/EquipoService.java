package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.EquipoDTO;

import java.util.List;
import java.util.Optional;

public interface EquipoService {
    List<EquipoDTO> obtenerTodos();
    Optional<EquipoDTO> obtenerPorId(Long id);
    EquipoDTO guardarEquipo(EquipoDTO equipoDTO);
    void eliminarEquipo(Long id);

    // Método extra para búsqueda por nombre
    List<EquipoDTO> buscarPorNombre(String nombreEquipo);}
