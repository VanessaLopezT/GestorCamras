package com.example.gestorcamras.controller;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.gestorcamras.dto.CamaraDTO;

@RestController
@RequestMapping("/api/camaras")
public class CamaraController {

    @Autowired
    private CamaraService camaraService;
    
    @Autowired
    private WebSocketService webSocketService;

    @GetMapping
    public List<CamaraDTO> listarCamaras() {
        return camaraService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CamaraDTO> obtenerCamaraPorId(@PathVariable Long id) {
        return camaraService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/equipo/{idEquipo}")
    public ResponseEntity<List<CamaraDTO>> obtenerCamarasPorEquipo(@PathVariable Long idEquipo) {
        List<CamaraDTO> camaras = camaraService.obtenerPorEquipo(idEquipo);
        return ResponseEntity.ok(camaras);
    }

    @PostMapping
    public ResponseEntity<CamaraDTO> crearCamara(@RequestBody CamaraDTO camaraDTO) {
        try {
            // Establecer valores por defecto si no están presentes
            if (camaraDTO.getFechaRegistro() == null) {
                camaraDTO.setFechaRegistro(LocalDateTime.now());
            }
            
            // Validar campos requeridos
            if (camaraDTO.getNombre() == null || camaraDTO.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Si la IP no está establecida, establecer un valor por defecto
            if (camaraDTO.getIp() == null || camaraDTO.getIp().trim().isEmpty()) {
                camaraDTO.setIp("0.0.0.0");
            }
            
            // Establecer estado activo por defecto si no está establecido
            camaraDTO.setActiva(true);
            
            // Guardar la cámara
            CamaraDTO guardada = camaraService.guardarCamara(camaraDTO);
            
            // Obtener la cámara recién guardada para asegurar que tenemos los datos más recientes
            if (guardada != null && guardada.getIdCamara() != null) {
                guardada = camaraService.obtenerPorId(guardada.getIdCamara())
                    .orElse(guardada); // Si no se puede obtener, usar la que ya tenemos
                
                // Notificar a través de WebSocket
                if (guardada.getEquipoId() != null) {
                    try {
                        Map<String, Object> mensaje = new HashMap<>();
                        mensaje.put("tipo", "nueva_camara");
                        mensaje.put("equipoId", guardada.getEquipoId());
                        mensaje.put("camara", guardada);
                        
                        // Enviar notificación
                        webSocketService.notificarNuevaCamara(guardada.getEquipoId(), mensaje);
                        
                        // Registrar la acción
                        System.out.println("Notificación WebSocket enviada para la cámara del equipo: " + guardada.getEquipoId());
                    } catch (Exception e) {
                        System.err.println("Error al enviar notificación WebSocket: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("No se pudo enviar notificación: equipoId es nulo");
                }
            }
            
            return ResponseEntity.ok(guardada);
        } catch (Exception e) {
            System.err.println("Error al guardar la cámara: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CamaraDTO> actualizarCamara(@PathVariable Long id, @RequestBody CamaraDTO camaraDetalle) {
        return camaraService.obtenerPorId(id).map(existing -> {
            camaraDetalle.setIdCamara(id);
            CamaraDTO actualizada = camaraService.guardarCamara(camaraDetalle);
            return ResponseEntity.ok(actualizada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCamara(@PathVariable Long id) {
        if (camaraService.obtenerPorId(id).isPresent()) {
            camaraService.eliminarCamara(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

