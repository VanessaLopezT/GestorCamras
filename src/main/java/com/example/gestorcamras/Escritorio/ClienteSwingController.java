package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.Escritorio.service.ClienteCamaraService;
import com.example.gestorcamras.Escritorio.service.ClienteEquipoService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

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

    public ClienteSwingController(String usuario, String cookieSesion, String servidorUrl) {
        this.cookieSesion = cookieSesion;
        this.servidorUrl = servidorUrl != null ? servidorUrl : "http://localhost:8080";
        // Inicializar servicios
        this.camaraService = new ClienteCamaraService(this.servidorUrl, this.cookieSesion);
        this.equipoService = new ClienteEquipoService(this.servidorUrl, this.cookieSesion);
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

    public boolean buscarEquipoPorIp(String ip, Consumer<String> equipoIdConsumer) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
                    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/ip/" + ip))
                    .header("Cookie", cookieSesion)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();
            
            log(String.format("Respuesta de buscarEquipoPorIp - Código: %d, Cuerpo: %s", responseCode, responseBody));

            if (responseCode == 200) {
                try {
                    JSONObject responseJson = new JSONObject(responseBody);
                    // Intentar obtener el ID de diferentes maneras según el formato de respuesta
                    String idEquipo = "";
                    
                    // Verificar diferentes formatos de respuesta
                    if (responseJson.has("idEquipo")) {
                        // Formato: {"idEquipo":5, ...}
                        idEquipo = String.valueOf(responseJson.getInt("idEquipo"));
                    } else if (responseJson.has("id")) {
                        // Formato: {"id":"5", ...}
                        idEquipo = responseJson.optString("id", "");
                    } else if (responseJson.has("content") && responseJson.get("content") instanceof JSONArray) {
                        // Formato paginado: {"content":[{"idEquipo":5, ...}], ...}
                        JSONArray content = responseJson.getJSONArray("content");
                        if (content.length() > 0) {
                            JSONObject equipo = content.getJSONObject(0);
                            if (equipo.has("idEquipo")) {
                                idEquipo = String.valueOf(equipo.getInt("idEquipo"));
                            } else {
                                idEquipo = equipo.optString("id", "");
                            }
                        }
                    }
                    
                    if (!idEquipo.isEmpty()) {
                        log("Equipo encontrado con ID: " + idEquipo);
                        if (equipoIdConsumer != null) {
                            equipoIdConsumer.accept(idEquipo);
                        }
                        return true;
                    } else {
                        log("No se pudo extraer el ID del equipo de la respuesta. Formato inesperado.");
                        log("Respuesta completa: " + responseBody);
                        return false;
                    }
                } catch (Exception e) {
                    log("Error al procesar la respuesta del equipo: " + e.getMessage());
                    return false;
                }
            } else if (responseCode == 404) {
                log("No se encontró equipo para la IP: " + ip);
                return false;
            } else if (responseCode == 302) {
                log("Redirección detectada al buscar equipo. La sesión pudo haber expirado.");
                return false;
            } else {
                log("Error al buscar equipo. Código: " + responseCode + ", Respuesta: " + responseBody);
                return false;
            }
        } catch (Exception ex) {
            log("Error en buscarEquipoPorIp: " + ex.getMessage());
            return false;
        }
    }

    private String obtenerDireccionIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log("Error al obtener la dirección IP: " + e.getMessage());
            return "127.0.0.1";
        }
    }

    public void registrarEquipo(Consumer<String> equipoIdConsumer) {
        try {
            String ip = obtenerDireccionIP();
            log("Buscando equipo existente para IP: " + ip);
            
            // Primero buscar si existe un equipo con esta IP
            buscarEquipoPorIp(ip, existingEquipoId -> {
                if (existingEquipoId != null && !existingEquipoId.isEmpty()) {
                    log("Usando equipo existente con ID: " + existingEquipoId);
                    if (equipoIdConsumer != null) {
                        equipoIdConsumer.accept(existingEquipoId);
                        iniciarPing();
                    }
                    return;
                }

                // Si no existe, proceder con el registro
                try {
                    String nombreEquipo = "EQ_" + ip.replace('.', '_') + "_" + System.currentTimeMillis();
                    String jsonBody = String.format(
                        "{\"nombre\":\"Equipo %s\",\"identificador\":\"%s\",\"ip\":\"%s\",\"puerto\":8080}",
                        nombreEquipo, nombreEquipo, ip
                    );

                    log("Registrando nuevo equipo: " + jsonBody);
                    
                    HttpClient client = HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(servidorUrl + "/api/equipos/registrar"))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("Cookie", cookieSesion)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            int statusCode = response.statusCode();
                            String responseBody = response.body();
                            
                            log(String.format("Respuesta del servidor - Código: %d, Cuerpo: %s", statusCode, responseBody));

                            if (statusCode == 200 || statusCode == 201) {
                                try {
                                    String idEquipo = "";
                                    if (responseBody != null && !responseBody.isEmpty()) {
                                        JSONObject jsonResponse = new JSONObject(responseBody);
                                        idEquipo = jsonResponse.optString("idEquipo", "");
                                        if (idEquipo.isEmpty()) {
                                            idEquipo = jsonResponse.optString("id", "");
                                        }
                                    }
                                    
                                    if (!idEquipo.isEmpty()) {
                                        log("Equipo registrado exitosamente con ID: " + idEquipo);
                                        if (equipoIdConsumer != null) {
                                            equipoIdConsumer.accept(idEquipo);
                                            iniciarPing();
                                        }
                                    } else {
                                        log("No se pudo obtener el ID del equipo de la respuesta");
                                        if (equipoIdConsumer != null) {
                                            equipoIdConsumer.accept(null);
                                        }
                                    }
                                } catch (Exception e) {
                                    log("Error al procesar la respuesta del servidor: " + e.getMessage());
                                    if (equipoIdConsumer != null) {
                                        equipoIdConsumer.accept(null);
                                    }
                                }
                            } else if (statusCode == 302) {
                                String location = response.headers().firstValue("Location").orElse("");
                                log("Redirección detectada. Posible sesión expirada: " + location);
                                if (equipoIdConsumer != null) {
                                    equipoIdConsumer.accept(null);
                                }
                            } else {
                                log("Error en el servidor. Código: " + statusCode + ", Respuesta: " + responseBody);
                                if (equipoIdConsumer != null) {
                                    equipoIdConsumer.accept(null);
                                }
                            }
                        })
                        .exceptionally(e -> {
                            log("Error en la solicitud de registro: " + e.getMessage());
                            if (equipoIdConsumer != null) {
                                equipoIdConsumer.accept(null);
                            }
                            return null;
                        });
                } catch (Exception e) {
                    log("Error al registrar equipo: " + e.getMessage());
                    if (equipoIdConsumer != null) {
                        equipoIdConsumer.accept(null);
                    }
                }
            });
        } catch (Exception e) {
            log("Error en registrarEquipo: " + e.getMessage());
            if (equipoIdConsumer != null) {
                equipoIdConsumer.accept(null);
            }
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

    public boolean verificarServidorActivo() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
                    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/ping"))
                    .header("Cookie", cookieSesion)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

            // Verificar si hay una redirección (302) y si es a la página de login
            if (responseCode == 302) {
                String location = response.headers().firstValue("Location").orElse("");
                if (location.contains("/login")) {
                    log("Sesión expirada o no autenticada. Redirigiendo a login...");
                    return false;
                }
            }

            if (responseCode == 200) {
                log("Servidor activo y conectado");
                return true;
            } else {
                log("Error al conectar al servidor. Código: " + responseCode + ", Respuesta: " + responseBody);
                return false;
            }
        } catch (Exception e) {
            log("Error al verificar conexión: " + e.getMessage());
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
                    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/" + equipoId))
                    .header("Cookie", cookieSesion)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int responseCode = response.statusCode();
                    String responseBody = response.body();
                    
                    if (responseCode == 200) {
                        try {
                            JSONObject obj = new JSONObject(responseBody);
                            JSONArray camarasArray = new JSONArray();
                            
                            if (obj.has("camaras")) {
                                Object camarasObj = obj.get("camaras");
                                
                                if (camarasObj instanceof JSONArray) {
                                    camarasArray = (JSONArray) camarasObj;
                                } else if (camarasObj instanceof JSONObject) {
                                    camarasArray.put(camarasObj);
                                }
                                
                                if (camarasConsumer != null) {
                                    camarasConsumer.accept(camarasArray);
                                }
                            } else {
                                log("No se encontraron cámaras para este equipo");
                                if (camarasConsumer != null) {
                                    camarasConsumer.accept(new JSONArray());
                                }
                            }
                        } catch (Exception e) {
                            log("Error al procesar la respuesta de las cámaras: " + e.getMessage());
                            if (camarasConsumer != null) {
                                camarasConsumer.accept(null);
                            }
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
                        log("Error al cargar cámaras. Código: " + responseCode + ", Respuesta: " + responseBody);
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
            log("Error en cargarCamaras: " + e.getMessage());
            if (camarasConsumer != null) {
                camarasConsumer.accept(null);
            }
        }

            // El código ya se movió dentro del manejador de la respuesta asíncrona
            // Este bloque ya no es necesario aquí
    }

    public void registrarCamaraLocal(String equipoId, Consumer<Long> camaraIdConsumer) {
        log("Iniciando registro de cámara local...");
        try {
            camaraService.registrarCamaraLocal(equipoId, camaraIdConsumer, this::log);
        } catch (Exception e) {
            log("Error al registrar cámara local: " + e.getMessage());
        }
    }

    private String obtenerDispositivoVideo() {
        return camaraService.obtenerDispositivoVideo();
    }

    public void asignarCamaraAEquipo(String equipoId, Long idCamara, Consumer<Boolean> callback) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/" + equipoId + "/camaras/" + idCamara))
                    .header("Cookie", cookieSesion)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int responseCode = response.statusCode();
                    if (responseCode == 200) {
                        callback.accept(true);
                    } else {
                        log("Error al asignar cámara al equipo. Código: " + responseCode);
                        callback.accept(false);
                    }
                })
                .exceptionally(e -> {
                    log("Error al asignar cámara al equipo: " + e.getMessage());
                    callback.accept(false);
                    return null;
                });
        } catch (Exception e) {
            log("Error al asignar cámara al equipo: " + e.getMessage());
            callback.accept(false);
        }
    }

    public void enviarArchivo(String equipoId, String camaraSeleccionada, File archivoSeleccionado, String tipo) {
        if (archivoSeleccionado == null) {
            log("Error: No hay archivo seleccionado para enviar.");
            return;
        }

        if (equipoId == null || equipoId.isEmpty() || camaraSeleccionada == null) {
            log("Error: Debes especificar equipo y seleccionar cámara.");
            return;
        }

        String boundary = "===" + System.currentTimeMillis() + "===";

        try {
            byte[] fileData = getMultipartFormData(boundary, tipo, camaraSeleccionada, archivoSeleccionado);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/" + equipoId + "/camaras/" + camaraSeleccionada + "/archivo"))
                    .header("Cookie", cookieSesion)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileData))
                    .build();
                    
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int responseCode = response.statusCode();
                    String respuesta = response.body();

                    if (responseCode == 200 || responseCode == 201) {
                        log("Archivo enviado correctamente. Código: " + responseCode);
                    } else {
                        log("Error al enviar archivo. Código: " + responseCode);
                        log(respuesta);
                    }
                })
                .exceptionally(e -> {
                    log("Excepción al enviar archivo: " + e.getMessage());
                    return null;
                });
        } catch (Exception e) {
            log("Error al preparar el envío del archivo: " + e.getMessage());
        }
    }

    private byte[] getMultipartFormData(String boundary, String tipo, String camaraSeleccionada, File archivoSeleccionado) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        // Parte: tipo de archivo
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"tipo\"\r\n");
        writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        writer.append(tipo.toUpperCase()).append("\r\n");
        writer.flush();

        // Parte: nombre cámara
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"camara\"\r\n");
        writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        writer.append(camaraSeleccionada).append("\r\n");
        writer.flush();

        // Parte: archivo
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"" + archivoSeleccionado.getName() + "\"\r\n");
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(archivoSeleccionado.getName()))
                .append("\r\n");
        writer.append("Content-Transfer-Encoding: binary\r\n\r\n");
        writer.flush();

        // Escribimos archivo binario
        try (FileInputStream inputStream = new FileInputStream(archivoSeleccionado)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Fin del multipart
        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.flush();

        return outputStream.toByteArray();
    }

    public void dispose() {
        if (timerPing != null) {
            timerPing.cancel();
        }
    }
}
