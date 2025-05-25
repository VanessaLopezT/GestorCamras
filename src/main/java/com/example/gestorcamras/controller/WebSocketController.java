package com.example.gestorcamras.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.gestorcamras.dto.EquipoDTO;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyEquipoUpdate(EquipoDTO equipo) {
        messagingTemplate.convertAndSend("/topic/equipos/update", equipo);
    }

    public void notifyEquipoConnected(EquipoDTO equipo) {
        messagingTemplate.convertAndSend("/topic/equipos/connected", equipo);
    }
    
    public void notifyNewFile(String message) {
        // Enviar notificación a todos los clientes suscritos a /topic/notificaciones
        messagingTemplate.convertAndSend("/topic/notificaciones", message);
        
        // También podemos enviar notificaciones a canales específicos si es necesario
        // messagingTemplate.convertAndSend("/topic/archivos/nuevos", message);
    }
}
