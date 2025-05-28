package com.example.gestorcamras.security;

import com.example.gestorcamras.dto.UsuarioDTO;
import com.example.gestorcamras.model.Rol;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.RolRepository;
import com.example.gestorcamras.repository.UsuarioRepository;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolRepository rolRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        try {
            // Autenticar al usuario
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena()));
            
            // Obtener el usuario
            Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    
            // Verificar si es una solicitud de la aplicación de escritorio
            String userAgent = httpRequest.getHeader("User-Agent") != null ? 
                    httpRequest.getHeader("User-Agent").toLowerCase() : "";
                    
            boolean isDesktopApp = userAgent.contains("java") || 
                               userAgent.contains("javafx") || 
                               userAgent.contains("desktop");
            
            if (isDesktopApp) {
                // Para la aplicación de escritorio, devolvemos un JSON con el formato que espera el cliente
                JSONObject response = new JSONObject();
                response.put("nombreRol", usuario.getRol().getNombre().toUpperCase()); // Aseguramos mayúsculas para consistencia
                response.put("mensaje", "Login exitoso");
                response.put("usuario", usuario.getNombre());
                
                return ResponseEntity.ok(response.toString());
            } else {
                // Para la aplicación web, redirigir al dashboard
                return ResponseEntity.ok("Login exitoso");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<String> register(@RequestBody UsuarioDTO dto) {
        if (usuarioRepository.findByCorreo(dto.getCorreo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Correo ya registrado");
        }

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + dto.getRolId()));


        Usuario nuevo = new Usuario();
        nuevo.setCorreo(dto.getCorreo());
        nuevo.setNombre(dto.getNombre());
        nuevo.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        nuevo.setFechaRegistro(LocalDateTime.now());
        nuevo.setRol(rol);

        usuarioRepository.save(nuevo);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

}