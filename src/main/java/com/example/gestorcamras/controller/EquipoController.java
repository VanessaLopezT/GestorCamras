package com.example.gestorcamras.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        // Primero intentamos interpretar el ID como un número (para compatibilidad con clientes existentes)
        try {
            Long idNum = Long.parseLong(id);
            return equipoService.obtenerPorId(idNum)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (NumberFormatException e) {
            // Si no es un número, asumimos que es un identificador de equipo
            return equipoService.obtenerPorIp(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
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

    @GetMapping("/buscarPorIp")
    public ResponseEntity<EquipoDTO> buscarPorIp(@RequestParam String ip) {
        return buscarEquipoPorIp(ip);
    }
    
    @GetMapping("/ip/{ip}")
    public ResponseEntity<EquipoDTO> buscarPorIpPath(@PathVariable String ip) {
        return buscarEquipoPorIp(ip);
    }
    
    private ResponseEntity<EquipoDTO> buscarEquipoPorIp(String ip) {
        List<EquipoDTO> equipos = equipoService.obtenerTodosPorIp(ip);
        if (equipos.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else if (equipos.size() == 1) {
            return ResponseEntity.ok(equipos.get(0));
        } else {
            // Si hay múltiples equipos con la misma IP, devolvemos el primero activo
            // o el primero de la lista si ninguno está activo
            EquipoDTO equipoActivo = equipos.stream()
                    .filter(EquipoDTO::getActivo)
                    .findFirst()
                    .orElse(equipos.get(0));
            return ResponseEntity.ok(equipoActivo);
        }
    }
}
