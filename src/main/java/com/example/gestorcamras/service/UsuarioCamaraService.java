package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.UsuarioCamaraDTO;
import com.example.gestorcamras.model.UsuarioCamaraId;
import java.util.List;
import java.util.Optional;

public interface UsuarioCamaraService {
    List<UsuarioCamaraDTO> obtenerTodas();
    Optional<UsuarioCamaraDTO> obtenerPorId(UsuarioCamaraId id);
    UsuarioCamaraDTO guardar(UsuarioCamaraDTO usuarioCamaraDTO);
    void eliminar(UsuarioCamaraId id);
}
