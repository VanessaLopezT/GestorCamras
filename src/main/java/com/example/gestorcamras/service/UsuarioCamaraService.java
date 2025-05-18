package com.example.gestorcamras.service;

import com.example.gestorcamras.model.UsuarioCamara;
import com.example.gestorcamras.model.UsuarioCamaraId;
import java.util.List;
import java.util.Optional;

public interface UsuarioCamaraService {
    List<UsuarioCamara> obtenerTodas();
    Optional<UsuarioCamara> obtenerPorId(UsuarioCamaraId id);
    UsuarioCamara guardar(UsuarioCamara usuarioCamara);
    void eliminar(UsuarioCamaraId id);
}
