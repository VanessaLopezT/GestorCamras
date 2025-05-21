package com.example.gestorcamras.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.gestorcamras.model.ArchivoMultimedia;

@Repository
public interface ArchivoMultimediaRepository extends JpaRepository<ArchivoMultimedia, Long> {
    List<ArchivoMultimedia> findByEquipoIdEquipo(Long equipoId);
    List<ArchivoMultimedia> findByCamara_IdCamara(Long camaraId);
} 