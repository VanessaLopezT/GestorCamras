package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.EquipoDTO;
import java.util.List;
import com.example.gestorcamras.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

        @GetMapping
    public List<EquipoDTO> listarEquipos(@RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.isEmpty()) {
            return equipoService.buscarPorNombre(nombre);
        } else {
            return equipoService.obtenerTodos();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoDTO> obtenerEquipoPorId(@PathVariable Long id) {
        return equipoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipoDTO> crearEquipo(@RequestBody EquipoDTO equipoDTO) {
        EquipoDTO guardado = equipoService.guardarEquipo(equipoDTO);
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoDTO> actualizarEquipo(@PathVariable Long id, @RequestBody EquipoDTO equipoDTO) {
        if (equipoService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        equipoDTO.setIdEquipo(id);
        EquipoDTO actualizado = equipoService.guardarEquipo(equipoDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEquipo(@PathVariable Long id) {
        if (equipoService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        equipoService.eliminarEquipo(id);
        return ResponseEntity.noContent().build();
    }
}
