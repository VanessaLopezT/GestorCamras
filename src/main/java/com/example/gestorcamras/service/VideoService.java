package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Video;

import java.util.List;
import java.util.Optional;

public interface VideoService {
    List<Video> obtenerTodos();
    Optional<Video> obtenerPorId(Long id);
    Video guardarVideo(Video video);
    void eliminarVideo(Long id);
}
