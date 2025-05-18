package com.example.gestorcamras.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.redis.IRedisCache;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.service.EquipoService;
import com.example.gestorcamras.dto.EquipoDTO;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private IRedisCache<Equipo> redisCache;

    private static final String PREFIX_CACHE = "equipo_";

    // Métodos de conversión entre entidad y DTO
    private EquipoDTO toDTO(Equipo equipo) {
        if (equipo == null) return null;
        return new EquipoDTO(
            equipo.getIdEquipo(),
            equipo.getNombreEquipo(),
            equipo.getIpAsignada(),
            equipo.getFechaRegistro()
        );
    }

    private Equipo toEntity(EquipoDTO dto) {
        if (dto == null) return null;
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(dto.getIdEquipo());
        equipo.setNombreEquipo(dto.getNombreEquipo());
        equipo.setIpAsignada(dto.getIpAsignada());
        equipo.setFechaRegistro(dto.getFechaRegistro());
        return equipo;
    }

    @Override
    public List<EquipoDTO> obtenerTodos() {
        return equipoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EquipoDTO> obtenerPorId(Long id) {
        Optional<Equipo> cacheado = redisCache.obtener(PREFIX_CACHE + id);
        if (cacheado.isPresent()) {
            return cacheado.map(this::toDTO);
        }
        Optional<Equipo> equipoBD = equipoRepository.findById(id);
        equipoBD.ifPresent(equipo -> redisCache.guardar(PREFIX_CACHE + id, equipo));
        return equipoBD.map(this::toDTO);
    }

    @Override
    public EquipoDTO guardarEquipo(EquipoDTO equipoDTO) {
        Equipo equipo = toEntity(equipoDTO);
        Equipo guardado = equipoRepository.save(equipo);
        redisCache.guardar(PREFIX_CACHE + guardado.getIdEquipo(), guardado);
        return toDTO(guardado);
    }

    @Override
    public void eliminarEquipo(Long id) {
        equipoRepository.deleteById(id);
        redisCache.eliminar(PREFIX_CACHE + id);
    }

    @Override
    public List<EquipoDTO> buscarPorNombre(String nombreEquipo) {
        return equipoRepository.findByNombreEquipoContainingIgnoreCase(nombreEquipo)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
}

