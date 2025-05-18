package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Filtro;
import com.example.gestorcamras.repository.FiltroRepository;
import com.example.gestorcamras.service.FiltroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FiltroServiceImpl implements FiltroService {

    @Autowired
    private FiltroRepository filtroRepository;

    @Override
    public List<Filtro> obtenerTodos() {
        return filtroRepository.findAll();
    }

    @Override
    public Optional<Filtro> obtenerPorId(Long id) {
        return filtroRepository.findById(id);
    }

    @Override
    public Filtro guardarFiltro(Filtro filtro) {
        return filtroRepository.save(filtro);
    }

    @Override
    public void eliminarFiltro(Long id) {
        filtroRepository.deleteById(id);
    }
}
