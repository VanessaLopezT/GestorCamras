package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {

    // Buscar imágenes por cámara

    List<Imagen> findByCamara_IdCamara(Long idCamara);
    // Buscar imágenes capturadas después de cierta fecha
    List<Imagen> findByFechaCapturaAfter(LocalDateTime fecha);

    // Buscar imágenes capturadas entre dos fechas
    List<Imagen> findByFechaCapturaBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Buscar una imagen por su ID y el ID del equipo al que pertenece
    @Query("SELECT i FROM Imagen i WHERE i.idImagen = :idImagen AND i.camara.equipo.id = :equipoId")
    Optional<Imagen> findByIdImagenAndCamara_EquipoId(@Param("idImagen") Long idImagen, @Param("equipoId") Long equipoId);
    // Buscar imágenes por nombre (contenga texto)
    List<Imagen> findByNombreContainingIgnoreCase(String nombre);

    // Buscar imágenes por tamaño mayor a un valor
    List<Imagen> findByTamañoGreaterThan(double tamaño);

}
