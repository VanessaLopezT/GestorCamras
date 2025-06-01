package com.example.gestorcamras.service;

import com.example.gestorcamras.Escritorio.model.ArchivoMultimediaDTO;
import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.repository.ArchivoMultimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        return archivos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArchivoMultimediaDTO> obtenerArchivosPorEquipo(Long equipoId) {
        List<ArchivoMultimedia> archivos = archivoMultimediaRepository.findByCamaraEquipoId(equipoId);
        return archivos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ArchivoMultimediaDTO convertToDTO(ArchivoMultimedia archivo) {
        ArchivoMultimediaDTO dto = new ArchivoMultimediaDTO();
        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setRutaArchivo(archivo.getRutaArchivo());
        dto.setTipo(archivo.getTipo().name());
        dto.setFechaCaptura(archivo.getFechaCaptura() != null ? archivo.getFechaCaptura().toString() : null);
        dto.setFechaSubida(archivo.getFechaSubida() != null ? archivo.getFechaSubida().toString() : null);
        if (archivo.getCamara() != null) {
            dto.setCamaraId(archivo.getCamara().getIdCamara());
        }
        if (archivo.getEquipo() != null) {
            dto.setEquipoId(archivo.getEquipo().getIdEquipo());
        }
        return dto;
    }
}
