package com.example.gestorcamras.Escritorio.service;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class ClienteEquipoService {
    private final String servidorUrl;
    private final String cookieSesion;

    public ClienteEquipoService(String servidorUrl, String cookieSesion) {
        this.servidorUrl = servidorUrl;
        this.cookieSesion = cookieSesion;
    }

    public void buscarEquipoPorIp(String ip, Consumer<String> equipoIdConsumer, Consumer<String> logger) {
        try {
            if (ip == null || ip.trim().isEmpty()) {
                logger.accept("Error: La dirección IP no puede estar vacía");
                return;
            }
            
            if (cookieSesion == null || cookieSesion.isEmpty()) {
                logger.accept("Error: No hay una sesión activa");
                return;
            }
            
            logger.accept("Buscando equipo con IP: " + ip);
            
            // Crear cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
            
            // Construir la URL de búsqueda
            String url = servidorUrl + "/api/equipos/ip/" + ip;
            logger.accept("URL de búsqueda: " + url);
            
            // Crear la solicitud
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Cookie", cookieSesion)
                .header("X-Requested-With", "XMLHttpRequest")
                .GET()
                .build();
                
            // Enviar la solicitud
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.accept("Respuesta del servidor - Código: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                // Intentar parsear la respuesta como JSON
                try {
                    JSONObject equipo = new JSONObject(response.body());
                    if (equipo.has("idEquipo")) {
                        String idEquipo = String.valueOf(equipo.getLong("idEquipo"));
                        logger.accept("Equipo encontrado con ID: " + idEquipo);
                        equipoIdConsumer.accept(idEquipo);
                        return;
                    }
                } catch (Exception e) {
                    logger.accept("Error al procesar la respuesta del servidor: " + e.getMessage());
                }
            } else if (response.statusCode() == 404) {
                logger.accept("No se encontró ningún equipo con la IP: " + ip);
            } else if (response.statusCode() == 302) {
                String location = response.headers().firstValue("Location").orElse("");
                if (location.contains("login")) {
                    logger.accept("Error: La sesión ha expirado");
                    return;
                }
            } else {
                logger.accept("Error al buscar equipo. Código: " + response.statusCode());
            }
            
            // Si llegamos aquí, no se encontró el equipo o hubo un error
            equipoIdConsumer.accept(null);
            
        } catch (Exception e) {
            logger.accept("Error inesperado al buscar equipo: " + e.getMessage());
            e.printStackTrace();
            equipoIdConsumer.accept(null);
        }
    }
    
    // Otros métodos relacionados con equipos pueden ir aquí
}
