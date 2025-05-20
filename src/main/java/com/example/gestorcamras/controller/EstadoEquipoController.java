package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.EquipoDTO;
import com.example.gestorcamras.dto.EstadoEquipoDTO;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.redis.RedisCacheService;
import com.example.gestorcamras.service.EquipoService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/equipos/{equipoId}/status")
public class EstadoEquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private RedisCacheService redisCacheService; // para guardar estado en cache

    @PostMapping
    public ResponseEntity<Void> actualizarEstadoEquipo(
            @PathVariable Long equipoId,
            @RequestBody EstadoEquipoDTO estadoDTO
    ) {
        Optional<Equipo> equipoOpt = equipoService.obtenerEntidadPorId(equipoId);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Equipo equipo = equipoOpt.get();
        equipo.setUltimaConexion(estadoDTO.getTimestamp());
        equipo.setActivo(estadoDTO.getActivo() != null ? estadoDTO.getActivo() : true);

        equipoService.guardarEntidad(equipo);

        EquipoDTO dto = new EquipoDTO();
        dto.setIdEquipo(equipo.getIdEquipo());
        dto.setNombre(equipo.getNombre());
        dto.setIp(equipo.getIp());
        dto.setUltimaConexion(equipo.getUltimaConexion());
        dto.setActivo(equipo.getActivo());

        equipoService.guardarEquipo(dto);

        redisCacheService.guardarEstadoEquipo(equipoId, estadoDTO);

        return ResponseEntity.ok().build();
    }
}
