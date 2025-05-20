package com.example.gestorcamras.Escritorio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class ClienteSwing extends JFrame {

    private JTextField txtServidorUrl;
    private JTextField txtEquipoId;
    private DefaultListModel<String> modeloCamaras;
    private JList<String> listaCamaras;
    private JTextArea txtLog;

    private File archivoSeleccionado; // archivo que se selecciona (imagen o video)

    public ClienteSwing() {
        setTitle("Cliente Equipo - Gestor de Cámaras");
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

        // Aquí iría la lógica para enviar el archivo HTTP
        log("Intentando enviar " + tipo + ": " + archivoSeleccionado.getName() +
                " al servidor " + servidorUrl +
                " para equipo " + equipoId +
                " y " + camaraSeleccionada);
    }

    private void log(String mensaje) {
        txtLog.append(mensaje + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteSwing app = new ClienteSwing();
            app.setVisible(true);
        });
    }
}

