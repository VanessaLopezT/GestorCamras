package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.dto.UsuarioDTO;
import java.util.stream.Collectors;
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

    private UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setCorreo(usuario.getCorreo());
        dto.setContrasena(usuario.getContrasena());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        dto.setRolId(usuario.getRol() != null ? usuario.getRol().getIdRol() : null);
        return dto;
    }

    private Usuario toEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(dto.getIdUsuario());
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasena(dto.getContrasena());
        usuario.setFechaRegistro(dto.getFechaRegistro());
        // El rol debe ser seteado desde el controller o con ayuda de un repositorio
        return usuario;
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Cacheable(value = "usuarios")
    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "usuario", key = "#id")
    public Optional<UsuarioDTO> obtenerPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    @Override
    @CachePut(value = "usuario", key = "#usuario.idUsuario")
    public UsuarioDTO guardarUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = toEntity(usuarioDTO);
        Usuario guardado = usuarioRepository.save(usuario);
        return toDTO(guardado);
    }

    @Override
    @CacheEvict(value = "usuario", key = "#id")
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
