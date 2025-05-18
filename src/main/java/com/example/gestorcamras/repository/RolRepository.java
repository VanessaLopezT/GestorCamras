package com.example.gestorcamras.repository;

import com.example.gestorcamras.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Rol findByNombre(String nombre);
    List<Rol> findByPermisosContaining(String permiso);

}
