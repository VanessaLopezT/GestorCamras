package com.example.gestorcamras.Escritorio;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;

import org.json.JSONObject;
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
            System.out.println("Buscando interfaces de red...");
            
            // Primero intentamos obtener la IP de la interfaz Wi-Fi
            String wifiIp = findInterfaceIp("Wireless", "Wi-Fi");
            if (wifiIp != null) {
                System.out.println("Usando IP de Wi-Fi: " + wifiIp);
                return wifiIp;
            }
            
            // Si no encontramos Wi-Fi, intentamos con Ethernet (pero no VirtualBox)
            String ethernetIp = findInterfaceIp("Ethernet");
            if (ethernetIp != null && !ethernetIp.startsWith("192.168.56.")) {  // Filtramos IPs de VirtualBox
                System.out.println("Usando IP de Ethernet: " + ethernetIp);
                return ethernetIp;
            }
            
            // Si no encontramos ninguna de las anteriores, buscamos la primera IP válida
            String firstIp = findFirstAvailableIp();
            System.out.println("Usando primera IP disponible: " + firstIp);
            return firstIp;
        } catch (Exception e) {
            System.err.println("Error al obtener la IP local: " + e.getMessage());
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
    
    private String findInterfaceIp(String... interfaceNames) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                String displayName = networkInterface.getDisplayName();
                String name = networkInterface.getName();
                
                // Saltar interfaces que no nos interesan
                if (networkInterface.isLoopback() || 
                    !networkInterface.isUp() || 
                    displayName == null ||
                    displayName.contains("Virtual") || 
                    displayName.contains("VirtualBox") ||
                    displayName.contains("WFP") || 
                    displayName.contains("Loopback") || 
                    displayName.contains("Teredo") ||
                    displayName.contains("Bluetooth") || 
                    displayName.contains("Pseudo") ||
                    displayName.contains("Miniport") ||
                    displayName.contains("Tunnel") ||
                    displayName.contains("Pseudo") ||
                    name == null || 
                    name.startsWith("vEthernet") ||
                    name.startsWith("Loopback") ||
                    name.startsWith("isatap")) {
                    continue;
                }
                
                System.out.println("Interfaz encontrada: " + displayName + " (" + name + ")");
                
                // Verificar si coincide con alguno de los nombres buscados
                for (String interfaceName : interfaceNames) {
                    if (displayName.contains(interfaceName) || name.contains(interfaceName)) {
                        String ip = getFirstValidIpFromInterface(networkInterface);
                        if (ip != null && !ip.startsWith("169.254") && !ip.startsWith("0.0.0.0") && !ip.startsWith("127.0.0.1")) {
                            System.out.println("IP seleccionada de " + displayName + ": " + ip);
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar interfaz: " + e.getMessage());
        }
        return null;
    }
    
    private String findFirstAvailableIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual() || iface.isPointToPoint()) {
                    continue;
                }
                
                String ip = getFirstValidIpFromInterface(iface);
                if (ip != null) {
                    return ip;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar IP disponible: " + e.getMessage());
        }
        return "127.0.0.1";
    }
    
    private String getFirstValidIpFromInterface(NetworkInterface iface) {
        Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if (!addr.isLoopbackAddress() && addr.getHostAddress().contains(".")) {
                String hostAddress = addr.getHostAddress();
                if (hostAddress.startsWith("192.168.") || 
                    hostAddress.startsWith("10.") || 
                    hostAddress.startsWith("172.16.")) {
                    System.out.println("IP seleccionada de " + iface.getDisplayName() + ": " + hostAddress);
                    return hostAddress;
                }
            }
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
            "Configuración de conexión - IP actual: " + (defaultIp != null ? defaultIp : "No configurada"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            defaultIp
        );
        
        if (ip != null) {
            ip = ip.trim();
            if (ip.isEmpty() && defaultIp != null) {
                ip = defaultIp;
            }
        }
        return ip;
    }
    
    public LoginFrame() {
        this(null);
    }
    
    public LoginFrame(String serverIp) {
        if (serverIp == null || serverIp.trim().isEmpty()) {
            // Obtener la IP local al iniciar si no se proporciona una IP
            String localIp = getLocalIpAddress();
            if (localIp != null) {
                this.serverIp = localIp;
            }
            
            // Pedir la IP del servidor al inicio
            String confirmedIp = showServerIpDialog(this.serverIp);
            if (confirmedIp == null) {
                // Si el usuario cancela, cerramos la aplicación
                System.exit(0);
            }
            this.serverIp = confirmedIp;
        } else {
            // Usar la IP proporcionada
            this.serverIp = serverIp;
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
        
        // Verificar que se haya configurado una IP de servidor
        if (serverIp == null || serverIp.trim().isEmpty()) {
            lbEstado.setText("Error: No se ha configurado la IP del servidor");
            return;
        }
        
        String servidorUrl = "http://" + serverIp.trim() + ":" + DEFAULT_PORT;
        System.out.println("Intentando conectar con el servidor en: " + servidorUrl);
        
        try {
            // Validar que la URL del servidor sea accesible
            lbEstado.setText("Conectando con el servidor en " + serverIp + "...");
            if (!isServerReachable(servidorUrl)) {
                String errorMsg = "No se puede conectar al servidor en " + servidorUrl + ". Verifique la IP e intente nuevamente.";
                System.err.println(errorMsg);
                lbEstado.setText(errorMsg);
                
                // Volver a pedir la IP del servidor
                String newIp = showServerIpDialog(serverIp);
                if (newIp != null && !newIp.trim().isEmpty()) {
                    serverIp = newIp.trim();
                    // Reintentar el login con la nueva IP
                    login();
                }
                return;
            }

            if (usuario.isEmpty() || password.isEmpty()) {
                lbEstado.setText("Por favor, complete todos los campos.");
                return;
            }
            
            // Autenticación tradicional (mismo método que en Cliente+Servidor)
            String params = "username=" + URLEncoder.encode(usuario, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
            
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))  // Aceptar todas las cookies
                .build();
            
            // 1. Hacer login para obtener la cookie de sesión
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(servidorUrl + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "*/*")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();
            
            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            
            // Verificar si la autenticación fue exitosa (código 302 para redirección o 200)
            if (loginResponse.statusCode() == 302 || loginResponse.statusCode() == 200) {
                // 2. Obtener información del usuario autenticado
                HttpRequest userRequest = HttpRequest.newBuilder()
                        .uri(URI.create(servidorUrl + "/api/usuario/actual"))
                        .GET()
                        .build();

                HttpResponse<String> userResponse = client.send(userRequest, HttpResponse.BodyHandlers.ofString());
                
                if (userResponse.statusCode() == 200) {
                    // Procesar la respuesta del usuario
                    JSONObject userData = new JSONObject(userResponse.body());
                    String rol = userData.optString("rol", userData.optString("nombreRol", ""));
                    
                    // Obtener la cookie de sesión para pasarla a la interfaz
                    String sessionCookie = loginResponse.headers()
                            .firstValue("Set-Cookie")
                            .orElse("")
                            .split(";")[0];
                    
                    // Manejar la interfaz según el rol
                    if (rol.equalsIgnoreCase("OPERADOR")) {
                        SwingUtilities.invokeLater(() -> {
                            ClienteSwingUI cliente = new ClienteSwingUI(usuario, sessionCookie, serverIp);
                            cliente.setVisible(true);
                            dispose();
                        });
                    } else if (rol.equalsIgnoreCase("VISUALIZADOR")) {
                        SwingUtilities.invokeLater(() -> {
                            VisualizadorUI visualizador = new VisualizadorUI(usuario, sessionCookie, serverIp);
                            visualizador.setVisible(true);
                            dispose();
                        });
                    } else {
                        lbEstado.setText("Rol no autorizado: " + rol);
                    }
                    return;  // Salir si todo fue exitoso
                }
            }
            
            // Si llegamos aquí, hubo un error en la autenticación
            lbEstado.setText("Credenciales inválidas o error de autenticación");
            
        } catch (Exception e) {
            String errorMsg = "Error al iniciar sesión: " + 
                (e.getMessage() != null ? e.getMessage() : "Error desconocido");
            System.err.println(errorMsg);
            e.printStackTrace();
            lbEstado.setText(errorMsg);
            
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
        } finally {
            if (lbEstado.getText().isEmpty()) {
                lbEstado.setText("Error desconocido al iniciar sesión");
            }
        }
    }
}