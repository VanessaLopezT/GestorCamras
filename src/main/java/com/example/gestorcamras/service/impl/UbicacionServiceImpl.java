package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Ubicacion;
import com.example.gestorcamras.repository.UbicacionRepository;
import com.example.gestorcamras.service.UbicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionServiceImpl implements UbicacionService {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Override
    @Cacheable(value = "ubicaciones")
    public List<Ubicacion> obtenerTodas() {
        return ubicacionRepository.findAll();
    }

    @Override
    @Cacheable(value = "ubicacion", key = "#id")
    public Optional<Ubicacion> obtenerPorId(Long id) {
        return ubicacionRepository.findById(id);
    }

    @Override
    @CachePut(value = "ubicacion", key = "#ubicacion.idUbicacion")
    public Ubicacion guardarUbicacion(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    @Override
    @CacheEvict(value = "ubicacion", key = "#id")
    public void eliminarUbicacion(Long id) {
        ubicacionRepository.deleteById(id);
    }
}
