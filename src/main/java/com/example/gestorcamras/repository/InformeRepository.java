package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Informe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InformeRepository extends JpaRepository<Informe, Long> {

    // Buscar informes por usuario
    List<Informe> findByUsuario_IdUsuario(Long idUsuario);

    // Buscar informes que contengan texto en el título o contenido (para búsqueda rápida)
    @Query("SELECT i FROM Informe i WHERE LOWER(i.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR CAST(i.contenido AS STRING) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Informe> buscarPorTituloOContenido(@Param("busqueda") String busqueda);

    // Buscar informes generados después de una fecha determinada
    List<Informe> findByFechaGeneracionAfter(LocalDateTime fecha);
}
