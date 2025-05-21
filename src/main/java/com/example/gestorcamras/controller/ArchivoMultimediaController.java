package com.example.gestorcamras.controller;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api")
public class ArchivoMultimediaController {

    @Autowired
    private ArchivoMultimediaRepository archivoRepository;

    @Autowired
    private CamaraRepository camaraRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    private final Path directorioArchivos = Paths.get("archivos_multimedia");

    public ArchivoMultimediaController() {
        try {
            Files.createDirectories(directorioArchivos);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio para archivos multimedia", e);
        }
    }

    @PostMapping("/equipos/{idEquipo}/camaras/{idCamara}/archivo")
    public ResponseEntity<Void> subirArchivo(
            @PathVariable Long idEquipo,
            @PathVariable Long idCamara,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("tipo") String tipo) {
        
        try {
            Equipo equipo = equipoRepository.findById(idEquipo)
                    .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
            
            Camara camara = camaraRepository.findById(idCamara)
                    .orElseThrow(() -> new RuntimeException("Cámara no encontrada"));

            // Verificar que la cámara pertenece al equipo
            if (!equipo.getCamaras().contains(camara)) {
                return ResponseEntity.badRequest().build();
            }

            // Crear nombre único para el archivo
            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaArchivo = directorioArchivos.resolve(nombreArchivo);

            // Guardar archivo en disco
            Files.copy(archivo.getInputStream(), rutaArchivo);

            // Crear registro en base de datos
            ArchivoMultimedia archivoMultimedia = new ArchivoMultimedia();
            archivoMultimedia.setNombreArchivo(nombreArchivo);
            archivoMultimedia.setRutaArchivo(rutaArchivo.toString());
            archivoMultimedia.setTipo(ArchivoMultimedia.TipoArchivo.valueOf(tipo.toUpperCase()));
            archivoMultimedia.setFechaCaptura(LocalDateTime.now());
            archivoMultimedia.setFechaSubida(LocalDateTime.now());
            archivoMultimedia.setCamara(camara);
            archivoMultimedia.setEquipo(equipo);

            archivoRepository.save(archivoMultimedia);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
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