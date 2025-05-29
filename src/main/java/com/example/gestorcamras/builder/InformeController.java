package com.example.gestorcamras.builder;

import com.example.gestorcamras.model.Informe;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de informes.
 * Expone endpoints para crear, recuperar y gestionar informes del sistema.
 */
@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final InformeService informeService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public InformeController(InformeService informeService, UsuarioRepository usuarioRepository) {
        this.informeService = informeService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene todos los informes del sistema.
     */
    @GetMapping
    @Cacheable("informes")
    public List<InformeDTO> listarInformes() {
        return informeService.obtenerTodos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un informe por su ID.
     */
    @GetMapping("/{id}")
    @Cacheable(value = "informes", key = "#id")
    public ResponseEntity<InformeDTO> obtenerPorId(@PathVariable Long id) {
        return informeService.obtenerPorId(id)
                .map(informe -> ResponseEntity.ok(toDTO(informe)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo informe básico.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "informes", allEntries = true)
    public InformeDTO crearInforme(@RequestBody CrearInformeRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Informe informe = informeService.crearInformeBasico(
                request.getTitulo(),
                request.getContenido(),
                request.getTamanio(),
                usuario
        );
        
        return toDTO(informe);
    }
    
    /**
     * Genera un informe detallado para un equipo específico.
     */
    @PostMapping("/equipo")
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "informes", allEntries = true)
    public InformeDTO generarInformeEquipo(@RequestBody InformeEquipoRequest request) {
        Informe informe = informeService.generarInformeEquipo(
                request.getTitulo(),
                request.getDescripcion(),
                request.getEquipoId(),
                request.getUsuarioId()
        );
        return toDTO(informe);
    }
    
    /**
     * Muestra la vista de un informe de equipo.
     */
    @GetMapping("/equipo/{equipoId}")
    public String verInformeEquipo(@PathVariable Long equipoId, org.springframework.ui.Model model) {
        // Obtener el ID del usuario autenticado (puedes modificar esto según tu sistema de autenticación)
        Long usuarioId = 1L; // Ejemplo: Obtener el ID del usuario autenticado
        
        // Generar el informe
        Informe informe = informeService.generarInformeEquipo(
            "Informe de Equipo",
            "Informe detallado del equipo",
            equipoId,
            usuarioId
        );
        
        // Agregar el informe al modelo para la vista
        model.addAttribute("informe", informe);
        
        // Devolver el nombre de la plantilla Thymeleaf
        return "informeEquipo";
    }
    
    /**
     * Genera un informe de actividad del sistema para un período específico.
     */
    @GetMapping("/actividad")
    @Cacheable(value = "informes", key = "#fechaInicio.toString() + '-' + #fechaFin.toString()")
    public InformeDTO generarInformeActividad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam Long usuarioId) {
        
        Informe informe = informeService.generarInformeActividad(fechaInicio, fechaFin, usuarioId);
        return toDTO(informe);
    }
    
    // Métodos auxiliares
    
    private InformeDTO toDTO(Informe informe) {
        InformeDTO dto = new InformeDTO();
        dto.setIdInfo(informe.getIdInfo());
        dto.setTitulo(informe.getTitulo());
        dto.setFechaGeneracion(informe.getFechaGeneracion());
        dto.setTamaño(informe.getTamaño());
        dto.setNombreEquipo(informe.getNombreEquipo());
        dto.setIpEquipo(informe.getIpEquipo());
        dto.setTipoEquipo(informe.getTipoEquipo());
        dto.setUbicacionEquipo(informe.getUbicacionEquipo());
        dto.setActivo(informe.isActivo());
        
        if (informe.getUsuario() != null) {
            dto.setUsuarioId(informe.getUsuario().getIdUsuario());
            dto.setNombreUsuario(informe.getUsuario().getNombre());
        }
        
        return dto;
    }
    
    // Clases DTO para las solicitudes
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CrearInformeRequest {
        private String titulo;
        private String contenido;
        private double tamanio;
        private Long usuarioId;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InformeEquipoRequest {
        private String titulo;
        private String descripcion;
        private Long equipoId;
        private Long usuarioId;
    }
    public InformeDTO crearInforme(@RequestBody InformeDTO informeDTO) {
        // Buscar el usuario por ID
        Usuario usuario = usuarioRepository.findById(informeDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + informeDTO.getUsuarioId()));
            
        // Crear el informe básico
        Informe informe = informeService.crearInformeBasico(
            informeDTO.getTitulo(),
            informeDTO.getContenido(),
            informeDTO.getTamaño(),
            usuario
        );
        
        // Guardar y retornar el informe
        Informe guardado = informeService.guardarInforme(informe);
        return toDTO(guardado);
    }

    // Actualizar informe (limpiar cache)
    @PutMapping("/{id}")
    @CacheEvict(value = {"informesCache", "informeCache"}, allEntries = true)
    public ResponseEntity<InformeDTO> actualizarInforme(@PathVariable Long id, @RequestBody InformeDTO informeDTO) {
        return informeService.obtenerPorId(id).map(informe -> {
            informe.setTitulo(informeDTO.getTitulo());
            informe.setFechaGeneracion(informeDTO.getFechaGeneracion());
            informe.setTamaño(informeDTO.getTamaño());
            informe.setContenido(informeDTO.getContenido());
            Informe actualizado = informeService.guardarInforme(informe);
            return ResponseEntity.ok(toDTO(actualizado));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Eliminar informe (limpiar cache)
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"informesCache", "informeCache"}, allEntries = true)
    public ResponseEntity<Void> eliminarInforme(@PathVariable Long id) {
        if (informeService.obtenerPorId(id).isPresent()) {
            informeService.eliminarInforme(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
