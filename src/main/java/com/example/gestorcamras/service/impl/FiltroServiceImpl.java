package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Filtro;
import com.example.gestorcamras.repository.FiltroRepository;
import com.example.gestorcamras.service.FiltroService;
import com.example.gestorcamras.dto.FiltroDTO;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FiltroServiceImpl implements FiltroService {

    @Autowired
    private FiltroRepository filtroRepository;

    // Métodos de conversión entre entidad y DTO
    private FiltroDTO toDTO(Filtro filtro) {
        if (filtro == null) return null;
        return new FiltroDTO(
            filtro.getIdFiltro(),
            filtro.getTipo(),
            filtro.getDescripcion()
        );
    }

    private Filtro toEntity(FiltroDTO dto) {
        if (dto == null) return null;
        Filtro filtro = new Filtro();
        filtro.setIdFiltro(dto.getIdFiltro());
        filtro.setTipo(dto.getTipo());
        filtro.setDescripcion(dto.getDescripcion());
        return filtro;
    }

    @Override
    public List<FiltroDTO> obtenerTodos() {
        return filtroRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FiltroDTO> obtenerPorId(Long id) {
        return filtroRepository.findById(id).map(this::toDTO);
    }

    @Override
    public FiltroDTO guardarFiltro(FiltroDTO filtroDTO) {
        Filtro filtro = toEntity(filtroDTO);
        Filtro guardado = filtroRepository.save(filtro);
        return toDTO(guardado);
    }

    @Override
    public void eliminarFiltro(Long id) {
        filtroRepository.deleteById(id);
    }
}

