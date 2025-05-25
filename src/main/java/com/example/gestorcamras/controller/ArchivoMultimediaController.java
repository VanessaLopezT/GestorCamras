package com.example.gestorcamras.controller;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gestorcamras.dto.ArchivoMultimediaDTO;
import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.repository.ArchivoMultimediaRepository;
import com.example.gestorcamras.repository.CamaraRepository;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.service.EquipoService;

@RestController
@RequestMapping("/api")
public class ArchivoMultimediaController {

    @Autowired
    private ArchivoMultimediaRepository archivoRepository;

    @Autowired
    private CamaraRepository camaraRepository;

    @Autowired
    private CamaraService camaraService;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private EquipoService equipoService;

    private final Path directorioArchivos = Paths.get("archivos_multimedia");

    public ArchivoMultimediaController() {
        try {
            Files.createDirectories(directorioArchivos);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio para archivos multimedia", e);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArchivoMultimediaController.class);

    @PostMapping("/equipos/{idEquipo}/camaras/{idCamara}/archivo")
    public ResponseEntity<Map<String, Object>> subirArchivo(
            @PathVariable Long idEquipo,
            @PathVariable Long idCamara,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo) {
        
        log.info("Recibida solicitud de subida de archivo - Equipo: {}, Cámara: {}, Tipo: {}", idEquipo, idCamara, tipo);
        log.info("Archivo recibido: {} ({} bytes)", archivo.getOriginalFilename(), archivo.getSize());
        
        try {
            log.debug("Validando tipo de archivo...");
            // Validar tipo de archivo
            log.debug("Validando tipo de archivo: {}", tipo);
            if (!tipo.equalsIgnoreCase("FOTO") && !tipo.equalsIgnoreCase("VIDEO")) {
                String errorMsg = "Tipo de archivo no válido. Debe ser FOTO o VIDEO";
                log.warn(errorMsg);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMsg));
            }

            // Verificar que el archivo no esté vacío
            log.debug("Verificando si el archivo está vacío...");
            if (archivo.isEmpty()) {
                String errorMsg = "El archivo no puede estar vacío";
                log.warn(errorMsg);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMsg));
            }

            // Verificar que el equipo y la cámara existen
            log.debug("Buscando equipo con ID: {}", idEquipo);
            Equipo equipo = equipoRepository.findById(idEquipo)
                    .orElseThrow(() -> {
                        String errorMsg = "Equipo no encontrado con ID: " + idEquipo;
                        log.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });
            
            log.debug("Buscando cámara con ID: {}", idCamara);
            Camara camara = camaraRepository.findById(idCamara)
                    .orElseThrow(() -> {
                        String errorMsg = "Cámara no encontrada con ID: " + idCamara;
                        log.error(errorMsg);
                        return new RuntimeException(errorMsg);
                    });

            // Verificar que la cámara pertenece al equipo (comprobar ambas relaciones)
            log.debug("Verificando si la cámara {} pertenece al equipo {}", camara.getIdCamara(), equipo.getIdEquipo());
            boolean relacionDirecta = equipo.getCamaras().contains(camara);
            boolean relacionInversa = camara.getEquipo() != null && camara.getEquipo().getIdEquipo().equals(equipo.getIdEquipo());
            
