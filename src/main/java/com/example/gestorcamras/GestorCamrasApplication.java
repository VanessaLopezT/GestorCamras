package com.example.gestorcamras;

import com.example.gestorcamras.Escritorio.LoginFrame;
import com.example.gestorcamras.Escritorio.ServidorUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
// Importaciones de red eliminadas ya que no se utilizan

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan({
    "com.example.gestorcamras",
    "com.example.gestorcamras.config",
    "com.example.gestorcamras.security"
})
@EnableJpaRepositories("com.example.gestorcamras.repository")
@EntityScan("com.example.gestorcamras.model")
public class GestorCamrasApplication {
    

    // Constantes para los modos de ejecución
    private static final String MODO_SERVIDOR = "--servidor";
    private static final String MODO_CLIENTE = "--cliente";
    private static final String MODO_AYUDA = "--ayuda";
    
    // La URL del servidor ahora se maneja en LoginFrame

    public static void main(String[] args) {
        // Configurar para permitir la interfaz gráfica
        System.setProperty("java.awt.headless", "false");
        
        // Si no hay argumentos o se solicita ayuda, mostrar diálogo de selección
        if (args.length == 0 || (args.length > 0 && (args[0].equals("--ayuda") || args[0].equals("-h") || args[0].equals("-?")))) {
            String[] opciones = {"Modo Completo (Servidor + Cliente)", 
                               "Solo Servidor", 
                               "Solo Cliente"};
            
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
                    // No pedimos la IP aquí, la pedirá el LoginFrame
                    args = new String[]{MODO_CLIENTE};
                    break;
                // Removed help option
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
                // Si es modo cliente, ya no necesitamos hacer nada especial con la IP
                // ya que la pedirá el LoginFrame
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
                    // Configuración por defecto para el cliente
                    // La IP del servidor la pedirá el LoginFrame
                    String host = "localhost";
                    int port = 8080;
                    
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
    
    // El método verificarServidorDisponible fue eliminado ya que la verificación de conexión
    // ahora se maneja en el LoginFrame
    
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
