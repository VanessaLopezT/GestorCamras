package com.example.gestorcamras.controller;

import com.example.gestorcamras.dto.EquipoMessage;
import com.example.gestorcamras.model.Equipo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class EquipoWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public EquipoWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Método para notificar a todos los clientes sobre un nuevo equipo
    public void notifyEquipoChange(Equipo equipo, String changeType) {
        EquipoMessage message = new EquipoMessage(changeType, equipo);
        messagingTemplate.convertAndSend("/topic/equipos", message);
    }
}
