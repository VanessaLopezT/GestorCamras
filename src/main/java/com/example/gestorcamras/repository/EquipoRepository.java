package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Equipo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    // Buscar equipos por nombre (ejemplo de método adicional)
    List<Equipo> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = {"camaras"})
    @NonNull Optional<Equipo> findById(@NonNull Long id); // sobreescribe findById

    // Verificar si existe un equipo por IP asignada
    boolean existsByIp(String ip);
    
    // Obtener el primer equipo que coincida con la IP
    Optional<Equipo> findFirstByIp(String ip);
    
    // Obtener todos los equipos con una IP específica
    List<Equipo> findAllByIp(String ip);
    
    // Verificar si ya existe un equipo con un identificador específico
    boolean existsByIdentificador(String identificador);
}