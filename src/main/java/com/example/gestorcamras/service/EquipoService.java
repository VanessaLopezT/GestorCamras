package com.example.gestorcamras.service;

import java.util.List;
import java.util.Optional;

import com.example.gestorcamras.dto.EquipoConCamarasDTO;
import com.example.gestorcamras.dto.EquipoDTO;
import com.example.gestorcamras.dto.EquipoDetalleDTO;
import com.example.gestorcamras.dto.RegistroEquipoRequest;
import com.example.gestorcamras.model.Equipo;

public interface EquipoService {
    List<EquipoDTO> obtenerTodos();
    Optional<EquipoDTO> obtenerPorId(Long id);
    EquipoDTO registrarEquipo(EquipoDTO equipoDTO);
    void actualizarPing(Long id);
    void asignarCamara(Long idEquipo, Long idCamara);
    EquipoDTO guardarEquipo(EquipoDTO equipoDTO);
    void eliminarEquipo(Long id);
    public EquipoDTO registrarEquipoConCamaras(RegistroEquipoRequest request);

        // Método extra para búsqueda por nombre
    List<EquipoDTO> buscarPorNombre(String nombreEquipo);
    Optional<EquipoDetalleDTO> obtenerDetallePorId(Long idEquipo);
    public EquipoDTO actualizarEquipoConCamaras(Long id, EquipoConCamarasDTO dto);
    Optional<Equipo> obtenerEntidadPorId(Long id);
    Equipo guardarEntidad(Equipo equipo);
}

