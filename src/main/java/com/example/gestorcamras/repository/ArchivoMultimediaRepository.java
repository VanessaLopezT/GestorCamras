package com.example.gestorcamras.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.gestorcamras.model.ArchivoMultimedia;

@Repository
public interface ArchivoMultimediaRepository extends JpaRepository<ArchivoMultimedia, Long> {
    
    @Query("SELECT DISTINCT a FROM ArchivoMultimedia a LEFT JOIN FETCH a.camara WHERE a.equipo.idEquipo = :equipoId")
    List<ArchivoMultimedia> findByEquipoIdEquipoWithCamara(@Param("equipoId") Long equipoId);
    
    @Query("SELECT DISTINCT a FROM ArchivoMultimedia a LEFT JOIN FETCH a.equipo WHERE a.camara.idCamara = :camaraId")
    List<ArchivoMultimedia> findByCamaraIdCamaraWithEquipo(@Param("camaraId") Long camaraId);
    
    // Métodos antiguos (mantener para compatibilidad)
    List<ArchivoMultimedia> findByEquipoIdEquipo(Long equipoId);
    List<ArchivoMultimedia> findByCamara_IdCamara(Long camaraId);
}