package com.example.gestorcamras.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.gestorcamras.dto.UsuarioDTO;
import com.example.gestorcamras.service.UsuarioService;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.UsuarioRepository;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/usuario/actual")
    public ResponseEntity<?> obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
            }
            
            String email = auth.getName();
            System.out.println("Obteniendo usuario para email: " + email);
            
            // Obtener el usuario directamente del repositorio
            Usuario usuario = usuarioRepository.findByCorreo(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));
            
            // Crear el DTO manualmente para evitar problemas de serialización
            UsuarioDTO dto = new UsuarioDTO();
            dto.setIdUsuario(usuario.getIdUsuario());
            dto.setNombre(usuario.getNombre());
            dto.setCorreo(usuario.getCorreo());
            dto.setFechaRegistro(usuario.getFechaRegistro());
            
            // Agregar información del rol
            if (usuario.getRol() != null) {
                dto.setRolId(usuario.getRol().getIdRol());
                dto.setNombreRol(usuario.getRol().getNombre());
            } else {
                System.out.println("Advertencia: El usuario " + email + " no tiene un rol asignado");
                dto.setRolId(null);
                dto.setNombreRol(null);
            }
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            System.err.println("Error en obtenerUsuarioActual: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el usuario actual: " + e.getMessage());
        }
    }

    @GetMapping("/usuarios")
    public List<UsuarioDTO> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioDTO obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @PostMapping("/usuarios")
    public UsuarioDTO guardar(@RequestBody UsuarioDTO usuarioDTO) {
        return usuarioService.guardarUsuario(usuarioDTO);
    }

    @DeleteMapping("/usuarios/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}
