package com.example.gestorcamras.pool;

import com.example.gestorcamras.model.Filtro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiltroRepository extends JpaRepository<Filtro, Long> {

    // Buscar filtros por tipo (ejemplo útil para consultas)
    List<Filtro> findByTipoContainingIgnoreCase(String tipo);

    // Buscar filtros que tengan descripcion que contenga un texto dado
    List<Filtro> findByDescripcionContainingIgnoreCase(String texto);

    // Consultar si ya existe un filtro por tipo exacto
    boolean existsByTipo(String tipo);

    // Consultar cantidad de filtros con tipo específico
    @Query("SELECT COUNT(f) FROM Filtro f WHERE LOWER(f.tipo) = LOWER(?1)")
    long countByTipoIgnoreCase(String tipo);
}
