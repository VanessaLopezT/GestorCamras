package com.example.gestorcamras.Escritorio.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
            String errorMsg = "Error: No se puede enviar mensaje - WebSocket no conectado";
            logger.accept(errorMsg);
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
        // Inicializar logger primero para asegurar que siempre tengamos un logger
        this.logger = logger != null ? logger : System.out::println;
        
        // Inicializar otros campos
        this.cookieSesion = cookieSesion != null ? cookieSesion : "";
        this.mensajeHandler = mensajeHandler != null ? mensajeHandler : json -> {};
        this.clientId = "client-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Asegurarse de que la URL use ws:// o wss:// y tenga el sufijo /ws/websocket
        if (serverUrl == null) {
            this.logger.accept("Error: serverUrl no puede ser nulo");
            throw new IllegalArgumentException("serverUrl no puede ser nulo");
        }
        
        if (serverUrl.startsWith("http://")) {
            this.serverUrl = serverUrl.replace("http://", "ws://") + "/ws/websocket";
        } else if (serverUrl.startsWith("https://")) {
            this.serverUrl = serverUrl.replace("https://", "wss://") + "/ws/websocket";
        } else if (!serverUrl.startsWith("ws://") && !serverUrl.startsWith("wss://")) {
            this.serverUrl = "ws://" + serverUrl + "/ws/websocket";
        } else {
            this.serverUrl = serverUrl;
        }

        this.logger.accept("Configurando WebSocket en: " + this.serverUrl);
        
        try {
            conectarWebSocket();
        } catch (Exception e) {
            this.logger.accept("Error al conectar WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Establece la conexión WebSocket con el servidor
     */
    public void suscribirCanales() {
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
            logger.accept("[WebSocket] Ya hay una reconexión en curso");
            return;
        }

        // Incrementar el contador de reintentos
        reintentosReconexion++;
        
        // No intentar reconectar si ya superamos el máximo de reintentos
        if (reintentosReconexion > 10) {
            String errorMsg = String.format("[WebSocket] Se superó el número máximo de reintentos de reconexión (%d)", reintentosReconexion - 1);
            logger.accept(errorMsg);
            System.err.println(errorMsg);
            return;
        }
        
        reconectando = true;
        logger.accept("[WebSocket] Iniciando proceso de reconexión (intento " + reintentosReconexion + ")");

        // Cerrar la conexión actual si existe
        if (webSocketClient != null) {
            try {
                logger.accept("[WebSocket] Cerrando conexión WebSocket actual...");
                webSocketClient.close();
            } catch (Exception e) {
                logger.accept("[WebSocket] Error al cerrar la conexión: " + e.getMessage());
            } finally {
                webSocketClient = null;
            }
        }
        
        // Calcular tiempo de espera con backoff exponencial (empezando en 1s, máximo 30s) y jitter
        int tiempoBase = Math.min(1000 * (int)Math.pow(2, reintentosReconexion - 1), 30000);
        int jitter = (int)(Math.random() * 1000); // Añadir jitter aleatorio (0-1000ms)
        int tiempoEspera = Math.max(1000, tiempoBase + jitter); // Mínimo 1 segundo
        
        String logMsg = String.format("[WebSocket] Intento de reconexión #%d en %d ms (base: %d, jitter: %d)", 
            reintentosReconexion, tiempoEspera, tiempoBase, jitter);
        logger.accept(logMsg);
        
        new Thread(() -> {
            try {
                Thread.sleep(tiempoEspera);
                logger.accept("[WebSocket] Iniciando nueva conexión...");
                conectarWebSocket();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                String errorMsg = "[WebSocket] Reconexión interrumpida: " + e.getMessage();
                logger.accept(errorMsg);
                System.err.println(errorMsg);
                reconectando = false;
            } catch (Exception e) {
                String errorMsg = "[WebSocket] Error en la reconexión: " + e.getClass().getSimpleName() + " - " + e.getMessage();
                logger.accept(errorMsg);
                System.err.println(errorMsg);
                
                // Marcar que ya no estamos en proceso de reconexión
                reconectando = false;
                
                // Solo reintentar si no es un error crítico
                if (!(e instanceof IllegalArgumentException || e instanceof SecurityException)) {
                    // Disminuir el contador para que el siguiente reintento use el mismo nivel
                    reintentosReconexion--;
                    logger.accept("[WebSocket] Reintentando conexión...");
                    reconectar();
                }
            }
        }, "WebSocket-Reconectar-" + reintentosReconexion).start();
    }

    private void conectarWebSocket() {
        try {
            // Cerrar la conexión existente si hay una
            if (webSocketClient != null) {
                try {
                    webSocketClient.close();
                } catch (Exception e) {
                    logger.accept("[WebSocket] Error al cerrar conexión existente: " + e.getMessage());
                }
            }

            // Asegurarse de que la URL use ws:// o wss://
            String urlConexion = serverUrl;
            if (!urlConexion.startsWith("ws://") && !urlConexion.startsWith("wss://")) {
                // Reemplazar http:// por ws:// y https:// por wss://
                urlConexion = urlConexion.replace("http://", "ws://").replace("https://", "wss://");
                logger.accept("[WebSocket] URL convertida a: " + urlConexion);
            }

            // Crear una nueva conexión WebSocket
            URI serverUri = new URI(urlConexion);
            logger.accept("[WebSocket] Conectando a " + serverUri);

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.accept("[WebSocket] Conexión establecida con éxito");
                    reconectando = false;
                    reintentosReconexion = 0;
                    
                    // Esperar un momento antes de enviar el CONNECT
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Enviar mensaje CONNECT STOMP
                    enviarMensajeSTOMP(STOMP_CONNECT);
                }


                @Override
                public void onMessage(String message) {
                    // Solo procesar el mensaje si no está vacío
                    if (message != null && !message.trim().isEmpty()) {
                        procesarMensajeSockJS(message);
                    }
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        if (bytes == null || bytes.remaining() == 0) {
                            return;
                        }
                        
                        // Hacer una copia del buffer para no modificar el original
                        ByteBuffer buffer = bytes.duplicate();
                        
                        // Verificar si es un mensaje de control SockJS (primer byte)
                        if (buffer.remaining() == 1) {
                            byte controlByte = buffer.get();
                            if (controlByte == 'o' || controlByte == 'h' || controlByte == 'c') {
                                String msg = String.valueOf((char)controlByte);
                                if (controlByte == 'c' && buffer.hasRemaining()) {
                                    // Mensaje de cierre SockJS
                                    byte[] remaining = new byte[buffer.remaining()];
                                    buffer.get(remaining);
                                    msg += new String(remaining, StandardCharsets.UTF_8);
                                }
                                procesarMensajeSockJS(msg);
                                return;
                            }
                            // Restablecer la posición si no era un mensaje de control
                            buffer.rewind();
                        }
                        
                        // Procesar mensajes STOMP
                        try {
                            String message = StandardCharsets.UTF_8.decode(buffer).toString();
                            if (!message.trim().isEmpty()) {
                                procesarMensajeSockJS(message);
                            }
                        } catch (Exception e) {
                            // Si falla la decodificación, ignorar el mensaje
                        }
                    } catch (Exception e) {
                        // Manejar cualquier otra excepción
                    }
                }

                public void onClose(int code, String reason, boolean remote) {
                    // Intentar reconectar si no fue un cierre intencional
                    if (code != 1000 && !reconectando) {  // 1000 = cierre normal
                        reconectar();
                    }
                }


                @Override
                public void onError(Exception ex) {
                    // Intentar reconectar en caso de error
                    if (!reconectando) {
                        reconectar();
                    }
                }
            };
            
            // Configurar encabezados estándar
            webSocketClient.addHeader("User-Agent", "Java-WebSocket");
            webSocketClient.addHeader("Accept-Encoding", "gzip, deflate, br");
            webSocketClient.addHeader("Accept-Language", "en-US,en;q=0.9");
            webSocketClient.addHeader("Cache-Control", "no-cache");
            webSocketClient.addHeader("Pragma", "no-cache");
            
            // Agregar la cookie de sesión si está disponible
            if (cookieSesion != null && !cookieSesion.isEmpty()) {
                webSocketClient.addHeader("Cookie", "JSESSIONID=" + cookieSesion);
            }

            // Conectar con timeout
            try {
                logger.accept("Iniciando conexión WebSocket...");
                webSocketClient.connect();
                
                // Esperar hasta 10 segundos para la conexión
                int intentos = 0;
                while (!webSocketClient.isOpen() && intentos < 50) {
                    try {
                        Thread.sleep(200);
                        intentos++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupción durante la conexión", e);
                    }
                }
                
                if (!webSocketClient.isOpen()) {
                    webSocketClient.close();
                    throw new IllegalStateException("Tiempo de espera agotado al conectar al WebSocket después de " + 
                        (50 * 200) + "ms");
                }
                
                logger.accept("Conexión WebSocket establecida exitosamente");
                
            } catch (Exception e) {
                String errorMsg = "[WebSocket] Error al conectar: " + e.getMessage();
                logger.accept(errorMsg);
                if (webSocketClient != null) {
                    try { webSocketClient.close(); } catch (Exception ignored) {}
                }
                throw new IllegalStateException("Error al conectar al WebSocket", e);
            }
            
        } catch (URISyntaxException e) {
            String errorMsg = "Error en la URL del WebSocket: " + e.getMessage();
            logger.accept(errorMsg);
            throw new IllegalArgumentException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error inesperado en WebSocket: " + e.getMessage();
            logger.accept(errorMsg);
            
            // Intentar reconectar si es un error de conexión
            if (e.getCause() instanceof java.net.ConnectException || 
                e.getCause() instanceof java.net.UnknownHostException) {
                reconectar();
            }
            
            throw new IllegalStateException("Error inesperado al conectar al WebSocket", e);
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
        if (mensaje == null || mensaje.trim().isEmpty()) {
            logger.accept("Mensaje SockJS vacío o nulo");
            return;
        }

        logger.accept("Procesando mensaje SockJS: " + mensaje);

        char tipo = mensaje.charAt(0);
        String contenido = mensaje.length() > 1 ? mensaje.substring(1) : "";

        try {
            switch (tipo) {
                case 'o': // open
                    logger.accept("Conexión SockJS establecida");
                    // No enviamos CONNECT aquí, ya se envía en onOpen
                    break;
                    
                case 'h': // heartbeat
                    logger.accept("Heartbeat recibido");
                    // Responder al heartbeat para mantener la conexión viva
                    if (webSocketClient != null && webSocketClient.isOpen()) {
                        webSocketClient.send("h");
                    }
                    break;
                    
                case 'a': // array de mensajes
                    try {
                        // El formato es a["mensaje1", "mensaje2", ...]
                        String jsonArrayStr = mensaje.substring(1); // Eliminar la 'a' inicial
                        JSONArray jsonArray = new JSONArray(jsonArrayStr);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String mensajeStomp = jsonArray.getString(i);
                            if (mensajeStomp != null && !mensajeStomp.trim().isEmpty()) {
                                procesarMensajeSTOMP(mensajeStomp);
                            }
                        }
                    } catch (Exception e) {
                        logger.accept("Error al procesar array de mensajes SockJS: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                    
                case 'c': // close
                    logger.accept("Conexión SockJS cerrada: " + contenido);
                    // Intentar reconectar
                    if (!reconectando) {
                        reconectar();
                    }
                    break;
                    
                case 'm': // message (no estándar, por si acaso)
                    if (contenido != null && !contenido.trim().isEmpty()) {
                        procesarMensajeSTOMP(contenido);
                    }
                    break;
                    
                default:
                    logger.accept("Tipo de mensaje SockJS no reconocido: " + tipo);
                    break;
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
     * Cierra la conexión WebSocket de manera ordenada
     */
    public void desconectar() {
        if (webSocketClient == null) {
            logger.accept("[WebSocket] No hay conexión WebSocket activa para cerrar");
            return;
        }
        
        try {
            logger.accept("[WebSocket] Iniciando cierre ordenado de la conexión WebSocket...");
            
            // Enviar mensaje de desconexión STOMP
            logger.accept("[WebSocket] Enviando mensaje DISCONNECT STOMP...");
            enviarMensajeSTOMP(STOMP_DISCONNECT);
            
            // Dar tiempo para que se envíe el mensaje de desconexión
            try {
                int tiempoEspera = 200; // ms
                logger.accept("[WebSocket] Esperando " + tiempoEspera + "ms para asegurar el envío...");
                Thread.sleep(tiempoEspera);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.accept("[WebSocket] Espera interrumpida: " + ie.getMessage());
            }
            
            // Cerrar la conexión
            logger.accept("[WebSocket] Cerrando conexión WebSocket...");
            webSocketClient.close();
            logger.accept("[WebSocket] Conexión WebSocket cerrada correctamente");
            System.out.println("[WebSocket] Conexión cerrada correctamente");
        } catch (Exception e) {
            String errorMsg = "[WebSocket] Error al cerrar la conexión: " + e.getClass().getSimpleName() + " - " + e.getMessage();
            logger.accept(errorMsg);
            System.err.println(errorMsg);
            
            // Intentar forzar el cierre si hubo un error
            try {
                logger.accept("[WebSocket] Forzando cierre de la conexión...");
                webSocketClient.close();
            } catch (Exception ex) {
                String forceCloseError = "[WebSocket] Error al forzar cierre: " + ex.getMessage();
                logger.accept(forceCloseError);
                System.err.println(forceCloseError);
            }
        } finally {
            webSocketClient = null;
            logger.accept("[WebSocket] Referencia WebSocket liberada");
        }
    }
}
