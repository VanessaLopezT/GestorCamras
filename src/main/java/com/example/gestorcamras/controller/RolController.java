package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.RolDTO;

import com.example.gestorcamras.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping
    public List<RolDTO> listarRoles() {
        return rolService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolDTO> obtenerRolPorId(@PathVariable Long id) {
        Optional<RolDTO> rolOpt = rolService.obtenerPorId(id);
        return rolOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RolDTO crearRol(@RequestBody RolDTO rolDTO) {
        return rolService.guardarRol(rolDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolDTO> actualizarRol(@PathVariable Long id, @RequestBody RolDTO rolDTO) {
        Optional<RolDTO> rolOpt = rolService.obtenerPorId(id);
        if (rolOpt.isPresent()) {
            rolDTO.setIdRol(id);
            return ResponseEntity.ok(rolService.guardarRol(rolDTO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        Optional<RolDTO> rolOpt = rolService.obtenerPorId(id);
        if (rolOpt.isPresent()) {
            rolService.eliminarRol(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
