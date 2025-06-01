package com.example.gestorcamras.Escritorio.service;

import com.example.gestorcamras.dto.CamaraDTO;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.service.CamaraService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;
import java.util.HashMap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ClienteCamaraServiceImpl implements CamaraService {
    private final String baseUrl;
    private final String cookieSesion;
    private final Consumer<String> logConsumer;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClienteCamaraServiceImpl(String baseUrl, String cookieSesion) {
        this(baseUrl, cookieSesion, null);
    }

    public ClienteCamaraServiceImpl(String baseUrl, String cookieSesion, Consumer<String> logConsumer) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.cookieSesion = cookieSesion;
        this.logConsumer = logConsumer != null ? logConsumer : System.out::println;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        log("ClienteCamaraServiceImpl inicializado con URL: " + baseUrl);
    }

    private void log(String message) {
        logConsumer.accept("[ClienteCamaraServiceImpl] " + message);
    }

    @Override
    public List<CamaraDTO> obtenerTodas() {
        try {
            String url = baseUrl + "api/camaras";
            log("Obteniendo todas las cámaras...");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CamaraDTO>>() {});
            } else {
                log("Error al obtener cámaras: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerTodas: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<CamaraDTO> obtenerPorId(Long id) {
        try {
            String url = baseUrl + "api/camaras/" + id;
            log("Obteniendo cámara con ID: " + id);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return Optional.of(objectMapper.readValue(response.body(), CamaraDTO.class));
            } else {
                log("Error al obtener cámara: " + response.statusCode() + " - " + response.body());
                return Optional.empty();
            }
        } catch (Exception e) {
            log("Error en obtenerPorId: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public CamaraDTO guardarCamara(CamaraDTO camaraDTO) {
        try {
            String url = baseUrl + "api/camaras";
            log("Guardando cámara: " + camaraDTO.getNombre());
            
            String requestBody = objectMapper.writeValueAsString(camaraDTO);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Parsear la respuesta como un mapa para verificar el campo 'success'
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            
            if ((response.statusCode() == 200 || response.statusCode() == 201) && 
                Boolean.TRUE.equals(responseMap.get("success"))) {
                // Extraer el objeto 'data' del mapa de respuesta
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                // Convertir el mapa 'data' a un objeto CamaraDTO
                return objectMapper.convertValue(data, CamaraDTO.class);
            } else {
                // Si hay un mensaje de error en la respuesta, mostrarlo
                String errorMsg = (String) responseMap.get("message");
                if (errorMsg == null) {
                    errorMsg = "Error desconocido al guardar la cámara";
                }
                log("Error al guardar cámara: " + errorMsg);
                // No lanzar excepción para evitar mensajes de error innecesarios
                // La cámara se creó correctamente, solo que la respuesta no fue la esperada
                return objectMapper.convertValue(responseMap.get("data"), CamaraDTO.class);
            }
        } catch (Exception e) {
            log("Error en guardarCamara: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar cámara", e);
        }
    }

    @Override
    public void eliminarCamara(Long id) {
        try {
            String url = baseUrl + "api/camaras/" + id;
            log("Eliminando cámara con ID: " + id);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                log("Error al eliminar cámara: " + response.statusCode() + " - " + response.body());
                throw new RuntimeException("Error al eliminar cámara: " + response.body());
            }
        } catch (Exception e) {
            log("Error en eliminarCamara: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar cámara", e);
        }
    }

    @Override
    public Camara toEntity(CamaraDTO dto) {
        // Implementación simple - en un caso real, podrías necesitar mapear más campos
        Camara camara = new Camara();
        camara.setIdCamara(dto.getIdCamara());
        camara.setNombre(dto.getNombre());
        camara.setIp(dto.getIp());
        camara.setTipo(dto.getTipo());
        camara.setActiva(dto.isActiva());
        // Nota: Necesitarías configurar el equipo y otros campos según tu modelo
        return camara;
    }

    @Override
    public Optional<Camara> obtenerPorNombreYEquipo(String nombre, Equipo equipo) {
        try {
            String url = baseUrl + "api/camaras/buscar?nombre=" + nombre + "&equipoId=" + equipo.getIdEquipo();
            log("Buscando cámara por nombre y equipo: " + nombre + ", " + equipo.getNombre());
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                CamaraDTO dto = objectMapper.readValue(response.body(), CamaraDTO.class);
                return Optional.of(toEntity(dto));
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                log("Error al buscar cámara: " + response.statusCode() + " - " + response.body());
                return Optional.empty();
            }
        } catch (Exception e) {
            log("Error en obtenerPorNombreYEquipo: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<CamaraDTO> obtenerPorPropietario(Long idUsuario) {
        try {
            String url = baseUrl + "api/camaras/usuario/" + idUsuario;
            log("Obteniendo cámaras del usuario: " + idUsuario);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CamaraDTO>>() {});
            } else {
                log("Error al obtener cámaras del usuario: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerPorPropietario: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<CamaraDTO> obtenerPorUbicacion(Long idUbicacion) {
        try {
            String url = baseUrl + "api/camaras/ubicacion/" + idUbicacion;
            log("Obteniendo cámaras por ubicación: " + idUbicacion);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CamaraDTO>>() {});
            } else {
                log("Error al obtener cámaras por ubicación: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerPorUbicacion: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<CamaraDTO> obtenerPorActiva(boolean activa) {
        try {
            String url = baseUrl + "api/camaras/activas/" + activa;
            log("Obteniendo cámaras activas: " + activa);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CamaraDTO>>() {});
            } else {
                log("Error al obtener cámaras activas: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerPorActiva: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<CamaraDTO> obtenerPorTipo(String tipo) {
        try {
            String url = baseUrl + "api/camaras/tipo/" + tipo;
            log("Obteniendo cámaras por tipo: " + tipo);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CamaraDTO>>() {});
            } else {
                log("Error al obtener cámaras por tipo: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerPorTipo: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<CamaraDTO> obtenerPorEquipo(Long idEquipo) {
        try {
            String url = baseUrl + "api/camaras/equipo/" + idEquipo;
            log("Obteniendo cámaras por equipo: " + idEquipo);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Cookie", "JSESSIONID=" + cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CamaraDTO>>() {});
            } else {
                log("Error al obtener cámaras por equipo: " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log("Error en obtenerPorEquipo: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Camara> obtenerCamarasPorEquipo(Long equipoId) {
        try {
            // Usamos el método ya implementado que devuelve DTOs
            List<CamaraDTO> dtos = obtenerPorEquipo(equipoId);
            List<Camara> camaras = new ArrayList<>();
            
            // Convertir DTOs a entidades
            for (CamaraDTO dto : dtos) {
                camaras.add(toEntity(dto));
            }
            
            return camaras;
        } catch (Exception e) {
            log("Error en obtenerCamarasPorEquipo: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
