package com.example.gestorcamras.service;

import java.util.List;
import java.util.Optional;

import com.example.gestorcamras.dto.UsuarioDTO;

public interface UsuarioService {
    List<UsuarioDTO> obtenerTodos();
    Optional<UsuarioDTO> obtenerPorId(Long id);
    Optional<UsuarioDTO> obtenerPorEmail(String email);
    UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO);
    void eliminarUsuario(Long id);
}
