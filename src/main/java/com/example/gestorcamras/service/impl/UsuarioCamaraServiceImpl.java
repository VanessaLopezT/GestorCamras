package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.UsuarioCamara;
import com.example.gestorcamras.dto.UsuarioCamaraDTO;
import java.util.stream.Collectors;
import com.example.gestorcamras.model.UsuarioCamaraId;
import com.example.gestorcamras.repository.UsuarioCamaraRepository;
import com.example.gestorcamras.service.UsuarioCamaraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioCamaraServiceImpl implements UsuarioCamaraService {

    private UsuarioCamaraDTO toDTO(UsuarioCamara usuarioCamara) {
        UsuarioCamaraDTO dto = new UsuarioCamaraDTO();
        dto.setUsuarioId(usuarioCamara.getUsuarioId());
        dto.setCamaraId(usuarioCamara.getCamaraId());
        dto.setPermisos(usuarioCamara.getPermisoEspecial());
        return dto;
    }

    private UsuarioCamara toEntity(UsuarioCamaraDTO dto) {
        UsuarioCamara usuarioCamara = new UsuarioCamara();
        usuarioCamara.setUsuarioId(dto.getUsuarioId());
        usuarioCamara.setCamaraId(dto.getCamaraId());
        usuarioCamara.setPermisoEspecial(dto.getPermisos());
        // usuarioCamara.setId(...)
        return usuarioCamara;
    }

    @Autowired
    private UsuarioCamaraRepository usuarioCamaraRepository;

    @Override
    public List<UsuarioCamaraDTO> obtenerTodas() {
        return usuarioCamaraRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioCamaraDTO> obtenerPorId(UsuarioCamaraId id) {
        return usuarioCamaraRepository.findById(id).map(this::toDTO);
    }

    @Override
    public UsuarioCamaraDTO guardar(UsuarioCamaraDTO usuarioCamaraDTO) {
        UsuarioCamara usuarioCamara = toEntity(usuarioCamaraDTO);
        UsuarioCamara guardado = usuarioCamaraRepository.save(usuarioCamara);
        return toDTO(guardado);
    }

    @Override
    public void eliminar(UsuarioCamaraId id) {
        usuarioCamaraRepository.deleteById(id);
    }
}
