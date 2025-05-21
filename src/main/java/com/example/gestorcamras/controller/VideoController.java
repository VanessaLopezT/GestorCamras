package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.VideoDTO;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.model.Video;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.service.EquipoService;
import com.example.gestorcamras.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipos/{equipoId}/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private CamaraService camaraService;

    @PostMapping
    public ResponseEntity<VideoDTO> subirVideoEquipo(
            @PathVariable Long equipoId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("nombreCamara") String nombreCamara,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam("duracion") Duration duracion
    ) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Equipo> equipoOpt = equipoService.obtenerEntidadPorId(equipoId);
        if (equipoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Equipo equipo = equipoOpt.get();

        Optional<Camara> camaraOpt = camaraService.obtenerPorNombreYEquipo(nombreCamara, equipo);
        if (camaraOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Camara camara = camaraOpt.get();

        String rutaAlmacenamiento = "/ruta/videos/" + archivo.getOriginalFilename();
        try {
            archivo.transferTo(new File(rutaAlmacenamiento));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Video video = new Video();
        video.setNombre(archivo.getOriginalFilename());
        video.setTamaño(archivo.getSize());
        video.setFechaCaptura(timestamp);
        video.setDuracion(duracion);
        video.setRutaAlmacenamiento(rutaAlmacenamiento);
        video.setCamara(camara);

        VideoDTO videoGuardado = videoService.guardarVideo(video);
        return ResponseEntity.status(HttpStatus.CREATED).body(videoGuardado);
    }
}
