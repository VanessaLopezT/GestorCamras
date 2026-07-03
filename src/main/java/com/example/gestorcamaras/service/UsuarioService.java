package com.example.gestorcamaras.service;

import java.util.List;
import java.util.Optional;

import com.example.gestorcamaras.dto.UsuarioDTO;

public interface UsuarioService {
    List<UsuarioDTO> obtenerTodos();
    Optional<UsuarioDTO> obtenerPorId(Long id);
    Optional<UsuarioDTO> obtenerPorEmail(String email);
    UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO);
    void eliminarUsuario(Long id);
}
