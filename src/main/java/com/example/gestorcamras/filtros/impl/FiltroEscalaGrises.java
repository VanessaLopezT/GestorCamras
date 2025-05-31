package com.example.gestorcamras.filtros.impl;

import com.example.gestorcamras.filtros.FiltroImagen;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Filtro que convierte una imagen a escala de grises.
 */
public class FiltroEscalaGrises implements FiltroImagen {
    
    private final String nombre = "Escala de Grises";
    private final String descripcion = "Convierte la imagen a escala de grises";

    @Override
    public BufferedImage aplicar(BufferedImage imagenOriginal) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("La imagen original no puede ser nula");
        }

        int ancho = imagenOriginal.getWidth();
        int alto = imagenOriginal.getHeight();
        BufferedImage imagenGris = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                Color color = new Color(imagenOriginal.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                // Fórmula para convertir a escala de grises (método de luminosidad)
                int gris = (int) (0.2989 * r + 0.5870 * g + 0.1140 * b);
                
                Color nuevoColor = new Color(gris, gris, gris);
                imagenGris.setRGB(x, y, nuevoColor.getRGB());
            }
        }
        
        return imagenGris;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public void reset() {
        // No hay estado que reiniciar en este filtro
    }
}
