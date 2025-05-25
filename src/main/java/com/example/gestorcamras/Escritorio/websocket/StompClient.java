package com.example.gestorcamras.Escritorio.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Cliente STOMP sobre WebSocket para recibir notificaciones en tiempo real del servidor
 */
public class StompClient {
    private WebSocketClient webSocketClient;
    private String serverUrl;
    private final String cookieSesion;
    private final String clientId;
    private final Consumer<String> logger;
    private final Consumer<JSONObject> mensajeHandler;
    private boolean reconectando = false;
    private int reintentosReconexion = 0;
    private String sessionId;
    
    // Constantes para mensajes SockJS
    private static final String SOCKJS_OPEN = "o";
    private static final String SOCKJS_HEARTBEAT = "h";
    private static final String SOCKJS_CLOSE = "c";
    private static final String SOCKJS_MESSAGE = "m";
    private static final String SOCKJS_ARRAY = "a";
    
    // Constantes para mensajes STOMP
    private static final String STOMP_CONNECT = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000";
    private static final String STOMP_SUBSCRIBE = "SUBSCRIBE\nid:sub-%s\ndestination:%s\n\n\u0000";
    private static final String STOMP_UNSUBSCRIBE = "UNSUBSCRIBE\nid:sub-%s\n\n\u0000";
    private static final String STOMP_DISCONNECT = "DISCONNECT\n\n\u0000";
    private static final String SOCKJS_INFO = "GET /info HTTP/1.1\n\n";
    
    /**
     * Envía un mensaje STOMP a través de la conexión WebSocket
     * @param mensaje El mensaje STOMP a enviar (sin el carácter de terminación nulo)
     */
    private void enviarMensajeSTOMP(String mensaje) {
        if (webSocketClient == null || !webSocketClient.isOpen()) {
            logger.accept("No se puede enviar mensaje: WebSocket no conectado");
            // Intentar reconectar si no estamos en medio de una reconexión
            if (!reconectando) {
                reconectar();
            }
            return;
        }
        
        try {
            // Asegurarse de que el mensaje termine con el carácter nulo
            String mensajeCompleto = mensaje.endsWith("\u0000") ? mensaje : mensaje + "\u0000";
            
            // Para mensajes STOMP, no usamos el formato SockJS
            if (mensaje.startsWith("CONNECT") || mensaje.startsWith("SUBSCRIBE") || 
                mensaje.startsWith("UNSUBSCRIBE") || mensaje.startsWith("SEND")) {
                logger.accept("Enviando mensaje STOMP directo: " + 
                    mensajeCompleto.trim().replace("\n", "\\n"));
                webSocketClient.send(mensajeCompleto);
                return;
            }
            
            // Para otros mensajes, usar formato SockJS
            StringBuilder sb = new StringBuilder();
            sb.append('a').append('[').append('"');
            
            // Escapar caracteres especiales
            for (int i = 0; i < mensajeCompleto.length(); i++) {
                char c = mensajeCompleto.charAt(i);
                switch (c) {
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '"':
                        sb.append("\\\\\"");
                        break;
                    case '\n':
                        sb.append("\\\\n");
                        break;
                    case '\u0000':
                        sb.append("\\\\u0000");
                        break;
                    default:
                        // Solo caracteres ASCII imprimibles
                        if (c >= 32 && c <= 126) {
                            sb.append(c);
                        } else {
                            // Escapar otros caracteres no ASCII
                            sb.append(String.format("\\\\u%04x", (int) c));
                        }
                }
            }
            
            sb.append('"').append(']');
            String mensajeFormateado = sb.toString();
            
            logger.accept("Enviando mensaje STOMP (formato SockJS): " + 
                mensajeFormateado.substring(0, Math.min(100, mensajeFormateado.length())) + 
                (mensajeFormateado.length() > 100 ? "..." : ""));
                
            webSocketClient.send(mensajeFormateado);
            
        } catch (Exception e) {
            logger.accept("Error al enviar mensaje STOMP: " + e.getMessage());
            e.printStackTrace();
            
            // Si hay un error al enviar, intentar reconectar
            if (!reconectando) {
                reconectar();
            }
        }
    }
    
    // Constantes para el protocolo SockJS y STOMP

    public StompClient(String serverUrl, String cookieSesion, 
                      Consumer<String> logger, Consumer<JSONObject> mensajeHandler) {
        // Construir la URL del WebSocket
        this.cookieSesion = cookieSesion;
        this.logger = logger != null ? logger : System.out::println;
        this.mensajeHandler = mensajeHandler != null ? mensajeHandler : json -> {};
        this.clientId = "client-" + UUID.randomUUID().toString().substring(0, 8);

        if (this.logger != null) {
            this.logger.accept("Configurando WebSocket en: " + this.serverUrl);
        }
        conectarWebSocket();
    }

