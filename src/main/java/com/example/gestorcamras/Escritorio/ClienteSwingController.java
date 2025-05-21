package com.example.gestorcamras.Escritorio;

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
import java.nio.charset.StandardCharsets;
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

    public ClienteSwingController(String usuario, String cookieSesion, String servidorUrl) {
        this.cookieSesion = cookieSesion;
        this.servidorUrl = servidorUrl != null ? servidorUrl : "http://localhost:8080";
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
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/ip/" + ip))
                    .header("Cookie", cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

            if (responseCode == 200) {
                JSONObject responseJson = new JSONObject(responseBody);
                String idEquipo = responseJson.getLong("id") + "";
                if (equipoIdConsumer != null) {
                    equipoIdConsumer.accept(idEquipo);
                }
                return true;
            } else if (responseCode == 404) {
                log("No se encontró equipo para la IP: " + ip);
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

    public void registrarEquipo(Consumer<String> equipoIdConsumer) {
        try {
            String ipLocal = getLocalIP();
            
            // Primero buscar si existe un equipo con esta IP
            if (buscarEquipoPorIp(ipLocal, equipoIdConsumer)) {
                log("Usando equipo existente para esta IP");
                return;
            }

            // Si no existe, crear uno nuevo
            String equipoId = "EQ_" + ipLocal.replace(".", "_") + "_" + System.currentTimeMillis();
            
            HttpClient client = HttpClient.newHttpClient();
            String json = String.format(
                "{\"nombre\":\"Equipo %s\",\"identificador\":\"%s\",\"ip\":\"%s\",\"puerto\":8080}", 
                equipoId, equipoId, ipLocal);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/registrar"))
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookieSesion)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

            if (responseCode == 200 || responseCode == 201) {
                try {
                    JSONObject responseJson = new JSONObject(responseBody);
                    String idEquipo = responseJson.getLong("id") + "";
                    log("Nuevo equipo registrado con ID: " + idEquipo);
                    if (equipoIdConsumer != null) {
                        equipoIdConsumer.accept(idEquipo);
                    }
                    iniciarPing();
                } catch (Exception e) {
                    log("Error al procesar respuesta del servidor: " + e.getMessage());
                }
            } else {
                log("Error al registrar nuevo equipo. Código: " + responseCode + ", Respuesta: " + responseBody);
            }
        } catch (Exception e) {
            log("Error al registrar equipo: " + e.getMessage());
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
            if (servidorUrl == null || servidorUrl.isEmpty()) {
                log("Error: URL del servidor no configurada");
                return false;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/ping"))
                    .header("Cookie", cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

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
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/" + equipoId))
                    .header("Cookie", cookieSesion)
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            
            if (responseCode == 404) {
                log("El equipo no existe, intentando registrar uno nuevo");
                registrarEquipo(id -> {
                    if (id != null) {
                        cargarCamaras(id, camarasConsumer);
                    }
                });
                return;
            } else if (responseCode != 200) {
                log("Error al cargar cámaras: " + response.body());
                return;
            }

            JSONObject obj = new JSONObject(response.body());
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
            }
        } catch (Exception e) {
            log("Error al cargar cámaras: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void registrarCamaraLocal(String equipoId, Consumer<Long> camaraIdConsumer) {
        try {
            String camaraLocal = obtenerDispositivoVideo();
            if (camaraLocal != null) {
                HttpClient client = HttpClient.newHttpClient();
                String json = String.format("{\"nombre\":\"Cámara Local\",\"dispositivo\":\"%s\"}", camaraLocal);
                
                String url = servidorUrl + "/api/camaras/registrar";
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Cookie", cookieSesion)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int responseCode = response.statusCode();

                if (responseCode == 200 || responseCode == 201) {
                    log("Cámara local registrada exitosamente");
                    
                    JSONObject respuesta = new JSONObject(response.body());
                    Long idCamara = respuesta.getLong("id");
                    
                    if (camaraIdConsumer != null) {
                        camaraIdConsumer.accept(idCamara);
                    }
                    
                    asignarCamaraAEquipo(equipoId, idCamara, success -> {
                        if (success) {
                            log("Cámara asignada al equipo exitosamente");
                        } else {
                            log("Error al asignar cámara al equipo");
                        }
                    });
                } else {
                    log("Error al registrar cámara local. Código: " + responseCode);
                }
            }
        } catch (Exception e) {
            log("Error al registrar cámara local: " + e.getMessage());
        }
    }

    private String obtenerDispositivoVideo() {
        try {
            return "0"; // ID del primer dispositivo de video
        } catch (Exception e) {
            log("Error al obtener dispositivo de video: " + e.getMessage());
            return null;
        }
    }

    private void asignarCamaraAEquipo(String equipoId, Long idCamara, Consumer<Boolean> callback) {
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
