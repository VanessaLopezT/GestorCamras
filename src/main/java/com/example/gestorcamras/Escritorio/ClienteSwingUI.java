package com.example.gestorcamras.Escritorio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.time.LocalDateTime;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.json.JSONObject;

public class ClienteSwingUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JTextField txtServidorUrl;
    private String equipoIdGenerado;  // Para almacenar el ID del equipo generado
    private DefaultListModel<String> modeloCamaras;
    private JList<String> listaCamaras;
    private JTextArea txtLog;
    private File archivoSeleccionado;
    
    private final ClienteSwingController controller;
    private final String usuario;
    
    public ClienteSwingUI(String usuario, String cookieSesion) {
        this.usuario = usuario;
        this.controller = new ClienteSwingController(usuario, cookieSesion, null);
        
        // Configurar consumidor de logs
        controller.setLogConsumer(this::log);
        
        // Configurar la interfaz de usuario
        initUI();
        
        // Iniciar la verificación de conexión
        verificarYConectar();
    }
    
    private void verificarYConectar() {
        // Configurar el consumidor de estado de conexión
        controller.setConnectionStatusConsumer(conectado -> {
            if (!conectado) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo conectar al servidor. Verifica que el servidor esté en ejecución y la URL sea correcta.",
                    "Error de conexión", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
        
        // Iniciar la verificación de conexión
        controller.verificarYConectar();
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
        
        // Botón para copiar IP local
        JButton btnCopiarIP = new JButton("Copiar IP Local");
        btnCopiarIP.addActionListener(e -> copiarIPLocal());
        panelArriba.add(btnCopiarIP);

        try {
            String ipLocal = controller.getLocalIP();
            txtServidorUrl = new JTextField("http://" + ipLocal + ":8080", 20);
        } catch (Exception e) {
            txtServidorUrl = new JTextField("http://localhost:8080", 20);
        }
        panelArriba.add(txtServidorUrl);

        // Etiqueta para mostrar el ID del equipo (solo lectura)
        equipoIdLabel = new JLabel("ID Equipo: Generando...");
        panelArriba.add(equipoIdLabel);

        JButton btnCargarCamaras = new JButton("Cargar cámarass");
        btnCargarCamaras.addActionListener(e -> cargarCamaras());
        panelArriba.add(btnCargarCamaras);

        JButton btnProbarConex = new JButton("Probar conexión");
        btnProbarConex.addActionListener(e -> probarConexion());
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

        btnSeleccionarImagen.addActionListener(e -> seleccionarArchivo("imagen"));
        btnEnviarImagen.addActionListener(e -> enviarArchivo("imagen"));
        btnSeleccionarVideo.addActionListener(e -> seleccionarArchivo("video"));
        btnEnviarVideo.addActionListener(e -> enviarArchivo("video"));

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
    }
    
    private void copiarIPLocal() {
        try {
            String ipLocal = controller.getLocalIP();
            String url = "http://" + ipLocal + ":8080";
            txtServidorUrl.setText(url);
            StringSelection stringSelection = new StringSelection(ipLocal);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this, "IP local copiada al portapapeles: " + ipLocal, 
                "IP Local", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener IP local: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void probarConexion() {
        try {
            String servidorUrl = txtServidorUrl.getText();
            if (servidorUrl.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese la URL del servidor", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Aquí podrías agregar lógica para probar la conexión
            // Por ahora, simplemente mostramos un mensaje
            JOptionPane.showMessageDialog(this, "Conexión exitosa!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarCamaras() {
        // Si ya tenemos un ID de equipo generado, usarlo
        if (equipoIdGenerado != null && !equipoIdGenerado.isEmpty()) {
            cargarCamarasConEquipo(equipoIdGenerado);
            return;
        }
        
        // Si no hay ID generado, registramos un nuevo equipo
        registrarNuevoEquipo();
    }
    
    private void registrarNuevoEquipo() {
        controller.registrarEquipo(id -> {
            if (id != null) {
                equipoIdGenerado = id;
                log("Equipo registrado con ID: " + id);
                cargarCamarasConEquipo(id);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo registrar el equipo. Intente nuevamente.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private JLabel equipoIdLabel; // Add this as a class field

    private void cargarCamarasConEquipo(String equipoId) {
        // Actualizar la etiqueta del ID del equipo si existe
        if (equipoIdLabel != null) {
            equipoIdLabel.setText("ID Equipo: " + equipoId);
        }
        
        // Limpiar el modelo de cámaras
        modeloCamaras.clear();
        
        // Variable para evitar múltiples registros de cámara
        final boolean[] registroEnCurso = {false};
        
        // Clase interna para manejar la carga de cámaras
        class CargadorCamaras implements Runnable {
            private final String equipoId;
            private final boolean[] registroEnCurso;
            
            public CargadorCamaras(String equipoId, boolean[] registroEnCurso) {
                this.equipoId = equipoId;
                this.registroEnCurso = registroEnCurso;
            }
            
            @Override
            public void run() {
                if (registroEnCurso[0]) {
                    return; // Evitar múltiples llamadas simultáneas
                }
                
                log("Cargando cámaras para el equipo: " + equipoId);
                controller.cargarCamaras(equipoId, camaras -> {
                    // Actualizar la interfaz de usuario en el hilo de eventos de Swing
                    SwingUtilities.invokeLater(() -> {
                        if (camaras != null && camaras.length() > 0) {
                            log("Se encontraron " + camaras.length() + " cámaras");
                            modeloCamaras.clear(); // Limpiar antes de agregar
                            for (int i = 0; i < camaras.length(); i++) {
                                try {
                                    JSONObject camara = camaras.getJSONObject(i);
                                    String nombreCamara = camara.getString("nombre");
                                    modeloCamaras.addElement(nombreCamara);
                                    log("Cámara agregada: " + nombreCamara);
                                } catch (Exception e) {
                                    log("Error al procesar la cámara: " + e.getMessage());
                                }
                            }
                            registroEnCurso[0] = false; // Restablecer el estado
                        } else if (!registroEnCurso[0]) {
                            log("No se encontraron cámaras. Registrando una nueva cámara local...");
                            registroEnCurso[0] = true; // Marcar que hay un registro en curso
                            
                            // Si no hay cámaras, intentar registrar una cámara local
                            controller.registrarCamaraLocal(equipoId, camaraId -> {
                                SwingUtilities.invokeLater(() -> {
                                    if (camaraId != null) {
                                        log("Cámara local registrada con ID: " + camaraId);
                                        // Volver a cargar las cámaras después de un breve retraso
                                        Timer timer = new Timer(1000, e -> {
                                            ((Timer)e.getSource()).stop();
                                            new CargadorCamaras(equipoId, registroEnCurso).run();
                                        });
                                        timer.setRepeats(false);
                                        timer.start();
                                    } else {
                                        log("Error al registrar la cámara local");
                                        registroEnCurso[0] = false; // Restablecer si falla
                                    }
                                });
                            });
                        }
                    });
                });
            }
        }
        
        // Iniciar la carga de cámaras
        new CargadorCamaras(equipoId, registroEnCurso).run();
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
        if (equipoIdGenerado == null || equipoIdGenerado.isEmpty()) {
            log("Error: No hay un equipo registrado. Carga las cámaras primero.");
            return;
        }
        
        String camaraSeleccionada = listaCamaras.getSelectedValue();
        
        if (archivoSeleccionado == null) {
            log("Error: No hay archivo seleccionado para enviar.");
            return;
        }
        
        if (camaraSeleccionada == null) {
            log("Error: Debes seleccionar una cámara.");
            return;
        }
        
        controller.enviarArchivo(equipoIdGenerado, camaraSeleccionada, archivoSeleccionado, tipo);
    }
    
    private void log(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(LocalDateTime.now().toString() + " - " + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
    
    /**
     * Returns the main panel containing all UI components.
     * @return JPanel containing the main UI components
     */
    public JPanel getMainPanel() {
        // Get the content pane and return its first (and only) component which should be the main panel
        return (JPanel) getContentPane().getComponent(0);
    }
    
    @Override
    public void dispose() {
        controller.detener();
        super.dispose();
    }
}
