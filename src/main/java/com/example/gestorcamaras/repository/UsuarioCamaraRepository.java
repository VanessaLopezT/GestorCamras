package com.example.gestorcamaras.repository;
import com.example.gestorcamaras.model.UsuarioCamara;
import com.example.gestorcamaras.model.UsuarioCamaraId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioCamaraRepository extends JpaRepository<UsuarioCamara, UsuarioCamaraId> {}
