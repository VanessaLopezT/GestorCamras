package com.example.gestorcamras.service;

import com.example.gestorcamras.Escritorio.model.ArchivoMultimediaDTO;

import java.util.List;

public interface IArchivoMultimediaService {
    List<ArchivoMultimediaDTO> obtenerArchivosPorCamara(Long camaraId);
    List<ArchivoMultimediaDTO> obtenerArchivosPorEquipo(Long equipoId);
}
