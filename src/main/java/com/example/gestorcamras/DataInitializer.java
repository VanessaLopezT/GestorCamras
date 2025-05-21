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
            Usuario adminGuardado = usuarioRepository.save(admin);
            System.out.println("Usuario Por defecto guardado con ID: " + adminGuardado.getIdUsuario());
        }
        else{ 
            System.out.println("Usuario Por defecto ya existe");
        }

        // Crear usuario operador por defecto
        if (usuarioRepository.findByCorreo("oper@gestor.com").isEmpty()) {
            Rol rolOperador = rolRepository.findByNombre("OPERADOR")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("OPERADOR");
                        return rolRepository.save(nuevoRol);
                    });

            Usuario operador = new Usuario();
            operador.setCorreo("oper@gestor.com");
            operador.setNombre("Operador por defecto");
            operador.setContrasena(passwordEncoder.encode("oper123"));
            operador.setRol(rolOperador);
            operador.setFechaRegistro(LocalDateTime.now());

            usuarioRepository.save(operador);
            Usuario operadorGuardado = usuarioRepository.save(operador);
            System.out.println("Usuario Operador guardado con ID: " + operadorGuardado.getIdUsuario());
        }
        else{ 
            System.out.println("Usuario Operador ya existe");
        }

        // Crear usuario visualizador por defecto
        if (usuarioRepository.findByCorreo("vis@gestor.com").isEmpty()) {
            Rol rolVisualizador = rolRepository.findByNombre("VISUALIZADOR")
                    .orElseGet(() -> {
                        Rol nuevoRol = new Rol();
                        nuevoRol.setNombre("VISUALIZADOR");
                        return rolRepository.save(nuevoRol);
                    });

            Usuario visualizador = new Usuario();
            visualizador.setCorreo("vis@gestor.com");
            visualizador.setNombre("Visualizador por defecto");
            visualizador.setContrasena(passwordEncoder.encode("vis123"));
            visualizador.setRol(rolVisualizador);
            visualizador.setFechaRegistro(LocalDateTime.now());

            usuarioRepository.save(visualizador);
            Usuario visualizadorGuardado = usuarioRepository.save(visualizador);
            System.out.println("Usuario Visualizador guardado con ID: " + visualizadorGuardado.getIdUsuario());
        }
        else{ 
            System.out.println("Usuario Visualizador ya existe");
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

