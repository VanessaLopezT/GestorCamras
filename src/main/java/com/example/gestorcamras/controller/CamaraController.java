package com.example.gestorcamras.controller;
import com.example.gestorcamras.service.CamaraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.gestorcamras.dto.CamaraDTO;

@RestController
@RequestMapping("/api/camaras")
public class CamaraController {

    @Autowired
    private CamaraService camaraService;

    @GetMapping
    public List<CamaraDTO> listarCamaras() {
        return camaraService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CamaraDTO> obtenerCamaraPorId(@PathVariable Long id) {
        return camaraService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CamaraDTO> crearCamara(@RequestBody CamaraDTO camaraDTO) {
        CamaraDTO guardada = camaraService.guardarCamara(camaraDTO);
        return ResponseEntity.ok(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CamaraDTO> actualizarCamara(@PathVariable Long id, @RequestBody CamaraDTO camaraDetalle) {
        return camaraService.obtenerPorId(id).map(existing -> {
            camaraDetalle.setIdCamara(id);
            CamaraDTO actualizada = camaraService.guardarCamara(camaraDetalle);
            return ResponseEntity.ok(actualizada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCamara(@PathVariable Long id) {
        if (camaraService.obtenerPorId(id).isPresent()) {
            camaraService.eliminarCamara(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

