package com.example.gestorcamras.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.gestorcamras.dto.UsuarioDTO;
import com.example.gestorcamras.service.UsuarioService;

@Controller
public class WebController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String mostrarDashboard(Model model, Authentication auth) {
        model.addAttribute("usuario", auth.getName());
        return "dashboard";
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(Model model) {
        List<UsuarioDTO> usuarios = usuarioService.obtenerTodos();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista";
    }

    @GetMapping("/usuarios/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        return "usuarios/formulario";
    }

    @PostMapping("/usuarios/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarUsuario(@ModelAttribute UsuarioDTO usuario) {
        usuarioService.guardarUsuario(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, Model model) {
        UsuarioDTO usuario = usuarioService.obtenerPorId(id).orElseThrow();
        model.addAttribute("usuario", usuario);
        return "usuarios/formulario";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/usuarios";
    }
}
