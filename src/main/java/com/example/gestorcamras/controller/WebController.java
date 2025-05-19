package com.example.gestorcamras.controller;
import com.example.gestorcamras.dto.UsuarioDTO;
import com.example.gestorcamras.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class WebController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        model.addAttribute("usuario", auth.getName());
        return "dashboard";
    }
}
