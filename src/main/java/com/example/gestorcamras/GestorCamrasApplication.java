package com.example.gestorcamras;

import com.example.gestorcamras.Escritorio.ClienteSwing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class GestorCamrasApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteSwing app = new ClienteSwing();
            app.setVisible(true);
        });
    }

}

