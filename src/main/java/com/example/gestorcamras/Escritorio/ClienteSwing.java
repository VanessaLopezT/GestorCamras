package com.example.gestorcamras.Escritorio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

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

    @Setter
    private String cookieSesion;

    private String usuarioLogueado;

    public ClienteSwing(String usuario, String cookieSesion) {
        this.cookieSesion = cookieSesion;
        this.usuarioLogueado = usuario;

        setTitle("Cliente Equipo - Gestor de Cámaras (Usuario: " + usuario + ")");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        iniciarPing();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel superior para URL y equipo
        JPanel panelArriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelArriba.add(new JLabel("Servidor URL:"));

        txtServidorUrl = new JTextField("http://192.168.1.15:8080", 20);

        panelArriba.add(txtServidorUrl);

        panelArriba.add(new JLabel("Equipo ID:"));
        txtEquipoId = new JTextField(5);
        panelArriba.add(txtEquipoId);

        JButton btnCargarCamaras = new JButton("Cargar cámaras");
        panelArriba.add(btnCargarCamaras);

        JButton btnprobarConexionServidor = new JButton("Probar conexión");
        panelArriba.add(btnprobarConexionServidor);

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

        btnprobarConexionServidor.addActionListener(e -> probarConexionServidor());
        btnCargarCamaras.addActionListener(e -> cargarCamaras());
        btnSeleccionarImagen.addActionListener(e -> seleccionarArchivo("imagen"));
        btnSeleccionarVideo.addActionListener(e -> seleccionarArchivo("video"));
        btnEnviarImagen.addActionListener(e -> enviarArchivo("imagen"));
        btnEnviarVideo.addActionListener(e -> enviarArchivo("video"));
    }

    private void iniciarPing() {
        log("Iniciando tarea de ping...");
        timerPing = new Timer();
        timerPing.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                enviarPing();
            }
        }, 0, 30000); // Ping cada 30 segundos
    }

    private void enviarPing() {
        String equipoId = txtEquipoId.getText().trim();
        if (equipoId.isEmpty()) {
            log("No se puede enviar ping: ID de equipo no especificado");
            return;
        }

        try {
            URL url = new URL(txtServidorUrl.getText().trim() + "/api/equipos/" + equipoId + "/ping");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cookie", cookieSesion);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log("Ping enviado correctamente");
            } else {
                log("Error al enviar ping. Código: " + responseCode);
            }
        } catch (Exception e) {
            log("Error al enviar ping: " + e.getMessage());
        }
    }

    private void cargarCamaras() {
        String equipoId = txtEquipoId.getText().trim();
        if (equipoId.isEmpty()) {
            log("Debe especificar un ID de equipo");
            return;
        }

        try {
            URL url = new URL(txtServidorUrl.getText().trim() + "/api/equipos/" + equipoId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", cookieSesion);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                String respuesta = sb.toString();
                log("Respuesta JSON: " + respuesta);  // DEBUG

                JSONObject obj = new JSONObject(respuesta);
                Object camarasObj = obj.get("camaras");

                modeloCamaras.clear();

                if (camarasObj instanceof JSONArray) {
                    JSONArray camaras = (JSONArray) camarasObj;
                    for (int i = 0; i < camaras.length(); i++) {
                        JSONObject camara = camaras.getJSONObject(i);
                        modeloCamaras.addElement(camara.getString("nombre"));
                    }
                } else if (camarasObj instanceof JSONObject) {
                    JSONObject camara = (JSONObject) camarasObj;
                    modeloCamaras.addElement(camara.getString("nombre"));
                } else {
                    log("El campo 'camaras' no contiene cámaras o tiene un formato inesperado");
                }

                log("Cámaras cargadas correctamente");
            } else {
                log("Error al cargar cámaras. Código: " + responseCode);
            }
        } catch (Exception e) {
            log("Error al cargar cámaras: " + e.getMessage());
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
            URL url = new URL(servidorUrl + "/api/equipos/" + equipoId + "/camaras/" + camaraSeleccionada + "/archivo");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("Cookie", cookieSesion);

            try (
                    OutputStream output = conn.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
                    FileInputStream inputStream = new FileInputStream(archivoSeleccionado);
            ) {
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
                writer.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"")
                        .append(archivoSeleccionado.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(archivoSeleccionado.getName()))
                        .append("\r\n");
                writer.append("Content-Transfer-Encoding: binary\r\n\r\n");
                writer.flush();

                // Escribimos archivo binario
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();

                writer.append("\r\n");
                writer.flush();

                // Fin del multipart
                writer.append("--").append(boundary).append("--").append("\r\n");
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                log("Archivo enviado correctamente. Código: " + responseCode);
            } else {
                log("Error al enviar archivo. Código: " + responseCode);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log(line);
                    }
                } catch (Exception ignored) {}
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

    private void probarConexionServidor() {
        String servidorUrl = txtServidorUrl.getText().trim();
        try {
            URL url = new URL(servidorUrl + "/api/equipos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Agrega esta línea:
            conn.setRequestProperty("Cookie", cookieSesion);

            int code = conn.getResponseCode();
            if (code == 200) {
                log("Conexión exitosa al servidor.");
            } else {
                log("Error al conectar con servidor. Código HTTP: " + code);
            }
        } catch (Exception e) {
            log("Excepción en conexión con servidor: " + e.getMessage());
        }
    }

}
