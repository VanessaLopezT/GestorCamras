package com.example.gestorcamras;

import com.example.gestorcamras.Escritorio.LoginFrame;
import com.example.gestorcamras.Escritorio.ServidorUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Importaciones eliminadas por no ser utilizadas
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
// Importación eliminada por no ser utilizada
import java.net.URL;
import java.net.HttpURLConnection;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
})
public class GestorCamrasApplication {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    

    // Constantes para los modos de ejecución
    private static final String MODO_SERVIDOR = "--servidor";
    private static final String MODO_CLIENTE = "--cliente";
    private static final String MODO_AYUDA = "--ayuda";
    
    // URL del servidor (para modo cliente)
    private static String serverUrl = "http://192.168.1.9:8080";

    public static void main(String[] args) {
        // Configurar para permitir la interfaz gráfica
        System.setProperty("java.awt.headless", "false");
        
        // Si no hay argumentos o se solicita ayuda, mostrar diálogo de selección
        if (args.length == 0 || (args.length > 0 && (args[0].equals("--ayuda") || args[0].equals("-h") || args[0].equals("-?")))) {
            String[] opciones = {"Modo Completo (Servidor + Cliente)", 
                               "Solo Servidor", 
                               "Solo Cliente", 
                               "Ayuda"};
            
            int seleccion = JOptionPane.showOptionDialog(
                null, 
                "Seleccione el modo de inicio:", 
                "Gestor de Cámaras - Modo de Inicio",
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones, 
                opciones[0]
            );
            
            switch (seleccion) {
                case 0: // Modo completo
                    args = new String[0];
                    break;
                case 1: // Solo servidor
                    args = new String[]{MODO_SERVIDOR};
                    break;
                case 2: // Solo cliente
                    String ipServidor = JOptionPane.showInputDialog(
                        null,
                        "Ingrese la dirección IP del servidor:",
                        "192.168.1.9"
                    );
                    // Si el usuario cancela o cierra el diálogo, salir
                    if (ipServidor == null) {
                        System.exit(0);
                    }
                    // Si el usuario no ingresa nada, usar la IP por defecto
                    ipServidor = ipServidor.trim();
                    if (ipServidor.isEmpty()) {
                        ipServidor = "192.168.1.9";
                    }
                    // Asegurarse de que la IP tenga el formato correcto (sin http://)
                    ipServidor = ipServidor.replace("http://", "").replace("https://", "");
                    args = new String[]{MODO_CLIENTE, ipServidor};
                    break;
                case 3: // Ayuda
                    mostrarAyuda();
                    return;
                case JOptionPane.CLOSED_OPTION: // Si cierra el diálogo
                default:
                    System.exit(0);
                    return;
            }
        }
        
        // Determinar el modo de ejecución
        String modo = determinarModo(args);
        
        // Mostrar ayuda si se solicitó
        if (MODO_AYUDA.equals(modo)) {
            mostrarAyuda();
            return;
        }
        
        // Iniciar Spring Boot (si no es modo solo cliente)
        ConfigurableApplicationContext context = null;
        
        // Deshabilitar el reinicio automático de DevTools
        System.setProperty("spring.devtools.restart.enabled", "false");
        
        try {
            // Iniciar Spring Boot si no es modo solo cliente
            if (!MODO_CLIENTE.equals(modo)) {
                System.out.println("Iniciando servidor Spring Boot...");
                SpringApplication app = new SpringApplication(GestorCamrasApplication.class);
                app.setBannerMode(Banner.Mode.OFF);
                app.setLogStartupInfo(false);
                context = app.run(args);
                System.out.println("✅ Servidor Spring Boot iniciado correctamente en el puerto 8080");
            }
            
            // Iniciar las interfaces según el modo
            if (MODO_SERVIDOR.equals(modo)) {
                iniciarInterfazServidor();
            } else if (MODO_CLIENTE.equals(modo)) {
                iniciarInterfazCliente();
            } else {
                // Modo completo (comportamiento por defecto)
                iniciarInterfazServidor();
                // Pequeño retraso para asegurar que el servidor esté listo
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                iniciarInterfazCliente();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar el stack trace completo en un JTextArea
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            
            JTextArea textArea = new JTextArea("Error al iniciar la aplicación: " + e.getMessage() + 
                                          "\n\nStack Trace:\n" + stackTrace);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            JOptionPane.showMessageDialog(
                null, 
                scrollPane,
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
            
            if (context != null) {
                SpringApplication.exit(context, () -> 1);
            }
            System.exit(1);
        }
    }
    
    private static String determinarModo(String[] args) {
        if (args.length > 0) {
            String modo = args[0].toLowerCase();
            if (MODO_SERVIDOR.equals(modo) || MODO_CLIENTE.equals(modo) || MODO_AYUDA.equals(modo)) {
                // Si es modo cliente, verificar si se proporcionó la IP del servidor
                if (MODO_CLIENTE.equals(modo) && args.length > 1) {
                    serverUrl = "http://" + args[1] + ":8080";
                }
                return modo;
            }
        }
        // Por defecto, modo completo (servidor + cliente)
        return "";
    }
    
    private static void iniciarInterfazServidor() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("No se puede iniciar la interfaz de servidor en modo sin pantalla");
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("com.example.gestorcamras.Escritorio.ServidorUI");
                ServidorUI servidor = new ServidorUI();
                servidor.setVisible(true);
                System.out.println("Interfaz de servidor iniciada");
            } catch (Exception e) {
                System.err.println("Error al iniciar la interfaz de servidor: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null, 
                    "Error al iniciar el servidor: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
    
    private static void iniciarInterfazCliente() {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("No se puede iniciar la interfaz de cliente en modo sin pantalla");
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Extraer el host y puerto de la URL del servidor
                    String host = "localhost";
                    int port = 8080;
                    
                    try {
                        URI uri = new URI(serverUrl);
                        host = uri.getHost();
                        port = uri.getPort() > 0 ? uri.getPort() : 8080;
                    } catch (Exception e) {
                        System.err.println("Error al analizar la URL del servidor: " + e.getMessage());
                    }
                    
                    // Verificar si el servidor está disponible (solo si es localhost)
                    if (host.equals("localhost") || host.equals("127.0.0.1")) {
                        if (!verificarServidorDisponible(30)) {
                            JOptionPane.showMessageDialog(
                                null, 
                                "No se pudo conectar al servidor en " + serverUrl, 
                                "Error de conexión", 
                                JOptionPane.ERROR_MESSAGE
                            );
                            return;
                        }
                    }
                    
                    // Establecer propiedades del sistema para que las clases de escritorio las usen
                    System.setProperty("gestorcamras.server.host", host);
                    System.setProperty("gestorcamras.server.port", String.valueOf(port));
                    
                    // Iniciar el login
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                    System.out.println("Interfaz de cliente iniciada");
                    
                } catch (Exception e) {
                    System.err.println("Error al iniciar la interfaz de cliente: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        null, 
                        "Error al iniciar el cliente: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }
    
    private static boolean verificarServidorDisponible(int segundosMaximos) {
        System.out.println("Verificando conexión con el servidor en " + serverUrl + "...");
        long tiempoInicio = System.currentTimeMillis();
        long tiempoLimite = tiempoInicio + (segundosMaximos * 1000L);
        
        while (System.currentTimeMillis() < tiempoLimite) {
            try {
                URL url = URI.create(serverUrl + "/").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    System.out.println("¡Conexión con el servidor establecida!");
                    return true;
                }
            } catch (Exception e) {
                // El servidor aún no está listo
            }
            
            try {
                Thread.sleep(1000); // Esperar 1 segundo entre intentos
                System.out.print(".");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        System.out.println("\nNo se pudo conectar al servidor después de " + segundosMaximos + " segundos");
        return false;
    }
    
    private static void mostrarAyuda() {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("=== Gestor de Cámaras - Modos de Ejecución ===\n\n");
        mensaje.append("Uso: java -jar gestor-camras.jar [MODO] [IP_SERVIDOR]\n\n");
        mensaje.append("Modos disponibles:\n");
        mensaje.append("  (sin argumentos)   Muestra el diálogo de selección de modo\n");
        mensaje.append("  --servidor         Solo servidor (interfaz de administración)\n");
        mensaje.append("  --cliente [IP]     Solo cliente, conectándose al servidor en la IP especificada\n");
        mensaje.append("  --ayuda            Muestra esta ayuda\n\n");
        mensaje.append("Ejemplos:\n");
        mensaje.append("  java -jar gestor-camras.jar                     # Muestra diálogo de selección\n");
        mensaje.append("  java -jar gestor-camras.jar --servidor          # Solo servidor\n");
        mensaje.append("  java -jar gestor-camras.jar --cliente 192.168.1.100  # Solo cliente, conectado al servidor en 192.168.1.100\n");
        
        // Mostrar en consola
        System.out.println(mensaje.toString());
        
        // Mostrar en diálogo si es posible
        if (!GraphicsEnvironment.isHeadless()) {
            JTextArea textArea = new JTextArea(mensaje.toString());
            textArea.setEditable(false);
            textArea.setBackground(null);
            textArea.setBorder(null);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 300));
            
            JOptionPane.showMessageDialog(
                null,
                scrollPane,
                "Ayuda - Gestor de Cámaras",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
