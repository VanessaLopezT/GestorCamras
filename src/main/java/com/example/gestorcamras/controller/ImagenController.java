package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.ImagenDTO;
import com.example.gestorcamras.model.Imagen;
import com.example.gestorcamras.service.ImagenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    // Convertir entidad a DTO
    private ImagenDTO convertirADTO(Imagen imagen) {
        return new ImagenDTO(
                imagen.getIdImagen(),
                imagen.getNombre(),
                imagen.getTamaño(),
                imagen.getFechaCaptura(),
                imagen.getRutaAlmacenamiento(),
                imagen.getCamara() != null ? imagen.getCamara().getIdCamara() : null
        );
    }

    // Convertir DTO a entidad (para guardar o actualizar)
    private Imagen convertirAEntidad(ImagenDTO dto) {
        Imagen imagen = new Imagen();
        imagen.setIdImagen(dto.getIdImagen());
        imagen.setNombre(dto.getNombre());
        imagen.setTamaño(dto.getTamaño());
        imagen.setFechaCaptura(dto.getFechaCaptura());
        imagen.setRutaAlmacenamiento(dto.getRutaAlmacenamiento());

        // Solo seteamos el id de la cámara para relacionar, sin cargar toda la entidad
        if (dto.getCamaraId() != null) {
            var camara = new com.example.gestorcamras.model.Camara();
            camara.setIdCamara(dto.getCamaraId());
            imagen.setCamara(camara);
        } else {
            imagen.setCamara(null);
        }
        return imagen;
    }

    @GetMapping
    public List<ImagenDTO> listarImagenes() {
        return imagenService.obtenerTodas()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenDTO> obtenerImagenPorId(@PathVariable Long id) {
        Optional<Imagen> imagenOpt = imagenService.obtenerPorId(id);
        return imagenOpt.map(imagen -> ResponseEntity.ok(convertirADTO(imagen)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ImagenDTO> crearImagen(@RequestBody ImagenDTO imagenDTO) {
        Imagen imagenGuardada = imagenService.guardarImagen(convertirAEntidad(imagenDTO));
        return ResponseEntity.ok(convertirADTO(imagenGuardada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImagenDTO> actualizarImagen(@PathVariable Long id, @RequestBody ImagenDTO imagenDTO) {
        Optional<Imagen> imagenOpt = imagenService.obtenerPorId(id);
        if (imagenOpt.isPresent()) {
            Imagen imagenExistente = imagenOpt.get();
            imagenExistente.setNombre(imagenDTO.getNombre());
            imagenExistente.setTamaño(imagenDTO.getTamaño());
            imagenExistente.setFechaCaptura(imagenDTO.getFechaCaptura());
            imagenExistente.setRutaAlmacenamiento(imagenDTO.getRutaAlmacenamiento());

            if (imagenDTO.getCamaraId() != null) {
                var camara = new com.example.gestorcamras.model.Camara();
                camara.setIdCamara(imagenDTO.getCamaraId());
                imagenExistente.setCamara(camara);
            } else {
                imagenExistente.setCamara(null);
            }

            Imagen imagenActualizada = imagenService.guardarImagen(imagenExistente);
            return ResponseEntity.ok(convertirADTO(imagenActualizada));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Long id) {
        Optional<Imagen> imagenOpt = imagenService.obtenerPorId(id);
        if (imagenOpt.isPresent()) {
            imagenService.eliminarImagen(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Ejemplo de filtro: buscar por cámara
    @GetMapping("/porCamara/{camaraId}")
    public List<ImagenDTO> obtenerPorCamara(@PathVariable Long camaraId) {
        return imagenService.obtenerPorCamara(camaraId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

}
