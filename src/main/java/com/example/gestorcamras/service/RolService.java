package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Rol;

import java.util.List;
import java.util.Optional;

public interface RolService {
    List<Rol> obtenerTodos();
    Optional<Rol> obtenerPorId(Long id);
    Rol guardarRol(Rol rol);
    void eliminarRol(Long id);
}
