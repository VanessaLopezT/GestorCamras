package com.example.gestorcamras.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para realizar geocodificación inversa (obtener dirección a partir de coordenadas)
 * Utiliza OpenStreetMap Nominatim de forma gratuita.
 */
@Service
public class GeocodingService {
    
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";
    private static final String USER_AGENT = "GestorCamaras/1.0";
    
    private final HttpClient httpClient;
    
    public GeocodingService() {
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }
    
    /**
     * Obtiene la dirección a partir de coordenadas geográficas
     * @param latitud Latitud del punto
     * @param longitud Longitud del punto
     * @return Una cadena con la dirección formateada o null si no se pudo obtener
     */
    public CompletableFuture<String> obtenerDireccion(double latitud, double longitud) {
        String url = String.format("%s?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1", 
                                 NOMINATIM_URL, latitud, longitud);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .GET()
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    JSONObject json = new JSONObject(response.body());
                    return formatearDireccion(json);
                }
                return null;
            })
            .exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
    }
    
    /**
     * Formatea la dirección a partir de la respuesta de Nominatim
     */
    private String formatearDireccion(JSONObject json) {
        if (json.has("address")) {
            JSONObject address = json.getJSONObject("address");
            
            // Intentar construir una dirección legible
            StringBuilder sb = new StringBuilder();
            
            // Primero la calle y número
            if (address.has("road")) {
                sb.append(address.getString("road"));
                if (address.has("house_number")) {
                    sb.append(" ").append(address.getString("house_number"));
                }
                sb.append(", ");
            }
            
            // Luego el código postal y localidad
            if (address.has("postcode")) {
                sb.append(address.getString("postcode")).append(" ");
            }
            
            if (address.has("city")) {
                sb.append(address.getString("city"));
            } else if (address.has("town")) {
                sb.append(address.getString("town"));
            } else if (address.has("village")) {
                sb.append(address.getString("village"));
            }
            
            // Si no hay suficiente información, devolver la dirección de visualización
            if (sb.length() == 0 && json.has("display_name")) {
                return json.getString("display_name");
            }
            
            return sb.toString();
        }
        
        return json.optString("display_name", "Ubicación desconocida");
    }
}
