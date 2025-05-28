package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.Escritorio.VisualizadorMultimediaUI;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.gestorcamras.Escritorio.DialogoCrearCamara;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class ServidorUI extends JFrame {
    private final Map<Long, EquipoEscritorioDTO> equiposActivos = new HashMap<>();
    private WebSocketClient webSocketClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String WS_URL = "ws://localhost:8080/ws";
    private String sessionId = "";
    private int messageId = 1;

    private static final String SERVER_URL = "http://localhost:8080/api";
    private static boolean isServerInstance = false;

    private JList<String> listaEquipos;
    private DefaultListModel<String> modeloListaEquipos;
    private JTable tablaCamaras;
    private DefaultTableModel modeloTablaCamaras;
    private JTextArea areaLogs;
    private JLabel lblEstadoServidor;
    private JPanel panelPrincipal;
    private JTabbedPane tabbedPane;
    private MapaCamarasPanel mapaPanel;

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
        // Panel principal con pestañas
        panelPrincipal = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de lista de equipos a la izquierda
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setPreferredSize(new Dimension(300, getHeight()));
        
        // Panel de la lista de equipos
        JPanel panelListaEquipos = new JPanel(new BorderLayout());
        panelListaEquipos.setBorder(BorderFactory.createTitledBorder("Equipos"));
        modeloListaEquipos = new DefaultListModel<>();
        listaEquipos = new JList<>(modeloListaEquipos);
        listaEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaEquipos.addListSelectionListener(this::seleccionarEquipo);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Botón para actualizar la lista de equipos
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarEquipos());
        
        // Botón para agregar cámara
        JButton btnAgregarCamara = new JButton("Agregar Cámara");
        btnAgregarCamara.addActionListener(e -> {
            int selectedIndex = listaEquipos.getSelectedIndex();
            if (selectedIndex >= 0) {
                String selected = modeloListaEquipos.getElementAt(selectedIndex);
                try {
                    // Obtener el ID y nombre del equipo seleccionado
                    long equipoId = -1;
                    String equipoNombre = "";
                    try {
                        // Buscar el patrón "ID: X" en el texto seleccionado
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("ID: (\\d+)");
                        java.util.regex.Matcher matcher = pattern.matcher(selected);
                        
                        if (matcher.find()) {
                            // Extraer solo la parte numérica del ID
                            String idStr = matcher.group(1);
                            equipoId = Long.parseLong(idStr);
                            
                            // Extraer el nombre del equipo (todo antes de "ID:")
                            int idIndex = selected.indexOf("ID:");
                            if (idIndex > 0) {
                                equipoNombre = selected.substring(0, idIndex).trim();
                            } else {
                                equipoNombre = "Equipo " + equipoId;
                            }
                        } else {
                            // Si no se encuentra el patrón, intentar extraer el ID de otra manera
                            int startIndex = selected.lastIndexOf("(") + 1;
                            int endIndex = selected.indexOf(")", startIndex);
                            if (startIndex > 0 && endIndex > startIndex) {
                                equipoId = Long.parseLong(selected.substring(startIndex, endIndex).replaceAll("[^0-9]", ""));
                                equipoNombre = selected.substring(0, startIndex - 1).trim();
                            } else {
                                throw new NumberFormatException("Formato de ID no reconocido");
                            }
                        }
                    } catch (Exception ex) {
                        log("Error al obtener el ID o nombre del equipo: " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, 
                            "Error al obtener la información del equipo: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Mostrar diálogo para crear nueva cámara
                    DialogoCrearCamara dialogo = new DialogoCrearCamara(this, equipoId, equipoNombre, this);
                    dialogo.setVisible(true);
                } catch (Exception ex) {
                    log("Error al obtener ID del equipo: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, 
                        "Error al obtener el ID del equipo seleccionado.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Por favor, seleccione un equipo primero.", 
                    "Selección requerida", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panelBotones.add(btnActualizar);
        panelBotones.add(btnAgregarCamara);
        
        panelListaEquipos.add(panelBotones, BorderLayout.NORTH);
        panelListaEquipos.add(new JScrollPane(listaEquipos), BorderLayout.CENTER);
        
        panelIzquierdo.add(panelListaEquipos, BorderLayout.CENTER);
        
        // Pestaña de cámaras
        JPanel panelCamaras = new JPanel(new BorderLayout());
        
        // Tabla de cámaras
        modeloTablaCamaras = new DefaultTableModel(new Object[]{"ID", "Nombre", "IP", "Estado", "Latitud", "Longitud", "Dirección"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaCamaras = new JTable(modeloTablaCamaras);
        tablaCamaras.setAutoCreateRowSorter(true);
        
        // Hacer que las filas tengan un tamaño fijo
        tablaCamaras.setRowHeight(25);
        
        // Ajustar el ancho de las columnas
        tablaCamaras.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaCamaras.getColumnModel().getColumn(1).setPreferredWidth(150); // Nombre
        tablaCamaras.getColumnModel().getColumn(2).setPreferredWidth(120); // IP
        tablaCamaras.getColumnModel().getColumn(3).setPreferredWidth(80);  // Estado
        tablaCamaras.getColumnModel().getColumn(4).setPreferredWidth(100); // Latitud
        tablaCamaras.getColumnModel().getColumn(5).setPreferredWidth(100); // Longitud
        tablaCamaras.getColumnModel().getColumn(6).setPreferredWidth(200); // Dirección
        
        JScrollPane scrollTabla = new JScrollPane(tablaCamaras);
        panelCamaras.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel de logs
        JPanel panelLogs = new JPanel(new BorderLayout());
        panelLogs.setBorder(BorderFactory.createTitledBorder("Registros"));
        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        panelLogs.add(new JScrollPane(areaLogs), BorderLayout.CENTER);
        
        // Panel inferior con logs
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setPreferredSize(new Dimension(getWidth(), 150));
        panelInferior.add(panelLogs, BorderLayout.CENTER);
        
        // Panel principal de la pestaña de cámaras
        JPanel panelCamarasPrincipal = new JPanel(new BorderLayout());
        panelCamarasPrincipal.add(panelCamaras, BorderLayout.CENTER);
        panelCamarasPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        // Añadir pestañas
        tabbedPane.addTab("Cámaras", panelCamarasPrincipal);
        
        // Inicializar el panel del mapa (inicialmente vacío)
        mapaPanel = new MapaCamarasPanel(new JSONArray());
        tabbedPane.addTab("Mapa", new JScrollPane(mapaPanel));
        
        // Configurar el diseño principal
        JPanel panelContenido = new JPanel(new BorderLayout());
        panelContenido.add(panelIzquierdo, BorderLayout.WEST);
        panelContenido.add(tabbedPane, BorderLayout.CENTER);
        
        panelPrincipal.add(panelContenido, BorderLayout.CENTER);
        
        // Barra de estado
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblEstadoServidor = new JLabel("Estado: Desconectado");
        panelEstado.add(lblEstadoServidor);
        panelPrincipal.add(panelEstado, BorderLayout.SOUTH);
        
        add(panelPrincipal);
        
        // Panel superior con información del servidor y botones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        
        // Panel de estado del servidor
        JPanel panelEstado2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblEstadoServidor = new JLabel("Obteniendo dirección IP...");
        panelEstado2.add(new JLabel("Servidor: "));
        panelEstado2.add(lblEstadoServidor);
        panelSuperior.add(panelEstado2, BorderLayout.WEST);
        
        // Panel de botones
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        
        // Botón para ver archivos recibidos
        JButton btnVerArchivos = new JButton("Ver Archivos Recibidos");
        btnVerArchivos.addActionListener(e -> {
            // Abrir la ventana de visualización de archivos
            SwingUtilities.invokeLater(() -> {
                try {
                    VisualizadorMultimediaUI visualizador = new VisualizadorMultimediaUI();
                    visualizador.setLocationRelativeTo(this);
                    visualizador.setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al abrir el visualizador de archivos: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
        });
        panelBoton.add(btnVerArchivos);
        
        panelSuperior.add(panelBoton, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Obtener y mostrar la IP del servidor
        actualizarDireccionIP();
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
                case "archivo_subido":
                    procesarArchivoSubido(json.optJSONObject("datos"));
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
        SwingUtilities.invokeLater(() -> {
            areaLogs.append("[" + java.time.LocalTime.now() + "] " + mensaje + "\n");
            areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
        });
    }
    
    /**
     * Actualiza la interfaz con la dirección IP del servidor
     */
    private void actualizarDireccionIP() {
        new Thread(() -> {
            try {
                String ip = obtenerDireccionIP();
                String mensaje = "Dirección IP: " + ip + " (Puerto: 8080)";
                SwingUtilities.invokeLater(() -> {
                    lblEstadoServidor.setText(mensaje);
                    log("Servidor iniciado en: http://" + ip + ":8080");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblEstadoServidor.setText("No se pudo determinar la dirección IP");
                    log("Error al obtener la dirección IP: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Obtiene la dirección IP local de la máquina
     * @return La dirección IP local o "localhost" si no se pudo determinar
     */
    private String obtenerDireccionIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignorar interfaces que no estén activas o sean loopback
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Solo direcciones IPv4
                    if (addr.isLoopbackAddress() || !(addr.getHostAddress().contains("."))) {
                        continue;
                    }
                    
                    String hostAddress = addr.getHostAddress();
                    // Verificar que sea una dirección IP privada
                    if (hostAddress.startsWith("192.168.") || 
                        hostAddress.startsWith("10.") || 
                        hostAddress.startsWith("172.16.")) {
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
            log("Error al obtener la dirección IP: " + e.getMessage());
        }
        return "localhost";
    }
    
    /**
     * Procesa una notificación de archivo subido
     * @param datos Datos del archivo subido
     */
    private void procesarArchivoSubido(JSONObject datos) {
        if (datos == null) {
            log("Datos de archivo subido nulos");
            return;
        }
        
        try {
            String nombreArchivo = datos.optString("nombreArchivo", "desconocido");
            String tipo = datos.optString("tipo", "desconocido");
            String camara = datos.optString("camara", "desconocida");
            long tamano = datos.optLong("tamano", 0);
            
            String mensaje = String.format("✅ Archivo recibido: %s\n   Tipo: %s\n   Cámara: %s\n   Tamaño: %d bytes", 
                nombreArchivo, tipo, camara, tamano);
                
            log(mensaje);
            
            // Mostrar notificación emergente
            mostrarNotificacion("Nuevo archivo recibido", 
                String.format("Archivo: %s\nCámara: %s", nombreArchivo, camara));
                
        } catch (Exception e) {
            log("Error al procesar notificación de archivo subido: " + e.getMessage());
        }
    }
    
    /**
     * Muestra una notificación emergente
     * @param titulo Título de la notificación
     * @param mensaje Mensaje a mostrar
     */
    private void mostrarNotificacion(String titulo, String mensaje) {
        // Usar JOptionPane como alternativa si SystemTray no está disponible
        if (!SystemTray.isSupported()) {
            log("Mostrando notificación en ventana emergente (SystemTray no soportado)");
            JOptionPane.showMessageDialog(
                this, 
                mensaje, 
                titulo, 
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        // Usar SystemTray si está disponible
        try {
            // Obtener el SystemTray
            SystemTray tray = SystemTray.getSystemTray();
            
            // Crear una imagen para el ícono (usando el ícono de la aplicación si está disponible)
            Image image = null;
            try {
                // Intentar cargar el ícono de la aplicación
                java.net.URL imageURL = getClass().getResource("/icon.png");
                if (imageURL != null) {
                    image = new ImageIcon(imageURL).getImage();
                } else {
                    // Crear una imagen simple si no se encuentra el ícono
                    image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = ((BufferedImage)image).createGraphics();
                    g2d.setColor(Color.BLUE);
                    g2d.fillOval(0, 0, 32, 32);
                    g2d.dispose();
                }
            } catch (Exception e) {
                log("No se pudo cargar el ícono: " + e.getMessage());
                // Crear una imagen simple en caso de error
                image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            }
            
            // Crear el objeto TrayIcon
            TrayIcon trayIcon = new TrayIcon(image, "Gestor de Cámaras");
            trayIcon.setImageAutoSize(true);
            
            // Agregar el ícono al system tray
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                log("Error al agregar el ícono al system tray: " + e.getMessage());
                // Mostrar notificación de todos modos, aunque no se pueda agregar al system tray
            }
            
            // Mostrar la notificación
            trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.INFO);
            
            // Programar la eliminación del ícono después de un tiempo
            new java.util.Timer().schedule( 
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        tray.remove(trayIcon);
                    }
                }, 
                5000 
            );
            
        } catch (Exception e) {
            log("Error al mostrar notificación: " + e.getMessage());
            // Mostrar notificación alternativa
            JOptionPane.showMessageDialog(
                this, 
                mensaje, 
                titulo, 
                JOptionPane.INFORMATION_MESSAGE
            );
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
        
        EquipoEscritorioDTO equipo = new EquipoEscritorioDTO(idEquipo, nombre, ip, true);
        
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
            EquipoEscritorioDTO equipoViejo = equiposActivos.get(idEquipo);
            if (equipoViejo != null) {
                // Crear una nueva instancia con el estado actualizado
                EquipoEscritorioDTO equipoActualizado = new EquipoEscritorioDTO(
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
                EquipoEscritorioDTO equipo = new EquipoEscritorioDTO(
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
        
        // Crear un nuevo array para almacenar las cámaras con ubicación
        JSONArray camarasConUbicacion = new JSONArray();
        
        // Agregar las cámaras a la tabla
        for (int i = 0; i < camaras.length(); i++) {
            JSONObject camara = camaras.getJSONObject(i);
            
            // Obtener el estado basado en el campo 'activa'
            boolean activa = camara.optBoolean("activa", false);
            String estado = activa ? "Activa" : "Inactiva";
            
            // Obtener la IP de la cámara
            String ip = camara.optString("ip", "Desconocida");
            
            // Obtener la ubicación de la cámara
            Double latitud = null;
            Double longitud = null;
            
            try {
                if (camara.has("latitud") && !camara.isNull("latitud")) {
                    latitud = camara.getDouble("latitud");
                }
                if (camara.has("longitud") && !camara.isNull("longitud")) {
                    longitud = camara.getDouble("longitud");
                }
            } catch (Exception e) {
                log("Error al obtener coordenadas de la cámara: " + e.getMessage());
            }
            
            String direccion = camara.optString("direccion", "");
            String tipo = camara.optString("tipo", "");
            
            // Si la cámara tiene ubicación, la añadimos al array para el mapa
            if (latitud != null && longitud != null) {
                JSONObject camaraConUbicacion = new JSONObject();
                camaraConUbicacion.put("idCamara", camara.getLong("idCamara"));
                camaraConUbicacion.put("nombre", camara.optString("nombre", "Sin nombre"));
                camaraConUbicacion.put("ip", ip);
                camaraConUbicacion.put("activa", activa);
                camaraConUbicacion.put("latitud", latitud);
                camaraConUbicacion.put("longitud", longitud);
                camaraConUbicacion.put("direccion", direccion);
                camaraConUbicacion.put("tipo", tipo);
                camarasConUbicacion.put(camaraConUbicacion);
                
                log("Cámara con ubicación: " + camaraConUbicacion.toString());
            }
            
            // Usar valores vacíos para latitud y longitud cuando sean nulos
            modeloTablaCamaras.addRow(new Object[]{
                camara.getLong("idCamara"),
                camara.optString("nombre", "Sin nombre"),
                ip,
                estado,
                latitud != null ? latitud : "",
                longitud != null ? longitud : "",
                direccion
            });
        }
        
        // Actualizar el mapa con las cámaras que tienen ubicación
        if (mapaPanel != null) {
            log("Actualizando mapa con " + camarasConUbicacion.length() + " cámaras con ubicación");
            mapaPanel.actualizarCamaras(camarasConUbicacion);
        } else {
            log("Error: mapaPanel es nulo");
        }
        
        log("Tabla de cámaras actualizada con " + camaras.length() + " cámaras");
    }
    
    public void cargarCamaras(long equipoId) {
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