    /**
     * Establece la conexión WebSocket con el servidor
     */
    private void suscribirCanales() {
        // Suscribirse a los canales necesarios
        suscribir("/topic/equipos");
        suscribir("/topic/camaras");
        suscribir("/topic/alarmas");
    }

    private void suscribir(String destino) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            String idSuscripcion = "sub-" + clientId + "-" + destino.hashCode();
            String subscribeMsg = String.format(STOMP_SUBSCRIBE, idSuscripcion, destino);
            enviarMensajeSTOMP(subscribeMsg);
            logger.accept("Suscrito a " + destino + " con ID: " + idSuscripcion);
        } else {
            logger.accept("No se puede suscribir a " + destino + ": WebSocket no está conectado");
        }
    }

    private void reconectar() {
        if (reconectando) {
            logger.accept("Ya hay una reconexión en curso");
            return;
        }

        reconectando = true;

        // Cerrar la conexión actual si existe
        if (webSocketClient != null) {
            try {
                logger.accept("Cerrando conexión WebSocket actual...");
                webSocketClient.close();
            } catch (Exception e) {
                logger.accept("Error al cerrar la conexión WebSocket: " + e.getMessage());
            } finally {
                webSocketClient = null;
            }
        }

        // Esperar antes de intentar reconectar
        int delay = Math.min(reintentosReconexion * 1000, 30000); // Hasta 30 segundos
        logger.accept(String.format("Intentando reconectar en %d segundos...", delay / 1000));

        new Thread(() -> {
            try {
                Thread.sleep(delay);

                // Incrementar el contador de reintentos (con un máximo razonable)
                reintentosReconexion = Math.min(reintentosReconexion + 1, 10);

                logger.accept("Iniciando reconexión (intento " + reintentosReconexion + ")");

                // Intentar reconectar
                conectarWebSocket();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.accept("Hilo de reconexión interrumpido");
            } catch (Exception e) {
                logger.accept("Error en el hilo de reconexión: " + e.getMessage());
                e.printStackTrace();
            } finally {
                reconectando = false;
            }
        }, "Reconexion-WebSocket").start();
    }

    private void conectarWebSocket() {
        try {
            logger.accept("Conectando a WebSocket en " + serverUrl);

            webSocketClient = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.accept("Conexión WebSocket establecida con " + serverUrl);
                    // No enviar nada aquí, esperar el mensaje 'o' del servidor
                }

                @Override
                public void onMessage(String message) {
                    try {
                        if (message == null || message.trim().isEmpty()) {
                            logger.accept("Mensaje de texto vacío recibido");
                            return;
                        }

                        logger.accept("Mensaje de texto recibido: " + message);

                        // Procesar mensajes SockJS
                        if (message.equals("o") || message.startsWith("a[")) {
                            procesarMensajeSockJS(message);
                        } else if (message.startsWith("{") || message.startsWith("[")) {
                            // Posible mensaje JSON directo
                            procesarMensajeSTOMP(message);
                        } else {
                            logger.accept("Mensaje de texto no reconocido: " + message);
                        }
                    } catch (Exception e) {
                        logger.accept("Error procesando mensaje de texto: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        if (bytes == null || bytes.remaining() == 0) {
                            logger.accept("Mensaje binario vacío recibido");
                            return;
                        }

                        // Verificar si es un mensaje de control SockJS (primer byte)
                        if (bytes.remaining() == 1) {
                            byte controlByte = bytes.get();
                            if (controlByte == 'o' || controlByte == 'h' || controlByte == 'c') {
                                String msg = String.valueOf((char)controlByte);
                                if (controlByte == 'c') {
                                    // Mensaje de cierre SockJS
                                    msg += new String(bytes.array(), bytes.position(), bytes.remaining(), StandardCharsets.UTF_8);
                                }
                                procesarMensajeSockJS(msg);
                                return;
                            }
                        }

                        // Intentar decodificar como texto
                        bytes.mark();
                        try {
                            String message = StandardCharsets.UTF_8.decode(bytes).toString();
                            logger.accept("Mensaje binario convertido a texto: " + message);
                            procesarMensajeSockJS(message);
                        } catch (Exception e) {
                            // Si falla, tratar como binario puro
                            bytes.reset();
                            logger.accept("Mensaje binario recibido (" + bytes.remaining() + " bytes)");
                            // Aquí podrías manejar mensajes binarios puros si es necesario
                        }
                    } catch (Exception e) {
                        logger.accept("Error procesando mensaje binario: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.accept(String.format("Conexión WebSocket cerrada. Código: %d, Razón: %s, Remoto: %b", 
                            code, reason, remote));
                    if (!reconectando) {
                        reconectar();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    logger.accept("Error en WebSocket: " + ex.toString());
                    ex.printStackTrace();
                    if (!reconectando) {
                        reconectar();
                    }
                }
            };

            // Agregar la cookie de sesión si está disponible
            if (cookieSesion != null && !cookieSesion.isEmpty()) {
                webSocketClient.addHeader("Cookie", "JSESSIONID=" + cookieSesion);
            }

            // Conectar con timeout
            try {
                webSocketClient.connectBlocking();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.accept("Conexión WebSocket interrumpida");
                return;
            }

            // Configurar encabezados
            webSocketClient.addHeader("Origin", "http://localhost:8080");

            // Agregar encabezado de cookie si está disponible
            if (cookieSesion != null && !cookieSesion.isEmpty()) {
                webSocketClient.addHeader("Cookie", "JSESSIONID=" + cookieSesion);
            }

        } catch (URISyntaxException e) {
            logger.accept("Error al conectar WebSocket: " + e.getMessage());
            reconectar();
        }
    }

    /**
     * Procesa un mensaje de control SockJS
     */
    private void procesarMensajeSTOMP(String mensaje) {
        try {
            if (mensaje == null || mensaje.trim().isEmpty()) {
                logger.accept("Mensaje STOMP vacío o nulo");
                return;
            }

            logger.accept("Procesando mensaje STOMP: " + mensaje);

            // Si es un mensaje CONNECTED, suscribirse a los canales necesarios
            if (mensaje.contains("CONNECTED")) {
                logger.accept("Conexión STOMP establecida");
                suscribirCanales();
            }

            // Procesar el mensaje como JSON si es un MESSAGE
            if (mensaje.contains("MESSAGE")) {
                try {
                    // Extraer el cuerpo del mensaje (después del doble salto de línea)
                    String[] partes = mensaje.split("\\n\\n", 2);
                    if (partes.length > 1) {
                        String cuerpo = partes[1].trim();
                        if (cuerpo.endsWith("\u0000")) {
                            cuerpo = cuerpo.substring(0, cuerpo.length() - 1);
                        }
                        JSONObject json = new JSONObject(cuerpo);
                        mensajeHandler.accept(json);
                    }
                } catch (Exception e) {
                    logger.accept("Error al procesar mensaje JSON: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.accept("Error al procesar mensaje STOMP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void procesarMensajeSockJS(String mensaje) {
        try {
            if (mensaje == null || mensaje.trim().isEmpty()) {
                logger.accept("Mensaje SockJS vacío o nulo");
                return;
            }

            logger.accept("Procesando mensaje SockJS: " + mensaje);

            if ("o".equals(mensaje) || 
                mensaje.startsWith("a[\"o\"]") || 
                mensaje.startsWith("a[\\\\\"o\\\\\"]") ||
                mensaje.startsWith("o[")) {
                // Mensaje de apertura de conexión SockJS
                logger.accept("Conexión SockJS establecida");

                // Resetear contador de reintentos cuando la conexión es exitosa
                if (reintentosReconexion > 0) {
                    logger.accept("Conexión restablecida después de " + reintentosReconexion + " intentos");
                    reintentosReconexion = 0;
                }

                // Enviar mensaje CONNECT STOMP después de un pequeño retraso
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        enviarMensajeSTOMP(STOMP_CONNECT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } catch (Exception e) {
            logger.accept("Error al procesar mensaje SockJS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifica si la conexión está activa
     */
    public boolean estaConectado() {
        return webSocketClient != null && webSocketClient.isOpen() && sessionId != null;
    }

    /**
     * Cierra la conexión WebSocket
     */
    /**
     * Cierra la conexión WebSocket de manera ordenada
     */
    public void desconectar() {
        if (webSocketClient != null) {
            try {
                // Enviar mensaje de desconexión STOMP
                enviarMensajeSTOMP(STOMP_DISCONNECT);
                
                // Dar tiempo para que se envíe el mensaje de desconexión
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                // Cerrar la conexión
                webSocketClient.close();
                logger.accept("Conexión WebSocket cerrada correctamente");
            } catch (Exception e) {
                logger.accept("Error al cerrar la conexión WebSocket: " + e.getMessage());
                try {
                    webSocketClient.close();
                } catch (Exception ex) {
                    // Ignorar errores al cerrar
                }
            } finally {
                webSocketClient = null;
            }
        }
    }
}
