package com.example.gestorcamras.Escritorio.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * Cliente WebSocket para recibir notificaciones en tiempo real del servidor
 */
public class ClienteWebSocket {
    private WebSocketClient webSocketClient;
    private final String serverUrl;
    private final String cookieSesion;
    private final Consumer<String> logger;
    private final Consumer<JSONObject> mensajeHandler;
    private boolean reconectando = false;

    /**
     * Crea una nueva instancia del cliente WebSocket
     * @param serverUrl URL base del servidor (ej: "ws://localhost:8080")
     * @param cookieSesion Cookie de sesión para autenticación
     * @param logger Logger para mensajes de depuración
     * @param mensajeHandler Manejador de mensajes recibidos
     */
    public ClienteWebSocket(String serverUrl, String cookieSesion, 
                           Consumer<String> logger, Consumer<JSONObject> mensajeHandler) {
        this.serverUrl = serverUrl.replace("http", "ws") + "/ws";
        this.cookieSesion = cookieSesion;
        this.logger = logger != null ? logger : msg -> {};
        this.mensajeHandler = mensajeHandler != null ? mensajeHandler : msg -> {};
        
        conectar();
    }

    /**
     * Establece la conexión WebSocket con el servidor
     */
    public void conectar() {
        try {
            webSocketClient = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.accept("Conexión WebSocket establecida con " + serverUrl);
                    reconectando = false;
                }

                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject json = new JSONObject(message);
                        mensajeHandler.accept(json);
                    } catch (Exception e) {
                        logger.accept("Error al procesar mensaje WebSocket: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    String mensaje = "Conexión WebSocket cerrada. Código: " + code + 
                                    ", Razón: " + reason + 
                                    ", Remoto: " + remote;
                    logger.accept(mensaje);
                    
                    if (!reconectando) {
                        reconectar();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    logger.accept("Error en WebSocket: " + ex.getMessage());
                }
            };
            
            // Agregar encabezado de cookie si está disponible
            if (cookieSesion != null && !cookieSesion.isEmpty()) {
                webSocketClient.addHeader("Cookie", "JSESSIONID=" + cookieSesion);
            }
            
            webSocketClient.connect();
            
        } catch (URISyntaxException e) {
            logger.accept("URL de WebSocket inválida: " + e.getMessage());
        }
    }
    
    /**
     * Intenta reconectar al servidor después de un retraso
     */
    private void reconectar() {
        if (reconectando) {
            return;
        }
        
        reconectando = true;
        logger.accept("Intentando reconectar en 5 segundos...");
        
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                if (webSocketClient != null && webSocketClient.isClosed()) {
                    logger.accept("Reconectando...");
                    conectar();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.accept("Hilo de reconexión interrumpido");
            } finally {
                reconectando = false;
            }
        }).start();
    }
    
    /**
     * Cierra la conexión WebSocket
     */
    public void cerrar() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
    
    /**
     * Verifica si la conexión está abierta
     */
    public boolean estaConectado() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}
