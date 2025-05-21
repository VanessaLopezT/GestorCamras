package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Informe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InformeRepository extends JpaRepository<Informe, Long> {

    // Buscar informes por usuario
    List<Informe> findByUsuario_IdUsuario(Long idUsuario);

    // Buscar informes que contengan texto en el título o contenido (para búsqueda rápida)
    List<Informe> findByTituloContainingIgnoreCaseOrContenidoContainingIgnoreCase(String titulo, String contenido);

    // Buscar informes generados después de una fecha determinada
    List<Informe> findByFechaGeneracionAfter(LocalDateTime fecha);

}
