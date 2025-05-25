package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.Escritorio.service.ClienteCamaraService;
import com.example.gestorcamras.Escritorio.service.ClienteEquipoService;
import com.example.gestorcamras.Escritorio.service.FileUploadService;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.NetworkInterface;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import com.example.gestorcamras.Escritorio.websocket.StompClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClienteSwingController {
    private final String servidorUrl;
    private final String cookieSesion;
    private Timer timerPing;
    private Consumer<String> logConsumer;
    private Consumer<Boolean> connectionStatusConsumer;
    private final ClienteCamaraService camaraService;
    private final ClienteEquipoService equipoService;
    private final FileUploadService fileUploadService;
    private StompClient stompClient;

    public ClienteSwingController(String usuario, String cookieSesion, String servidorUrl) {
        this.cookieSesion = cookieSesion;
        this.servidorUrl = servidorUrl != null ? servidorUrl : "http://localhost:8080";
        // Inicializar servicios
        this.camaraService = new ClienteCamaraService(this.servidorUrl, cookieSesion);
        this.equipoService = new ClienteEquipoService(this.servidorUrl, cookieSesion);
        this.fileUploadService = new FileUploadService(this.servidorUrl, cookieSesion, this::log);
        
        // Configurar logger para el servicio de equipos si es necesario
        if (this.equipoService instanceof ClienteEquipoService) {
            ((ClienteEquipoService) this.equipoService).setLogger(this::log);
        }
        
        // Inicializar WebSocket
        inicializarWebSocket();
    }

    /**
     * Inicializa la conexión WebSocket con el servidor usando STOMP
     */
    private void inicializarWebSocket() {
        try {
            stompClient = new StompClient(
                servidorUrl,
                cookieSesion,
                this::log,
                this::procesarMensajeWebSocket
            );
        } catch (Exception e) {
            log("Error al inicializar STOMP WebSocket: " + e.getMessage());
        }
    }
    
    /**
     * Procesa los mensajes recibidos a través de WebSocket
     */
    private void procesarMensajeWebSocket(JSONObject mensaje) {
        if (mensaje == null) return;
        
        String tipo = mensaje.optString("tipo");
        JSONObject datos = mensaje.optJSONObject("datos");
        
        if (tipo == null || datos == null) {
            log("Mensaje WebSocket con formato inválido: " + mensaje);
            return;
        }
        
        switch (tipo) {
            case "equipo_actualizado":
                // Actualizar estado del equipo en la interfaz
                log("Equipo actualizado: " + datos);
                if (connectionStatusConsumer != null) {
                    connectionStatusConsumer.accept(true);
                }
                break;
                
            case "nueva_camara":
                // Notificar sobre nueva cámara detectada
                log("Nueva cámara detectada: " + datos);
                break;
                
            case "alarma":
                // Manejar notificación de alarma
                log("¡Alarma! " + datos.optString("mensaje", "Sin detalles"));
                break;
                
            default:
                log("Tipo de mensaje WebSocket no manejado: " + tipo);
                break;
        }
    }
    
    public void setLogConsumer(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    public void setConnectionStatusConsumer(Consumer<Boolean> connectionStatusConsumer) {
        this.connectionStatusConsumer = connectionStatusConsumer;
    }

    private void log(String message) {
        if (logConsumer != null) {
            logConsumer.accept(message);
        }
    }

    public void verificarYConectar() {
        try {
            int intentos = 0;
            final int MAX_INTENTOS = 5;
            final int TIEMPO_ESPERA = 2000;

            while (intentos < MAX_INTENTOS) {
                if (verificarServidorActivo()) {
                    log("Servidor conectado exitosamente");
                    if (connectionStatusConsumer != null) {
                        connectionStatusConsumer.accept(true);
                    }
                    return;
                }
                intentos++;
                log("Intento " + intentos + "/" + MAX_INTENTOS + ": Servidor no disponible. Reintentando...");
                Thread.sleep(TIEMPO_ESPERA);
            }

            log("No se pudo conectar al servidor después de " + MAX_INTENTOS + " intentos");
            if (connectionStatusConsumer != null) {
                connectionStatusConsumer.accept(false);
            }
        } catch (InterruptedException e) {
            log("Error al intentar conectar: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log("Error al obtener la dirección IP local: " + e.getMessage());
            return "127.0.0.1";
        }
    }

    /**
     * Busca un equipo por su dirección IP utilizando el servicio de equipos
     * @param ip Dirección IP a buscar
     * @param equipoIdConsumer Callback que recibe el ID del equipo si se encuentra
     * @return true si la búsqueda se inició correctamente, false en caso de error
     */
    public boolean buscarEquipoPorIp(String ip, Consumer<String> equipoIdConsumer) {
        try {
            if (ip == null || ip.trim().isEmpty()) {
                log("Error: La dirección IP no puede estar vacía");
                if (equipoIdConsumer != null) equipoIdConsumer.accept(null);
                return false;
            }
            
            log("Buscando equipo con IP: " + ip);
            
            // Usar el servicio de equipos para buscar por IP
            equipoService.buscarEquipoPorIp(ip, equipoId -> {
                if (equipoId != null && !equipoId.isEmpty()) {
                    log("Equipo encontrado con ID: " + equipoId);
                    // Si se proporcionó un consumidor, pasarle el ID del equipo
                    if (equipoIdConsumer != null) {
                        equipoIdConsumer.accept(equipoId);
                    }
                } else {
                    log("No se encontró un equipo con la IP: " + ip);
                    if (equipoIdConsumer != null) {
                        equipoIdConsumer.accept(null);
                    }
                }
            }, this::log);
            
            return true; // La operación se inició correctamente
            
        } catch (Exception e) {
            log("Error al buscar equipo por IP: " + e.getMessage());
            e.printStackTrace();
            if (equipoIdConsumer != null) {
                equipoIdConsumer.accept(null);
            }
            return false;
        }
    }

    /**
     * Obtiene la dirección IP más adecuada para identificar este equipo en la red
     * @return La dirección IP preferida o "127.0.0.1" si no se puede determinar
     */
    private String obtenerDireccionIP() {
        try {
            // Primero intentar con la IP del host local
            String localIP = InetAddress.getLocalHost().getHostAddress();
            
            // Lista de prefijos de IP a excluir (redes virtuales, locales, etc.)
            String[] excludedPrefixes = {
                "127.",          // Loopback
                "192.168.56.",   // VirtualBox
                "169.254.",      // Link-local
                "172.16.", "172.17.", "172.18.", "172.19.", "172.20.",
                "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
                "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.", // Privadas clase B
                "10.",           // Privadas clase A
                "192.168."        // Privadas clase C
            };
            
            // Verificar si la IP local es adecuada
            if (esIPValida(localIP, excludedPrefixes)) {
                log("Usando dirección IP local: " + localIP);
                return localIP;
            }
            
            // Si la IP local no es adecuada, buscar en todas las interfaces de red
            log("Buscando dirección IP en interfaces de red...");
            List<String> posiblesIPs = new ArrayList<>();
            
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    
                    // Filtrar interfaces no adecuadas
                    if (iface == null || !iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                        continue;
                    }
                    
                    // Obtener direcciones IP de la interfaz
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                            String ip = addr.getHostAddress();
                            if (esIPValida(ip, excludedPrefixes)) {
                                posiblesIPs.add(ip);
                                log("IP encontrada en interfaz " + iface.getDisplayName() + ": " + ip);
                            }
                        }
                    }
                }
                
                // Si encontramos IPs válidas, devolver la primera
                if (!posiblesIPs.isEmpty()) {
                    String ipSeleccionada = posiblesIPs.get(0);
                    log("Seleccionada dirección IP: " + ipSeleccionada);
                    return ipSeleccionada;
                }
            } catch (Exception e) {
                log("Error al buscar direcciones IP en interfaces de red: " + e.getMessage());
            }
            
            // Si no se encuentra ninguna IP adecuada, usar la local con advertencia
            log("ADVERTENCIA: No se encontró una IP de red adecuada, usando " + localIP);
            return localIP;
            
        } catch (Exception e) {
            String errorMsg = "Error al obtener la dirección IP: " + e.getMessage();
            log(errorMsg);
            return "127.0.0.1";
        }
    }

    /**
     * Registra un nuevo equipo en el servidor o reutiliza uno existente con la misma IP
     * @param equipoIdConsumer Callback que se llama con el ID del equipo registrado o existente
     */
    public void registrarEquipo(Consumer<String> equipoIdConsumer) {
        if (equipoIdConsumer == null) {
            log("Error: El callback de equipoIdConsumer no puede ser nulo");
            return;
        }
        
        try {
            // Obtener la dirección IP del equipo
            String ip = obtenerDireccionIP();
            log("Buscando equipo existente para IP: " + ip);
            
            // Verificar si ya hay una cookie de sesión
            if (cookieSesion == null || cookieSesion.isEmpty()) {
                log("Error: No hay una sesión activa para registrar el equipo");
                equipoIdConsumer.accept(null);
                return;
            }
            
            // Primero buscar si existe un equipo con esta IP
            buscarEquipoPorIp(ip, existingEquipoId -> {
                try {
                    if (existingEquipoId != null && !existingEquipoId.isEmpty()) {
                        log("Usando equipo existente con ID: " + existingEquipoId);
                        // Iniciar el ping para mantener la conexión activa
                        iniciarPing();
                        // Notificar al consumidor
                        equipoIdConsumer.accept(existingEquipoId);
                        return;
                    }

                    // Si no existe, proceder con el registro
                    log("No se encontró un equipo existente para la IP: " + ip);
                    
                    // Generar un nombre único para el equipo
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String nombreEquipo = "EQ_" + ip.replace('.', '_') + "_" + timestamp;
                    
                    // Crear el cuerpo de la petición
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("nombre", "Equipo " + nombreEquipo);
                    jsonBody.put("identificador", nombreEquipo);
                    jsonBody.put("ip", ip);
                    jsonBody.put("puerto", 8080);
                    
                    log("Registrando nuevo equipo: " + jsonBody.toString());
                    
                    // Configurar el cliente HTTP
                    HttpClient client = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                            
                    // Crear la petición de registro
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(servidorUrl + "/api/equipos/registrar"))
                            .header("Content-Type", "application/json")
                            .header("Cookie", cookieSesion)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                            .build();

                    log("Enviando solicitud de registro de equipo...");
                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            int statusCode = response.statusCode();
                            String responseBody = response.body();
                            
                            log(String.format("Respuesta del servidor - Código: %d, Cuerpo: %s", 
                                statusCode, responseBody));
                            
                            if (statusCode == 200 || statusCode == 201) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(responseBody);
                                    String nuevoEquipoId = String.valueOf(jsonResponse.getLong("idEquipo"));
                                    log("Equipo registrado exitosamente con ID: " + nuevoEquipoId);
                                    
                                    // Iniciar el ping para mantener la conexión activa
                                    iniciarPing();
                                    
                                    // Notificar al consumidor
                                    equipoIdConsumer.accept(nuevoEquipoId);
                                } catch (Exception e) {
                                    log("Error al procesar la respuesta del servidor: " + e.getMessage());
                                    equipoIdConsumer.accept(null);
                                }
                            } else if (statusCode == 302) {
                                String location = response.headers().firstValue("Location").orElse("");
                                log("Redirección detectada. Posible sesión expirada: " + location);
                                equipoIdConsumer.accept(null);
                            } else {
                                log("Error en el servidor. Código: " + statusCode + ", Respuesta: " + responseBody);
                                equipoIdConsumer.accept(null);
                            }
                        })
                        .exceptionally(e -> {
                            log("Error en la solicitud de registro: " + e.getMessage());
                            if (e.getCause() != null) {
                                log("Causa: " + e.getCause().getMessage());
                            }
                            equipoIdConsumer.accept(null);
                            return null;
                        });
                } catch (Exception e) {
                    String errorMsg = "Error al registrar equipo: " + e.getMessage();
                    log(errorMsg);
                    if (e.getCause() != null) {
                        log("Causa: " + e.getCause().getMessage());
                    }
                    equipoIdConsumer.accept(null);
                }
            });
        } catch (Exception e) {
            String errorMsg = "Error en registrarEquipo: " + e.getMessage();
            log(errorMsg);
            if (e.getCause() != null) {
                log("Causa: " + e.getCause().getMessage());
            }
            equipoIdConsumer.accept(null);
        }
    }

    private void iniciarPing() {
        timerPing = new Timer();
        timerPing.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (verificarServidorActivo()) {
                        log("[" + LocalDateTime.now() + "] Servidor activo");
                    } else {
                        log("[" + LocalDateTime.now() + "] No se puede conectar al servidor");
                    }
                } catch (Exception e) {
                    log("[" + LocalDateTime.now() + "] Error de conexión: " + e.getMessage());
                }
            }
        }, 0, 5000);
    }

    /**
     * Verifica si el servidor está activo y accesible
     * @return true si el servidor responde correctamente, false en caso contrario
     */
    public boolean verificarServidorActivo() {
        // Verificar si la URL del servidor es válida
        if (servidorUrl == null || servidorUrl.trim().isEmpty()) {
            log("Error: URL del servidor no configurada");
            return false;
        }
        
        // Verificar si hay una cookie de sesión
        if (cookieSesion == null || cookieSesion.trim().isEmpty()) {
            log("Error: No hay una sesión activa");
            return false;
        }
        
        try {
            // Configurar el cliente HTTP con un tiempo de espera
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
                    
            // Crear la petición de ping
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/ping"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Cookie", cookieSesion)
                    .header("Accept", "application/json")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();

            // Enviar la petición de forma síncrona con un tiempo de espera
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                
                // Verificar si hay una redirección (302) y si es a la página de login
                if (statusCode == 302) {
                    String location = response.headers().firstValue("Location").orElse("");
                    if (location.contains("/login")) {
                        log("Sesión expirada o no autenticada. Redirigiendo a login...");
                        return false;
                    }
                }
                
                if (statusCode == 200) {
                    log("Ping exitoso al servidor");
                    return true;
                } else {
                    String responseBody = response.body();
                    log("Error en la respuesta del servidor. Código: " + statusCode + ", Respuesta: " + responseBody);
                    return false;
                }
            } catch (java.net.ConnectException e) {
                log("No se pudo conectar al servidor: " + e.getMessage());
                return false;
            } catch (java.net.UnknownHostException e) {
                log("No se pudo resolver el host del servidor: " + e.getMessage());
                return false;
            } catch (java.net.http.HttpTimeoutException e) {
                log("Tiempo de espera agotado al conectar con el servidor");
                return false;
            } catch (java.io.IOException e) {
                log("Error de E/S al conectar con el servidor: " + e.getMessage());
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restablecer el estado de interrupción
                log("La operación de ping fue interrumpida");
                return false;
            } catch (Exception e) {
                log("Error inesperado al verificar el servidor: " + e.getMessage());
                if (e.getCause() != null) {
                    log("Causa: " + e.getCause().getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            log("Error general al verificar el servidor: " + e.getMessage());
            return false;
        }
    }

    public void cargarCamaras(String equipoId, Consumer<JSONArray> camarasConsumer) {
        if (equipoId == null || equipoId.isEmpty()) {
            log("Error: ID de equipo no especificado");
            if (camarasConsumer != null) {
                camarasConsumer.accept(null);
            }
            return;
        }

        try {
            log("Intentando cargar cámaras para el equipo: " + equipoId);
            
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
                    
            String url = servidorUrl + "/api/camaras/equipo/" + equipoId;
            log("Solicitando cámaras desde: " + url);
                    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Cookie", cookieSesion)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int responseCode = response.statusCode();
                    String responseBody = response.body();
                    log("Respuesta del servidor - Código: " + responseCode + ", Cuerpo: " + responseBody);

                    try {
                        if (responseCode == 200) {
                            JSONArray camarasArray = new JSONArray(responseBody);
                            log("Se encontraron " + camarasArray.length() + " cámaras");

                            if (camarasConsumer != null) {
                                camarasConsumer.accept(camarasArray);
                            }
                        } else if (responseCode == 302) {
                            String location = response.headers().firstValue("Location").orElse("");
                            if (location.contains("/login")) {
                                log("Sesión expirada. Por favor, inicie sesión nuevamente.");
                            } else {
                                log("Redirección inesperada al cargar cámaras: " + location);
                            }
                            if (camarasConsumer != null) {
                                camarasConsumer.accept(null);
                            }
                        } else if (responseCode == 404) {
                            log("El equipo no existe, intentando registrar uno nuevo");
                            registrarEquipo(id -> {
                                if (id != null) {
                                    cargarCamaras(id, camarasConsumer);
                                } else if (camarasConsumer != null) {
                                    camarasConsumer.accept(null);
                                }
                            });
                        } else {
                            log("Error al cargar cámaras. Código: " + responseCode);
                            if (camarasConsumer != null) {
                                camarasConsumer.accept(null);
                            }
                        }
                    } catch (Exception e) {
                        log("Error al procesar la respuesta de las cámaras: " + e.getMessage());
                        if (camarasConsumer != null) {
                            camarasConsumer.accept(null);
                        }
                    }
                })
                .exceptionally(e -> {
                    log("Error en la solicitud de cámaras: " + e.getMessage());
                    if (camarasConsumer != null) {
                        camarasConsumer.accept(null);
                    }
                    return null;
                });
    } catch (Exception e) {
        log("Error inesperado al cargar cámaras: " + e.getMessage());
        if (camarasConsumer != null) {
            camarasConsumer.accept(null);
            }
        }
    }

    /**
     * Registra una cámara local en el servidor
     * @param equipoId ID del equipo al que pertenece la cámara
     * @param camaraIdConsumer Callback que se llama con el ID de la cámara registrada
     */
    public void registrarCamaraLocal(String equipoId, Consumer<Long> camaraIdConsumer) {
        log("Iniciando registro de cámara local...");
        
        // Validar parámetros de entrada
        if (equipoId == null || equipoId.trim().isEmpty()) {
            log("Error: El ID del equipo no puede estar vacío");
            if (camaraIdConsumer != null) camaraIdConsumer.accept(null);
            return;
        }
        
        try {
            // Verificar que tenemos una cookie de sesión
            if (cookieSesion == null || cookieSesion.isEmpty()) {
                log("Error: No hay una sesión activa");
                if (camaraIdConsumer != null) camaraIdConsumer.accept(null);
                return;
            }
            
            log("Registrando cámara local para el equipo: " + equipoId);
            
            // Usar el servicio de cámaras para registrar la cámara
            camaraService.registrarCamaraLocal(equipoId, 
                idCamara -> {
                    if (idCamara != null) {
                        log("Cámara registrada exitosamente con ID: " + idCamara);
                    } else {
                        log("No se pudo registrar la cámara");
                    }
                    if (camaraIdConsumer != null) {
                        camaraIdConsumer.accept(idCamara);
                    }
                }, 
                this::log
            );
        } catch (Exception e) {
            String errorMsg = "Error al registrar cámara local: " + e.getMessage();
            log(errorMsg);
            e.printStackTrace();
            if (camaraIdConsumer != null) {
                camaraIdConsumer.accept(null);
            }
        }
    }

    /**
     * Asigna una cámara a un equipo en el servidor
     * @param equipoId ID del equipo al que se asignará la cámara
     * @param idCamara ID de la cámara a asignar
     * @param callback Callback que se llama con el resultado de la operación
     */
    public void asignarCamaraAEquipo(String equipoId, Long idCamara, Consumer<Boolean> callback) {
        if (equipoId == null || equipoId.trim().isEmpty() || idCamara == null) {
            log("Error: Parámetros inválidos para asignar cámara al equipo");
            if (callback != null) callback.accept(false);
            return;
        }
        
        log(String.format("Asignando cámara %d al equipo %s", idCamara, equipoId));
        
        try {
            // Validar la cookie de sesión
            if (cookieSesion == null || cookieSesion.isEmpty()) {
                log("Error: No hay una sesión activa");
                if (callback != null) callback.accept(false);
                return;
            }
            
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
            
            String url = String.format("%s/api/equipos/%s/camaras/%d", servidorUrl, equipoId, idCamara);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Cookie", cookieSesion)
                .header("Accept", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int responseCode = response.statusCode();
                    String responseBody = response.body();
                    
                    log(String.format("Respuesta al asignar cámara - Código: %d, Cuerpo: %s", 
                        responseCode, responseBody));
                    
                    if (responseCode == 200) {
                        log("Cámara asignada exitosamente al equipo");
                        if (callback != null) callback.accept(true);
                    } else {
                        String errorMsg = "Error al asignar cámara al equipo. Código: " + responseCode;
                        log(errorMsg);
                        if (callback != null) {
                            callback.accept(false);
                        }
                    }
                })
                .exceptionally(e -> {
                    String errorMsg = "Error al asignar cámara al equipo: " + e.getMessage();
                    log(errorMsg);
                    if (callback != null) {
                        callback.accept(false);
                    }
                    return null;
                });
        } catch (Exception e) {
            String errorMsg = "Error inesperado al asignar cámara al equipo: " + e.getMessage();
            log(errorMsg);
            if (callback != null) {
                callback.accept(false);
            }
        }
    }

    /**
     * Envía un archivo al servidor
     * @param equipoId ID del equipo
     * @param camaraSeleccionada Nombre de la cámara seleccionada
     * @param archivoSeleccionado Archivo a enviar
     * @param tipoParam Tipo de archivo (FOTO/VIDEO)
     */
    public void enviarArchivo(String equipoId, String camaraSeleccionada, File archivoSeleccionado, String tipoParam) {
        log("=== INICIO DE ENVÍO DE ARCHIVO ===");
        log("Equipo ID: " + equipoId);
        log("Cámara seleccionada: " + camaraSeleccionada);
        log("Archivo: " + (archivoSeleccionado != null ? archivoSeleccionado.getAbsolutePath() : "null"));
        log("Tamaño del archivo: " + (archivoSeleccionado != null ? archivoSeleccionado.length() + " bytes" : "N/A"));
        log("Tipo de archivo: " + tipoParam);
        
        if (archivoSeleccionado == null) {
            log("Error: No hay archivo seleccionado para enviar.");
            return;
        }

        if (equipoId == null || equipoId.isEmpty() || camaraSeleccionada == null) {
            log("Error: Debes especificar equipo y seleccionar cámara.");
            return;
        }

        // Validar que el tipo sea válido (FOTO/VIDEO)
        if (!tipoParam.equalsIgnoreCase("FOTO") && !tipoParam.equalsIgnoreCase("VIDEO")) {
            log("Error: El tipo de archivo debe ser 'FOTO' o 'VIDEO'");
            return;
        }
        
        // Convertir a mayúsculas para asegurar que coincida con el enum
        final String tipo = tipoParam.toUpperCase();
        log("Tipo validado: " + tipo);

        // Obtener las cámaras del equipo
        Consumer<String> successHandler = new Consumer<String>() {
            @Override
            public void accept(String camaras) {
                try {
                    JSONArray camarasArray = new JSONArray(camaras);
                    String camaraId = null;
                    final String[] nombreCamara = { null };
                    
                    // Buscar la cámara por nombre
                    for (int i = 0; i < camarasArray.length(); i++) {
                        JSONObject camara = camarasArray.getJSONObject(i);
                        if (camara.getString("nombre").equals(camaraSeleccionada)) {
                            // Manejar el ID de la cámara que puede ser número o cadena
                            if (camara.get("idCamara") instanceof Integer) {
                                camaraId = String.valueOf(camara.getInt("idCamara"));
                            } else {
                                camaraId = camara.getString("idCamara");
                            }
                            nombreCamara[0] = camara.getString("nombre");
                            log("Cámara encontrada - ID: " + camaraId + ", Nombre: " + nombreCamara[0]);
                            break;
                        }
                    }
                    
                    if (camaraId == null) {
                        log("Error: No se encontró el ID de la cámara: " + camaraSeleccionada);
                        log("Cámaras disponibles: " + camarasArray.toString(2));
                        return;
                    }
                    
                    // Usar el servicio de subida de archivos
                    fileUploadService.enviarArchivo(equipoId, camaraId, nombreCamara[0], archivoSeleccionado, tipo);
                    
                } catch (Exception e) {
                    log("Error al procesar la respuesta de las cámaras: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        
        // Crear un consumer para manejar errores
        Consumer<String> errorHandler = new Consumer<String>() {
            @Override
            public void accept(String error) {
                log("Error al obtener las cámaras: " + error);
            }
        };
        
        // Llamar al servicio para obtener las cámaras
        camaraService.obtenerCamaras(equipoId, successHandler, errorHandler);
    }
    
    private boolean esIPValida(String ip, String[] excludedPrefixes) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        // Verificar si la IP coincide con alguno de los prefijos excluidos
        for (String prefix : excludedPrefixes) {
            if (ip.startsWith(prefix)) {
                return false;
            }
        }
        
        // Verificar el formato de la IP usando una expresión regular simple
        String[] octetos = ip.split("\\.");
        if (octetos.length != 4) {
            return false;
        }
        
        try {
            for (String octetoStr : octetos) {
                int octeto = Integer.parseInt(octetoStr);
                if (octeto < 0 || octeto > 255) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Detiene cualquier operación en curso y libera recursos.
     */
    public void detener() {
        // Detener el temporizador de ping si está activo
        if (timerPing != null) {
            timerPing.cancel();
            timerPing = null;
        }
        
        // Cerrar la conexión WebSocket si está abierta
        if (stompClient != null) {
            try {
                stompClient.desconectar();
            } catch (Exception e) {
                log("Error al cerrar la conexión WebSocket: " + e.getMessage());
            }
        }
        
        // Notificar que la conexión se ha cerrado
        if (connectionStatusConsumer != null) {
            connectionStatusConsumer.accept(false);
        }
        
        log("Recursos liberados correctamente");
    }
}
