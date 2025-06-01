package com.example.gestorcamras.Escritorio.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class FileUploadService {
    private final String servidorUrl;
    private final String cookieSesion;
    private final Consumer<String> logConsumer;
    private static final String LINE_END = "\r\n";
    private static final String TWO_HYPHENS = "--";

    public FileUploadService(String servidorUrl, String cookieSesion, Consumer<String> logConsumer) {
        this.servidorUrl = servidorUrl != null ? servidorUrl : "http://localhost:8080";
        this.cookieSesion = cookieSesion;
        this.logConsumer = logConsumer;
    }

    /**
     * Envía un archivo al servidor
     * @param equipoId ID del equipo
     * @param camaraId ID de la cámara
     * @param nombreCamara Nombre de la cámara
     * @param archivoSeleccionado Archivo a enviar
     * @param tipo Tipo de archivo (FOTO/VIDEO)
     */
    public void enviarArchivo(String equipoId, String camaraId, String nombreCamara, 
                            File archivoSeleccionado, String tipo) {
        log("=== INICIO DE ENVÍO DE ARCHIVO ===");
        log("Equipo ID: " + equipoId);
        log("Cámara ID: " + camaraId);
        log("Nombre cámara: " + nombreCamara);
        log("Archivo: " + archivoSeleccionado.getAbsolutePath());
        log("Tamaño del archivo: " + archivoSeleccionado.length() + " bytes");
        log("Tipo: " + tipo);

        try {
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            log("Generando datos multipart con boundary: " + boundary);
            
            byte[] fileData = getMultipartFormData(boundary, tipo, nombreCamara, archivoSeleccionado);
            log("Datos multipart generados correctamente. Tamaño: " + fileData.length + " bytes");
            
            String url = String.format("%s/api/equipos/%s/camaras/%s/archivo", servidorUrl, equipoId, camaraId);
            log("URL de envío: " + url);
            
            String contentType = "multipart/form-data; boundary=" + boundary;
            log("Content-Type: " + contentType);
            log("Tamaño del archivo: " + archivoSeleccionado.length() + " bytes");
            
            // Construir la petición HTTP
            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(fileData);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", contentType)
                    .header("Accept", "*/*")
                    .header("Cache-Control", "no-cache")
                    .header("User-Agent", "Java-http-client/17.0.2")
                    .POST(bodyPublisher);
                    
            // Agregar la cookie solo si no es nula ni vacía
            if (cookieSesion != null && !cookieSesion.trim().isEmpty()) {
                requestBuilder.header("Cookie", cookieSesion.trim());
                log("Agregando cookie de sesión a la petición");
            }
            
            HttpRequest request = requestBuilder.build();
            
            // Log de la petición completa
            log("=== DETALLES DE LA PETICIÓN ===");
            log("URL: " + request.uri());
            log("Método: " + request.method());
            log("Headers: " + request.headers().map());
            log("==============================");
            
            log("Enviando petición HTTP...");
            
            // Enviar la petición
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String respuesta = response.body();
            
            log("=== RESPUESTA DEL SERVIDOR ===");
            log("Código de estado: " + responseCode);
            log("Headers de respuesta: " + response.headers().map());
            log("Cuerpo de la respuesta: " + respuesta);
            log("=============================");
            
            if (responseCode == 200 || responseCode == 201) {
                log("Archivo enviado correctamente al servidor para la cámara: " + nombreCamara);
                // Mostrar mensaje de éxito al usuario
                mostrarMensajeExito("Archivo enviado exitosamente a la cámara: " + nombreCamara);
            } else {
                String mensajeError = "Error al enviar el archivo. ";
                String detalles = "";
                
                // Determinar el tipo de error
                if (responseCode == 409) {
                    mensajeError = "No se pudo procesar la solicitud. ";
                    detalles = "Otro equipo está realizando una operación similar. Por favor, intente nuevamente en unos momentos.";
                } else if (responseCode == 500) {
                    mensajeError = "Error en el servidor. ";
                    detalles = "El servidor encontró un error al procesar su solicitud. Por favor, intente más tarde.";
                } else if (responseCode == 503) {
                    mensajeError = "Servicio no disponible. ";
                    detalles = "El servidor está ocupado procesando otras solicitudes. Por favor, intente nuevamente en unos momentos.";
                } else {
                    detalles = "Código de error: " + responseCode;
                }
                
                log("Error al enviar archivo. Código: " + responseCode);
                log("Respuesta del servidor: " + respuesta);
                
                // Mostrar mensaje de error detallado al usuario
                mostrarError(mensajeError, detalles);
            }
        } catch (Exception e) {
            log("=== ERROR AL ENVIAR ARCHIVO ===");
            log("Mensaje: " + e.getMessage());
            log("Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            log("Stack trace:");
            for (StackTraceElement element : e.getStackTrace()) {
                log("    at " + element.toString());
            }
            log("================================");
        }
    }

    /**
     * Genera los datos para una petición multipart/form-data para subir un archivo
     * @param boundary Límite para las partes del formulario
     * @param tipo Tipo de archivo (FOTO/VIDEO)
     * @param nombreCamara Nombre de la cámara
     * @param archivo Archivo a subir
     * @return Arreglo de bytes con los datos del formulario multipart
     * @throws IOException Si ocurre un error al leer el archivo
     */
    private byte[] getMultipartFormData(String boundary, String tipo, String nombreCamara, File archivo) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            // 1. Escribir el campo 'archivo' primero
            String mimeType = Files.probeContentType(archivo.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            // Encabezado del archivo
            String filePartHeader = 
                TWO_HYPHENS + boundary + LINE_END +
                "Content-Disposition: form-data; name=\"archivo\"; filename=\"" + archivo.getName() + "\"" + LINE_END +
                "Content-Type: " + mimeType + LINE_END +
                LINE_END;
                
            outputStream.write(filePartHeader.getBytes(StandardCharsets.UTF_8));
            
            // Contenido del archivo
            byte[] fileBytes = Files.readAllBytes(archivo.toPath());
            outputStream.write(fileBytes);
            outputStream.write(LINE_END.getBytes(StandardCharsets.UTF_8));
            
            // 2. Escribir el campo 'tipo'
            String tipoPart = 
                TWO_HYPHENS + boundary + LINE_END +
                "Content-Disposition: form-data; name=\"tipo\"" + LINE_END +
                LINE_END +
                tipo + LINE_END;
                
            outputStream.write(tipoPart.getBytes(StandardCharsets.UTF_8));
            
            // 3. Cerrar el multipart
            String closingBoundary = TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END;
            outputStream.write(closingBoundary.getBytes(StandardCharsets.UTF_8));
            
            byte[] result = outputStream.toByteArray();
            
            // Log de depuración
            log("Datos multipart generados correctamente");
            log("Tamaño total: " + result.length + " bytes");
            log("Tamaño del archivo: " + fileBytes.length + " bytes");
            log("Tipo MIME: " + mimeType);
            
            // No mostrar vista previa del contenido binario
            
            return result;
        } catch (Exception e) {
            log("Error al generar datos multipart: " + e.getMessage());
            throw e;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                log("Error al cerrar el stream: " + e.getMessage());
            }
        }
    }
    
    private void log(String mensaje) {
        if (logConsumer != null) {
            logConsumer.accept("[FileUploadService] " + mensaje);
        } else {
            System.out.println("[FileUploadService] " + mensaje);
        }
    }
    
    /**
     * Muestra un mensaje de error al usuario
     * @param titulo Título del mensaje de error
     * @param mensajeError Mensaje de error a mostrar
     */
    /**
     * Muestra un mensaje de error al usuario
     * @param titulo Título del mensaje de error
     * @param mensajeError Mensaje de error a mostrar
     */
    private void mostrarError(String titulo, String mensajeError) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null, 
                "<html><body><p style='width: 300px;'>" + mensajeError + "</p></body></html>",
                titulo,
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * Muestra un mensaje de éxito al usuario
     * @param mensajeExito Mensaje de éxito a mostrar
     */
    private void mostrarMensajeExito(String mensajeExito) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null, 
                "<html><body><p style='width: 300px;'>" + mensajeExito + "</p></body></html>",
                "Operación exitosa",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
}