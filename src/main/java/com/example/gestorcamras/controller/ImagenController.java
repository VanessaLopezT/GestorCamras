package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.FiltroDTO;
import com.example.gestorcamras.dto.ImagenDTO;
import com.example.gestorcamras.dto.ImagenProcesadaDTO;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.model.Imagen;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.service.EquipoService;
import com.example.gestorcamras.service.ImagenService;

import com.example.gestorcamras.service.ProcesadorImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @Autowired
    private ProcesadorImagenService procesadorImagenService;

    @Autowired
    private CamaraService camaraService;

    @Autowired
    private EquipoService equipoService;


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


    @PostMapping("/{id}/procesar")
    public ResponseEntity<ImagenProcesadaDTO> procesarImagen(
            @PathVariable Long id,
            @RequestBody FiltroDTO filtroDTO) {

        Optional<Imagen> imagenOpt = imagenService.obtenerPorId(id);

        if (imagenOpt.isPresent()) {
            ImagenDTO imagenDTO = convertirADTO(imagenOpt.get());
            ImagenProcesadaDTO resultado = procesadorImagenService.procesarImagen(
                    imagenDTO,
                    filtroDTO
            );
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.notFound().build();

    }

    @PostMapping("/equipos/{equipoId}/imagenes")
    public ResponseEntity<ImagenDTO> subirImagenEquipo(
            @PathVariable Long equipoId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("nombreCamara") String nombreCamara,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp
    ) {
        // 1. Validar archivo no vacío
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Buscar equipo y cámara (ejemplo rápido, deberías manejar si no existen)
        Optional<Equipo> equipoOpt = equipoService.obtenerEntidadPorId(equipoId);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Equipo equipo = equipoOpt.get();

        Optional<Camara> camaraOpt = camaraService.obtenerPorNombreYEquipo(nombreCamara, equipo);
        if (camaraOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Camara camara = camaraOpt.get();

        // 3. Guardar archivo en disco (ejemplo simple, cambia ruta según tu configuración)
        String rutaAlmacenamiento = "/ruta/imagenes/" + archivo.getOriginalFilename();
        try {
            archivo.transferTo(new File(rutaAlmacenamiento));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 4. Crear entidad Imagen y guardar en BD
        Imagen imagen = new Imagen();
        imagen.setNombre(archivo.getOriginalFilename());
        imagen.setTamaño(archivo.getSize());
        imagen.setFechaCaptura(timestamp);
        imagen.setRutaAlmacenamiento(rutaAlmacenamiento);
        imagen.setCamara(camara);

        Imagen imagenGuardada = imagenService.guardarImagen(imagen);

        // 5. Convertir a DTO y retornar
        ImagenDTO dto = convertirADTO(imagenGuardada);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }


}
