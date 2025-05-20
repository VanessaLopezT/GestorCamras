package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Equipo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    // Buscar equipos por nombre (ejemplo de método adicional)
    List<Equipo> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = {"camaras"})
    Optional<Equipo> findById(Long id); // sobreescribe findById

    // Verificar si existe un equipo por IP asignada (único)
    boolean existsByIp(String ip);
}
