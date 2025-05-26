package com.example.gestorcamras.Escritorio.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;

import java.util.function.Consumer;
import org.json.JSONObject;

public class ClienteCamaraService {
    private final String servidorUrl;
    private final String cookieSesion;
    
    public ClienteCamaraService(String servidorUrl, String cookieSesion) {
        this.servidorUrl = servidorUrl.endsWith("/") ? 
                          servidorUrl.substring(0, servidorUrl.length() - 1) : 
                          servidorUrl;
        this.cookieSesion = cookieSesion;
    }
    
    /**
     * Registra una cámara local en el servidor
     * @param equipoId ID del equipo al que pertenece la cámara
     * @param camaraIdConsumer Callback que se llama con el ID de la cámara registrada
     * @param logger Callback para registrar mensajes
     */
    public void registrarCamaraLocal(String equipoId, Consumer<Long> camaraIdConsumer, Consumer<String> logger) {
        try {
            // Validar parámetros de entrada
            if (equipoId == null || equipoId.trim().isEmpty()) {
                logger.accept("Error: El ID del equipo no puede estar vacío");
                return;
            }
            
            // Verificar que tenemos una cookie de sesión
            if (cookieSesion == null || cookieSesion.isEmpty()) {
                logger.accept("Error: No hay una sesión activa");
                return;
            }
            
            // Crear cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
            
            // Proceder directamente con el registro de la cámara
            registrarCamaraConToken(client, equipoId, camaraIdConsumer, logger, null);
                
        } catch (Exception e) {
            logger.accept("Error inesperado al registrar cámara: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    
    /**
     * Registra una cámara en el servidor
     */
    private void registrarCamaraConToken(HttpClient client, String equipoId, 
                                         Consumer<Long> camaraIdConsumer, 
                                         Consumer<String> logger, 
                                         String csrfToken) {
        try {
            logger.accept("Registrando cámara local...");
            String nombreCamara = ("Cámara_Local_" + System.currentTimeMillis()).replace(" ", "_");
            
            // Crear el objeto JSON base para la cámara
            JSONObject camaraJson = new JSONObject();
            camaraJson.put("nombre", nombreCamara);
            camaraJson.put("ip", "127.0.0.1");
            camaraJson.put("tipo", "local");
            camaraJson.put("activa", true);
            camaraJson.put("equipoId", Long.parseLong(equipoId));
            
            // Primero, obtener la ubicación
            obtenerUbicacion(ubicacion -> {
                try {
                    // Una vez que tenemos la ubicación, actualizamos el JSON de la cámara
                    if (ubicacion != null) {
                        camaraJson.put("latitud", ubicacion.getDouble("latitud"));
                        camaraJson.put("longitud", ubicacion.getDouble("longitud"));
                        camaraJson.put("direccion", ubicacion.getString("direccion"));
                    }
                    
                    // Ahora que tenemos la ubicación (o no), procedemos con el registro
                    registrarCamaraConUbicacion(client, camaraJson, camaraIdConsumer, logger, csrfToken);
                    
                } catch (Exception e) {
                    logger.accept("Error al procesar la ubicación: " + e.getMessage());
                    // Continuar con el registro aunque falle la ubicación
                    registrarCamaraConUbicacion(client, camaraJson, camaraIdConsumer, logger, csrfToken);
                }
            }, logger);
            
        } catch (Exception e) {
            logger.accept("Error inesperado al registrar cámara: " + e.getMessage());
            if (camaraIdConsumer != null) camaraIdConsumer.accept(null);
        }
    }
    
    /**
     * Obtiene la ubicación actual del sistema usando un servicio web externo
     * @param callback Se llama con la ubicación obtenida (o null si falla)
     * @param logger Logger para mensajes de depuración
     */
    private void obtenerUbicacion(Consumer<JSONObject> callback, Consumer<String> logger) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://ip-api.com/json/"))
                .timeout(Duration.ofSeconds(5))
                .build();
            
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JSONObject locationData = new JSONObject(response.body());
                            if ("success".equals(locationData.optString("status"))) {
                                double lat = locationData.getDouble("lat");
                                double lon = locationData.getDouble("lon");
                                String city = locationData.optString("city", "");
                                String country = locationData.optString("country", "");
                                String region = locationData.optString("regionName", "");
                                
                                // Construir una dirección legible
                                StringBuilder direccion = new StringBuilder();
                                if (!city.isEmpty()) direccion.append(city).append(", ");
                                if (!region.isEmpty() && !region.equals(city)) direccion.append(region).append(", ");
                                if (!country.isEmpty()) direccion.append(country);
                                
                                // Crear el objeto de ubicación
                                JSONObject ubicacion = new JSONObject();
                                ubicacion.put("latitud", lat);
                                ubicacion.put("longitud", lon);
                                ubicacion.put("direccion", direccion.toString().replaceAll(", $", ""));
                                
                                logger.accept(String.format("Ubicación obtenida: %f, %f - %s", 
                                    lat, lon, direccion));
                                
                                callback.accept(ubicacion);
                                return;
                            } else {
                                logger.accept("No se pudo obtener la ubicación: " + 
                                    locationData.optString("message", "Error desconocido"));
                            }
                        } catch (Exception e) {
                            logger.accept("Error al procesar la ubicación: " + e.getMessage());
                        }
                    } else {
                        logger.accept("Error al obtener la ubicación: HTTP " + response.statusCode());
                    }
                    // Si llegamos aquí, hubo un error
                    callback.accept(null);
                })
                .exceptionally(e -> {
                    logger.accept("Excepción al obtener la ubicación: " + e.getMessage());
                    callback.accept(null);
                    return null;
                });
            
            logger.accept("Solicitando ubicación...");
            
        } catch (Exception e) {
            logger.accept("Error al intentar obtener la ubicación: " + e.getMessage());
            callback.accept(null);
        }
    }
    
    /**
     * Registra la cámara en el servidor con la ubicación obtenida (si la hay)
     */
    private void registrarCamaraConUbicacion(HttpClient client, JSONObject camaraJson, 
                                            Consumer<Long> camaraIdConsumer, 
                                            Consumer<String> logger, String csrfToken) {
        try {
            logger.accept("Enviando solicitud de registro de cámara...");
            logger.accept("URL: " + servidorUrl + "/api/camaras");
            logger.accept("Cuerpo: " + camaraJson.toString());
            
            // Construir la solicitud con los headers necesarios
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(servidorUrl + "/api/camaras"))
                .header("Content-Type", "application/json")
                .header("Cookie", cookieSesion)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", servidorUrl)
                .header("Origin", servidorUrl)
                .POST(BodyPublishers.ofString(camaraJson.toString()))
                .build();
                
            // Enviar la solicitud de forma asíncrona
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int statusCode = response.statusCode();
                    String responseBody = response.body();
                    
                    logger.accept(String.format("Respuesta del servidor - Código: %d, Cuerpo: %s", 
                            statusCode, responseBody));
                    
                    if (statusCode == 302) {
                        // Si hay una redirección, obtener la ubicación
                        String location = response.headers().firstValue("Location").orElse("");
                        logger.accept("Redireccionando a: " + location);
                        if (location.contains("login")) {
                            logger.accept("Error: La sesión ha expirado");
                            return;
                        }
                    }
                    
                    if (statusCode == 201 || statusCode == 200) {  // 201 Created o 200 OK
                        try {
                            JSONObject respuesta = new JSONObject(responseBody);
                            String idKey = respuesta.has("idCamara") ? "idCamara" : "id";
                            if (respuesta.has(idKey)) {
                                long idCamara = respuesta.getLong(idKey);
                                logger.accept("Cámara local registrada exitosamente con ID: " + idCamara);
                                camaraIdConsumer.accept(idCamara);
                                return;
                            } else {
                                logger.accept("La respuesta del servidor no contiene un ID de cámara válido: " + responseBody);
                            }
                        } catch (Exception e) {
                            logger.accept("Error al procesar la respuesta del servidor: " + e.getMessage());
                        }
                    } 
                    
                    // Si llegamos aquí, hubo un error
                    logger.accept("Error al registrar cámara local. Código: " + statusCode + 
                                ", Respuesta: " + responseBody);
                })
                .exceptionally(e -> {
                    logger.accept("Error en la solicitud de registro de cámara: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                });
                
        } catch (NumberFormatException e) {
            logger.accept("Error: El ID del equipo no es un número válido: " + e.getMessage());
        } catch (Exception e) {
            logger.accept("Error inesperado al registrar cámara: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String obtenerDispositivoVideo() {
        return "0"; // ID del primer dispositivo de video
    }
    
    /**
     * Obtiene la lista de cámaras de un equipo específico
     * @param equipoId ID del equipo del que se quieren obtener las cámaras
     * @param onSuccess Callback que se llama con la lista de cámaras en formato JSON
     * @param onError Callback que se llama si ocurre un error
     */
    public void obtenerCamaras(String equipoId, Consumer<String> onSuccess, Consumer<String> onError) {
        try {
            if (equipoId == null || equipoId.trim().isEmpty()) {
                onError.accept("El ID del equipo no puede estar vacío");
                return;
            }
            
            HttpClient client = HttpClient.newHttpClient();
            String url = servidorUrl + "/api/camaras/equipo/" + equipoId;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Cookie", cookieSesion)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
                    
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int statusCode = response.statusCode();
                    String responseBody = response.body();
                    
                    if (statusCode == 200) {
                        onSuccess.accept(responseBody);
                    } else {
                        onError.accept("Error al obtener cámaras. Código: " + statusCode + ", Respuesta: " + responseBody);
                    }
                })
                .exceptionally(e -> {
                    onError.accept("Error en la solicitud de cámaras: " + e.getMessage());
                    return null;
                });
        } catch (Exception e) {
            onError.accept("Error inesperado al obtener cámaras: " + e.getMessage());
        }
    }
}
