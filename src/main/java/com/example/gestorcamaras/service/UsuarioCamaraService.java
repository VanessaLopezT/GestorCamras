package com.example.gestorcamaras.service;

import com.example.gestorcamaras.dto.UsuarioCamaraDTO;
import com.example.gestorcamaras.model.UsuarioCamaraId;
import java.util.List;
import java.util.Optional;

public interface UsuarioCamaraService {
    List<UsuarioCamaraDTO> obtenerTodas();
    Optional<UsuarioCamaraDTO> obtenerPorId(UsuarioCamaraId id);
    UsuarioCamaraDTO guardar(UsuarioCamaraDTO usuarioCamaraDTO);
    void eliminar(UsuarioCamaraId id);
}
