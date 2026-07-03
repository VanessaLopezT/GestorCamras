package com.example.gestorcamaras.dto;

import com.example.gestorcamaras.model.ArchivoMultimedia;

import java.util.List;

/**
 * Única conversión entidad → DTO para archivos multimedia.
 * Al añadir un campo nuevo: actualizar ArchivoMultimediaDTO y este mapper.
 * Nada más debe convertir ArchivoMultimedia manualmente.
 */
public final class ArchivoMultimediaMapper {

    private ArchivoMultimediaMapper() {
    }

    public static ArchivoMultimediaDTO toDTO(ArchivoMultimedia archivo) {
        if (archivo == null) {
            return null;
        }
        ArchivoMultimediaDTO dto = new ArchivoMultimediaDTO();
        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setRutaArchivo(archivo.getRutaArchivo());
        dto.setTipo(archivo.getTipo() != null ? archivo.getTipo().name() : null);
        dto.setFechaCaptura(archivo.getFechaCaptura());
        dto.setFechaSubida(archivo.getFechaSubida());
        if (archivo.getCamara() != null) {
            dto.setCamaraId(archivo.getCamara().getIdCamara());
        }
        if (archivo.getEquipo() != null) {
            dto.setEquipoId(archivo.getEquipo().getIdEquipo());
        }
        return dto;
    }

    public static List<ArchivoMultimediaDTO> toDTOList(List<ArchivoMultimedia> archivos) {
        if (archivos == null) {
            return List.of();
        }
        return archivos.stream()
                .map(ArchivoMultimediaMapper::toDTO)
                .toList();
    }
}
