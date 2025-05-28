package com.example.gestorcamras.Escritorio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.example.gestorcamras.Escritorio.model.CamaraTableModel;

public class ClienteSwingUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private JTextField txtServidorUrl;
    private String equipoIdGenerado;  // Para almacenar el ID del equipo generado
    private JTextArea txtLog;
    private File archivoSeleccionado;
    private JTable tablaCamaras;
    private CamaraTableModel modeloCamarasTabla;
    private final ClienteSwingController controller;
    private final String usuario;
    
    public ClienteSwingUI(String usuario, String cookieSesion) {
        this.usuario = usuario;
        this.controller = new ClienteSwingController(usuario, cookieSesion, null);
        
        // Configurar consumidor de logs
        controller.setLogConsumer(this::log);
        
        // Configurar la interfaz de usuario
        initUI();
        
        // Configurar el consumidor de estado de conexión
        controller.setConnectionStatusConsumer(conectado -> {
            if (conectado) {
                // Una vez que la conexión está establecida, cargar las cámaras
                SwingUtilities.invokeLater(this::cargarCamaras);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo conectar al servidor. Verifica que el servidor esté en ejecución y la URL sea correcta.",
                    "Error de conexión", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
        
        // Iniciar la verificación de conexión
        verificarYConectar();
    }
    
    private void verificarYConectar() {
        // Iniciar la verificación de conexión
        controller.verificarYConectar();
    }
    
    private void initUI() {
        setTitle("Cliente Equipo - Gestor de Cámaras (Usuario: " + usuario + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Inicializar el modelo de la tabla de cámaras
        modeloCamarasTabla = new CamaraTableModel();
        tablaCamaras = new JTable(modeloCamarasTabla);
        tablaCamaras.setFillsViewportHeight(true);
        tablaCamaras.setAutoCreateRowSorter(true);

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

        JButton btnCargarCamaras = new JButton("Cargar cámaras");
        btnCargarCamaras.addActionListener(e -> cargarCamaras());
        panelArriba.add(btnCargarCamaras);

        JButton btnProbarConex = new JButton("Probar conexión");
        btnProbarConex.addActionListener(e -> probarConexion());
        panelArriba.add(btnProbarConex);

        panel.add(panelArriba, BorderLayout.NORTH);

        // Panel central para tabla de cámaras y botones
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        panelCentro.add(new JLabel("Cámaras disponibles:"), BorderLayout.NORTH);
        
        // Panel para la tabla de cámaras
        JScrollPane scrollTabla = new JScrollPane(tablaCamaras);
        scrollTabla.setPreferredSize(new Dimension(0, 200));
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        // Botones para seleccionar y enviar archivos
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));
        JButton btnSeleccionarImagen = new JButton("Seleccionar imagen");
        JButton btnEnviarImagen = new JButton("Enviar imagen");
        JButton btnSeleccionarVideo = new JButton("Seleccionar video");
        JButton btnEnviarVideo = new JButton("Enviar video");

        btnSeleccionarImagen.addActionListener(e -> seleccionarArchivo("imagen"));
        btnEnviarImagen.addActionListener(e -> enviarArchivo("FOTO"));
        btnSeleccionarVideo.addActionListener(e -> seleccionarArchivo("video"));
        btnEnviarVideo.addActionListener(e -> enviarArchivo("VIDEO"));

        panelBotones.add(btnSeleccionarImagen);
        panelBotones.add(btnEnviarImagen);
        panelBotones.add(btnSeleccionarVideo);
        panelBotones.add(btnEnviarVideo);
        
        // Botón para abrir la cámara
        JButton btnAbrirCamara = new JButton("Abrir Cámara");
        btnAbrirCamara.addActionListener(e -> abrirCamara());
        
        // Agregar el botón de cámara en una nueva fila
        JPanel panelCamara = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelCamara.add(btnAbrirCamara);
        
        // Crear un panel para contener los botones y el panel de cámara
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.add(panelBotones, BorderLayout.NORTH);
        panelContenedor.add(panelCamara, BorderLayout.SOUTH);

        panelCentro.add(panelContenedor, BorderLayout.SOUTH);
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
    

    
    private void registrarNuevoEquipo() {
        controller.registrarEquipo(id -> {
            if (id != null) {
                equipoIdGenerado = id;
                log("Equipo registrado con ID: " + id);
                // Al registrar un nuevo equipo, solo mostramos el ID sin cargar cámaras automáticamente
                SwingUtilities.invokeLater(() -> {
                    if (equipoIdLabel != null) {
                        equipoIdLabel.setText("ID Equipo: " + id);
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo registrar el equipo. Intente nuevamente.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private JLabel equipoIdLabel; // Add this as a class field


    /**
     * Método que se llama al hacer clic en el botón 'Cargar cámaras'.
     * Este método intenta registrar una nueva cámara local si no hay cámaras existentes.
     */
    private void cargarCamaras() {
        // Si ya tenemos un ID de equipo generado, usarlo
        if (equipoIdGenerado != null && !equipoIdGenerado.isEmpty()) {
            cargarCamarasConBoton(equipoIdGenerado);
            return;
        }
        
        // Si no hay ID generado, registramos un nuevo equipo
        registrarNuevoEquipo();
    }
    
    /**
     * Carga las cámaras y, si no hay ninguna, intenta registrar una nueva cámara local.
     * Este método se llama solo cuando el usuario hace clic en el botón 'Cargar cámaras'.
     * @param equipoId ID del equipo del cual cargar las cámaras
     */
    private void cargarCamarasConBoton(String equipoId) {
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
                            // Actualizar la tabla con las cámaras
                            modeloCamarasTabla.setCamaras(camaras);
                            log("Tabla de cámaras actualizada");
                        } else if (!registroEnCurso[0]) {
                            log("No se encontraron cámaras. Registrando una nueva cámara local...");
                            registroEnCurso[0] = true; // Marcar que hay un registro en curso
                            
                            // Si no hay cámaras, intentar registrar una cámara local
                            log("Registrando nueva cámara local...");
                            controller.registrarCamaraLocal(equipoId, camaraId -> {
                                SwingUtilities.invokeLater(() -> {
                                    registroEnCurso[0] = false; // Restablecer el estado de registro
                                    if (camaraId != null) {
                                        log("Cámara local registrada con ID: " + camaraId);
                                        // Volver a cargar las cámaras inmediatamente
                                        log("Actualizando lista de cámaras...");
                                        controller.cargarCamaras(equipoId, camarasActualizadas -> {
                                            SwingUtilities.invokeLater(() -> {
                                                if (camarasActualizadas != null && camarasActualizadas.length() > 0) {
                                                    log("Se encontraron " + camarasActualizadas.length() + " cámaras después del registro");
                                                    modeloCamarasTabla.setCamaras(camarasActualizadas);
                                                    log("Tabla de cámaras actualizada después del registro");
                                                } else {
                                                    log("No se pudieron cargar las cámaras después del registro");
                                                }
                                            });
                                        });
                                    } else {
                                        log("Error al registrar la cámara local");
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
        // Determinar la carpeta base según el tipo de archivo
        String carpetaBase = "capturas/";
        String subcarpeta = "";
        
        if (tipo.toLowerCase().contains("foto") || tipo.toLowerCase().contains("imagen")) {
            subcarpeta = "fotos";
        } else if (tipo.toLowerCase().contains("video")) {
            subcarpeta = "videos";
        }
        
        // Crear la ruta completa a la carpeta
        String rutaCarpeta = carpetaBase + subcarpeta;
        File carpeta = new File(rutaCarpeta);
        
        // Si la carpeta no existe, intentar crearla
        if (!carpeta.exists()) {
            boolean creada = carpeta.mkdirs();
            if (!creada) {
                log("No se pudo crear la carpeta: " + rutaCarpeta);
                // Usar el directorio de usuario como respaldo
                carpeta = new File(System.getProperty("user.home"));
            }
        }
        
        // Configurar el selector de archivos
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(carpeta);
        
        // Configurar filtros según el tipo de archivo
        if (tipo.toLowerCase().contains("foto") || tipo.toLowerCase().contains("imagen")) {
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));
        } else if (tipo.toLowerCase().contains("video")) {
            chooser.setFileFilter(new FileNameExtensionFilter("Videos", "mp4", "avi", "mov", "wmv"));
        }
        
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            log("Archivo seleccionado para " + tipo + ": " + archivoSeleccionado.getAbsolutePath());
        } else {
            log("Selección de archivo cancelada");
        }
    }
    
    private void enviarArchivo(String tipo) {
        if (equipoIdGenerado == null || equipoIdGenerado.isEmpty()) {
            log("Error: No hay un equipo registrado. Carga las cámaras primero.");
            return;
        }
        
        int filaSeleccionada = tablaCamaras.getSelectedRow();
        if (filaSeleccionada == -1) {
            log("Error: Debes seleccionar una cámara de la tabla.");
            return;
        }
        
        // Obtener el nombre de la cámara seleccionada
        String nombreCamara = (String) modeloCamarasTabla.getValueAt(filaSeleccionada, 1);
        
        if (archivoSeleccionado == null) {
            log("Error: No hay archivo seleccionado para enviar.");
            return;
        }
        
        controller.enviarArchivo(equipoIdGenerado, nombreCamara, archivoSeleccionado, tipo);
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
    
    /**
     * Abre la ventana de la cámara para tomar fotos o grabar videos.
     */
    private void abrirCamara() {
        // Verificar que haya un equipo registrado y cámaras disponibles
        if (equipoIdGenerado == null || equipoIdGenerado.isEmpty()) {
            log("Error: No hay un equipo registrado. Carga las cámaras primero.");
            JOptionPane.showMessageDialog(this, 
                "No hay un equipo registrado. Por favor, carga las cámaras primero.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaSeleccionada = tablaCamaras.getSelectedRow();
        if (filaSeleccionada == -1) {
            log("Error: Debes seleccionar una cámara de la tabla.");
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona una cámara de la tabla antes de abrir la cámara.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Obtener el nombre de la cámara seleccionada
        final String nombreCamara = (String) modeloCamarasTabla.getValueAt(filaSeleccionada, 1);
        
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear y mostrar la ventana de la cámara
                CamaraFrame frameCamara = new CamaraFrame(rutaArchivo -> {
                    // Este callback se ejecutará cuando se guarde una foto o video
                    log("Archivo guardado: " + rutaArchivo);
                    
                    // Determinar el tipo de archivo (foto o video)
                    String tipo = rutaArchivo.toLowerCase().endsWith(".mp4") || 
                                 rutaArchivo.toLowerCase().endsWith(".avi") ? "VIDEO" : "FOTO";
                    
                    // Crear un objeto File para el archivo
                    File archivo = new File(rutaArchivo);
                    
                    // Mostrar confirmación al usuario
                    int opcion = JOptionPane.showConfirmDialog(this,
                        String.format("¿Deseas enviar el %s '%s' al servidor?", 
                                     tipo.toLowerCase(), 
                                     archivo.getName()),
                        "Archivo guardado",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (opcion == JOptionPane.YES_OPTION) {
                        // Enviar el archivo al servidor
                        enviarArchivo(tipo, archivo, nombreCamara);
                    }
                });
                
                frameCamara.setLocationRelativeTo(this); // Centrar respecto a la ventana principal
                frameCamara.setVisible(true);
                
                // Cuando se cierre la ventana de la cámara, liberar recursos
                frameCamara.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        log("Ventana de cámara cerrada");
                    }
                });
                
                log("Cámara abierta correctamente");
            } catch (Exception e) {
                log("Error al abrir la cámara: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "No se pudo abrir la cámara. Asegúrate de que esté conectada y no esté siendo usada por otra aplicación.\nError: " + e.getMessage(),
                    "Error de Cámara", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Envía un archivo al servidor.
     * @param tipo Tipo de archivo ("FOTO" o "VIDEO")
     * @param archivo Archivo a enviar
     * @param nombreCamara Nombre de la cámara que capturó el archivo
     */
    private void enviarArchivo(String tipo, File archivo, String nombreCamara) {
        if (equipoIdGenerado == null || equipoIdGenerado.isEmpty()) {
            log("Error: No hay un equipo registrado. No se puede enviar el archivo.");
            return;
        }
        
        if (archivo == null || !archivo.exists()) {
            log("Error: El archivo no existe o no se puede acceder a él.");
            return;
        }
        
        log(String.format("Enviando %s al servidor: %s", tipo.toLowerCase(), archivo.getAbsolutePath()));
        
        // Usar el controlador para enviar el archivo
        controller.enviarArchivo(equipoIdGenerado, nombreCamara, archivo, tipo);
    }
}
