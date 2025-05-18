package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.ImagenProcesada;
import com.example.gestorcamras.repository.ImagenProcesadaRepository;
import com.example.gestorcamras.service.ImagenProcesadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImagenProcesadaServiceImpl implements ImagenProcesadaService {

    @Autowired
    private ImagenProcesadaRepository imagenProcesadaRepository;

    @Override
    public List<ImagenProcesada> obtenerTodas() {
        return imagenProcesadaRepository.findAll();
    }

    @Override
    @Cacheable(value = "imgProcesadaCache", key = "#id")
    public Optional<ImagenProcesada> obtenerPorId(Long id) {
        return imagenProcesadaRepository.findById(id);
    }

    @Override
    @CacheEvict(value = "imgProcesadaCache", key = "#imagenProcesada.idImgProcesada")
    public ImagenProcesada guardarImagenProcesada(ImagenProcesada imagenProcesada) {
        return imagenProcesadaRepository.save(imagenProcesada);
    }

    @Override
    @CacheEvict(value = "imgProcesadaCache", key = "#id")
    public void eliminarImagenProcesada(Long id) {
        imagenProcesadaRepository.deleteById(id);
    }
}
