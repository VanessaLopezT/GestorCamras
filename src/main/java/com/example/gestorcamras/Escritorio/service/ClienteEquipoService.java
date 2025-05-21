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
    private Consumer<String> logger = msg -> {}; // Logger por defecto no hace nada

    public ClienteEquipoService(String servidorUrl, String cookieSesion) {
        this.servidorUrl = servidorUrl.endsWith("/") ? 
                          servidorUrl.substring(0, servidorUrl.length() - 1) : 
                          servidorUrl;
        this.cookieSesion = cookieSesion;
    }
    
    /**
     * Establece el logger a utilizar
     */
    public void setLogger(Consumer<String> logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    /**
     * Busca un equipo por su dirección IP
     * @param ip Dirección IP a buscar
     * @param equipoIdConsumer Callback que recibe el ID del equipo encontrado (o null si no se encuentra)
     * @param logger Logger opcional (si no se proporciona, se usará el logger interno)
     */
    /**
     * Busca un equipo por su dirección IP
     * @param ip Dirección IP a buscar (no puede ser nula o vacía)
     * @param equipoIdConsumer Callback que recibirá el ID del equipo si se encuentra (opcional)
     * @param logger Logger para mensajes de depuración (opcional)
     * @throws IllegalArgumentException si la IP es nula o vacía
     */
    public void buscarEquipoPorIp(String ip, Consumer<String> equipoIdConsumer, Consumer<String> logger) {
        // Validar parámetros de entrada
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección IP no puede ser nula o vacía");
        }
        
        // Usar el logger proporcionado o el interno si es nulo
        final Consumer<String> log = (logger != null) ? logger : this.logger;
        
        // Verificar sesión
        if (cookieSesion == null || cookieSesion.isEmpty()) {
            String errorMsg = "Error: No hay una sesión activa";
            log.accept(errorMsg);
            if (equipoIdConsumer != null) {
                equipoIdConsumer.accept(null);
            }
            return;
        }
        
        log.accept("Buscando equipo con IP: " + ip);
        
        try {
            // Crear cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
            
            // Construir la URL de búsqueda
            String url = servidorUrl + "/api/equipos/ip/" + ip;
            log.accept("URL de búsqueda: " + url);
            
            // Crear la solicitud
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Cookie", cookieSesion)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .GET()
                    .build();
                    
                log.accept("Enviando solicitud para buscar equipo...");
                
                // Enviar la solicitud de forma asíncrona
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        try {
                            int statusCode = response.statusCode();
                            String responseBody = response.body();
                            
                            log.accept(String.format("Respuesta del servidor - Código: %d", statusCode));
                            
                            if (statusCode == 200) {
                                procesarRespuestaExitosa(responseBody, equipoIdConsumer, log);
                            } else if (statusCode == 404) {
                                log.accept("No se encontró ningún equipo con la IP: " + ip);
                                notificarEquipoNoEncontrado(equipoIdConsumer);
                            } else {
                                log.accept("Error en la respuesta del servidor. Código: " + statusCode);
                                notificarError(equipoIdConsumer);
                            }
                        } catch (Exception e) {
                            log.accept("Error al procesar la respuesta: " + e.getMessage());
                            notificarError(equipoIdConsumer);
                        }
                    })
                    .exceptionally(e -> {
                        log.accept("Error al realizar la solicitud: " + e.getMessage());
                        notificarError(equipoIdConsumer);
                        return null;
                    });
                    
            } catch (Exception e) {
                log.accept("Error al crear la solicitud: " + e.getMessage());
                notificarError(equipoIdConsumer);
            }
            
        } catch (Exception e) {
            log.accept("Error inesperado al buscar equipo: " + e.getMessage());
            e.printStackTrace();
            equipoIdConsumer.accept(null);
        }
    }
    
    private void procesarRespuestaExitosa(String responseBody, Consumer<String> equipoIdConsumer, Consumer<String> log) {
        try {
            JSONObject equipo = new JSONObject(responseBody);
            if (equipo.has("idEquipo")) {
                String idEquipo = String.valueOf(equipo.getLong("idEquipo"));
                log.accept("Equipo encontrado con ID: " + idEquipo);
                equipoIdConsumer.accept(idEquipo);
                return;
            }
        } catch (Exception e) {
            log.accept("Error al procesar la respuesta del servidor: " + e.getMessage());
        }
        equipoIdConsumer.accept(null);
    }
    
    private void notificarEquipoNoEncontrado(Consumer<String> equipoIdConsumer) {
        equipoIdConsumer.accept(null);
    }
    
    private void notificarError(Consumer<String> equipoIdConsumer) {
        equipoIdConsumer.accept(null);
    }
    
    // Otros métodos relacionados con equipos pueden ir aquí
}
