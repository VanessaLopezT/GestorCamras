package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Imagen;
import com.example.gestorcamras.repository.ImagenRepository;
import com.example.gestorcamras.service.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImagenServiceImpl implements ImagenService {

    @Autowired
    private ImagenRepository imagenRepository;

    @Override
    public List<Imagen> obtenerTodas() {
        return imagenRepository.findAll();
    }

    @Override
    public Optional<Imagen> obtenerPorId(Long id) {
        return imagenRepository.findById(id);
    }

    @Override
    public Imagen guardarImagen(Imagen imagen) {
        return imagenRepository.save(imagen);
    }

    @Override
    public void eliminarImagen(Long id) {
        imagenRepository.deleteById(id);
    }
    @Override
    public List<Imagen> obtenerPorCamara(Long camaraId) {
        return imagenRepository.findByCamara_IdCamara(camaraId);
    }

}
