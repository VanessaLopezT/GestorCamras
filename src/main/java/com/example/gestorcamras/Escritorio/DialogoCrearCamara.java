package com.example.gestorcamras.Escritorio;

import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DialogoCrearCamara extends JDialog {
    private JTextField txtLatitud;
    private JTextField txtLongitud;
    private JTextArea txtDireccion;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private long equipoId;
    private ServidorUI servidorUI;
    private static final String SERVER_URL = "http://localhost:8080/api";
    private String equipoNombre;

    public DialogoCrearCamara(Frame owner, long equipoId, String equipoNombre, ServidorUI servidorUI) {
        super(owner, "Agregar Nueva Cámara al Equipo: " + equipoNombre, true);
        this.equipoId = equipoId;
        this.equipoNombre = equipoNombre;
        this.servidorUI = servidorUI;
        inicializarComponentes();
        configurarVentana();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel de formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Título informativo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel("<html><b>Ingrese la ubicación de la nueva cámara:</b></html>");
        panelFormulario.add(lblInfo, gbc);
        
        // Espaciador
        gbc.gridy++;
        panelFormulario.add(Box.createVerticalStrut(10), gbc);
        
        // Latitud
        gbc.gridwidth = 1;
        gbc.gridy++;
        panelFormulario.add(new JLabel("Latitud:"), gbc);
        
        gbc.gridx = 1;
        txtLatitud = new JTextField("4.1420");
        panelFormulario.add(txtLatitud, gbc);
        
        // Longitud
        gbc.gridx = 0;
        gbc.gridy++;
        panelFormulario.add(new JLabel("Longitud:"), gbc);
        
        gbc.gridx = 1;
        txtLongitud = new JTextField("-73.6266");
        panelFormulario.add(txtLongitud, gbc);
        
        // Dirección
        gbc.gridx = 0;
        gbc.gridy++;
        panelFormulario.add(new JLabel("Dirección:"), gbc);
        
        gbc.gridx = 1;
        txtDireccion = new JTextArea("Vereda Barcelona, Villavicencio, Meta, Colombia", 3, 20);
        txtDireccion.setLineWrap(true);
        txtDireccion.setWrapStyleWord(true);
        panelFormulario.add(new JScrollPane(txtDireccion), gbc);
        
        add(panelFormulario, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarCamara());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private void configurarVentana() {
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }
    
    private boolean validarUbicacion() {
        // Validar que al menos se proporcione dirección o coordenadas
        if (txtDireccion.getText().trim().isEmpty() && 
            txtLatitud.getText().trim().isEmpty() && 
            txtLongitud.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Debe proporcionar al menos una ubicación (coordenadas o dirección).", 
                "Ubicación requerida", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validar formato de coordenadas si se proporcionaron
        try {
            if (!txtLatitud.getText().trim().isEmpty()) {
                double latitud = Double.parseDouble(txtLatitud.getText().trim());
                if (latitud < -90 || latitud > 90) {
                    JOptionPane.showMessageDialog(this, 
                        "La latitud debe estar entre -90 y 90 grados.", 
                        "Error de formato", 
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            
            if (!txtLongitud.getText().trim().isEmpty()) {
                double longitud = Double.parseDouble(txtLongitud.getText().trim());
                if (longitud < -180 || longitud > 180) {
                    JOptionPane.showMessageDialog(this, 
                        "La longitud debe estar entre -180 y 180 grados.", 
                        "Error de formato", 
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            
            // Si se proporciona una coordenada, la otra también debe estar presente
            if ((!txtLatitud.getText().trim().isEmpty() && txtLongitud.getText().trim().isEmpty()) ||
                (txtLatitud.getText().trim().isEmpty() && !txtLongitud.getText().trim().isEmpty())) {
                JOptionPane.showMessageDialog(this, 
                    "Debe proporcionar tanto latitud como longitud.", 
                    "Coordenadas incompletas", 
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Las coordenadas deben ser valores numéricos válidos.", 
                "Error de formato", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private boolean validarFormatoIP(String ip) {
        // Implementación básica de validación de IP
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }
    
    private void guardarCamara() {
        // Validar la ubicación antes de continuar
        if (!validarUbicacion()) {
            return;
        }
        
        // Mostrar diálogo de confirmación
        int confirmacion = JOptionPane.showConfirmDialog(this, 
            "¿Desea crear la cámara con la siguiente configuración?\n\n" +
            "Equipo: " + equipoNombre + "\n" +
            "Latitud: " + (txtLatitud.getText().trim().isEmpty() ? "No especificada" : txtLatitud.getText().trim()) + "\n" +
            "Longitud: " + (txtLongitud.getText().trim().isEmpty() ? "No especificada" : txtLongitud.getText().trim()) + "\n" +
            "Dirección: " + (txtDireccion.getText().trim().isEmpty() ? "No especificada" : txtDireccion.getText().trim()),
            "Confirmar creación de cámara",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            return; // El usuario canceló la operación
        }
        
        // Mostrar indicador de carga
        JDialog loadingDialog = new JDialog(this, "Creando cámara...", true);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.add(new JLabel("Creando cámara, por favor espere...", JLabel.CENTER), BorderLayout.CENTER);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Iniciar la operación en segundo plano
        new Thread(() -> {
            try {
                // Mostrar el diálogo de carga
                SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
                
                // Obtener el token JWT del sistema (opcional)
                String jwtToken = System.getProperty("jwt.token");
                
                // Configurar el cliente HTTP
                HttpClient client = HttpClient.newHttpClient();
                
                // Crear objeto JSON con los datos de la cámara
                JSONObject camaraJson = new JSONObject();
                
                // Generar nombre automático con formato corto y marca de tiempo
                String timestamp = String.valueOf(System.currentTimeMillis());
                String nombreCamara = "Cámara_Local_" + timestamp;
                
                // Configurar valores por defecto
                camaraJson.put("nombre", nombreCamara);
                camaraJson.put("ip", "127.0.0.1"); // IP local por defecto
                camaraJson.put("tipo", "IP"); // Tipo genérico de cámara
                camaraJson.put("activa", true); // Activa por defecto
                
                // Agregar coordenadas si se proporcionaron
                if (!txtLatitud.getText().trim().isEmpty()) {
                    camaraJson.put("latitud", Double.parseDouble(txtLatitud.getText().trim()));
                }
                if (!txtLongitud.getText().trim().isEmpty()) {
                    camaraJson.put("longitud", Double.parseDouble(txtLongitud.getText().trim()));
                }
                
                // Agregar dirección si se proporcionó
                if (!txtDireccion.getText().trim().isEmpty()) {
                    camaraJson.put("direccion", txtDireccion.getText().trim());
                }
                
                // Agregar ID del equipo
                camaraJson.put("equipoId", equipoId);
                
                // Crear la solicitud HTTP
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/camaras"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(camaraJson.toString()));
                    
                // Agregar el token de autorización si está disponible
                if (jwtToken != null && !jwtToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + jwtToken);
                }
                
                HttpRequest request = requestBuilder.build();
                
                // Enviar la solicitud
                HttpResponse<String> response = client.send(
                    request, 
                    HttpResponse.BodyHandlers.ofString()
                );
                
                // Cerrar el diálogo de carga
                SwingUtilities.invokeLater(loadingDialog::dispose);
                
                // Considerar tanto 200 como 201 como códigos de éxito
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    // Cámara creada exitosamente
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            DialogoCrearCamara.this, 
                            "Cámara creada exitosamente.", 
                            "Éxito", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // Actualizar la lista de cámaras en la interfaz principal
                        if (servidorUI != null) {
                            // Agregar un pequeño retraso para asegurar que el servidor haya terminado de procesar
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1000); // Esperar 1 segundo
                                    SwingUtilities.invokeLater(() -> {
                                        servidorUI.cargarCamaras(equipoId);
                                        // Cerrar el diálogo después de iniciar la carga
                                        dispose();
                                    });
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    SwingUtilities.invokeLater(() -> {
                                        servidorUI.cargarCamaras(equipoId);
                                        dispose();
                                    });
                                }
                            }).start();
                        } else {
                            dispose();
                        }
                    });
                } else {
                    // Crear mensaje de error final
                    final String errorMessage = "Error al crear la cámara. Código: " + response.statusCode() + 
                        (response.body() != null && !response.body().isEmpty() ? "\n" + response.body() : "");
                    
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(
                            DialogoCrearCamara.this, 
                            errorMessage, 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        )
                    );
                }
            } catch (Exception ex) {
                // Cerrar el diálogo de carga en caso de error
                SwingUtilities.invokeLater(loadingDialog::dispose);
                
                // Mostrar mensaje de error
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(
                        DialogoCrearCamara.this, 
                        "Error al conectar con el servidor: " + ex.getMessage(), 
                        "Error de conexión", 
                        JOptionPane.ERROR_MESSAGE
                    )
                );
            }
        }).start();
    }
}
