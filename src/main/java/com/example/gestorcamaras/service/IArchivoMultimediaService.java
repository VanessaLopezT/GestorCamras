package com.example.gestorcamaras.service;

import com.example.gestorcamaras.dto.ArchivoMultimediaDTO;

import java.util.List;

public interface IArchivoMultimediaService {
    List<ArchivoMultimediaDTO> obtenerArchivosPorCamara(Long camaraId);
    List<ArchivoMultimediaDTO> obtenerArchivosPorEquipo(Long equipoId);
}
