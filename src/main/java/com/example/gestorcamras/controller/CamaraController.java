package com.example.gestorcamras.controller;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        CamaraDTO guardada = camaraService.guardarCamara(camaraDTO);
        
        // Notificar a través de WebSocket
        if (guardada != null && guardada.getEquipoId() != null) {
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
            System.err.println("No se pudo enviar notificación: cámara o equipoId es nulo");
        }
        
        return ResponseEntity.ok(guardada);
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

