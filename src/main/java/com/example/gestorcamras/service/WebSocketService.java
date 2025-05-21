package com.example.gestorcamras.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WebSocketService {
    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    public void notificarNuevaCamara(Long equipoId, Object mensaje) {
        try {
            // Notificar al equipo específico
            String destinoEquipo = String.format("/topic/equipos/%d/camaras", equipoId);
            String mensajeJson = objectMapper.writeValueAsString(mensaje);
            
            log.info("Enviando notificación a {}: {}", destinoEquipo, mensajeJson);
            
            // Enviar al tema específico del equipo
            messagingTemplate.convertAndSend(destinoEquipo, mensajeJson);
            
            // Notificar actualización general de equipos
            String mensajeActualizacion = "actualizar";
            log.info("Enviando notificación de actualización general");
            messagingTemplate.convertAndSend("/topic/equipos/actualizacion", mensajeActualizacion);
            
        } catch (JsonProcessingException e) {
            log.error("Error al serializar mensaje para WebSocket", e);
        } catch (Exception e) {
            log.error("Error al enviar notificación WebSocket", e);
        }
    }
}
