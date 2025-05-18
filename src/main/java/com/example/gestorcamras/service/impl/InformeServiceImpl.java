package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Informe;
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
}
