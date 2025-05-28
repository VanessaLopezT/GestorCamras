package com.example.gestorcamras.Escritorio;

import javax.swing.*;
import java.awt.*;

public class VisualizadorUI extends JFrame {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private final String usuario;
    @SuppressWarnings("unused")
    private final String sessionCookie;
    private final String serverIp;

    public VisualizadorUI(String usuario, String sessionCookie, String serverIp) {
        this.usuario = usuario;
        this.sessionCookie = sessionCookie;
        this.serverIp = serverIp;
        
        // Configuración básica de la ventana
        setTitle("Visualizador - Gestor de Cámaras");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel panel = new JPanel(new BorderLayout());
        
        // Barra de título con información del usuario
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Usuario: " + usuario));
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Área de contenido principal
        JLabel lblBienvenida = new JLabel("Bienvenido Visualizador: " + usuario, JLabel.CENTER);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblBienvenida, BorderLayout.CENTER);
        
        // Botón de salir
        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.addActionListener(e -> {
            // Aquí podrías agregar lógica para cerrar sesión en el servidor
            dispose();
            new LoginFrame(serverIp).setVisible(true);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnSalir);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(panel);
    }
}
