package com.example.gestorcamras.Escritorio.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Cliente STOMP sobre WebSocket para recibir notificaciones en tiempo real del servidor
 */
public class StompClient {
    private WebSocketClient webSocketClient;
    private final String serverUrl;
    private final String cookieSesion;
    private final Consumer<String> logger;
    private final Consumer<JSONObject> mensajeHandler;
    private boolean reconectando = false;
    private String sessionId;
    private String clientId;
    
    // Constantes para el protocolo STOMP sobre SockJS
    private static final String STOMP_CONNECT_TEMPLATE = "[\"CONNECT\\naccept-version:1.1,1.0\\nheart-beat:10000,10000\\n\\n\\u0000\"]";
    private static final String STOMP_SUBSCRIBE_TEMPLATE = "[\"SUBSCRIBE\\nid:%s\\ndestination:%s\\n\\n\\u0000\"]";
    private static final String STOMP_DISCONNECT_TEMPLATE = "[\"DISCONNECT\\n\\n\\u0000\"]";

    public StompClient(String serverUrl, String cookieSesion, 
                      Consumer<String> logger, Consumer<JSONObject> mensajeHandler) {
        // Asegurarse de que la URL termine con /ws/websocket para SockJS
        String baseUrl = serverUrl.replace("http", "ws");
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        this.serverUrl = baseUrl + "ws/websocket";
        this.cookieSesion = cookieSesion;
        this.logger = logger != null ? logger : msg -> {};
        this.mensajeHandler = mensajeHandler != null ? mensajeHandler : msg -> {};
        this.clientId = UUID.randomUUID().toString().substring(0, 8);
        
        conectar();
    }

    /**
     * Establece la conexión WebSocket con el servidor
     */
    public void conectar() {
        try {
            logger.accept("Conectando a WebSocket en " + serverUrl);
            
            webSocketClient = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.accept("Conexión WebSocket establecida con " + serverUrl);
                    // Enviar comando CONNECT de STOMP sobre SockJS
                    send(STOMP_CONNECT_TEMPLATE);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        logger.accept("Mensaje recibido: " + message);
                        
                        // Procesar mensaje de bienvenida de SockJS
                        if (message.startsWith("o{\"")) {
                            logger.accept("Conexión SockJS establecida");
                            // Enviar mensaje de conexión STOMP
                            send(STOMP_CONNECT_TEMPLATE);
                        }
                        // Procesar mensajes de texto de SockJS (empiezan con 'a')
                        else if (message.startsWith("a[\"")) {
                            // Extraer el contenido del mensaje (eliminar 'a[' al inicio y ']' al final)
                            String content = message.substring(2, message.length() - 1);
                            
                            // Procesar mensajes STOMP
                            if (content.startsWith("\"CONNECTED")) {
                                logger.accept("Conexión STOMP establecida");
                                // Suscribirse a los temas necesarios
                                suscribir("/topic/equipos");
                                suscribir("/topic/camaras");
                                suscribir("/topic/alarmas");
                            } 
                            // Procesar mensajes de datos
                            else if (content.startsWith("\"MESSAGE")) {
                                procesarMensajeSTOMP(content);
                            }
                        }
                        // Procesar mensajes de latido (h)
                        else if (message.equals("h") || message.equals("h\n")) {
                            // Responder al latido del servidor
                            send("h\n");
                        }
                    } catch (Exception e) {
                        logger.accept("Error al procesar mensaje: " + e.getMessage());
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
            
            // Configurar encabezados
            webSocketClient.addHeader("Origin", "http://localhost:8080");
            
            // Agregar encabezado de cookie si está disponible
            if (cookieSesion != null && !cookieSesion.isEmpty()) {
                webSocketClient.addHeader("Cookie", "JSESSIONID=" + cookieSesion);
            }
            
            // Conectar con timeout
            webSocketClient.connectBlocking();
            
        } catch (Exception e) {
            logger.accept("Error al conectar WebSocket: " + e.getMessage());
            reconectar();
        }
    }
    
    /**
     * Procesa un mensaje STOMP recibido a través de SockJS
     */
    private void procesarMensajeSTOMP(String mensajeSockJS) {
        try {
            // El mensaje viene en formato SockJS: a["Mensaje STOMP"]
            // Extraemos el mensaje STOMP real
            String mensajeSTOMP = mensajeSockJS.substring(2, mensajeSockJS.length() - 1);
            
            // Dividir el mensaje en encabezados y cuerpo
            int bodyStart = mensajeSTOMP.indexOf("\n\n");
            if (bodyStart == -1) {
                logger.accept("Formato de mensaje STOMP no válido: " + mensajeSockJS);
                return;
            }
            
            String body = mensajeSTOMP.substring(bodyStart + 2).trim();
            
            // Procesar el cuerpo del mensaje
            if (body.endsWith("\\u0000")) {
                body = body.substring(0, body.length() - 6); // Eliminar '\\u0000'
            } else if (body.endsWith("\"")) {
                body = body.substring(0, body.length() - 1); // Eliminar comilla final
            }
            
            // Eliminar comilla inicial si existe
            if (body.startsWith("\"")) {
                body = body.substring(1);
            }
            
            // Procesar el mensaje como JSON
            try {
                JSONObject json = new JSONObject(body);
                logger.accept("Mensaje STOMP procesado: " + json.toString(2));
                mensajeHandler.accept(json);
            } catch (Exception e) {
                logger.accept("El mensaje no es un JSON válido: " + body);
            }
            
        } catch (Exception e) {
            logger.accept("Error al procesar mensaje STOMP: " + e.getMessage());
            logger.accept("Mensaje original: " + mensajeSockJS);
        }
    }
    
    /**
     * Suscribirse a un tema STOMP
     */
    private void suscribir(String destino) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String subscriptionId = "sub-" + destino.replace("/", "-") + "-" + clientId;
            String mensaje = String.format(STOMP_SUBSCRIBE_TEMPLATE, subscriptionId, destino);
            webSocketClient.send(mensaje);
            logger.accept("Suscrito a " + destino + " con ID: " + subscriptionId);
        } else {
            logger.accept("No se puede suscribir a " + destino + ": WebSocket no está conectado");
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
            if (webSocketClient.isOpen()) {
                webSocketClient.send(STOMP_DISCONNECT_TEMPLATE);
            }
            webSocketClient.close();
        }
    }
    
    /**
     * Verifica si la conexión está abierta
     */
    public boolean estaConectado() {
        return webSocketClient != null && webSocketClient.isOpen() && sessionId != null;
    }
}
