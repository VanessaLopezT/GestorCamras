package com.example.gestorcamaras.pool;

import com.example.gestorcamaras.dto.FiltroDTO;

import java.util.List;
import java.util.Optional;

public interface FiltroService {
    List<FiltroDTO> obtenerTodos();
    Optional<FiltroDTO> obtenerPorId(Long id);
    FiltroDTO guardarFiltro(FiltroDTO filtroDTO);
    void eliminarFiltro(Long id);
}
