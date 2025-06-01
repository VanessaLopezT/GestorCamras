package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.FiltroAplicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FiltroAplicadoRepository extends JpaRepository<FiltroAplicado, Long> {
    
    // Usando @Query para mayor claridad
    @Query("SELECT f FROM FiltroAplicado f WHERE f.archivo.idArchivo = :archivoId")
    List<FiltroAplicado> findByArchivoId(@Param("archivoId") Long archivoId);
    
    @Query("SELECT f FROM FiltroAplicado f WHERE f.archivo.equipo.idEquipo = :equipoId")
    List<FiltroAplicado> findByArchivoEquipoId(@Param("equipoId") Long equipoId);
}
