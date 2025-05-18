package com.example.gestorcamras.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.redis.IRedisCache;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.service.EquipoService;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private IRedisCache<Equipo> redisCache;

    private static final String PREFIX_CACHE = "equipo_";

    @Override
    public List<Equipo> obtenerTodos() {
        return equipoRepository.findAll();
    }

    @Override
    public Optional<Equipo> obtenerPorId(Long id) {
        Optional<Equipo> cacheado = redisCache.obtener(PREFIX_CACHE + id);
        if (cacheado.isPresent()) {
            return cacheado;
        }
        Optional<Equipo> equipoBD = equipoRepository.findById(id);
        equipoBD.ifPresent(equipo -> redisCache.guardar(PREFIX_CACHE + id, equipo));
        return equipoBD;
    }

    @Override
    public Equipo guardarEquipo(Equipo equipo) {
        Equipo guardado = equipoRepository.save(equipo);
        redisCache.guardar(PREFIX_CACHE + guardado.getIdEquipo(), guardado);
        return guardado;
    }

    @Override
    public void eliminarEquipo(Long id) {
        equipoRepository.deleteById(id);
        redisCache.eliminar(PREFIX_CACHE + id);
    }

    @Override
    public List<Equipo> buscarPorNombre(String nombreEquipo) {
        return equipoRepository.findByNombreEquipoContainingIgnoreCase(nombreEquipo);
    }

}
