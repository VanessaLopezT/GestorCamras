package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.UsuarioDTO;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<UsuarioDTO> obtenerTodos();
    Optional<UsuarioDTO> obtenerPorId(Long id);
    UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO);
    void eliminarUsuario(Long id);
}
