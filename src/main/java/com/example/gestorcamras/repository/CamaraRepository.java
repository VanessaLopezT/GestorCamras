package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Ubicacion;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamaraRepository extends JpaRepository<Camara, Long> {

    List<Camara> findByActiva(boolean activa);

    List<Camara> findByPropietario(Usuario propietario);

    Optional<Camara> findByNombreAndEquipo(String nombre, Equipo equipo);


    // Método que usa el service: buscar por idUsuario en propietario
    List<Camara> findByPropietarioIdUsuario(Long idUsuario);

    List<Camara> findByUbicacion(Ubicacion ubicacion);

    // Método con idUbicacion para facilitar la consulta
    List<Camara> findByUbicacionId(Long idUbicacion);

    List<Camara> findByEquipo(Equipo equipo);
    List<Camara> findByEquipoIdEquipo(Long idEquipo);

    List<Camara> findByTipo(String tipo);

    List<Camara> findByNombreContainingIgnoreCase(String nombre);

    List<Camara> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    boolean existsByIp(String ip);

    Camara findByIp(String ip);
}
