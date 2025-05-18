package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Filtro;

import java.util.List;
import java.util.Optional;

public interface FiltroService {
    List<Filtro> obtenerTodos();
    Optional<Filtro> obtenerPorId(Long id);
    Filtro guardarFiltro(Filtro filtro);
    void eliminarFiltro(Long id);
}
