package com.example.gestorcamras.service;

import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.repository.ArchivoMultimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchivoMultimediaService {

    @Autowired
    private ArchivoMultimediaRepository archivoMultimediaRepository;

    public List<ArchivoMultimedia> obtenerArchivosPorCamara(Long camaraId) {
        return archivoMultimediaRepository.findByCamara_IdCamara(camaraId);
    }

    public List<ArchivoMultimedia> obtenerArchivosPorEquipo(Long equipoId) {
        return archivoMultimediaRepository.findByCamaraEquipoId(equipoId);
    }
}
