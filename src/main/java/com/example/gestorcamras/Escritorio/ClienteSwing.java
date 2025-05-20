package com.example.gestorcamras.Escritorio;

import lombok.Getter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

import lombok.Setter;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.concurrent.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.DefaultListModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClienteSwing extends JFrame {

    private JTextField txtServidorUrl;
    private JTextField txtEquipoId;
    private DefaultListModel<String> modeloCamaras;
    private JList<String> listaCamaras;
    private JTextArea txtLog;

    private File archivoSeleccionado; // archivo que se selecciona (imagen o video)

    // --- Variable para guardar cookie de sesión ---
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
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel superior para URL y equipo
        JPanel panelArriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelArriba.add(new JLabel("Servidor URL:"));
        txtServidorUrl = new JTextField("http://localhost:8080", 20);
        panelArriba.add(txtServidorUrl);

        panelArriba.add(new JLabel("Equipo ID:"));
        txtEquipoId = new JTextField(5);
        panelArriba.add(txtEquipoId);

        JButton btnCargarCamaras = new JButton("Cargar cámaras");
        panelArriba.add(btnCargarCamaras);

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

        // Listeners básicos (más adelante implementaremos lógica)
        btnCargarCamaras.addActionListener(e -> cargarCamarasSimuladas());

        btnSeleccionarImagen.addActionListener(e -> seleccionarArchivo("imagen"));

        btnSeleccionarVideo.addActionListener(e -> seleccionarArchivo("video"));

        btnEnviarImagen.addActionListener(e -> enviarArchivo("imagen"));

        btnEnviarVideo.addActionListener(e -> enviarArchivo("video"));
    }

    private void cargarCamarasSimuladas() {
        modeloCamaras.clear();
        // Por ahora simulamos 3 cámaras
        modeloCamaras.addElement("Cámara 1");
        modeloCamaras.addElement("Cámara 2");
        modeloCamaras.addElement("Cámara 3");
        log("Cámaras simuladas cargadas.");
    }

    private void seleccionarArchivo(String tipo) {
        JFileChooser chooser = new JFileChooser();
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            log("Archivo seleccionado para " + tipo + ": " + archivoSeleccionado.getAbsolutePath());
        }
    }

    private void log(String mensaje) {
        txtLog.append(mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    // Método para hacer login y guardar cookie
    private boolean hacerLogin(String usuario, String password) {
        try {
            URL url = new URL(txtServidorUrl.getText().trim() + "/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false); // para capturar cookie

            // Construimos datos de formulario: username=...&password=...
            String params = "username=" + URLEncoder.encode(usuario, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8");

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(params.length()));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_OK) {
                // Capturamos la cookie JSESSIONID del header "Set-Cookie"
                String headerCookies = conn.getHeaderField("Set-Cookie");
                if (headerCookies != null) {
                    for (String cookie : headerCookies.split(";")) {
                        if (cookie.startsWith("JSESSIONID")) {
                            cookieSesion = cookie;
                            log("Login exitoso. Cookie sesión guardada: " + cookieSesion);
                            return true;
                        }
                    }
                }
            }
            log("Login fallido. Código HTTP: " + responseCode);
        } catch (Exception e) {
            log("Error en login: " + e.getMessage());
        }
        return false;
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

        // Verificamos que estemos logueados
        if (cookieSesion == null) {
            // No permitir enviar si no está logueado, mostrar mensaje de login
            log("No se pudo enviar: no hay sesión activa. Por favor, relogin.");
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
                writer.append(tipo).append("\r\n");
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


}
