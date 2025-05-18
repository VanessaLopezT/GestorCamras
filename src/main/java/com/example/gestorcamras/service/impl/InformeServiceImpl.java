package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Informe;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.model.InformeBuilder;
import com.example.gestorcamras.repository.InformeRepository;
import com.example.gestorcamras.service.InformeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InformeServiceImpl implements InformeService {

    @Autowired
    private InformeRepository informeRepository;

    @Autowired
    private com.example.gestorcamras.repository.UsuarioRepository usuarioRepository;

    @Override
    public List<Informe> obtenerTodos() {
        return informeRepository.findAll();
    }

    @Override
    public Optional<Informe> obtenerPorId(Long id) {
        return informeRepository.findById(id);
    }

    @Override
    public Informe guardarInforme(Informe informe) {
        return informeRepository.save(informe);
    }

    @Override
    public void eliminarInforme(Long id) {
        informeRepository.deleteById(id);
    }

    @Override
    public Informe construirInforme(String titulo, String contenido, double tamaño, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + usuarioId));
        InformeBuilder builder = new InformeBuilder()
                .titulo(titulo)
                .fechaGeneracion(java.time.LocalDateTime.now())
                .tamaño(tamaño)
                .contenido(contenido)
                .usuario(usuario);
        return builder.build();
    }
}

