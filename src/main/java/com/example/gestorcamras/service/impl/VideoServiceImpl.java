package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Video;
import com.example.gestorcamras.repository.VideoRepository;
import com.example.gestorcamras.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Override
    public List<Video> obtenerTodos() {
        return videoRepository.findAll();
    }

    @Override
    public Optional<Video> obtenerPorId(Long id) {
        return videoRepository.findById(id);
    }

    @Override
    public Video guardarVideo(Video video) {
        return videoRepository.save(video);
    }

    @Override
    public void eliminarVideo(Long id) {
        videoRepository.deleteById(id);
    }
}
