package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.EquipoConCamarasDTO;
import com.example.gestorcamras.dto.EquipoDTO;
import java.util.List;

import com.example.gestorcamras.dto.EquipoDetalleDTO;
import com.example.gestorcamras.dto.RegistroEquipoRequest;
import com.example.gestorcamras.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<EquipoDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        return equipoService.obtenerDetallePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Método alternativo para obtener EquipoDTO simple, con ruta distinta
    @GetMapping("/simple/{id}")
    public ResponseEntity<EquipoDTO> obtenerEquipoSimplePorId(@PathVariable Long id) {
        return equipoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipoDTO> crearEquipo(@RequestBody EquipoDTO equipoDTO) {
        EquipoDTO guardado = equipoService.guardarEquipo(equipoDTO);
        return ResponseEntity.ok(guardado);
    }

    @PostMapping("/register")
    public ResponseEntity<EquipoDTO> registrarEquipo(@RequestBody RegistroEquipoRequest request) {
        EquipoDTO registrado = equipoService.registrarEquipoConCamaras(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
    }

    // Dejamos solo un método PUT para actualizar con cámaras
    @PutMapping("/{id}")
    public ResponseEntity<EquipoDTO> actualizarEquipo(
            @PathVariable Long id,
            @RequestBody EquipoConCamarasDTO dto) {
        try {
            EquipoDTO actualizado = equipoService.actualizarEquipoConCamaras(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEquipo(@PathVariable Long id) {
        try {
            equipoService.eliminarEquipo(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
