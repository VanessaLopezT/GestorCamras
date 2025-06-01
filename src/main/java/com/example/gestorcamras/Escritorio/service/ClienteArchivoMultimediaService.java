package com.example.gestorcamras.Escritorio.service;

import com.example.gestorcamras.Escritorio.model.ArchivoMultimediaDTO;
import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.service.IArchivoMultimediaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ClienteArchivoMultimediaService implements IArchivoMultimediaService {
    private final String baseUrl;
    private final String cookieSesion;
    private final Consumer<String> logConsumer;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClienteArchivoMultimediaService(String baseUrl, String cookieSesion) {
        this(baseUrl, cookieSesion, null);
    }

    public ClienteArchivoMultimediaService(String baseUrl, String cookieSesion, Consumer<String> logConsumer) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.cookieSesion = cookieSesion;
        this.logConsumer = logConsumer != null ? logConsumer : System.out::println;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        log("ClienteArchivoMultimediaService inicializado con URL: " + baseUrl);
    }

    private void log(String message) {
        logConsumer.accept("[ClienteArchivoMultimediaService] " + message);
    }

    @Override
    public List<ArchivoMultimediaDTO> obtenerArchivosPorCamara(Long camaraId) {
        try {
            String url = baseUrl + "api/camaras/" + camaraId + "/archivos";
            log("Solicitando archivos para cámara: " + camaraId + " desde URL: " + url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            log("Enviando petición a: " + url);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log("Respuesta recibida con código: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(
                    response.body(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ArchivoMultimediaDTO.class)
                );
            } else {
                log("Error al obtener archivos: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerArchivosPorCamara: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<ArchivoMultimediaDTO> obtenerArchivosPorEquipo(Long equipoId) {
        try {
            String url = baseUrl + "api/equipos/" + equipoId + "/archivos";
            log("Solicitando archivos para equipo: " + equipoId + " desde URL: " + url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            log("Enviando petición a: " + url);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log("Respuesta recibida con código: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(
                    response.body(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ArchivoMultimediaDTO.class)
                );
            } else {
                log("Error al obtener archivos: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerArchivosPorEquipo: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
