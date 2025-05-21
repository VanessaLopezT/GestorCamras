package com.example.gestorcamras.Escritorio.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

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
            String nombreCamara = "Cámara Local " + System.currentTimeMillis();
            
            // Crear el objeto JSON para la cámara
            JSONObject camaraJson = new JSONObject();
            camaraJson.put("nombre", nombreCamara);
            camaraJson.put("ip", "127.0.0.1");
            camaraJson.put("tipo", "local");
            camaraJson.put("activa", true);
            camaraJson.put("equipoId", Long.parseLong(equipoId));
            
            // Crear la solicitud para registrar la cámara
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
                    
                    if (statusCode == 201) {  // 201 Created
                        try {
                            JSONObject respuesta = new JSONObject(responseBody);
                            if (respuesta.has("id")) {
                                long idCamara = respuesta.getLong("id");
                                logger.accept("Cámara local registrada exitosamente con ID: " + idCamara);
                                camaraIdConsumer.accept(idCamara);
                                return;
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
}
