package com.example.gestorcamras.pool;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

@Component
public class FiltroBasicoFactory {

    public FiltroOperacion obtenerFiltro(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "grises" -> this::convertirAGrises;
            case "invertir" -> this::invertirColores;
            case "brillo" -> this::aumentarBrillo;
            default -> throw new IllegalArgumentException("Filtro no soportado: " + tipo);
        };
    }

    private BufferedImage convertirAGrises(BufferedImage original) {
        BufferedImage salida = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                Color color = new Color(original.getRGB(x, y));
                int gris = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                Color grisColor = new Color(gris, gris, gris);
                salida.setRGB(x, y, grisColor.getRGB());
            }
        }
        return salida;
    }

    private BufferedImage invertirColores(BufferedImage original) {
        BufferedImage salida = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                Color color = new Color(original.getRGB(x, y));
                Color invertido = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
                salida.setRGB(x, y, invertido.getRGB());
            }
        }
        return salida;
    }

    private BufferedImage aumentarBrillo(BufferedImage original) {
        BufferedImage salida = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                Color color = new Color(original.getRGB(x, y));
                int r = Math.min(255, color.getRed() + 50);
                int g = Math.min(255, color.getGreen() + 50);
                int b = Math.min(255, color.getBlue() + 50);
                salida.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return salida;
    }
}
