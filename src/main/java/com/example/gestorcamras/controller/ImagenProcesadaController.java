package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.ImagenProcesadaDTO;
import com.example.gestorcamras.model.ImagenProcesada;
import com.example.gestorcamras.service.ImagenProcesadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/imagenes-procesadas")
public class ImagenProcesadaController {

    @Autowired
    private ImagenProcesadaService imagenProcesadaService;

    // Convierte entidad a DTO
    private ImagenProcesadaDTO convertirADTO(ImagenProcesada entidad) {
        ImagenProcesadaDTO dto = new ImagenProcesadaDTO();
        dto.setIdImgProcesada(entidad.getIdImgProcesada());
        dto.setNombre(entidad.getNombre());
        dto.setFechaProcesamiento(entidad.getFechaProcesamiento());
        dto.setTamaño(entidad.getTamaño());
        dto.setRutaImagen(entidad.getRutaImagen());
        dto.setImagenOriginalId(entidad.getImagenOriginal() != null ? entidad.getImagenOriginal().getIdImagen() : null);
        dto.setFiltroId(entidad.getFiltro() != null ? entidad.getFiltro().getIdFiltro() : null);
        return dto;
    }

    // Convierte DTO a entidad (solo campos básicos y relaciones por id)
    private ImagenProcesada convertirAEntidad(ImagenProcesadaDTO dto) {
        ImagenProcesada entidad = new ImagenProcesada();
        entidad.setIdImgProcesada(dto.getIdImgProcesada());
        entidad.setNombre(dto.getNombre());
        entidad.setFechaProcesamiento(dto.getFechaProcesamiento());
        entidad.setTamaño(dto.getTamaño());
        entidad.setRutaImagen(dto.getRutaImagen());
        // Las asociaciones se deben resolver en el Service o Controller con Repositorios de Imagen y Filtro
        // Aquí dejamos nulo, se debe manejar al guardar
        entidad.setImagenOriginal(null);
        entidad.setFiltro(null);
        return entidad;
    }

    @GetMapping
    public List<ImagenProcesadaDTO> listarTodas() {
        return imagenProcesadaService.obtenerTodas().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenProcesadaDTO> obtenerPorId(@PathVariable Long id) {
        Optional<ImagenProcesada> opt = imagenProcesadaService.obtenerPorId(id);
        return opt.map(imagen -> ResponseEntity.ok(convertirADTO(imagen)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ImagenProcesadaDTO> crearImagenProcesada(@RequestBody ImagenProcesadaDTO dto) {
        ImagenProcesada entidad = convertirAEntidad(dto);
        // Aquí faltaría resolver ImagenOriginal y Filtro con sus servicios/repositorios para asignarlos a la entidad
        ImagenProcesada guardada = imagenProcesadaService.guardarImagenProcesada(entidad);
        return ResponseEntity.ok(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImagenProcesadaDTO> actualizarImagenProcesada(@PathVariable Long id, @RequestBody ImagenProcesadaDTO dto) {
        Optional<ImagenProcesada> opt = imagenProcesadaService.obtenerPorId(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        ImagenProcesada entidad = opt.get();
        entidad.setNombre(dto.getNombre());
        entidad.setFechaProcesamiento(dto.getFechaProcesamiento());
        entidad.setTamaño(dto.getTamaño());
        entidad.setRutaImagen(dto.getRutaImagen());
        // Aquí también manejar relaciones (ImagenOriginal, Filtro)
        ImagenProcesada actualizada = imagenProcesadaService.guardarImagenProcesada(entidad);
        return ResponseEntity.ok(convertirADTO(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarImagenProcesada(@PathVariable Long id) {
        if (!imagenProcesadaService.obtenerPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        imagenProcesadaService.eliminarImagenProcesada(id);
        return ResponseEntity.noContent().build();
    }
}
