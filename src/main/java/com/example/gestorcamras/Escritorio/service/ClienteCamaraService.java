package com.example.gestorcamras.Escritorio.service;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class ClienteCamaraService {
    private final String servidorUrl;
    private final String cookieSesion;

    public ClienteCamaraService(String servidorUrl, String cookieSesion) {
        this.servidorUrl = servidorUrl;
        this.cookieSesion = cookieSesion;
    }

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
            
            // Crear nombre único para la cámara
            String nombreCamara = "Cámara Local " + System.currentTimeMillis();
            
            // Crear el objeto JSON para la cámara
            JSONObject camaraJson = new JSONObject();
            camaraJson.put("nombre", nombreCamara);
            camaraJson.put("ip", "127.0.0.1");
            camaraJson.put("tipo", "local");
            camaraJson.put("activa", true);
            camaraJson.put("equipoId", Long.parseLong(equipoId));
            
            // Crear cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
                
            // 1. Obtener token CSRF
            logger.accept("Obteniendo token CSRF...");
            HttpRequest csrfRequest = HttpRequest.newBuilder()
                .uri(URI.create(servidorUrl + "/api/csrf"))
                .header("Cookie", cookieSesion)
                .GET()
                .build();
                
            HttpResponse<String> csrfResponse = client.send(csrfRequest, HttpResponse.BodyHandlers.ofString());
            
            // Extraer el token CSRF de la respuesta
            String csrfToken = null;
            if (csrfResponse.statusCode() == 200) {
                try {
                    JSONObject csrfJson = new JSONObject(csrfResponse.body());
                    csrfToken = csrfJson.optString("token");
                } catch (Exception e) {
                    logger.accept("Error al parsear la respuesta CSRF: " + e.getMessage());
                }
            }
            
            if (csrfToken == null) {
                logger.accept("Error: No se pudo obtener el token CSRF");
                return;
            }
            
            logger.accept("Token CSRF obtenido correctamente");
            
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
                .header("X-XSRF-TOKEN", csrfToken)
                .header("Referer", servidorUrl)
                .header("Origin", servidorUrl)
                .POST(HttpRequest.BodyPublishers.ofString(camaraJson.toString()))
                .build();
                
            // Enviar la solicitud
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.accept("Respuesta del servidor - Código: " + response.statusCode());
            logger.accept("Cuerpo de la respuesta: " + response.body());
            
            if (response.statusCode() == 302) {
                // Si hay una redirección, obtener la ubicación
                String location = response.headers().firstValue("Location").orElse("");
                logger.accept("Redireccionando a: " + location);
                if (location.contains("login")) {
                    logger.accept("Error: La sesión ha expirado");
                    return;
                }
            }
            
            if (response.statusCode() == 201) {  // 201 Created
                try {
                    JSONObject respuesta = new JSONObject(response.body());
                    if (respuesta.has("id")) {
                        long idCamara = respuesta.getLong("id");
                        logger.accept("Cámara local registrada exitosamente con ID: " + idCamara);
                        camaraIdConsumer.accept(idCamara);
                    } else {
                        logger.accept("Error: La respuesta no contiene el ID de la cámara");
                    }
                } catch (Exception e) {
                    logger.accept("Error al procesar la respuesta del servidor: " + e.getMessage());
                }
            } else {
                logger.accept("Error al registrar cámara local. Código: " + response.statusCode() + ", Respuesta: " + response.body());
            }
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
