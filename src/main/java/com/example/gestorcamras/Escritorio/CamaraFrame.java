package com.example.gestorcamras.Escritorio;

import javax.swing.*;
import java.awt.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

/**
 * Interfaz de usuario para la cámara web.
 */
public class CamaraFrame extends JFrame {
    private ManejadorCamara manejadorCamara;
    private JLabel etiquetaVistaPrevia;
    private JButton btnTomarFoto;
    private JButton btnGrabarVideo;
    private JLabel etiquetaEstado;
    private Timer timerActualizacion;
    private boolean grabando = false;
    private final Consumer<String> onArchivoGuardadoListener;

    /**
     * Crea una nueva instancia de CamaraFrame.
     * @param onArchivoGuardadoListener Callback que se ejecutará cuando se guarde un archivo (foto o video).
     *                                 Recibe como parámetro la ruta del archivo guardado.
     */
    public CamaraFrame(Consumer<String> onArchivoGuardadoListener) {
        this.onArchivoGuardadoListener = onArchivoGuardadoListener;
        
        setTitle("Cámara Web");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        // Configurar componentes
        etiquetaVistaPrevia = new JLabel("Inicializando cámara...", JLabel.CENTER);
        etiquetaVistaPrevia.setPreferredSize(new Dimension(640, 480));
        etiquetaVistaPrevia.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnTomarFoto = new JButton("Tomar Foto");
        btnGrabarVideo = new JButton("Iniciar Grabación");
        etiquetaEstado = new JLabel("Listo");
        
        panelBotones.add(btnTomarFoto);
        panelBotones.add(btnGrabarVideo);
        panelBotones.add(etiquetaEstado);
        
        // Agregar componentes al frame
        add(etiquetaVistaPrevia, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar manejador de eventos
        configurarEventos();
        
        // Inicializar la cámara en un hilo separado para no bloquear la interfaz
        new Thread(this::inicializarCamara).start();
        
        // Configurar el temporizador para actualizar la vista previa
        timerActualizacion = new Timer(33, e -> actualizarVistaPrevia()); // ~30 FPS
        timerActualizacion.start();
        
        // Manejar el cierre de la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                detenerCamara();
                dispose();
            }
        });
    }
    
    private void configurarEventos() {
        // Evento para tomar foto
        btnTomarFoto.addActionListener(e -> tomarFoto());
        
        // Evento para grabar video
        btnGrabarVideo.addActionListener(e -> {
            if (!grabando) {
                iniciarGrabacion();
            } else {
                detenerGrabacion();
            }
        });
    }
    
    private void inicializarCamara() {
        try {
            // Actualizar la interfaz en el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                etiquetaEstado.setText("Inicializando cámara...");
                btnTomarFoto.setEnabled(false);
                btnGrabarVideo.setEnabled(false);
            });
            
            // Inicializar el manejador de la cámara con el listener de archivo guardado
            manejadorCamara = new ManejadorCamara(etiquetaVistaPrevia, rutaArchivo -> {
                // Notificar al listener de la ventana cuando se guarde un archivo
                if (onArchivoGuardadoListener != null) {
                    onArchivoGuardadoListener.accept(rutaArchivo);
                }
            });
            
            // Actualizar la interfaz
            SwingUtilities.invokeLater(() -> {
                etiquetaEstado.setText("Cámara lista");
                btnTomarFoto.setEnabled(true);
                btnGrabarVideo.setEnabled(true);
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                etiquetaEstado.setText("Error: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "No se pudo inicializar la cámara: " + e.getMessage(),
                    "Error de Cámara", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    private void actualizarVistaPrevia() {
        if (manejadorCamara != null) {
            manejadorCamara.actualizarVistaPrevia();
        }
    }
    
    private void tomarFoto() {
        if (manejadorCamara == null) return;
        
        // Deshabilitar botones temporalmente
        btnTomarFoto.setEnabled(false);
        etiquetaEstado.setText("Tomando foto...");
        
        // Ejecutar en un hilo separado para no bloquear la interfaz
        new Thread(() -> {
            try {
                String rutaFoto = manejadorCamara.tomarFoto();
                
                SwingUtilities.invokeLater(() -> {
                    if (rutaFoto != null) {
                        etiquetaEstado.setText("Foto guardada: " + rutaFoto);
                        // No mostrar mensaje de confirmación de guardado local
                        // Solo se mostrará el mensaje de envío al servidor
                    }
                    btnTomarFoto.setEnabled(true);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    etiquetaEstado.setText("Error al tomar foto");
                    JOptionPane.showMessageDialog(this,
                        "Error al tomar la foto: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    btnTomarFoto.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void iniciarGrabacion() {
        if (manejadorCamara == null) return;
        
        // Deshabilitar botones temporalmente
        btnTomarFoto.setEnabled(false);
        btnGrabarVideo.setEnabled(false);
        etiquetaEstado.setText("Iniciando grabación...");
        
        // Ejecutar en un hilo separado para no bloquear la interfaz
        new Thread(() -> {
            try {
                boolean exito = manejadorCamara.iniciarGrabacion();
                
                SwingUtilities.invokeLater(() -> {
                    if (exito) {
                        grabando = true;
                        btnGrabarVideo.setText("Detener Grabación");
                        etiquetaEstado.setText("Grabando...");
                        btnGrabarVideo.setBackground(new Color(255, 100, 100)); // Rojo claro
                    } else {
                        etiquetaEstado.setText("No se pudo iniciar la grabación");
                    }
                    btnTomarFoto.setEnabled(false); // Deshabilitar mientras se graba
                    btnGrabarVideo.setEnabled(true);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    etiquetaEstado.setText("Error al iniciar grabación");
                    JOptionPane.showMessageDialog(this,
                        "Error al iniciar la grabación: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    btnTomarFoto.setEnabled(true);
                    btnGrabarVideo.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void detenerGrabacion() {
        if (manejadorCamara == null || !grabando) return;
        
        // Deshabilitar botón temporalmente
        btnGrabarVideo.setEnabled(false);
        etiquetaEstado.setText("Deteniendo grabación...");
        
        // Ejecutar en un hilo separado para no bloquear la interfaz
        new Thread(() -> {
            try {
                String rutaVideo = manejadorCamara.detenerGrabacion();
                
                SwingUtilities.invokeLater(() -> {
                    grabando = false;
                    btnGrabarVideo.setText("Iniciar Grabación");
                    btnGrabarVideo.setBackground(UIManager.getColor("Button.background"));
                    
                    if (rutaVideo != null) {
                        etiquetaEstado.setText("Grabación guardada: " + rutaVideo);
                        // No mostrar mensaje de confirmación de guardado local
                        // Solo se mostrará el mensaje de envío al servidor
                    } else {
                        etiquetaEstado.setText("Listo");
                    }
                    
                    btnTomarFoto.setEnabled(true);
                    btnGrabarVideo.setEnabled(true);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    grabando = false;
                    etiquetaEstado.setText("Error al detener grabación");
                    JOptionPane.showMessageDialog(this,
                        "Error al detener la grabación: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    btnTomarFoto.setEnabled(true);
                    btnGrabarVideo.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void detenerCamara() {
        // Detener el temporizador
        if (timerActualizacion != null && timerActualizacion.isRunning()) {
            timerActualizacion.stop();
        }
        
        // Detener la grabación si está en curso
        if (grabando) {
            detenerGrabacion();
        }
        
        // Liberar recursos de la cámara
        if (manejadorCamara != null) {
            manejadorCamara.liberarRecursos();
            manejadorCamara = null;
        }
    }
    
    public static void main(String[] args) {
        // Configurar el Look and Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear una instancia con un listener nulo para el método main
            CamaraFrame frame = new CamaraFrame(rutaArchivo -> {
                System.out.println("Archivo guardado: " + rutaArchivo);
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}