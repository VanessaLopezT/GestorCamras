package com.example.gestorcamras.controller;
import com.example.gestorcamras.service.CamaraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.gestorcamras.model.Camara;

@RestController
@RequestMapping("/api/camaras")
public class CamaraController {

    @Autowired
    private CamaraService camaraService;

    @GetMapping
    public List<Camara> listarCamaras() {
        return camaraService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camara> obtenerCamaraPorId(@PathVariable Long id) {
        return camaraService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Camara crearCamara(@RequestBody Camara camara) {
        return camaraService.guardarCamara(camara);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Camara> actualizarCamara(@PathVariable Long id, @RequestBody Camara camaraDetalle) {
        return camaraService.obtenerPorId(id).map(cam -> {
            cam.setNombre(camaraDetalle.getNombre());
            cam.setIp(camaraDetalle.getIp());
            cam.setActiva(camaraDetalle.isActiva());
            cam.setTipo(camaraDetalle.getTipo());
            // Otras propiedades si quieres actualizar
            return ResponseEntity.ok(camaraService.guardarCamara(cam));
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
