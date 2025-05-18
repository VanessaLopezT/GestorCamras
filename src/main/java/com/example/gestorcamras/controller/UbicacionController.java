package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.UbicacionDTO;

import com.example.gestorcamras.service.UbicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController {

    @Autowired
    private UbicacionService ubicacionService;

    @GetMapping
    public List<UbicacionDTO> listarUbicaciones() {
        return ubicacionService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UbicacionDTO> obtenerUbicacionPorId(@PathVariable Long id) {
        Optional<UbicacionDTO> ubicacionOpt = ubicacionService.obtenerPorId(id);
        return ubicacionOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public UbicacionDTO crearUbicacion(@RequestBody UbicacionDTO ubicacionDTO) {
        return ubicacionService.guardarUbicacion(ubicacionDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UbicacionDTO> actualizarUbicacion(@PathVariable Long id, @RequestBody UbicacionDTO ubicacionDTO) {
        Optional<UbicacionDTO> ubicacionOpt = ubicacionService.obtenerPorId(id);
        if (ubicacionOpt.isPresent()) {
            ubicacionDTO.setId(id);
            return ResponseEntity.ok(ubicacionService.guardarUbicacion(ubicacionDTO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUbicacion(@PathVariable Long id) {
        Optional<UbicacionDTO> ubicacionOpt = ubicacionService.obtenerPorId(id);
        if (ubicacionOpt.isPresent()) {
            ubicacionService.eliminarUbicacion(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
