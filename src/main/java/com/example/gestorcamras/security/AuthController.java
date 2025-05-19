package com.example.gestorcamras.security;

import com.example.gestorcamras.dto.UsuarioDTO;
import com.example.gestorcamras.model.Rol;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.RolRepository;
import com.example.gestorcamras.repository.UsuarioRepository;

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
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena()));

        return ResponseEntity.ok("Login exitoso"); // eventualmente aquí devuelves JWT
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
