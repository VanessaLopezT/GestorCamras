package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.FiltroDTO;

import java.util.List;
import java.util.Optional;

public interface FiltroService {
    List<FiltroDTO> obtenerTodos();
    Optional<FiltroDTO> obtenerPorId(Long id);
    FiltroDTO guardarFiltro(FiltroDTO filtroDTO);
    void eliminarFiltro(Long id);
}
