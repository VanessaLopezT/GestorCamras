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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.gestorcamras.dto.NotificacionFiltroDTO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.example.gestorcamras.dto.ArchivoMultimediaDTO;
import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.repository.ArchivoMultimediaRepository;
import com.example.gestorcamras.repository.CamaraRepository;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.service.EquipoService;
import com.example.gestorcamras.repository.FiltroAplicadoRepository;
import com.example.gestorcamras.model.FiltroAplicado;

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
    
    @Autowired
    private FiltroAplicadoRepository filtroAplicadoRepository;
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArchivoMultimediaController.class);
    
    /**
     * Endpoint para notificar que se ha aplicado un filtro a una imagen
     */
    @PostMapping("/archivos/notificar-filtro")
    @Transactional
    public ResponseEntity<?> notificarFiltroAplicado(@RequestBody NotificacionFiltroDTO notificacion) {
        try {
            // Registrar en los logs
            log.info("Filtro aplicado - Archivo ID: {}, Equipo: {}, Cámara: {}, Filtro: {}", 
                    notificacion.getIdArchivo(), 
                    notificacion.getIdEquipo(), 
                    notificacion.getIdCamara(), 
                    notificacion.getNombreFiltro());
            
            // Guardar en la base de datos
            ArchivoMultimedia archivo = archivoRepository.findById(notificacion.getIdArchivo())
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + notificacion.getIdArchivo()));
            
            FiltroAplicado filtro = new FiltroAplicado();
            filtro.setArchivo(archivo);
            filtro.setNombreFiltro(notificacion.getNombreFiltro());
            filtro.setFechaAplicacion(LocalDateTime.now());
            filtroAplicadoRepository.save(filtro);
            
            log.info("Filtro guardado en la base de datos: {}", filtro);
            
            return ResponseEntity.ok().body("Filtro aplicado correctamente");
        } catch (Exception e) {
            log.error("Error al registrar el filtro aplicado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el filtro: " + e.getMessage());
        }
    }

    private final Path directorioArchivos = Paths.get("archivos_multimedia");

    public ArchivoMultimediaController() {
        try {
            Files.createDirectories(directorioArchivos);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio para archivos multimedia", e);
        }
    }

    @PostMapping("/equipos/{idEquipo}/camaras/{idCamara}/archivo")
    @Transactional
    public ResponseEntity<?> subirArchivo(
            @PathVariable Long idEquipo,
            @PathVariable Long idCamara,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo) {
        
        try {
            // Validar tipo de archivo
            if (!tipo.equalsIgnoreCase("FOTO") && !tipo.equalsIgnoreCase("VIDEO")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tipo de archivo no válido. Debe ser FOTO o VIDEO"));
            }

            // Validar que el archivo no esté vacío
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se puede subir un archivo vacío"));
            }

            // Verificar que el equipo y la cámara existen
            Equipo equipo = equipoRepository.findById(idEquipo)
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + idEquipo));
            
            Camara camara = camaraRepository.findById(idCamara)
                    .orElseThrow(() -> new RuntimeException("Cámara no encontrada con ID: " + idCamara));

            // Verificar que la cámara pertenece al equipo (comprobar ambas relaciones)
            boolean relacionDirecta = equipo.getCamaras().contains(camara);
            boolean relacionInversa = camara.getEquipo() != null && camara.getEquipo().getIdEquipo().equals(equipo.getIdEquipo());
            
            if (!relacionDirecta && !relacionInversa) {
                String errorMsg = String.format("La cámara %s no está asociada al equipo %s", 
                    camara.getIdCamara(), equipo.getIdEquipo());
                
                // Intentar corregir la relación si es necesario
                try {
                    camara.setEquipo(equipo);
                    camaraRepository.save(camara);
                    equipo.getCamaras().add(camara);
                    equipoRepository.save(equipo);
                    log.info("Relación corregida: Cámara {} asociada al equipo {}", 
                        camara.getIdCamara(), equipo.getIdEquipo());
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", errorMsg));
                }
            } else if (relacionDirecta != relacionInversa) {
                // Corregir inconsistencia en las relaciones
                try {
                    if (relacionDirecta && !relacionInversa) {
                        camara.setEquipo(equipo);
                    } else if (!relacionDirecta && relacionInversa) {
                        equipo.getCamaras().add(camara);
                    }
                    camaraRepository.save(camara);
                    equipoRepository.save(equipo);
                } catch (Exception e) {
                    // Continuar aunque falle la corrección
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
                // Obtener la ruta relativa al directorio de archivos multimedia
                String rutaRelativa = directorioArchivos.relativize(rutaArchivo).toString();
                log.debug("Ruta relativa del archivo: {}", rutaRelativa);
                
                ArchivoMultimedia archivoMultimedia = new ArchivoMultimedia();
                archivoMultimedia.setNombreArchivo(nombreArchivo);
                archivoMultimedia.setRutaArchivo(rutaRelativa);
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
        return archivoRepository.findByEquipoIdEquipoWithCamara(idEquipo).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @GetMapping("/camaras/{idCamara}/archivos")
    public List<ArchivoMultimediaDTO> obtenerArchivosCamara(@PathVariable Long idCamara) {
        return archivoRepository.findByCamaraIdCamaraWithEquipo(idCamara).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @GetMapping("/archivos/{id}")
    public ResponseEntity<Resource> obtenerArchivo(@PathVariable Long id) {
        try {
            ArchivoMultimedia archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + id));
            
            Path rutaArchivo = directorioArchivos.resolve(archivo.getRutaArchivo()).normalize();
            
            // Verificar que el archivo existe y es legible
            if (!Files.exists(rutaArchivo) || !Files.isReadable(rutaArchivo)) {
                throw new RuntimeException("No se puede leer el archivo: " + rutaArchivo);
            }
            
            // Determinar el tipo de contenido
            String contentType = "application/octet-stream";
            if (archivo.getTipo() == ArchivoMultimedia.TipoArchivo.FOTO) {
                String nombreArchivo = archivo.getNombreArchivo().toLowerCase();
                if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (nombreArchivo.endsWith(".png")) {
                    contentType = "image/png";
                } else if (nombreArchivo.endsWith(".gif")) {
                    contentType = "image/gif";
                }
            } else if (archivo.getTipo() == ArchivoMultimedia.TipoArchivo.VIDEO) {
                String nombreArchivo = archivo.getNombreArchivo().toLowerCase();
                if (nombreArchivo.endsWith(".mp4")) {
                    contentType = "video/mp4";
                } else if (nombreArchivo.endsWith(".avi")) {
                    contentType = "video/x-msvideo";
                } else if (nombreArchivo.endsWith(".mov")) {
                    contentType = "video/quicktime";
                }
            }
            
            // Crear el recurso
            Resource resource = new UrlResource(rutaArchivo.toUri());
            
            // Construir la respuesta
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombreArchivo() + "\"")
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error al obtener el archivo: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
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