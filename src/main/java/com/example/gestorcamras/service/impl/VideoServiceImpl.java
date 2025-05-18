package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Video;
import com.example.gestorcamras.dto.VideoDTO;
import java.util.stream.Collectors;
import com.example.gestorcamras.repository.VideoRepository;
import com.example.gestorcamras.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {

    private VideoDTO toDTO(Video video) {
        VideoDTO dto = new VideoDTO();
        dto.setIdVideo(video.getIdVideo());
        dto.setNombre(video.getNombre());
        dto.setTamano(video.getTamaño());
        dto.setFechaCaptura(video.getFechaCaptura());
        dto.setDuracion(video.getDuracion());
        dto.setRutaAlmacenamiento(video.getRutaAlmacenamiento());
        dto.setCamaraId(video.getCamara() != null ? video.getCamara().getIdCamara() : null);
        return dto;
    }

    private Video toEntity(VideoDTO dto) {
        Video video = new Video();
        video.setIdVideo(dto.getIdVideo());
        video.setNombre(dto.getNombre());
        video.setTamaño(dto.getTamano());
        video.setFechaCaptura(dto.getFechaCaptura());
        video.setDuracion(dto.getDuracion());
        video.setRutaAlmacenamiento(dto.getRutaAlmacenamiento());
        // El camara debe ser seteado desde el controller o con ayuda de un repositorio
        return video;
    }

    @Autowired
    private VideoRepository videoRepository;

    @Override
    public List<VideoDTO> obtenerTodos() {
        return videoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<VideoDTO> obtenerPorId(Long id) {
        return videoRepository.findById(id).map(this::toDTO);
    }

    @Override
    public VideoDTO guardarVideo(VideoDTO videoDTO) {
        Video video = toEntity(videoDTO);
        Video guardado = videoRepository.save(video);
        return toDTO(guardado);
    }

    @Override
    public void eliminarVideo(Long id) {
        videoRepository.deleteById(id);
    }
}
