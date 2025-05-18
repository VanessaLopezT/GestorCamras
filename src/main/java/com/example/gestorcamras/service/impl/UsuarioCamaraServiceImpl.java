package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.UsuarioCamara;
import com.example.gestorcamras.model.UsuarioCamaraId;
import com.example.gestorcamras.repository.UsuarioCamaraRepository;
import com.example.gestorcamras.service.UsuarioCamaraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioCamaraServiceImpl implements UsuarioCamaraService {

    @Autowired
    private UsuarioCamaraRepository usuarioCamaraRepository;

    @Override
    public List<UsuarioCamara> obtenerTodas() {
        return usuarioCamaraRepository.findAll();
    }

    @Override
    public Optional<UsuarioCamara> obtenerPorId(UsuarioCamaraId id) {
        return usuarioCamaraRepository.findById(id);
    }

    @Override
    public UsuarioCamara guardar(UsuarioCamara usuarioCamara) {
        return usuarioCamaraRepository.save(usuarioCamara);
    }

    @Override
    public void eliminar(UsuarioCamaraId id) {
        usuarioCamaraRepository.deleteById(id);
    }
}