            if (!relacionDirecta && !relacionInversa) {
                String errorMsg = String.format("La cámara %s no está asociada al equipo %s. " +
                    "Verifique las relaciones en la base de datos.", camara.getIdCamara(), equipo.getIdEquipo());
                log.warn(errorMsg);
                
                // Intentar corregir la relación si es necesario
                try {
                    log.info("Intentando corregir la relación entre cámara {} y equipo {}", camara.getIdCamara(), equipo.getIdEquipo());
                    camara.setEquipo(equipo);
                    camaraRepository.save(camara);
                    equipo.getCamaras().add(camara);
                    equipoRepository.save(equipo);
                    log.info("Relación corregida exitosamente");
                } catch (Exception e) {
                    log.error("Error al intentar corregir la relación: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", errorMsg + " No se pudo corregir automáticamente."));
                }
            } else if (relacionDirecta != relacionInversa) {
                // Hay una inconsistencia en las relaciones, corregirla
                log.warn("Inconsistencia detectada en las relaciones entre cámara {} y equipo {}", 
                    camara.getIdCamara(), equipo.getIdEquipo());
                try {
                    if (relacionDirecta && !relacionInversa) {
                        camara.setEquipo(equipo);
                    } else if (!relacionDirecta && relacionInversa) {
                        equipo.getCamaras().add(camara);
                    }
                    camaraRepository.save(camara);
                    equipoRepository.save(equipo);
                    log.info("Inconsistencia en relaciones corregida");
                } catch (Exception e) {
                    log.error("Error al corregir inconsistencia en relaciones: {}", e.getMessage());
                }
            }

            // Crear directorio si no existe
            Path directorioFinal = directorioArchivos
                .resolve(equipo.getIdEquipo().toString())
                .resolve(camara.getIdCamara().toString())
                .resolve(tipo.toLowerCase());
            
            log.debug("Creando directorio para el archivo: {}", directorioFinal);
            try {
                Files.createDirectories(directorioFinal);
                log.info("Directorio creado exitosamente: {}", directorioFinal);
            } catch (IOException e) {
                String errorMsg = "Error al crear directorio: " + e.getMessage();
                log.error(errorMsg, e);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", errorMsg));
            }

            // Generar nombre único para el archivo
            String nombreOriginal = archivo.getOriginalFilename();
            if (nombreOriginal == null || nombreOriginal.isEmpty()) {
                String errorMsg = "El nombre del archivo no puede ser nulo o vacío";
                log.warn(errorMsg);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMsg));
            }
            
            log.debug("Procesando archivo original: {}", nombreOriginal);
            String extension = "";
            int lastDot = nombreOriginal.lastIndexOf('.');
            if (lastDot > 0) {
                extension = nombreOriginal.substring(lastDot);
                log.debug("Extensión detectada: {}", extension);
            }
            
            String nombreArchivo = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                                 "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            log.debug("Nombre de archivo generado: {}", nombreArchivo);
            
            Path rutaArchivo = directorioFinal.resolve(nombreArchivo);
            log.debug("Ruta completa del archivo: {}", rutaArchivo);

            // Guardar archivo en disco
            log.debug("Guardando archivo en disco...");
            try (InputStream inputStream = archivo.getInputStream()) {
                long bytesCopied = Files.copy(inputStream, rutaArchivo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("Archivo guardado exitosamente. Tamaño: {} bytes", bytesCopied);
            } catch (IOException e) {
                String errorMsg = "Error al guardar el archivo: " + e.getMessage();
                log.error(errorMsg, e);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", errorMsg));
            }

            // Crear registro en base de datos
            log.debug("Creando registro en la base de datos...");
            try {
                ArchivoMultimedia archivoMultimedia = new ArchivoMultimedia();
                archivoMultimedia.setNombreArchivo(nombreArchivo);
                archivoMultimedia.setRutaArchivo(rutaArchivo.toString());
                archivoMultimedia.setTipo(ArchivoMultimedia.TipoArchivo.valueOf(tipo.toUpperCase()));
                archivoMultimedia.setFechaCaptura(LocalDateTime.now());
                archivoMultimedia.setFechaSubida(LocalDateTime.now());
                archivoMultimedia.setCamara(camara);
                archivoMultimedia.setEquipo(equipo);

                ArchivoMultimedia archivoGuardado = archivoRepository.save(archivoMultimedia);
                log.info("Registro de archivo guardado en la base de datos con ID: {}", archivoGuardado.getIdArchivo());
                
                // Crear respuesta con información del archivo subido
                Map<String, Object> response = new HashMap<>();
                response.put("id", archivoGuardado.getIdArchivo());
                response.put("nombreArchivo", archivoGuardado.getNombreArchivo());
                response.put("ruta", archivoGuardado.getRutaArchivo());
                response.put("tipo", archivoGuardado.getTipo().toString());
                response.put("fechaSubida", archivoGuardado.getFechaSubida());
                response.put("camaraId", camara.getIdCamara());
                response.put("equipoId", equipo.getIdEquipo());
                response.put("mensaje", "Archivo subido exitosamente");
                
                log.info("Archivo subido exitosamente: {}", response);
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                // Intentar eliminar el archivo si falla el guardado en la base de datos
                try {
                    Files.deleteIfExists(rutaArchivo);
                    log.warn("Archivo eliminado del sistema de archivos después de un error en la base de datos");
                } catch (IOException ex) {
                    log.error("Error al intentar eliminar el archivo después de un error en la base de datos", ex);
                }
                
                String errorMsg = "Error al guardar en la base de datos: " + e.getMessage();
                log.error(errorMsg, e);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", errorMsg));
            }
            
        } catch (Exception e) {
            String errorMsg = "Error al procesar la solicitud: " + e.getMessage();
            log.error(errorMsg, e);
            
            if (e instanceof RuntimeException) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", errorMsg));
            } else {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", errorMsg));
            }
        }
    }

    @GetMapping("/equipos/{idEquipo}/archivos")
    public List<ArchivoMultimediaDTO> obtenerArchivosEquipo(@PathVariable Long idEquipo) {
        return archivoRepository.findByEquipoIdEquipo(idEquipo).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @GetMapping("/camaras/{idCamara}/archivos")
    public List<ArchivoMultimediaDTO> obtenerArchivosCamara(@PathVariable Long idCamara) {
        return archivoRepository.findByCamara_IdCamara(idCamara).stream()
                .map(this::convertirADTO)
                .toList();
    }

    private ArchivoMultimediaDTO convertirADTO(ArchivoMultimedia archivo) {
        ArchivoMultimediaDTO dto = new ArchivoMultimediaDTO();
        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setRutaArchivo(archivo.getRutaArchivo());
        dto.setTipo(archivo.getTipo().toString());
        dto.setFechaCaptura(archivo.getFechaCaptura());
        dto.setFechaSubida(archivo.getFechaSubida());
        dto.setCamaraId(archivo.getCamara().getIdCamara());
        dto.setEquipoId(archivo.getEquipo().getIdEquipo());
        return dto;
    }
} 