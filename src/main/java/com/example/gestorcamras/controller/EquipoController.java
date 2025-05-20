package com.example.gestorcamras.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gestorcamras.dto.EquipoDTO;
import com.example.gestorcamras.service.EquipoService;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @GetMapping
    public List<EquipoDTO> obtenerTodos() {
            return equipoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public EquipoDTO obtenerPorId(@PathVariable Long id) {
        return equipoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
    }

    @PostMapping("/registrar")
    public EquipoDTO registrarEquipo(@RequestBody EquipoDTO equipoDTO) {
        return equipoService.registrarEquipo(equipoDTO);
    }

    @PostMapping("/{id}/ping")
    public ResponseEntity<Void> actualizarPing(@PathVariable Long id) {
        equipoService.actualizarPing(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{idEquipo}/camaras/{idCamara}")
    public ResponseEntity<Void> asignarCamara(
            @PathVariable Long idEquipo,
            @PathVariable Long idCamara) {
        equipoService.asignarCamara(idEquipo, idCamara);
        return ResponseEntity.ok().build();
    }
}
