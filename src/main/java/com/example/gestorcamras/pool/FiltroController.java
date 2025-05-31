package com.example.gestorcamras.pool;

import com.example.gestorcamras.dto.FiltroDTO;

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

    @GetMapping
    public List<FiltroDTO> listarFiltros(@RequestParam(required = false) String tipo) {
        // Si se requiere filtrar por tipo, filtrar sobre los DTOs
        List<FiltroDTO> filtros = filtroService.obtenerTodos();
        if (tipo != null && !tipo.isEmpty()) {
            return filtros.stream()
                    .filter(f -> f.getTipo().toLowerCase().contains(tipo.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return filtros;
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiltroDTO> obtenerFiltroPorId(@PathVariable Long id) {
        return filtroService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FiltroDTO> crearFiltro(@RequestBody FiltroDTO filtroDTO) {
        if (filtroService.obtenerTodos().stream()
                .anyMatch(f -> f.getTipo().equalsIgnoreCase(filtroDTO.getTipo()))) {
            return ResponseEntity.badRequest().build(); // evita duplicados por tipo
        }
        FiltroDTO guardado = filtroService.guardarFiltro(filtroDTO);
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiltroDTO> actualizarFiltro(@PathVariable Long id, @RequestBody FiltroDTO filtroDTO) {
        if (filtroService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        filtroDTO.setIdFiltro(id);
        FiltroDTO actualizado = filtroService.guardarFiltro(filtroDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFiltro(@PathVariable Long id) {
        if (filtroService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        filtroService.eliminarFiltro(id);
        return ResponseEntity.noContent().build();
    }
}
