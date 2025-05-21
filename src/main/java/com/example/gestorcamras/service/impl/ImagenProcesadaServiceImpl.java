package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.ImagenProcesada;
import com.example.gestorcamras.repository.ImagenProcesadaRepository;
import com.example.gestorcamras.service.ImagenProcesadaService;
import com.example.gestorcamras.dto.ImagenProcesadaDTO;
import java.util.stream.Collectors;
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

    // Métodos de conversión entre entidad y DTO
    private ImagenProcesadaDTO toDTO(ImagenProcesada imagen) {
        if (imagen == null) return null;
        ImagenProcesadaDTO dto = new ImagenProcesadaDTO();
        dto.setIdImgProcesada(imagen.getIdImgProcesada());
        dto.setNombre(imagen.getNombre());
        dto.setFechaProcesamiento(imagen.getFechaProcesamiento());
        dto.setTamaño(imagen.getTamaño());
        dto.setRutaImagen(imagen.getRutaImagen());
        dto.setImagenOriginalId(imagen.getImagenOriginal() != null ? imagen.getImagenOriginal().getIdImagen() : null);
        dto.setFiltroId(imagen.getFiltro() != null ? imagen.getFiltro().getIdFiltro() : null);
        return dto;
    }

    private ImagenProcesada toEntity(ImagenProcesadaDTO dto) {
        if (dto == null) return null;
        ImagenProcesada imagen = new ImagenProcesada();
        imagen.setIdImgProcesada(dto.getIdImgProcesada());
        imagen.setNombre(dto.getNombre());
        imagen.setFechaProcesamiento(dto.getFechaProcesamiento());
        imagen.setTamaño(dto.getTamaño());
        imagen.setRutaImagen(dto.getRutaImagen());
        // La asignación de imagenOriginal y filtro debe hacerse en el controlador o con un servicio adicional si es necesario
        return imagen;
    }

    @Override
    public List<ImagenProcesadaDTO> obtenerTodas() {
        return imagenProcesadaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "imgProcesadaCache", key = "#id")
    public Optional<ImagenProcesadaDTO> obtenerPorId(Long id) {
        return imagenProcesadaRepository.findById(id).map(this::toDTO);
    }

    @Override
    @CacheEvict(value = "imgProcesadaCache", key = "#imagenDTO.idImagenProcesada")
    public ImagenProcesadaDTO guardarImagen(ImagenProcesadaDTO imagenDTO) {
        ImagenProcesada imagen = toEntity(imagenDTO);
        ImagenProcesada guardada = imagenProcesadaRepository.save(imagen);
        return toDTO(guardada);
    }

    @Override
    @CacheEvict(value = "imgProcesadaCache", key = "#id")
    public void eliminarImagen(Long id) {
        imagenProcesadaRepository.deleteById(id);
    }
}
