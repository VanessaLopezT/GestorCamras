package com.example.gestorcamaras.service;

import org.springframework.stereotype.Service;

import com.example.gestorcamaras.controller.WebSocketController;

@Service
public class NotificationService {
    
    private final WebSocketController webSocketController;
    
    public NotificationService(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }
    
    public void enviarNotificacion(String mensaje) {
        webSocketController.notifyNewFile(mensaje);
    }
    
    public void notificarNuevoArchivo(String nombreArchivo, String tipo, String camara) {
        String mensaje = String.format("Nuevo archivo recibido: %s (Tipo: %s, Cámara: %s)", 
            nombreArchivo, tipo, camara);
        enviarNotificacion(mensaje);
    }
}
