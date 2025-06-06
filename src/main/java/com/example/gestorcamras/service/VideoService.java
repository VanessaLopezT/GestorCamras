package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.VideoDTO;
import com.example.gestorcamras.model.Video;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    List<VideoDTO> obtenerTodos();
    Optional<VideoDTO> obtenerPorId(Long id);
    VideoDTO guardarVideo(VideoDTO videoDTO);
    void eliminarVideo(Long id);
    VideoDTO guardarVideo(Video video);

}
