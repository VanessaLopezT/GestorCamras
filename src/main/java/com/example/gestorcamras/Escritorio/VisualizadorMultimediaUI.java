package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.Escritorio.dto.ArchivoMultimediaDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class VisualizadorMultimediaUI extends JFrame {
    // Método para registrar mensajes de depuración
    private void log(String mensaje) {
        System.out.println("[VisualizadorMultimediaUI] " + mensaje);
    }
    
    // Método para registrar mensajes en el hilo de Swing
    private void logEnSwing(String mensaje) {
        SwingUtilities.invokeLater(() -> log(mensaje));
    }
    
    private Long obtenerIdCamaraSeleccionada() {
        int selectedCamaraIndex = comboCamaras.getSelectedIndex();
        if (selectedCamaraIndex > 0) {
            String selectedCamara = comboCamaras.getItemAt(selectedCamaraIndex);
            return Long.parseLong(
                selectedCamara.substring(selectedCamara.indexOf("ID: ") + 4, selectedCamara.length() - 1)
            );
        }
        return null;
    }
    private JComboBox<String> comboEquipos;
    private JComboBox<String> comboCamaras;
    private JButton btnActualizar;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JPanel panelMultimedia;
    private JLabel lblImagen;
    private JLabel lblInfo;
    private JButton btnDescargar;
    private JButton btnEliminar;
    
    private List<ArchivoMultimediaDTO> archivosActuales;
    private int indiceActual = -1;

    public VisualizadorMultimediaUI() {
        setTitle("Visualizador de Multimedia");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Panel principal con borde
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel de controles superiores
        JPanel panelControles = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Configuración de los controles
        gbc.gridx = 0; gbc.gridy = 0;
        panelControles.add(new JLabel("Equipo:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        comboEquipos = new JComboBox<>();
        comboEquipos.addItem("Seleccione un equipo");
        panelControles.add(comboEquipos, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panelControles.add(new JLabel("Cámara:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1.0;
        comboCamaras = new JComboBox<>();
        comboCamaras.addItem("Todas las cámaras");
        panelControles.add(comboCamaras, gbc);
        
        gbc.gridx = 4; gbc.weightx = 0;
        btnActualizar = new JButton("Actualizar");
        panelControles.add(btnActualizar, gbc);
        
        // Panel de navegación
        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAnterior = new JButton("Anterior");
        btnSiguiente = new JButton("Siguiente");
        panelNavegacion.add(btnAnterior);
        panelNavegacion.add(btnSiguiente);
        
        // Panel de multimedia
        panelMultimedia = new JPanel(new BorderLayout());
        panelMultimedia.setBackground(Color.WHITE);
        panelMultimedia.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        lblImagen = new JLabel("Seleccione un equipo y una cámara para comenzar", JLabel.CENTER);
        lblImagen.setHorizontalAlignment(JLabel.CENTER);
        lblImagen.setVerticalAlignment(JLabel.CENTER);
        panelMultimedia.add(lblImagen, BorderLayout.CENTER);
        
        // Panel de información
        lblInfo = new JLabel(" ");
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Panel de botones de acción (ocultos pero con la lógica mantenida)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnDescargar = new JButton("Descargar");
        btnEliminar = new JButton("Eliminar");
        // Ocultar los botones pero mantener la lógica
        btnDescargar.setVisible(false);
        btnEliminar.setVisible(false);
        // Mantener las referencias para posibles usos futuros
        panelAcciones.setVisible(false); // Ocultar el panel de acciones
        
        // Agregar componentes al panel principal
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelControles, BorderLayout.NORTH);
        panelSuperior.add(panelNavegacion, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(new JScrollPane(panelMultimedia), BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(lblInfo, BorderLayout.CENTER);
        panelInferior.add(panelAcciones, BorderLayout.SOUTH);
        
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        // Configurar acciones
        configurarAcciones();
        
        // Cargar datos iniciales
        cargarEquipos();
        
        add(panelPrincipal);
    }
    
    private void configurarAcciones() {
        btnActualizar.addActionListener(e -> cargarArchivosMultimedia());
        btnAnterior.addActionListener(e -> mostrarArchivoAnterior());
        btnSiguiente.addActionListener(e -> mostrarArchivoSiguiente());
        btnDescargar.addActionListener(e -> descargarArchivoActual());
        btnEliminar.addActionListener(e -> eliminarArchivoActual());
        
        comboEquipos.addActionListener(e -> {
            if (comboEquipos.getSelectedIndex() > 0) {
                cargarCamaras(comboEquipos.getSelectedIndex());
            } else {
                comboCamaras.removeAllItems();
                comboCamaras.addItem("Todas las cámaras");
            }
        });
    }
    
    private void cargarEquipos() {
        logEnSwing("Iniciando carga de equipos...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String serverUrl = "http://localhost:8080/api/equipos";
                    log("Realizando solicitud a: " + serverUrl);
                    
                    // Crear cliente HTTP
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(serverUrl))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                    
                    log("Enviando solicitud HTTP...");
                    java.net.http.HttpResponse<String> response = client.send(
                        request, 
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                    );
                    
                    int statusCode = response.statusCode();
                    log("Código de respuesta: " + statusCode);
                    
                    if (statusCode == 200) {
                        String responseBody = response.body();
                        log("Respuesta recibida: " + responseBody);
                        
                        JSONArray equipos = new JSONArray(responseBody);
                        logEnSwing("Equipos recibidos: " + equipos.length());
                        
                        // Actualizar la interfaz en el hilo de eventos de Swing
                        SwingUtilities.invokeLater(() -> actualizarListaEquipos(equipos));
                    } else {
                        String errorMessage = "Error al cargar equipos. Código: " + statusCode;
                        log(errorMessage);
                        log("Cuerpo de la respuesta: " + response.body());
                        
                        // Mostrar el error en la interfaz
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(VisualizadorMultimediaUI.this, 
                                errorMessage, "Error", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception e) {
                    String errorMsg = "Error al cargar equipos: " + e.getMessage();
                    log(errorMsg);
                    e.printStackTrace();
                    
                    // Mostrar el error en la interfaz
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(VisualizadorMultimediaUI.this, 
                            errorMsg, "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            
            @Override
            protected void done() {
                log("Finalizada la carga de equipos");
            }
        };
        
        worker.execute();
    }
    
    private void actualizarListaEquipos(JSONArray equipos) {
        SwingUtilities.invokeLater(() -> {
            comboEquipos.removeAllItems();
            comboEquipos.addItem("Seleccione un equipo");
            
            for (int i = 0; i < equipos.length(); i++) {
                JSONObject equipoJson = equipos.getJSONObject(i);
                String nombreEquipo = equipoJson.optString("nombre", "Equipo sin nombre");
                long idEquipo = equipoJson.getLong("idEquipo");
                comboEquipos.addItem(nombreEquipo + " (ID: " + idEquipo + ")");
            }
        });
    }
    
    private void cargarCamaras(int selectedIndex) {
        if (selectedIndex <= 0) {
            comboCamaras.removeAllItems();
            comboCamaras.addItem("Todas las cámaras");
            return;
        }
        
        // Extraer el ID del equipo del texto seleccionado (ejemplo: "Equipo 1 (ID: 1)")
        String selectedItem = comboEquipos.getItemAt(selectedIndex);
        String idStr = selectedItem.substring(selectedItem.indexOf("ID: ") + 4, selectedItem.length() - 1);
        long idEquipo = Long.parseLong(idStr);
        
        cargarCamarasDesdeServidor(idEquipo);
    }
    
    private void cargarCamarasDesdeServidor(long idEquipo) {
        logEnSwing("Cargando cámaras para el equipo ID: " + idEquipo);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Construir la URL de la API
                    String apiUrl = "http://localhost:8080/api/camaras/equipo/" + idEquipo;
                    logEnSwing("Solicitando cámaras a: " + apiUrl);
                    
                    // Crear cliente HTTP
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(apiUrl))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                    
                    // Enviar solicitud
                    java.net.http.HttpResponse<String> response = client.send(
                        request, 
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                    );
                    
                    int statusCode = response.statusCode();
                    logEnSwing("Respuesta del servidor: " + statusCode);
                    
                    if (statusCode == 200) {
                        String responseBody = response.body();
                        logEnSwing("Cámaras recibidas: " + responseBody);
                        
                        // Procesar la respuesta JSON
                        JSONArray jsonArray = new JSONArray(responseBody);
                        List<String> camaras = new ArrayList<>();
                        camaras.add("Todas las cámaras");
                        
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject camara = jsonArray.getJSONObject(i);
                            String nombreCamara = camara.getString("nombre");
                            long idCamara = camara.getLong("idCamara");
                            camaras.add(nombreCamara + " (ID: " + idCamara + ")");
                        }
                        
                        // Actualizar el combo de cámaras en el hilo de Swing
                        SwingUtilities.invokeLater(() -> {
                            comboCamaras.removeAllItems();
                            for (String camara : camaras) {
                                comboCamaras.addItem(camara);
                            }
                            logEnSwing("Cámaras cargadas: " + (camaras.size() - 1));
                        });
                    } else {
                        logEnSwing("Error al cargar cámaras. Código: " + statusCode);
                        logEnSwing("Cuerpo de la respuesta: " + response.body());
                        
                        // Mostrar mensaje de error en el diálogo
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                VisualizadorMultimediaUI.this,
                                "No se encontraron cámaras para este equipo o el servicio no está disponible.",
                                "Error al cargar cámaras",
                                JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                } catch (Exception e) {
                    logEnSwing("Error al cargar cámaras: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Mostrar mensaje de error en el diálogo
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            VisualizadorMultimediaUI.this,
                            "Error al conectar con el servidor: " + e.getMessage(),
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                logEnSwing("Finalizada la carga de cámaras");
            }
        };
        
        worker.execute();
    }
    
    private void cargarArchivosMultimedia() {
        int selectedEquipoIndex = comboEquipos.getSelectedIndex();
        if (selectedEquipoIndex <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione un equipo primero", 
                "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extraer el ID del equipo
        String selectedEquipo = comboEquipos.getItemAt(selectedEquipoIndex);
        final long idEquipo = Long.parseLong(
            selectedEquipo.substring(selectedEquipo.indexOf("ID: ") + 4, selectedEquipo.length() - 1)
        );
        
        // Extraer el ID de la cámara si se seleccionó una específica
        final Long idCamara = obtenerIdCamaraSeleccionada();
        logEnSwing("Cargando archivos multimedia para equipo: " + idEquipo + 
            (idCamara != null ? ", cámara: " + idCamara : ""));
            
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Construir la URL de la API basada en si se seleccionó una cámara específica o no
                    String apiUrl;
                    if (idCamara != null) {
                        // Si se seleccionó una cámara específica, usar el endpoint de cámaras
                        apiUrl = String.format("http://localhost:8080/api/camaras/%d/archivos", idCamara);
                    } else {
                        // Si no, usar el endpoint de equipos
                        apiUrl = String.format("http://localhost:8080/api/equipos/%d/archivos", idEquipo);
                    }
                    
                    logEnSwing("Solicitando archivos a: " + apiUrl);
                    
                    // Crear cliente HTTP con configuración para seguir redirecciones
                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                        .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                        .build();
                        
                    // Crear la solicitud con headers para evitar caché
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(apiUrl))
                        .header("Accept", "application/json")
                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .GET()
                        .build();
                    
                    // Enviar solicitud
                    java.net.http.HttpResponse<String> response = client.send(
                        request, 
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                    );
                    
                    int statusCode = response.statusCode();
                    logEnSwing("Respuesta del servidor: " + statusCode);
                    
                    // Si hay una redirección (3xx), obtener la nueva ubicación
                    if (statusCode >= 300 && statusCode < 400) {
                        String newLocation = response.headers().firstValue("Location").orElse("");
                        logEnSwing("Redireccionando a: " + newLocation);
                        
                        if (!newLocation.isEmpty()) {
                            // Hacer una nueva solicitud a la URL de redirección
                            request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(newLocation))
                                .header("Accept", "application/json")
                                .GET()
                                .build();
                                
                            response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                            statusCode = response.statusCode();
                            logEnSwing("Respuesta después de redirección: " + statusCode);
                        }
                    }
                    
                    if (statusCode == 200) {
                        String responseBody = response.body();
                        logEnSwing("Archivos recibidos: " + responseBody);
                        
                        // Procesar la respuesta JSON
                        JSONArray archivosJson = new JSONArray(responseBody);
                        List<ArchivoMultimediaDTO> archivos = new ArrayList<>();
                        
                        for (int i = 0; i < archivosJson.length(); i++) {
                            try {
                                JSONObject archivoJson = archivosJson.getJSONObject(i);
                                ArchivoMultimediaDTO archivo = new ArchivoMultimediaDTO();
                                
                                // Mapear los campos según la estructura de ArchivoMultimediaDTO
                                archivo.setId(archivoJson.getLong("idArchivo"));
                                archivo.setNombre(archivoJson.optString("nombreArchivo", "archivo_sin_nombre"));
                                archivo.setRuta(archivoJson.optString("rutaArchivo", ""));
                                archivo.setTipo(archivoJson.optString("tipo", "desconocido"));
                                
                                try {
                                    // Convertir fechas de String a LocalDateTime
                                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                                    if (archivoJson.has("fechaCaptura")) {
                                        archivo.setFechaCaptura(LocalDateTime.parse(
                                            archivoJson.getString("fechaCaptura"), formatter));
                                    }
                                    
                                    if (archivoJson.has("fechaSubida")) {
                                        archivo.setFechaSubida(LocalDateTime.parse(
                                            archivoJson.getString("fechaSubida"), formatter));
                                    }
                                } catch (Exception e) {
                                    logEnSwing("Error al parsear fechas: " + e.getMessage());
                                }
                                
                                // Obtener IDs de las relaciones
                                if (archivoJson.has("camaraId")) {
                                    archivo.setIdCamara(archivoJson.getLong("camaraId"));
                                } else if (archivoJson.has("idCamara")) {
                                    archivo.setIdCamara(archivoJson.getLong("idCamara"));
                                }
                                
                                if (archivoJson.has("equipoId")) {
                                    archivo.setIdEquipo(archivoJson.getLong("equipoId"));
                                } else if (archivoJson.has("idEquipo")) {
                                    archivo.setIdEquipo(archivoJson.getLong("idEquipo"));
                                }
                                
                                archivos.add(archivo);
                            } catch (Exception e) {
                                logEnSwing("Error al procesar archivo en índice " + i + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        // Actualizar la interfaz en el hilo de eventos de Swing
                        SwingUtilities.invokeLater(() -> {
                            archivosActuales = archivos;
                            // Limpiar el panel de multimedia
                            panelMultimedia.removeAll();
                            panelMultimedia.revalidate();
                            panelMultimedia.repaint();
                            
                            if (!archivosActuales.isEmpty()) {
                                indiceActual = 0;
                                mostrarArchivoActual();
                            } else {
                                // Mostrar mensaje de que no hay archivos
                                JLabel mensaje = new JLabel("No se encontraron archivos multimedia para esta cámara", JLabel.CENTER);
                                mensaje.setFont(new Font("Arial", Font.PLAIN, 16));
                                panelMultimedia.add(mensaje, BorderLayout.CENTER);
                                lblInfo.setText("");
                                
                                // Actualizar la interfaz
                                panelMultimedia.revalidate();
                                panelMultimedia.repaint();
                            }
                            
                            // Actualizar estado de los botones
                            btnAnterior.setEnabled(false);
                            btnSiguiente.setEnabled(false);
                        });
                        
                    } else {
                        String errorMessage = "Error al cargar archivos. Código: " + statusCode;
                        String responseBody = response.body();
                        logEnSwing(errorMessage);
                        logEnSwing("Cuerpo de la respuesta: " + responseBody);
                        
                        // Determinar el mensaje de error más específico
                        String mensajeUsuario = "Error al cargar archivos multimedia";
                        if (statusCode == 401 || statusCode == 403) {
                            mensajeUsuario = "No tiene permisos para ver estos archivos. Inicie sesión primero.";
                        } else if (statusCode == 404) {
                            mensajeUsuario = "No se encontraron archivos para la selección actual.";
                        } else if (statusCode == 302) {
                            mensajeUsuario = "Redirección inesperada. El servidor está redirigiendo la solicitud.";
                        }
                        
                        final String mensajeFinal = mensajeUsuario + " (Código: " + statusCode + ")";
                        
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(VisualizadorMultimediaUI.this, 
                                mensajeFinal, "Error", JOptionPane.ERROR_MESSAGE));
                    }
                    
                } catch (Exception e) {
                    String errorMsg = "Error al cargar archivos multimedia: " + e.getMessage();
                    logEnSwing(errorMsg);
                    e.printStackTrace();
                    
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(VisualizadorMultimediaUI.this, 
                            "Error al conectar con el servidor: " + e.getMessage(), 
                            "Error de conexión", JOptionPane.ERROR_MESSAGE));
                    e.printStackTrace();
                    
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(VisualizadorMultimediaUI.this, 
                            "Error al cargar archivos multimedia: " + e.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        };
        
        worker.execute();
    }
    
    private void mostrarArchivoActual() {
        if (indiceActual >= 0 && indiceActual < archivosActuales.size()) {
            ArchivoMultimediaDTO archivo = archivosActuales.get(indiceActual);
            
            // Actualizar etiqueta de información
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String info = String.format("Archivo %d de %d | %s | %s | %s | %s", 
                indiceActual + 1, archivosActuales.size(),
                archivo.getNombre(),
                archivo.getTipo(),
                archivo.getFechaCaptura() != null ? archivo.getFechaCaptura().format(formatter) : "Sin fecha",
                archivo.getRuta() != null ? archivo.getRuta() : "Sin ruta");
            
            lblInfo.setText(info);
            
            // Limpiar el panel de multimedia
            panelMultimedia.removeAll();
            
            try {
                // Obtener y validar la ruta del archivo
                final String rutaArchivo = archivo.getRuta();
                
                if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
                    throw new Exception("La ruta del archivo está vacía");
                }
                
                // Construir la URL del archivo
                String urlArchivo;
                try {
                    if (rutaArchivo.startsWith("http")) {
                        // Si ya es una URL completa, usarla directamente
                        urlArchivo = rutaArchivo;
                    } else {
                        // Normalizar la ruta reemplazando las barras invertidas por barras normales
                        String rutaNormalizada = rutaArchivo.replace("\\", "/");
                        // Construir la URL completa
                        urlArchivo = "http://localhost:8080/" + rutaNormalizada;
                    }
                } catch (Exception e) {
                    throw new Exception("Error al construir la URL: " + e.getMessage(), e);
                }
                
                logEnSwing("Cargando archivo desde: " + urlArchivo);
                
                // Obtener el tipo de archivo (debería ser "FOTO" o "VIDEO")
                String tipoArchivo = archivo.getTipo() != null ? archivo.getTipo().toUpperCase() : "";
                
                // Si el tipo no está especificado, intentar determinarlo por la extensión
                if (tipoArchivo.isEmpty()) {
                    String nombreArchivo = rutaArchivo.toLowerCase();
                    if (nombreArchivo.matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                        tipoArchivo = "FOTO";
                    } else if (nombreArchivo.matches(".*\\.(mp4|avi|mov|wmv|flv|mkv)$")) {
                        tipoArchivo = "VIDEO";
                    }
                }
                
                if (tipoArchivo.equals("FOTO")) {
                    // Cargar y mostrar imagen
                    JLabel labelImagen = new JLabel();
                    labelImagen.setHorizontalAlignment(JLabel.CENTER);
                    
                    // Cargar la imagen en un hilo separado para no bloquear la interfaz
                    new Thread(() -> {
                        try {
                            // Crear URI y URL de manera segura
                            URI uri = new URI(urlArchivo);
                            URL url = uri.toURL();
                            java.awt.Image imagenOriginal = javax.imageio.ImageIO.read(url);
                            
                            // Redimensionar la imagen para que se ajuste al panel
                            if (imagenOriginal != null) {
                                int anchoPanel = panelMultimedia.getWidth();
                                int altoPanel = panelMultimedia.getHeight();
                                
                                // Calcular el tamaño manteniendo la relación de aspecto
                                int ancho = imagenOriginal.getWidth(null);
                                int alto = imagenOriginal.getHeight(null);
                                double relacion = Math.min(
                                    (double) anchoPanel / ancho,
                                    (double) altoPanel / alto
                                ) * 0.9; // 90% del tamaño disponible para dejar margen
                                
                                int nuevoAncho = (int) (ancho * relacion);
                                int nuevoAlto = (int) (alto * relacion);
                                
                                // Escalar la imagen
                                java.awt.Image imagenEscalada = imagenOriginal.getScaledInstance(
                                    nuevoAncho, nuevoAlto, java.awt.Image.SCALE_SMOOTH);
                                
                                // Mostrar la imagen en el hilo de Swing
                                SwingUtilities.invokeLater(() -> {
                                    labelImagen.setIcon(new ImageIcon(imagenEscalada));
                                    panelMultimedia.add(new JScrollPane(labelImagen), BorderLayout.CENTER);
                                    panelMultimedia.revalidate();
                                    panelMultimedia.repaint();
                                });
                            }
                        } catch (Exception e) {
                            logEnSwing("Error al cargar la imagen: " + e.getMessage());
                            SwingUtilities.invokeLater(() -> {
                                labelImagen.setText("No se pudo cargar la imagen: " + e.getMessage());
                                panelMultimedia.add(new JScrollPane(labelImagen), BorderLayout.CENTER);
                                panelMultimedia.revalidate();
                            });
                        }
                    }).start();
                    
                } else if (tipoArchivo.equals("VIDEO")) {
                    // Mostrar reproductor de video
                    JPanel panelVideo = new JPanel(new BorderLayout());
                    JLabel lblMensaje = new JLabel("Video detectado. Haz clic en el botón para reproducir.", JLabel.CENTER);
                    panelVideo.add(lblMensaje, BorderLayout.CENTER);
                    
                    // Crear botón de reproducción
                    JButton btnReproducir = new JButton("Abrir video con reproductor del sistema");
                    
                    // Usar una copia final de la URL para el lambda
                    final String finalUrlArchivo = urlArchivo;
                    
                    btnReproducir.addActionListener(e -> {
                        try {
                            // Crear un archivo temporal para el video
                            java.io.File tempFile = java.io.File.createTempFile("video_", ".mp4");
                            
                            // Normalizar la URL reemplazando las barras invertidas codificadas
                            String urlNormalizada = finalUrlArchivo.replace("%5C", "/");
                            // Crear URL de manera segura
                            URI uri = new URI(urlNormalizada);
                            URL videoUrl = uri.toURL();
                            
                            try (java.io.BufferedInputStream in = new java.io.BufferedInputStream(videoUrl.openStream());
                                 java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(tempFile)) {
                                
                                byte[] dataBuffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                                }
                                
                                // Abrir con el reproductor predeterminado del sistema
                                if (java.awt.Desktop.isDesktopSupported()) {
                                    java.awt.Desktop.getDesktop().open(tempFile);
                                    
                                    // Eliminar el archivo temporal cuando la aplicación termine
                                    tempFile.deleteOnExit();
                                } else {
                                    throw new Exception("No se puede abrir el reproductor de video");
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(this, 
                                    "No se pudo abrir el reproductor de video: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, 
                                "Error al preparar el video: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    
                    panelVideo.add(btnReproducir, BorderLayout.SOUTH);
                    panelMultimedia.add(new JScrollPane(panelVideo), BorderLayout.CENTER);
                    
                } else {
                    // Tipo de archivo no reconocido
                    JLabel labelDesconocido = new JLabel("Tipo de archivo no soportado para vista previa: " + archivo.getTipo(), JLabel.CENTER);
                    panelMultimedia.add(new JScrollPane(labelDesconocido), BorderLayout.CENTER);
                }
                
            } catch (Exception e) {
                logEnSwing("Error al mostrar el archivo: " + e.getMessage());
                JLabel labelError = new JLabel("Error al cargar el archivo: " + e.getMessage(), JLabel.CENTER);
                panelMultimedia.add(new JScrollPane(labelError), BorderLayout.CENTER);
            }
            
            // Forzar la actualización de la interfaz
            panelMultimedia.revalidate();
            panelMultimedia.repaint();
        }
        
        // Actualizar estado de los botones de navegación
        btnAnterior.setEnabled(indiceActual > 0);
        btnSiguiente.setEnabled(archivosActuales != null && indiceActual < archivosActuales.size() - 1);
        btnDescargar.setEnabled(indiceActual >= 0);
        btnEliminar.setEnabled(indiceActual >= 0);
    }
    
    private void mostrarArchivoAnterior() {
        if (indiceActual > 0) {
            indiceActual--;
            mostrarArchivoActual();
        }
    }
    
    private void mostrarArchivoSiguiente() {
        if (archivosActuales != null && indiceActual < archivosActuales.size() - 1) {
            indiceActual++;
            mostrarArchivoActual();
        }
    }
    
    private void descargarArchivoActual() {
        if (indiceActual >= 0 && indiceActual < archivosActuales.size()) {
            ArchivoMultimediaDTO archivo = archivosActuales.get(indiceActual);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(archivo.getNombre()));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File destino = fileChooser.getSelectedFile();
                    // En una implementación real, descargarías el archivo desde el servidor
                    // Aquí solo simulamos la descarga creando un archivo vacío
                    try (FileOutputStream fos = new FileOutputStream(destino)) {
                        fos.write(0);
                    }
                    JOptionPane.showMessageDialog(this, 
                        "Archivo descargado exitosamente: " + destino.getAbsolutePath(), 
                        "Descarga exitosa", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al descargar el archivo: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void eliminarArchivoActual() {
        if (indiceActual >= 0 && indiceActual < archivosActuales.size()) {
            ArchivoMultimediaDTO archivo = archivosActuales.get(indiceActual);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar el archivo " + archivo.getNombre() + "?",
                "Confirmar eliminación", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // En una implementación real, aquí harías una llamada a la API para eliminar el archivo
                    JOptionPane.showMessageDialog(this, 
                        "Archivo eliminado exitosamente: " + archivo.getNombre(), 
                        "Eliminación exitosa", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Actualizar la lista de archivos
                    cargarArchivosMultimedia();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al eliminar el archivo: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
