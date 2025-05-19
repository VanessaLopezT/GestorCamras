package com.example.gestorcamras;

import com.example.gestorcamras.model.Rol;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.RolRepository;
import com.example.gestorcamras.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolRepository rolRepository;

    @PostConstruct
    public void init() {
        crearRolSiNoExiste("ADMINISTRADOR");
        crearRolSiNoExiste("OPERADOR");
        crearRolSiNoExiste("VISUALIZADOR");
        if (usuarioRepository.findByCorreo("admin@gestor.com").isEmpty()) {
            Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("ADMINISTRADOR");
                        return rolRepository.save(nuevoRol);
                    });

            Usuario admin = new Usuario();
            admin.setCorreo("admin@gestor.com");
            admin.setNombre("Administrador por defecto");
            admin.setContrasena(passwordEncoder.encode("admin123"));
            admin.setRol(rolAdmin);
            admin.setFechaRegistro(LocalDateTime.now());

            usuarioRepository.save(admin);
        }
    }

    private void crearRolSiNoExiste(String nombreRol) {
        if (rolRepository.findByNombre(nombreRol).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombreRol);
            rolRepository.save(rol);
        }
    }
}

