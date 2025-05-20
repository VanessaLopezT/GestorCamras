package com.example.gestorcamras;

import com.example.gestorcamras.Escritorio.ClienteSwing;
import com.example.gestorcamras.Escritorio.LoginFrame;
import com.example.gestorcamras.Escritorio.ServidorUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;

@SpringBootApplication
public class GestorCamrasApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GestorCamrasApplication.class);
        app.setHeadless(false); // Permite usar interfaces gráficas
        app.run(args);

        System.out.println("¿Es entorno headless? " + GraphicsEnvironment.isHeadless());
        System.out.println("java.awt.headless = " + System.getProperty("java.awt.headless"));

        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                new ServidorUI().setVisible(true);
            });
        }
        else {
            System.out.println("No hay entorno gráfico, no se lanza interfaz Swing.");
        }
    }
}

