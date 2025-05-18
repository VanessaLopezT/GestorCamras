package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.UsuarioRepository;
import com.example.gestorcamras.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Cacheable(value = "usuarios")
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    @Cacheable(value = "usuario", key = "#id")
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @CachePut(value = "usuario", key = "#usuario.idUsuario")
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    @CacheEvict(value = "usuario", key = "#id")
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
