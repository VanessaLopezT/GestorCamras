package com.example.gestorcamras.controller;

import com.example.gestorcamras.model.Rol;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.RolRepository;
import com.example.gestorcamras.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/ADMINISTRADOR/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioAdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        // Asignar rol si viene en el formulario
        if (usuario.getRol() != null && usuario.getRol().getIdRol() != null) {
            Rol rol = rolRepository.findById(usuario.getRol().getIdRol()).orElse(null);
            usuario.setRol(rol);
        }

        if (usuario.getIdUsuario() != null) {
            // EDICIÓN
            Usuario existente = usuarioRepository.findById(usuario.getIdUsuario()).orElse(null);
            if (existente != null) {
                // Si no se ingresó nueva contraseña, conservar la actual
                if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
                    usuario.setContrasena(existente.getContrasena());
                } else {
                    // Se ingresó nueva contraseña: codificarla
                    usuario.setContrasena(new BCryptPasswordEncoder().encode(usuario.getContrasena()));
                }

                // Conservar fecha de registro original
                usuario.setFechaRegistro(existente.getFechaRegistro());
            }
        } else {
            // CREACIÓN
            if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "La contraseña no puede estar vacía para un nuevo usuario.");
                return "redirect:/ADMINISTRADOR/usuarios/nuevo";
            }
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setContrasena(new BCryptPasswordEncoder().encode(usuario.getContrasena()));
        }

        // Guardar usuario (ya sea nuevo o editado)
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("exito", "Usuario guardado exitosamente");
        return "redirect:/ADMINISTRADOR/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/ADMINISTRADOR/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/formulario";
    }

}

