package com.example.gestorcamras.Escritorio;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField tfUsuario;
    private JPasswordField pfClave;
    private JButton btnLogin;
    private JLabel lbEstado;

    public LoginFrame() {
        setTitle("Login - Solo Operadores");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsuario = new JLabel("Usuario:");
        gbc.gridx = 0; gbc.gridy = 0;
        add(lbUsuario, gbc);

        tfUsuario = new JTextField(20);
        gbc.gridx = 1; gbc.gridy =0;
        add(tfUsuario, gbc);
        JLabel lbClave = new JLabel("Contraseña:");
        gbc.gridx = 0; gbc.gridy = 1;
        add(lbClave, gbc);

        pfClave = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(pfClave, gbc);

        btnLogin = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(btnLogin, gbc);

        JButton btnDefault = new JButton("Iniciar con Operador por Defecto");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(btnDefault, gbc);

        lbEstado = new JLabel("");
        lbEstado.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(lbEstado, gbc);

        btnLogin.addActionListener(e -> login());
        btnDefault.addActionListener(e -> loginUsuarioDefault());

        // Enter para login
        pfClave.addActionListener(e -> login());
    }

    private void loginUsuarioDefault() {
        // Actualizar la UI en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            try {
                // Limpiar cualquier mensaje de error previo
                lbEstado.setText("");
                
                // Establecer las credenciales por defecto
                tfUsuario.setText("oper@gestor.com");
                pfClave.setText("oper123");
                
                // Forzar la actualización de la UI
                tfUsuario.repaint();
                pfClave.repaint();
                
                // Usar un temporizador para iniciar sesión después de que la UI se actualice
                javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
                    login();
                });
                timer.setRepeats(false);
                timer.start();
            } catch (Exception ex) {
                lbEstado.setText("Error al configurar credenciales: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void login() {
        String usuario = tfUsuario.getText().trim();
        String password = new String(pfClave.getPassword());
        
        // Obtener la configuración del servidor desde las propiedades del sistema
        String host = System.getProperty("gestorcamras.server.host", "localhost");
        String port = System.getProperty("gestorcamras.server.port", "8080");
        String servidorUrl = "http://" + host + ":" + port;

        if (usuario.isEmpty() || password.isEmpty()) {
            lbEstado.setText("Por favor, complete todos los campos.");
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String params = "username=" + URLEncoder.encode(usuario, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            System.out.println("Código de respuesta del servidor: " + code);
            System.out.println("Headers de respuesta: " + response.headers().map());
            
            if (code == 302 || code == 200) {
                // Obtener la cookie de sesión
                String headerCookies = response.headers().firstValue("Set-Cookie").orElse(null);
                if (headerCookies != null) {
                    String sessionCookie = headerCookies.split(";")[0];
                    System.out.println("Session Cookie: " + sessionCookie);
                    
                    // Verificar rol del usuario
                    try {
                        HttpRequest requestUser = HttpRequest.newBuilder()
                                .uri(URI.create(servidorUrl + "/api/usuario/actual"))
                                .header("Cookie", sessionCookie)
                                .GET()
                                .build();

                        System.out.println("Solicitando información del usuario...");
                        HttpResponse<String> responseUser = client.send(requestUser, HttpResponse.BodyHandlers.ofString());
                        int responseCode = responseUser.statusCode();
                        System.out.println("Código de respuesta de /api/usuario/actual: " + responseCode);
                        System.out.println("Cuerpo de la respuesta: " + responseUser.body());
                        
                        if (responseCode == 200) {
                            String jsonResponse = responseUser.body();
                            System.out.println("Respuesta del servidor: " + jsonResponse);
                            JSONObject obj = new JSONObject(jsonResponse);
                            String nombreRol = obj.getString("nombreRol");
                            System.out.println("Nombre del rol: " + nombreRol);

                            if (nombreRol.equalsIgnoreCase("OPERADOR")) {
                                SwingUtilities.invokeLater(() -> {
                                    ClienteSwingUI cliente = new ClienteSwingUI(usuario, sessionCookie);
                                    cliente.setVisible(true);
                                    dispose();
                                });
                                return; // Salir del método después de iniciar sesión exitosamente
                            } else {
                                lbEstado.setText("No autorizado: usuario no es OPERADOR. Rol actual: " + nombreRol);
                            }
                        } else {
                            lbEstado.setText("Error al obtener información del usuario. Código: " + responseCode);
                            String errorBody = responseUser.body();
                            if (errorBody != null && !errorBody.isEmpty()) {
                                System.out.println("Error del servidor: " + errorBody);
                                lbEstado.setText("Error: " + errorBody);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Error al obtener información del usuario: " + ex.getMessage());
                        ex.printStackTrace();
                        lbEstado.setText("Error al obtener información del usuario: " + ex.getMessage());
                    }
                } else {
                    lbEstado.setText("Error: No se recibió cookie de sesión");
                }
            } else {
                lbEstado.setText("Credenciales inválidas.");
            }
        } catch (Exception e) {
            lbEstado.setText("Error al iniciar sesión: " + e.getMessage());
        } finally {
            // Asegurarse de que el mensaje de error se muestre
            if (lbEstado.getText().isEmpty()) {
                lbEstado.setText("Error desconocido al iniciar sesión");
            }
        }
    }
}