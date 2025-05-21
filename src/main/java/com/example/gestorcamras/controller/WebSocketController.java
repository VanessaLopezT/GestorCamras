package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.EquipoDTO;
// Controlador para manejar las notificaciones WebSocket
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
}
