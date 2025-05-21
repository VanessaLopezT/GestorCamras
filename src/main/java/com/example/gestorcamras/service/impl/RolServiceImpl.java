package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Rol;
import com.example.gestorcamras.repository.RolRepository;
import com.example.gestorcamras.service.RolService;
import com.example.gestorcamras.dto.RolDTO;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    @Autowired
    private RolRepository rolRepository;

    // Conversión de entidad a DTO
    private RolDTO toDTO(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombre(rol.getNombre());
        dto.setPermisos(rol.getPermisos());
        if (rol.getUsuarios() != null) {
            dto.setUsuarioIds(rol.getUsuarios().stream().map(u -> u.getIdUsuario()).collect(Collectors.toList()));
        }
        return dto;
    }

    // Conversión de DTO a entidad
    private Rol toEntity(RolDTO dto) {
        Rol rol = new Rol();
        rol.setIdRol(dto.getIdRol());
        rol.setNombre(dto.getNombre());
        rol.setPermisos(dto.getPermisos());
        // No asignamos usuarios aquí
        return rol;
    }

    @Override
    public List<RolDTO> obtenerTodos() {
        return rolRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<RolDTO> obtenerPorId(Long id) {
        return rolRepository.findById(id).map(this::toDTO);
    }

    @Override
    public RolDTO guardarRol(RolDTO rolDTO) {
        Rol rol = toEntity(rolDTO);
        Rol guardado = rolRepository.save(rol);
        return toDTO(guardado);
    }

    @Override
    public void eliminarRol(Long id) {
        rolRepository.deleteById(id);
    }
}

