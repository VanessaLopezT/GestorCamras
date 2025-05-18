package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> obtenerTodos();
    Optional<Usuario> obtenerPorId(Long id);
    Usuario guardarUsuario(Usuario usuario);
    void eliminarUsuario(Long id);
}
