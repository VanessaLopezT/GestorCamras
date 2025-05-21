package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {

    // Buscar imágenes por cámara

    List<Imagen> findByCamara_IdCamara(Long idCamara);
    // Buscar imágenes capturadas después de cierta fecha
    List<Imagen> findByFechaCapturaAfter(LocalDateTime fecha);

    // Buscar imágenes capturadas entre dos fechas
    List<Imagen> findByFechaCapturaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar imágenes por nombre (contenga texto)
    List<Imagen> findByNombreContainingIgnoreCase(String nombre);

    // Buscar imágenes por tamaño mayor a un valor
    List<Imagen> findByTamañoGreaterThan(double tamaño);

}
