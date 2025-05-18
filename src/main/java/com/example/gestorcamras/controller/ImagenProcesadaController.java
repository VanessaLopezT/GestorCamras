package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.ImagenProcesadaDTO;
import com.example.gestorcamras.service.ImagenProcesadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/imagenes-procesadas")
public class ImagenProcesadaController {

    @Autowired
    private ImagenProcesadaService imagenProcesadaService;



    @GetMapping
    public List<ImagenProcesadaDTO> listarTodas() {
        return imagenProcesadaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenProcesadaDTO> obtenerPorId(@PathVariable Long id) {
        Optional<ImagenProcesadaDTO> opt = imagenProcesadaService.obtenerPorId(id);
        return opt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ImagenProcesadaDTO> crearImagenProcesada(@RequestBody ImagenProcesadaDTO dto) {
        ImagenProcesadaDTO guardada = imagenProcesadaService.guardarImagen(dto);
        return ResponseEntity.ok(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImagenProcesadaDTO> actualizarImagenProcesada(@PathVariable Long id, @RequestBody ImagenProcesadaDTO dto) {
        Optional<ImagenProcesadaDTO> opt = imagenProcesadaService.obtenerPorId(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        ImagenProcesadaDTO existente = opt.get();
        existente.setNombre(dto.getNombre());
        existente.setFechaProcesamiento(dto.getFechaProcesamiento());
        existente.setTamaño(dto.getTamaño());
        existente.setRutaImagen(dto.getRutaImagen());
        existente.setImagenOriginalId(dto.getImagenOriginalId());
        existente.setFiltroId(dto.getFiltroId());
        ImagenProcesadaDTO actualizada = imagenProcesadaService.guardarImagen(existente);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarImagenProcesada(@PathVariable Long id) {
        imagenProcesadaService.eliminarImagen(id);
        return ResponseEntity.noContent().build();
    }
}
