package com.example.gestorcamaras.service;

import com.example.gestorcamaras.dto.ArchivoMultimediaDTO;
import com.example.gestorcamaras.dto.ArchivoMultimediaMapper;
import com.example.gestorcamaras.model.ArchivoMultimedia;
import com.example.gestorcamaras.repository.ArchivoMultimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchivoMultimediaService implements IArchivoMultimediaService {

    private final ArchivoMultimediaRepository archivoMultimediaRepository;

    @Autowired
    public ArchivoMultimediaService(ArchivoMultimediaRepository archivoMultimediaRepository) {
        this.archivoMultimediaRepository = archivoMultimediaRepository;
    }

    @Override
    public List<ArchivoMultimediaDTO> obtenerArchivosPorCamara(Long camaraId) {
        List<ArchivoMultimedia> archivos = archivoMultimediaRepository.findByCamara_IdCamara(camaraId);
        return ArchivoMultimediaMapper.toDTOList(archivos);
    }

    @Override
    public List<ArchivoMultimediaDTO> obtenerArchivosPorEquipo(Long equipoId) {
        List<ArchivoMultimedia> archivos = archivoMultimediaRepository.findByCamaraEquipoId(equipoId);
        return ArchivoMultimediaMapper.toDTOList(archivos);
    }
}
