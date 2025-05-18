package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.FiltroDTO;
import com.example.gestorcamras.model.Filtro;
import com.example.gestorcamras.service.FiltroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/filtros")
public class FiltroController {

    @Autowired
    private FiltroService filtroService;

    // Convertir entidad a DTO
    private FiltroDTO convertirADTO(Filtro filtro) {
        return new FiltroDTO(filtro.getIdFiltro(), filtro.getTipo(), filtro.getDescripcion());
    }

    // Convertir DTO a entidad
    private Filtro convertirAEntidad(FiltroDTO dto) {
        Filtro filtro = new Filtro();
        filtro.setIdFiltro(dto.getIdFiltro());
        filtro.setTipo(dto.getTipo());
        filtro.setDescripcion(dto.getDescripcion());
        return filtro;
    }

    @GetMapping
    public List<FiltroDTO> listarFiltros(@RequestParam(required = false) String tipo) {
        List<Filtro> filtros;
        if (tipo != null && !tipo.isEmpty()) {
            filtros = filtroService.obtenerTodos().stream()
                    .filter(f -> f.getTipo().toLowerCase().contains(tipo.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            filtros = filtroService.obtenerTodos();
        }
        return filtros.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiltroDTO> obtenerFiltroPorId(@PathVariable Long id) {
        return filtroService.obtenerPorId(id)
                .map(filtro -> ResponseEntity.ok(convertirADTO(filtro)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FiltroDTO> crearFiltro(@RequestBody FiltroDTO filtroDTO) {
        if (filtroService.obtenerTodos().stream()
                .anyMatch(f -> f.getTipo().equalsIgnoreCase(filtroDTO.getTipo()))) {
            return ResponseEntity.badRequest().build(); // evita duplicados por tipo
        }
        Filtro filtroGuardado = filtroService.guardarFiltro(convertirAEntidad(filtroDTO));
        return ResponseEntity.ok(convertirADTO(filtroGuardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiltroDTO> actualizarFiltro(@PathVariable Long id, @RequestBody FiltroDTO filtroDTO) {
        return filtroService.obtenerPorId(id).map(filtro -> {
            filtro.setTipo(filtroDTO.getTipo());
            filtro.setDescripcion(filtroDTO.getDescripcion());
            Filtro filtroActualizado = filtroService.guardarFiltro(filtro);
            return ResponseEntity.ok(convertirADTO(filtroActualizado));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFiltro(@PathVariable Long id) {
        if (filtroService.obtenerPorId(id).isPresent()) {
            filtroService.eliminarFiltro(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
