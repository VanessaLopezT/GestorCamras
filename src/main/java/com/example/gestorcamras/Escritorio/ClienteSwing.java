package com.example.gestorcamras.Escritorio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Setter;

public class ClienteSwing extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JTextField txtServidorUrl;
    private JTextField txtEquipoId;
    private DefaultListModel<String> modeloCamaras;
    private JList<String> listaCamaras;
    private JTextArea txtLog;

    private File archivoSeleccionado;
    private Timer timerPing;

    private String usuario;
    private final String servidorUrl;
    private final String cookieSesion;

    public ClienteSwing(String usuario, String cookieSesion) {
        this.usuario = usuario;
        this.cookieSesion = cookieSesion;
        this.servidorUrl = "http://localhost:8080";
        setTitle("Cliente Equipo - Gestor de Cámaras (Usuario: " + usuario + ")");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        verificarYConectar();
    }

    private void verificarYConectar() {
        try {
            // Intentar conectar hasta que el servidor esté disponible
            int intentos = 0;
            final int MAX_INTENTOS = 5;
            final int TIEMPO_ESPERA = 2000; // 2 segundos

            while (intentos < MAX_INTENTOS) {
                if (verificarServidorActivo()) {
                    log("Servidor conectado exitosamente");
                    break;
                }
                intentos++;
                log("Intento " + intentos + "/" + MAX_INTENTOS + ": Servidor no disponible. Reintentando...");
                Thread.sleep(TIEMPO_ESPERA);
            }

            if (intentos >= MAX_INTENTOS) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo conectar al servidor después de " + MAX_INTENTOS + " intentos.\n" +
                    "Por favor, asegúrese de que:\n" +
                    "1. El servidor Spring Boot esté ejecutándose\n" +
                    "2. La URL del servidor sea correcta\n" +
                    "3. El puerto 8080 esté disponible", 
                    "Error de conexión", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Cerrar la aplicación si no se puede conectar
                return;
            }

            // Si el servidor está activo, proceder con el registro del equipo
            registrarEquipo();
            String equipoId = txtEquipoId.getText();
            if (!equipoId.isEmpty()) {
                cargarCamaras();
            } else {
                log("Error: No se pudo obtener el ID del equipo");
            }
        } catch (InterruptedException e) {
            log("Error al intentar conectar: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void initUI() {
        setTitle("Cliente Equipo - Gestor de Cámaras (Usuario: " + usuario + ")");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel superior para URL y equipo
        JPanel panelArriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelArriba.add(new JLabel("Servidor URL:"));
        
        // Add button to copy local IP
        JButton btnCopiarIP = new JButton("Copiar IP Local");
        btnCopiarIP.addActionListener(e -> {
            try {
                String ipLocal = getLocalIP();
                txtServidorUrl.setText("http://" + ipLocal + ":8080");
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(ipLocal), null);
                JOptionPane.showMessageDialog(this, "IP local copiada al portapapeles: " + ipLocal, 
                    "IP Local", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al obtener IP local: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelArriba.add(btnCopiarIP);

        try {
            String ipLocal = getLocalIP();
            txtServidorUrl = new JTextField("http://" + ipLocal + ":8080", 20);
        } catch (Exception e) {
            txtServidorUrl = new JTextField("http://localhost:8080", 20);
        }
        panelArriba.add(txtServidorUrl);

        panelArriba.add(new JLabel("Equipo ID:"));
        txtEquipoId = new JTextField(5);
        panelArriba.add(txtEquipoId);

        JButton btnCargarCamaras = new JButton("Cargar cámaras");
        panelArriba.add(btnCargarCamaras);

        JButton btnProbarConex = new JButton("Probar conexión");
        panelArriba.add(btnProbarConex);

        panel.add(panelArriba, BorderLayout.NORTH);

        // Panel central para lista de cámaras y botones
        modeloCamaras = new DefaultListModel<>();
        listaCamaras = new JList<>(modeloCamaras);
        listaCamaras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollCamaras = new JScrollPane(listaCamaras);
        scrollCamaras.setPreferredSize(new Dimension(200, 150));

        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(new JLabel("Cámaras disponibles:"), BorderLayout.NORTH);
        panelCentro.add(scrollCamaras, BorderLayout.CENTER);

        // Botones para seleccionar y enviar archivos
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));
        JButton btnSeleccionarImagen = new JButton("Seleccionar imagen");
        JButton btnEnviarImagen = new JButton("Enviar imagen");
        JButton btnSeleccionarVideo = new JButton("Seleccionar video");
        JButton btnEnviarVideo = new JButton("Enviar video");

        panelBotones.add(btnSeleccionarImagen);
        panelBotones.add(btnEnviarImagen);
        panelBotones.add(btnSeleccionarVideo);
        panelBotones.add(btnEnviarVideo);

        panelCentro.add(panelBotones, BorderLayout.SOUTH);

        panel.add(panelCentro, BorderLayout.CENTER);

        // Área de log
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setPreferredSize(new Dimension(580, 100));
        panel.add(scrollLog, BorderLayout.SOUTH);

        add(panel);

        // Listeners
        btnProbarConex.addActionListener(e -> {
            try {
                String servidorUrl = txtServidorUrl.getText();
                if (servidorUrl.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor ingrese la URL del servidor", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Deshabilitar botón mientras se prueba la conexión
                btnProbarConex.setEnabled(false);
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(servidorUrl + "/api/ping"))
                        .header("Cookie", cookieSesion)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int responseCode = response.statusCode();
                
                if (responseCode == 200) {
                    JOptionPane.showMessageDialog(this, "Conexión exitosa!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
                // Habilitar botón después de la prueba
                btnProbarConex.setEnabled(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                btnProbarConex.setEnabled(true);
            }
        });
        btnCargarCamaras.addActionListener(e -> cargarCamaras());
        btnSeleccionarImagen.addActionListener(e -> seleccionarArchivo("imagen"));
        btnSeleccionarVideo.addActionListener(e -> seleccionarArchivo("video"));
        btnEnviarImagen.addActionListener(e -> enviarArchivo("imagen"));
        btnEnviarVideo.addActionListener(e -> enviarArchivo("video"));
    }


    private String getLocalIP() throws Exception {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            log("Error al obtener la dirección IP local: " + e.getMessage());
            return "127.0.0.1";
        }
    }

    private boolean buscarEquipoPorIp(String ip) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/ip/" + ip))
                    .header("Cookie", cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

            if (responseCode == 200) {
                JSONObject responseJson = new JSONObject(responseBody);
                Long idEquipo = responseJson.getLong("id");
                txtEquipoId.setText(idEquipo.toString());
                return true;
            } else if (responseCode == 404) {
                log("No se encontró equipo para la IP: " + ip);
                return false;
            } else {
                log("Error al buscar equipo. Código: " + responseCode + ", Respuesta: " + responseBody);
                return false;
            }
        } catch (Exception ex) {
            log("Error en buscarEquipoPorIp: " + ex.getMessage());
            return false;
        }
    }

    private void registrarEquipo() {
        try {
            String ipLocal = getLocalIP();
            
            // Primero buscar si existe un equipo con esta IP
            if (buscarEquipoPorIp(ipLocal)) {
                log("Usando equipo existente para esta IP");
                return;
            }

            // Si no existe, crear uno nuevo
            String equipoId = txtEquipoId.getText().trim();
            
            if (equipoId.isEmpty()) {
                // Generar un ID único basado en la IP y el nombre del equipo
                equipoId = "EQ_" + ipLocal.replace(".", "_") + "_" + System.currentTimeMillis();
                txtEquipoId.setText(equipoId);
            }

            HttpClient client = HttpClient.newHttpClient();
            String json = String.format("{\"nombre\":\"Equipo %s\",\"identificador\":\"%s\",\"ip\":\"%s\",\"puerto\":8080}", 
                    equipoId, equipoId, ipLocal);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(txtServidorUrl.getText().trim() + "/api/equipos/registrar"))
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookieSesion)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

            if (responseCode == 200 || responseCode == 201) {
                try {
                    JSONObject responseJson = new JSONObject(responseBody);
                    Long idEquipo = responseJson.getLong("id");
                    log("Nuevo equipo registrado con ID: " + idEquipo);
                    txtEquipoId.setText(idEquipo.toString());
                    iniciarPing();
                } catch (Exception e) {
                    log("Error al procesar respuesta del servidor: " + e.getMessage());
                }
            } else {
                log("Error al registrar nuevo equipo. Código: " + responseCode + ", Respuesta: " + responseBody);
            }
        } catch (Exception e) {
            log("Error al registrar equipo: " + e.getMessage());
        }
    }

    private void iniciarPing() {
        timerPing = new Timer();
        timerPing.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String servidorUrl = txtServidorUrl.getText();
                    if (servidorUrl.isEmpty()) {
                        txtLog.append("[" + LocalDateTime.now() + "] URL del servidor no configurada\n");
                        return;
                    }

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(servidorUrl + "/api/ping"))
                            .header("Cookie", cookieSesion)
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    int responseCode = response.statusCode();
                    if (responseCode == 200) {
                        txtLog.append("[" + LocalDateTime.now() + "] Servidor activo\n");
                    } else {
                        txtLog.append("[" + LocalDateTime.now() + "] No se puede conectar al servidor\n");
                    }
                } catch (Exception e) {
                    txtLog.append("[" + LocalDateTime.now() + "] Error de conexión: " + e.getMessage() + "\n");
                }
            }
        }, 0, 5000);
    }

    private boolean verificarServidorActivo() {
        try {
            String servidorUrl = txtServidorUrl.getText();
            if (servidorUrl.isEmpty()) {
                log("Error: URL del servidor no configurada");
                return false;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/ping"))
                    .header("Cookie", cookieSesion)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String responseBody = response.body();

            if (responseCode == 200) {
                log("Servidor activo y conectado");
                return true;
            } else {
                log("Error al conectar al servidor. Código: " + responseCode + ", Respuesta: " + responseBody);
                return false;
            }
        } catch (Exception e) {
            log("Error al verificar conexión: " + e.getMessage());
            return false;
        }
    }

    private void cargarCamaras() {
        String equipoId = txtEquipoId.getText().trim();
        if (equipoId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe especificar un ID de equipo", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Verificar si el equipo existe
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(txtServidorUrl.getText().trim() + "/api/equipos/" + equipoId))
                    .header("Cookie", cookieSesion)
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            
            if (responseCode == 404) {
                // El equipo no existe, intentar registrar uno nuevo
                registrarEquipo();
                return;
            } else if (responseCode != 200) {
                JOptionPane.showMessageDialog(this, "Error al verificar equipo: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Registrar la cámara local
            registrarCamaraLocal(equipoId);
            
            // Cargar las cámaras
            JSONObject obj = new JSONObject(response.body());
            modeloCamaras.clear();
            
            if (obj.has("camaras")) {
                Object camarasObj = obj.get("camaras");
                
                if (camarasObj instanceof JSONArray) {
                    JSONArray camaras = (JSONArray) camarasObj;
                    for (int i = 0; i < camaras.length(); i++) {
                        JSONObject camara = camaras.getJSONObject(i);
                        modeloCamaras.addElement(camara.getString("nombre"));
                    }
                } else if (camarasObj instanceof JSONObject) {
                    JSONObject camara = (JSONObject) camarasObj;
                    modeloCamaras.addElement(camara.getString("nombre"));
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se encontraron cámaras para este equipo", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar cámaras: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void registrarCamaraLocal(String equipoId) {
        try {
            // Obtener la cámara local (primer dispositivo de video disponible)
            String camaraLocal = obtenerDispositivoVideo();
            if (camaraLocal != null) {
                HttpClient client = HttpClient.newHttpClient();
                String json = String.format("{\"nombre\":\"Cámara Local\",\"dispositivo\":\"%s\"}", camaraLocal);
                
                // Construir la URL para el registro de la cámara
                String url = txtServidorUrl.getText().trim() + "/api/camaras/registrar";
                
                // Crear la solicitud HTTP
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Cookie", cookieSesion)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                // Enviar la solicitud y obtener la respuesta
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int responseCode = response.statusCode();

                if (responseCode == 200 || responseCode == 201) {
                    log("Cámara local registrada exitosamente");
                    
                    // Asignar la cámara al equipo
                    JSONObject respuesta = new JSONObject(response.body());
                    Long idCamara = respuesta.getLong("id");
                    asignarCamaraAEquipo(equipoId, idCamara);
                } else {
                    log("Error al registrar cámara local. Código: " + responseCode);
                }
            }
        } catch (Exception e) {
            log("Error al registrar cámara local: " + e.getMessage());
        }
    }

    private String obtenerDispositivoVideo() {
        try {
            // Aquí deberíamos usar una API de sistema para obtener los dispositivos de video
            // Por ahora, usaremos un valor por defecto
            return "0"; // ID del primer dispositivo de video
        } catch (Exception e) {
            log("Error al obtener dispositivo de video: " + e.getMessage());
            return null;
        }
    }

    private void asignarCamaraAEquipo(String equipoId, Long idCamara) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(txtServidorUrl.getText().trim() + "/api/equipos/" + equipoId + "/camaras/" + idCamara))
                    .header("Cookie", cookieSesion)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();

            if (responseCode != 200) {
                log("Error al asignar cámara al equipo. Código: " + responseCode);
            }
        } catch (Exception e) {
            log("Error al asignar cámara al equipo: " + e.getMessage());
        }
    }

    private void seleccionarArchivo(String tipo) {
        JFileChooser chooser = new JFileChooser();
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            log("Archivo seleccionado para " + tipo + ": " + archivoSeleccionado.getAbsolutePath());
        }
    }

    private byte[] getMultipartFormData(String boundary, String tipo, String camaraSeleccionada, File archivoSeleccionado) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        // Parte: tipo de archivo
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"tipo\"\r\n");
        writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        writer.append(tipo.toUpperCase()).append("\r\n");
        writer.flush();

        // Parte: nombre cámara
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"camara\"\r\n");
        writer.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
        writer.append(camaraSeleccionada).append("\r\n");
        writer.flush();

        // Parte: archivo
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"" + archivoSeleccionado.getName() + "\"\r\n");
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(archivoSeleccionado.getName()))
                .append("\r\n");
        writer.append("Content-Transfer-Encoding: binary\r\n\r\n");
        writer.flush();

        // Escribimos archivo binario
        try (FileInputStream inputStream = new FileInputStream(archivoSeleccionado)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // Fin del multipart
        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.flush();

        return outputStream.toByteArray();
    }

    private void enviarArchivo(String tipo) {
        if (archivoSeleccionado == null) {
            log("Error: No hay archivo seleccionado para enviar.");
            return;
        }
        String servidorUrl = txtServidorUrl.getText().trim();
        String equipoId = txtEquipoId.getText().trim();
        String camaraSeleccionada = listaCamaras.getSelectedValue();

        if (servidorUrl.isEmpty() || equipoId.isEmpty() || camaraSeleccionada == null) {
            log("Error: Debes ingresar URL servidor, equipo y seleccionar cámara.");
            return;
        }

        String boundary = "===" + System.currentTimeMillis() + "===";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/api/equipos/" + equipoId + "/camaras/" + camaraSeleccionada + "/archivo"))
                    .header("Cookie", cookieSesion)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(getMultipartFormData(boundary, tipo, camaraSeleccionada, archivoSeleccionado)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int responseCode = response.statusCode();
            String respuesta = response.body();

            if (responseCode == 200 || responseCode == 201) {
                log("Archivo enviado correctamente. Código: " + responseCode);
            } else {
                log("Error al enviar archivo. Código: " + responseCode);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(respuesta.getBytes(StandardCharsets.UTF_8))))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log(line);
                    }
                } catch (Exception ignored) {
                    // Ignorar el error de lectura
                }
            }

        } catch (Exception e) {
            log("Excepción al enviar archivo: " + e.getMessage());
        }
    }

    private void log(String mensaje) {
        txtLog.append(LocalDateTime.now().toString() + " - " + mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    @Override
    public void dispose() {
        if (timerPing != null) {
            timerPing.cancel();
        }
        super.dispose();
    }
}
