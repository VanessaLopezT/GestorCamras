package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.InformeDTO;
import com.example.gestorcamras.model.Informe;
import com.example.gestorcamras.service.InformeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    @Autowired
    private InformeService informeService;

    // Convertir entity a DTO
    private InformeDTO toDTO(Informe informe) {
        InformeDTO dto = new InformeDTO();
        dto.setIdInfo(informe.getIdInfo());
        dto.setTitulo(informe.getTitulo());
        dto.setFechaGeneracion(informe.getFechaGeneracion());
        dto.setTamaño(informe.getTamaño());
        dto.setContenido(informe.getContenido());
        dto.setUsuarioId(informe.getUsuario() != null ? informe.getUsuario().getIdUsuario() : null);
        return dto;
    }

    // Listar todos (cacheable porque es consulta frecuente)
    @GetMapping
    @Cacheable(value = "informesCache")
    public List<InformeDTO> listarInformes() {
        return informeService.obtenerTodos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Obtener por id (cacheable)
    @GetMapping("/{id}")
    @Cacheable(value = "informeCache", key = "#id")
    public ResponseEntity<InformeDTO> obtenerInformePorId(@PathVariable Long id) {
        return informeService.obtenerPorId(id)
                .map(informe -> ResponseEntity.ok(toDTO(informe)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear informe (limpiar cache)
    @PostMapping
    @CacheEvict(value = {"informesCache", "informeCache"}, allEntries = true)
    public InformeDTO crearInforme(@RequestBody InformeDTO informeDTO) {
        Informe informe = new Informe();
        informe.setTitulo(informeDTO.getTitulo());
        informe.setFechaGeneracion(informeDTO.getFechaGeneracion());
        informe.setTamaño(informeDTO.getTamaño());
        informe.setContenido(informeDTO.getContenido());
        // Usuario lo debe setear el Service con findById para evitar inconsistencias
        // o enviar como entidad ya seteada
        // Aquí solo seteamos id, suponiendo que el Service se encargue
        // Para simplificar aquí no lo seteo.

        Informe guardado = informeService.guardarInforme(informe);
        return toDTO(guardado);
    }

    // Actualizar informe (limpiar cache)
    @PutMapping("/{id}")
    @CacheEvict(value = {"informesCache", "informeCache"}, allEntries = true)
    public ResponseEntity<InformeDTO> actualizarInforme(@PathVariable Long id, @RequestBody InformeDTO informeDTO) {
        return informeService.obtenerPorId(id).map(informe -> {
            informe.setTitulo(informeDTO.getTitulo());
            informe.setFechaGeneracion(informeDTO.getFechaGeneracion());
            informe.setTamaño(informeDTO.getTamaño());
            informe.setContenido(informeDTO.getContenido());
            Informe actualizado = informeService.guardarInforme(informe);
            return ResponseEntity.ok(toDTO(actualizado));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Eliminar informe (limpiar cache)
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"informesCache", "informeCache"}, allEntries = true)
    public ResponseEntity<Void> eliminarInforme(@PathVariable Long id) {
        if (informeService.obtenerPorId(id).isPresent()) {
            informeService.eliminarInforme(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
