package com.example.gestorcamras.Escritorio;

import org.json.JSONArray;
import org.json.JSONObject;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

import java.util.HashMap;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class ServidorUI extends JFrame {
    private final Map<Long, EquipoDTO> equiposActivos = new HashMap<>();
    private WebSocketClient webSocketClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String WS_URL = "ws://localhost:8080/ws/websocket";
    private String sessionId = "";
    private int messageId = 1;

    private static final String SERVER_URL = "http://localhost:8080/api";
    private static boolean isServerInstance = false;

    private JList<String> listaEquipos;
    private DefaultListModel<String> modeloListaEquipos;
    private JTable tablaCamaras;
    private DefaultTableModel modeloTablaCamaras;
    private JTextArea areaLogs;

    public ServidorUI() {
        if (!isServerInstance) {
            isServerInstance = true;
        } else {
            JOptionPane.showMessageDialog(null, "Solo puede haber una instancia del servidor", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        setTitle("Servidor Gestor de Cámaras");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Inicializar componentes de la interfaz
        inicializarComponentes();
        
        // Cargar los equipos iniciales
        cargarEquipos();
        
        // Configurar WebSocket
        configurarWebSocket();
    }

    private void inicializarComponentes() {
        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        
        // Panel de lista de equipos
        JPanel panelEquipos = new JPanel(new BorderLayout());
        panelEquipos.setBorder(BorderFactory.createTitledBorder("Equipos Conectados"));
        panelEquipos.setPreferredSize(new Dimension(300, 0));
        
        modeloListaEquipos = new DefaultListModel<>();
        listaEquipos = new JList<>(modeloListaEquipos);
        listaEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaEquipos.addListSelectionListener(this::seleccionarEquipo);
        panelEquipos.add(new JScrollPane(listaEquipos), BorderLayout.CENTER);
        
        // Panel de cámaras
        JPanel panelCamaras = new JPanel(new BorderLayout());
        panelCamaras.setBorder(BorderFactory.createTitledBorder("Cámaras del Equipo"));
        
        modeloTablaCamaras = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "IP", "Estado"}, 0
        );
        tablaCamaras = new JTable(modeloTablaCamaras);
        panelCamaras.add(new JScrollPane(tablaCamaras), BorderLayout.CENTER);
        
        // Panel de logs
        JPanel panelLogs = new JPanel(new BorderLayout());
        panelLogs.setBorder(BorderFactory.createTitledBorder("Registros"));
        panelLogs.setPreferredSize(new Dimension(0, 150));
        
        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        panelLogs.add(new JScrollPane(areaLogs), BorderLayout.CENTER);
        
        // Organizar paneles
        JSplitPane splitPanePrincipal = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            panelEquipos,
            panelCamaras
        );
        splitPanePrincipal.setResizeWeight(0.3);
        
        panelPrincipal.add(splitPanePrincipal, BorderLayout.CENTER);
        panelPrincipal.add(panelLogs, BorderLayout.SOUTH);
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        // Botón de actualizar
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarEquipos());
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnActualizar);
        add(panelBoton, BorderLayout.NORTH);
    }
    
    private void configurarWebSocket() {
        try {
            // Cerrar conexión anterior si existe
            if (webSocketClient != null) {
                try {
                    webSocketClient.close();
                } catch (Exception e) {
                    log("Error al cerrar conexión anterior: " + e.getMessage());
                }
            }
            
            log("Conectando a WebSocket en " + WS_URL);
            
            // Crear nueva conexión WebSocket
            webSocketClient = new WebSocketClient(new URI(WS_URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log("Conexión WebSocket establecida");
                    
                    // Enviar comando CONNECT de STOMP sobre SockJS
                    String connectFrame = "[\"CONNECT\\naccept-version:1.1,1.0\\nheart-beat:10000,10000\\n\\n\\u0000\"]";
                    send(connectFrame);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        log("Mensaje recibido: " + message);
                        
                        // Procesar mensajes SockJS
                        if (message.startsWith("a[\"{\"serverid")) {
                            // Mensaje de bienvenida de SockJS
                            int start = message.indexOf('{');
                            int end = message.lastIndexOf('}') + 1;
                            if (start > 0 && end > start) {
                                String jsonStr = message.substring(start, end);
                                org.json.JSONObject json = new org.json.JSONObject(jsonStr);
                                sessionId = json.getString("sessionId");
                                log("Sesión SockJS establecida: " + sessionId);
                                
                                // Una vez que tenemos la sesión, suscribirse a los temas
                                suscribirATopic("equipos");
                                suscribirATopic("camaras");
                                suscribirATopic("alarmas");
                                // Suscribirse a actualizaciones de equipos
                                suscribirATopic("equipos/actualizacion");
                            }
                        } 
                        // Procesar mensajes STOMP
                        else if (message.startsWith("a[\"MESSAGE")) {
                            // Extraer el contenido del mensaje STOMP
                            String content = message.substring(message.indexOf('\n') + 1);
                            procesarMensajeWebSocket(content);
                        } 
                        else if (message.startsWith("a[\"CONNECTED")) {
                            log("Conexión STOMP establecida correctamente");
                        } 
                        else if (message.startsWith("a[\"ERROR")) {
                            log("Error en el servidor STOMP: " + message);
                        }
                    } catch (Exception e) {
                        log("Error al procesar mensaje WebSocket: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log("Conexión WebSocket cerrada: " + reason + " (code: " + code + ")");
                    // Intentar reconectar después de un retraso
                    reconectarWebSocket();
                }

                @Override
                public void onError(Exception ex) {
                    log("Error en WebSocket: " + ex.getMessage());
                }
                
                private void suscribirATopic(String topic) {
                    String subscriptionId = "sub-" + topic + "-" + messageId++;
                    String subscription = String.format("[\"SUBSCRIBE\\nid:%s\\ndestination:/topic/%s\\n\\n\\u0000\"]", 
                            subscriptionId, topic);
                    log("Suscribiendo a tema: " + topic + " con ID: " + subscriptionId);
                    send(subscription);
                }
            };
            
            // Configurar cabeceras
            webSocketClient.addHeader("Origin", "http://localhost:8080");
            
            // Iniciar la conexión WebSocket
            webSocketClient.connect();
            log("Conectando a WebSocket en " + WS_URL);
            
        } catch (Exception e) {
            log("Error al configurar WebSocket: " + e.getMessage());
            // Intentar reconectar después de un error
            reconectarWebSocket();
        }
    }
    
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private int reconnectAttempts = 0;
    
    private void reconectarWebSocket() {
        // Verificar si ya estamos intentando reconectar
        if (webSocketClient != null && webSocketClient.isOpen()) {
            return;
        }
        
        // Verificar si hemos excedido el número máximo de intentos
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            log("Se ha alcanzado el número máximo de intentos de reconexión (" + MAX_RECONNECT_ATTEMPTS + ").");
            JOptionPane.showMessageDialog(this, 
                "No se pudo reconectar con el servidor después de " + MAX_RECONNECT_ATTEMPTS + " intentos.",
                "Error de conexión", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calcular el tiempo de espera (backoff exponencial con un máximo de 30 segundos)
        int delay = Math.min(30, (int) Math.pow(2, reconnectAttempts));
        reconnectAttempts++;
        
        log(String.format("Intentando reconectar en %d segundos (intento %d/%d)...", 
            delay, reconnectAttempts, MAX_RECONNECT_ATTEMPTS));
        
        // Programar el próximo intento de reconexión
        scheduler.schedule(() -> {
            try {
                log("Intentando establecer conexión WebSocket...");
                configurarWebSocket();
            } catch (Exception e) {
                log("Error al intentar reconexión: " + e.getMessage());
                // Programar el próximo intento
                reconectarWebSocket();
            }
        }, delay, TimeUnit.SECONDS);
    }
    
    private void procesarMensajeWebSocket(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            log("Mensaje WebSocket vacío recibido");
            return;
        }
        
        log("Mensaje WebSocket recibido: " + mensaje);
        
        try {
            // Verificar si es un mensaje de actualización de equipos
            if (mensaje.contains("actualizar")) {
                log("Recibida notificación para actualizar lista de equipos");
                SwingUtilities.invokeLater(this::cargarEquipos);
                return;
            }
            
            // Intentar analizar el mensaje como JSON
            JSONObject json;
            try {
                json = new JSONObject(mensaje);
            } catch (Exception e) {
                log("No se pudo analizar el mensaje como JSON: " + e.getMessage());
                return;
            }
            
            // Obtener el tipo de mensaje
            String tipo = json.optString("tipo", "");
            if (tipo.isEmpty()) {
                log("Mensaje sin tipo especificado: " + mensaje);
                return;
            }
            
            // Procesar según el tipo de mensaje
            switch (tipo) {
                case "equipo_conectado":
                    procesarEquipoConectado(json.optJSONObject("datos"));
                    break;
                case "equipo_actualizado":
                    procesarEquipoActualizado(json.optJSONObject("datos"));
                    break;
                case "camara_actualizada":
                case "nueva_camara":
                    procesarCamaraActualizada(json);
                    break;
                case "alarma":
                    procesarAlarma(json.optJSONObject("datos"));
                    break;
                case "heartbeat":
                    // Solo registrar el latido si es necesario para depuración
                    log("Latido recibido del servidor");
                    break;
                default:
                    log("Mensaje WebSocket no reconocido (tipo: " + tipo + "): " + mensaje);
            }
        } catch (Exception e) {
            log("Error al procesar mensaje WebSocket: " + e.getMessage());
            log("Mensaje problemático: " + mensaje);
        }
    }
    
    private void log(String mensaje) {
        if (areaLogs != null) {
            SwingUtilities.invokeLater(() -> {
                areaLogs.append("[" + java.time.LocalTime.now() + "] " + mensaje + "\n");
                areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
            });
        } else {
            System.out.println("[" + java.time.LocalTime.now() + "] " + mensaje);
        }
    }
    
    private void procesarAlarma(JSONObject datos) {
        if (datos == null) {
            log("Datos de alarma nulos");
            return;
        }
        
        String mensaje = datos.optString("mensaje", "Alarma recibida");
        String tipo = datos.optString("tipo", "desconocido");
        long timestamp = datos.optLong("timestamp", System.currentTimeMillis());
        
        String mensajeCompleto = String.format("[%s] Alarma (%s): %s", 
            new java.util.Date(timestamp).toString(), 
            tipo, 
            mensaje);
            
        log(mensajeCompleto);
        
        // Mostrar notificación visual si la alarma es crítica
        if (tipo.equalsIgnoreCase("critica")) {
            JOptionPane.showMessageDialog(this, 
                mensaje, 
                "¡Alarma Crítica!", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void procesarEquipoConectado(JSONObject datos) {
        long idEquipo = datos.getLong("idEquipo");
        String nombre = datos.optString("nombre", "Equipo " + idEquipo);
        String ip = datos.optString("ip", "");
        
        EquipoDTO equipo = new EquipoDTO(idEquipo, nombre, ip, true);
        
        SwingUtilities.invokeLater(() -> {
            equiposActivos.put(idEquipo, equipo);
            actualizarListaEquipos();
            log("Nuevo equipo conectado: " + nombre + " (ID: " + idEquipo + ")");
        });
    }
    
    private void procesarEquipoActualizado(JSONObject datos) {
        long idEquipo = datos.getLong("idEquipo");
        boolean activo = datos.optBoolean("activo", true);
        
        SwingUtilities.invokeLater(() -> {
            EquipoDTO equipoViejo = equiposActivos.get(idEquipo);
            if (equipoViejo != null) {
                // Crear una nueva instancia con el estado actualizado
                EquipoDTO equipoActualizado = new EquipoDTO(
                    equipoViejo.getId(),
                    equipoViejo.getNombre(),
                    equipoViejo.getIp(),
                    activo
                );
                equiposActivos.put(idEquipo, equipoActualizado);
                
                // Si el equipo está seleccionado, actualizar sus cámaras
                if (listaEquipos.getSelectedIndex() >= 0) {
                    String selected = modeloListaEquipos.getElementAt(listaEquipos.getSelectedIndex());
                    if (selected.contains("ID: " + idEquipo)) {
                        cargarCamaras(idEquipo);
                    }
                }
                log("Estado del equipo actualizado: " + equipoActualizado.getNombre() + " - " + (activo ? "Activo" : "Inactivo"));
            }
        });
    }
    
    private void procesarCamaraActualizada(JSONObject datos) {
        try {
            long idEquipo = datos.getLong("equipoId");
            JSONObject camaraJson = datos.optJSONObject("camara");
            
            log("Procesando actualización de cámara para equipo: " + idEquipo);
            
            if (camaraJson != null) {
                log("Datos de la cámara recibidos: " + camaraJson.toString());
            }
            
            // Actualizar la interfaz en el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                try {
                    // Actualizar la lista de equipos primero
                    log("Actualizando lista de equipos...");
                    cargarEquipos();
                    
                    // Si hay un equipo seleccionado que coincide con el equipo de la cámara, actualizar sus cámaras
                    if (listaEquipos.getSelectedIndex() >= 0) {
                        String selected = modeloListaEquipos.getElementAt(listaEquipos.getSelectedIndex());
                        if (selected.contains("ID: " + idEquipo)) {
                            log("Equipo seleccionado coincide. Actualizando lista de cámaras...");
                            cargarCamaras(idEquipo);
                        } else {
                            log("Equipo seleccionado no coincide. No se actualizarán las cámaras.");
                        }
                    } else {
                        log("Sin equipo seleccionado. No se actualizarán las cámaras.");
                    }
                } catch (Exception e) {
                    log("Error al actualizar la interfaz: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            log("Error al procesar actualización de cámara: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void actualizarListaEquipos() {
        modeloListaEquipos.clear();
        equiposActivos.values().forEach(equipo -> {
            modeloListaEquipos.addElement(equipo.getNombre() + " (ID: " + equipo.getId() + ")");
        });
    }
    
    private void cargarEquipos() {
        log("Iniciando carga de equipos...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    log("Realizando solicitud a: " + SERVER_URL + "/equipos");
                    
                    // Crear cliente HTTP
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(SERVER_URL + "/equipos"))
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
                        log("Equipos recibidos: " + equipos.length());
                        
                        // Actualizar la interfaz en el hilo de eventos de Swing
                        SwingUtilities.invokeLater(() -> actualizarListaEquipos(equipos));
                    } else {
                        String errorMessage = "Error al cargar equipos. Código: " + statusCode;
                        log(errorMessage);
                        log("Cuerpo de la respuesta: " + response.body());
                        
                        // Mostrar el error en la interfaz
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(ServidorUI.this, 
                                errorMessage, "Error", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception e) {
                    String errorMsg = "Error al cargar equipos: " + e.getMessage();
                    log(errorMsg);
                    e.printStackTrace();
                    
                    // Mostrar el error en la interfaz
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(ServidorUI.this, 
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
            modeloListaEquipos.clear();
            equiposActivos.clear();
            
            for (int i = 0; i < equipos.length(); i++) {
                JSONObject equipoJson = equipos.getJSONObject(i);
                EquipoDTO equipo = new EquipoDTO(
                    equipoJson.getLong("idEquipo"),
                    equipoJson.optString("nombre", "Sin nombre"),
                    equipoJson.optString("ip", ""),
                    equipoJson.optBoolean("activo", false)
                );
                
                equiposActivos.put(equipo.getId(), equipo);
                modeloListaEquipos.addElement(equipo.getNombre() + " (ID: " + equipo.getId() + ")");
            }
            
            if (!equiposActivos.isEmpty()) {
                listaEquipos.setSelectedIndex(0);
            }
        });
    }
    
    private void seleccionarEquipo(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        
        int selectedIndex = listaEquipos.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selected = modeloListaEquipos.getElementAt(selectedIndex);
            // Extraer el ID del equipo del texto seleccionado
            try {
                int startIndex = selected.lastIndexOf("ID: ") + 4;
                int endIndex = selected.indexOf(")", startIndex);
                long equipoId = Long.parseLong(selected.substring(startIndex, endIndex));
                cargarCamaras(equipoId);
            } catch (Exception ex) {
                log("Error al obtener ID del equipo: " + ex.getMessage());
            }
        }
    }
    
    private void actualizarTablaCamaras(JSONArray camaras) {
        // Limpiar la tabla actual
        modeloTablaCamaras.setRowCount(0);
        
        // Agregar las cámaras a la tabla
        for (int i = 0; i < camaras.length(); i++) {
            JSONObject camara = camaras.getJSONObject(i);
            modeloTablaCamaras.addRow(new Object[]{
                camara.getLong("idCamara"),
                camara.optString("nombre", "Sin nombre"),
                camara.optString("ubicacion", "Desconocida"),
                camara.optString("estado", "Desconocido"),
                camara.optString("tipo", "Desconocido")
            });
        }
        
        log("Tabla de cámaras actualizada con " + camaras.length() + " cámaras");
    }
    
    private void cargarCamaras(long equipoId) {
        log("Iniciando carga de cámaras para el equipo ID: " + equipoId);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    log("Solicitando cámaras para el equipo: " + equipoId);
                    
                    // Crear cliente HTTP
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    String url = SERVER_URL + "/camaras/equipo/" + equipoId;
                    log("URL de solicitud: " + url);
                    
                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
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
                        
                        JSONArray camaras = new JSONArray(responseBody);
                        log("Cámaras recibidas: " + camaras.length());
                        
                        // Actualizar la interfaz en el hilo de eventos de Swing
                        SwingUtilities.invokeLater(() -> actualizarTablaCamaras(camaras));
                    } else {
                        String errorMessage = "Error al cargar cámaras. Código: " + statusCode;
                        log(errorMessage);
                        log("Cuerpo de la respuesta: " + response.body());
                        
                        // Mostrar el error en la interfaz
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(ServidorUI.this, 
                                errorMessage, "Error", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception e) {
                    log("Error al cargar cámaras: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Para propagar cualquier excepción que haya ocurrido
                    log("Carga de cámaras completada");
                } catch (Exception e) {
                    log("Error al cargar cámaras: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    @Override
    public void dispose() {
        try {
            if (webSocketClient != null) {
                if (webSocketClient.isOpen()) {
                    webSocketClient.close();
                }
                webSocketClient = null;
            }
            
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (Exception e) {
            log("Error al cerrar WebSocket: " + e.getMessage());
        } finally {
            super.dispose();
        }
    }
    
    // El punto de entrada principal (main) se encuentra en otra clase de la aplicación
}
