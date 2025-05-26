package com.example.gestorcamras.Escritorio;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
    
    // Constantes para la configuración
    private static final int DEFAULT_PORT = 8080;
    private String serverIp = "127.0.0.1"; // Valor por defecto

    /**
     * Obtiene la dirección IP local de la máquina
     * @return La dirección IP local o null si no se pudo determinar
     */
    private String getLocalIpAddress() {
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
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Muestra un diálogo para que el usuario confirme o modifique la IP del servidor
     * @param defaultIp La IP por defecto a mostrar
     * @return La IP ingresada por el usuario o null si se canceló
     */
    private String showServerIpDialog(String defaultIp) {
        String ip = (String) JOptionPane.showInputDialog(
            null,
            "Ingrese la dirección IP del servidor:",
            "Configuración de conexión",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            defaultIp
        );
        
        if (ip != null) {
            ip = ip.trim();
            if (ip.isEmpty()) {
                ip = defaultIp;
            }
        }
        return ip;
    }
    
    public LoginFrame() {
        // Obtener la IP local al iniciar
        String localIp = getLocalIpAddress();
        if (localIp != null) {
            serverIp = localIp;
            System.out.println("IP local detectada: " + serverIp);
        }
        setTitle("Login - Gestor de Cámaras");
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

    private boolean isServerReachable(String url) {
        try {
            System.out.println("Verificando conexión con el servidor: " + url);
            // Crear un cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
                
            // Crear la solicitud
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/equipos")) // Endpoint que siempre debería existir
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            
            // Enviar la solicitud y obtener la respuesta
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Código de respuesta del servidor: " + response.statusCode());
                return response.statusCode() < 400; // Cualquier código de éxito (2xx, 3xx)
            } catch (Exception e) {
                System.err.println("Error al conectar con el servidor: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al crear la solicitud HTTP: " + e.getMessage());
            return false;
        }
    }

    private void loginUsuarioDefault() {
        // Mostrar mensaje de estado
        lbEstado.setText("Iniciando con usuario por defecto...");
        
        // Actualizar la UI en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            try {
                // Establecer las credenciales por defecto
                tfUsuario.setText("oper@gestor.com");
                pfClave.setText("oper123");
                
                System.out.println("Credenciales establecidas. Iniciando sesión...");
                
                // Llamar directamente a login después de un pequeño retraso
                // para asegurar que la UI se actualice
                new Thread(() -> {
                    try {
                        // Pequeña pausa para asegurar que la UI se actualice
                        Thread.sleep(100);
                        // Ejecutar el login en el hilo de eventos
                        SwingUtilities.invokeLater(this::login);
                    } catch (Exception e) {
                        e.printStackTrace();
                        lbEstado.setText("Error al iniciar sesión: " + e.getMessage());
                    }
                }).start();
                
            } catch (Exception ex) {
                lbEstado.setText("Error al configurar credenciales: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void login() {
        String usuario = tfUsuario.getText().trim();
        String password = new String(pfClave.getPassword());
        
        // Mostrar diálogo para confirmar o modificar la IP del servidor
        String confirmedIp = showServerIpDialog(serverIp);
        if (confirmedIp == null) {
            // Usuario canceló la operación
            return;
        }
        
        // Actualizar la IP del servidor
        serverIp = confirmedIp;
        String servidorUrl = "http://" + serverIp + ":" + DEFAULT_PORT;
        
        System.out.println("=== INICIO DE SESIÓN ===");
        System.out.println("URL del servidor: " + servidorUrl);
        System.out.println("Usuario: " + usuario);
        
        try {
            // Validar que la URL del servidor sea accesible
            if (!isServerReachable(servidorUrl)) {
                String errorMsg = "No se puede conectar al servidor en " + servidorUrl;
                System.err.println(errorMsg);
                lbEstado.setText(errorMsg);
                return;
            }
        } catch (Exception e) {
            String errorMsg = "Error al verificar la conexión con el servidor: " + e.getMessage();
            System.err.println(errorMsg);
            lbEstado.setText(errorMsg);
            return;
        }

        if (usuario.isEmpty() || password.isEmpty()) {
            lbEstado.setText("Por favor, complete todos los campos.");
            return;
        }

        try {
            System.out.println("Preparando solicitud de login...");
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
            String params = "username=" + URLEncoder.encode(usuario, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
            System.out.println("Parámetros de login: " + params.replace(password, "*****"));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "*/*")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();
            
            System.out.println("Enviando solicitud de login...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Respuesta recibida del servidor");
            int code = response.statusCode();
            System.out.println("Código de respuesta del servidor: " + code);
            System.out.println("Headers de respuesta: " + response.headers().map());
            
            System.out.println("Código de respuesta del login: " + code);
            
            // Verificar si hay redirección
            if (code == 302 || code == 200) {
                // Obtener la cookie de sesión
                String headerCookies = response.headers().firstValue("Set-Cookie").orElse(null);
                if (headerCookies != null) {
                    String sessionCookie = headerCookies.split(";")[0];
                    System.out.println("Session Cookie obtenida: " + sessionCookie);
                    
                    // Verificar si la cookie es válida
                    if (sessionCookie == null || sessionCookie.trim().isEmpty()) {
                        throw new RuntimeException("La cookie de sesión está vacía");
                    }
                    
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
                            } else if (nombreRol.equalsIgnoreCase("VISUALIZADOR")) {
                                SwingUtilities.invokeLater(() -> {
                                    VisualizadorUI visualizador = new VisualizadorUI(usuario, sessionCookie);
                                    visualizador.setVisible(true);
                                    dispose();
                                });
                                return; // Salir del método después de iniciar sesión exitosamente
                            } else {
                                lbEstado.setText("No autorizado: El rol " + nombreRol + " no tiene acceso a esta aplicación.");
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
            String errorMsg = "Error al iniciar sesión: " + (e.getMessage() != null ? e.getMessage() : "Error desconocido");
            System.err.println(errorMsg);
            e.printStackTrace();
            lbEstado.setText(errorMsg);
            
            // Mostrar más detalles del error en la consola
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
        } finally {
            // Asegurarse de que el mensaje de error se muestre
            if (lbEstado.getText().isEmpty()) {
                lbEstado.setText("Error desconocido al iniciar sesión");
            }
        }
    }
}