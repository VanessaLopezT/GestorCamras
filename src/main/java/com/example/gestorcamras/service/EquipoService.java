package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoService {
    List<Equipo> obtenerTodos();
    Optional<Equipo> obtenerPorId(Long id);
    Equipo guardarEquipo(Equipo equipo);
    void eliminarEquipo(Long id);

    // Método extra para búsqueda por nombre
    List<Equipo> buscarPorNombre(String nombreEquipo);}
