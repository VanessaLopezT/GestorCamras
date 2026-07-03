package com.example.gestorcamaras.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.gestorcamaras.model.ImagenProcesada;

import java.util.List;

@Repository
public interface ImagenProcesadaRepository extends JpaRepository<ImagenProcesada, Long> {

    // Buscar todas las imágenes procesadas por el id de la imagen original
    List<ImagenProcesada> findByImagenOriginal_IdImagen(Long imagenOriginalId);


    // Contar cuántas imágenes procesadas hay por filtro
    long countByFiltro_IdFiltro(Long filtroId);

    // Consulta personalizada: Buscar imágenes procesadas entre fechas
    @Query("SELECT i FROM ImagenProcesada i WHERE i.fechaProcesamiento BETWEEN ?1 AND ?2")
    List<ImagenProcesada> findByFechaProcesamientoBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
