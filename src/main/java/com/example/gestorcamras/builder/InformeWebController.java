package com.example.gestorcamras.builder;

import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.repository.ArchivoMultimediaRepository;
import com.example.gestorcamras.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ADMINISTRADOR")
public class InformeWebController {

    private final EquipoService equipoService;
    private final ArchivoMultimediaRepository archivoMultimediaRepository;

    @Autowired
    public InformeWebController(EquipoService equipoService, ArchivoMultimediaRepository archivoMultimediaRepository) {
        this.equipoService = equipoService;
        this.archivoMultimediaRepository = archivoMultimediaRepository;
    }

    @GetMapping("/informes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarInformes(Model model) {
        List<Equipo> equipos = equipoService.obtenerTodos().stream()
                .map(dto -> equipoService.obtenerEntidadPorId(dto.getIdEquipo()).orElse(null))
                .filter(equipo -> equipo != null)
                .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("equipos", equipos);
        return "informes";
    }
    
    @GetMapping("/informes/equipo/{equipoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public String verInformeEquipo(@PathVariable Long equipoId, Model model) {
        System.out.println("=== INICIO verInformeEquipo ===");
        System.out.println("ID de equipo recibido: " + equipoId);
        
        try {
            System.out.println("Buscando equipo con ID: " + equipoId);
            
            // Obtener el equipo usando el servicio
            Optional<Equipo> equipoOpt = equipoService.obtenerEntidadPorId(equipoId);
            
            if (!equipoOpt.isPresent()) {
                System.err.println("No se encontró el equipo con ID: " + equipoId);
                return "redirect:/ADMINISTRADOR/informes?error=equipo_no_encontrado";
            }
            
            Equipo equipo = equipoOpt.get();
            System.out.println("Equipo encontrado: " + equipo.getNombre());
            
            // Crear el informe usando el patrón Builder
            InformeBuilder builder = new InformeBuilder()
                    .conTitulo("Informe de Equipo: " + equipo.getNombre())
                    .conEquipo(equipo);
                    
            // Si el equipo tiene cámaras, las agregamos al informe
            if (equipo.getCamaras() != null && !equipo.getCamaras().isEmpty()) {
                builder.conCamaras(new java.util.ArrayList<>(equipo.getCamaras()));
            }
            
            // Obtener archivos multimedia del equipo
            List<ArchivoMultimedia> archivos = archivoMultimediaRepository.findByEquipoIdEquipo(equipoId);
            if (archivos != null && !archivos.isEmpty()) {
                builder.conArchivosMultimedia(archivos);
            }
            
            // Construir el informe
            String contenidoInforme = builder.construir();
            
            // Agregar los atributos al modelo
            model.addAttribute("contenidoInforme", contenidoInforme);
            model.addAttribute("equipo", equipo);
            model.addAttribute("fechaGeneracion", java.time.LocalDateTime.now());
            
            System.out.println("Redirigiendo a la plantilla informeEquipo");
            return "informeEquipo";
        } catch (Exception e) {
            System.err.println("Error en verInformeEquipo: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/ADMINISTRADOR/informes?error=error_interno";
        } finally {
            System.out.println("=== FIN verInformeEquipo ===");
        }
    }
}
