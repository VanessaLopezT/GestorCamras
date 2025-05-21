package com.example.gestorcamras.service;


import com.example.gestorcamras.dto.RolDTO;

import java.util.List;
import java.util.Optional;

public interface RolService {
    List<RolDTO> obtenerTodos();
    Optional<RolDTO> obtenerPorId(Long id);
    RolDTO guardarRol(RolDTO rolDTO);
    void eliminarRol(Long id);
}
