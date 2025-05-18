package com.example.gestorcamras.repository;
import com.example.gestorcamras.model.UsuarioCamara;
import com.example.gestorcamras.model.UsuarioCamaraId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioCamaraRepository extends JpaRepository<UsuarioCamara, UsuarioCamaraId> {}
