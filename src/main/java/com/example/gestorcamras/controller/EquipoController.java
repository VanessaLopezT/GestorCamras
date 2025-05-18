package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.EquipoDTO;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    // Convertir entidad a DTO
    private EquipoDTO mapToDTO(Equipo equipo) {
        return new EquipoDTO(
                equipo.getIdEquipo(),
                equipo.getNombreEquipo(),
                equipo.getIpAsignada(),
                equipo.getFechaRegistro()
        );
    }

    @GetMapping
    public List<EquipoDTO> listarEquipos(@RequestParam(required = false) String nombre) {
        List<Equipo> equipos;
        if (nombre != null && !nombre.isEmpty()) {
            equipos = equipoService.buscarPorNombre(nombre);
        } else {
            equipos = equipoService.obtenerTodos();
        }
        return equipos.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoDTO> obtenerEquipoPorId(@PathVariable Long id) {
        Optional<Equipo> equipo = equipoService.obtenerPorId(id);
        return equipo.map(value -> ResponseEntity.ok(mapToDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipoDTO> crearEquipo(@RequestBody EquipoDTO equipoDTO) {
        Equipo equipo = new Equipo();
        equipo.setNombreEquipo(equipoDTO.getNombreEquipo());
        equipo.setIpAsignada(equipoDTO.getIpAsignada());
        equipo.setFechaRegistro(equipoDTO.getFechaRegistro());
        Equipo guardado = equipoService.guardarEquipo(equipo);
        return ResponseEntity.ok(mapToDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoDTO> actualizarEquipo(@PathVariable Long id, @RequestBody EquipoDTO equipoDTO) {
        Optional<Equipo> optEquipo = equipoService.obtenerPorId(id);
        if (optEquipo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Equipo equipo = optEquipo.get();
        equipo.setNombreEquipo(equipoDTO.getNombreEquipo());
        equipo.setIpAsignada(equipoDTO.getIpAsignada());
        equipo.setFechaRegistro(equipoDTO.getFechaRegistro());
        Equipo actualizado = equipoService.guardarEquipo(equipo);
        return ResponseEntity.ok(mapToDTO(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEquipo(@PathVariable Long id) {
        Optional<Equipo> equipo = equipoService.obtenerPorId(id);
        if (equipo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        equipoService.eliminarEquipo(id);
        return ResponseEntity.noContent().build();
    }
}
