package com.example.gestorcamras;

import com.example.gestorcamras.Escritorio.LoginFrame;
import com.example.gestorcamras.Escritorio.ServidorUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class GestorCamrasApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GestorCamrasApplication.class);
        app.setHeadless(false);
        app.run(args);

        System.out.println("¿Es entorno headless? " + GraphicsEnvironment.isHeadless());
        System.out.println("java.awt.headless = " + System.getProperty("java.awt.headless"));

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Class.forName("com.example.gestorcamras.Escritorio.ServidorUI");
                    ServidorUI servidor = new ServidorUI();
                    servidor.setVisible(true);
                    new LoginFrame().setVisible(true);
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(null, "No se pudo iniciar el servidor", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            });
        } else {
            System.out.println("No hay entorno gráfico, no se lanza interfaz Swing.");
        }
    }
}
