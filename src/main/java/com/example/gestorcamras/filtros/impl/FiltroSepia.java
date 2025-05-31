package com.example.gestorcamras.filtros.impl;

import com.example.gestorcamras.filtros.FiltroImagen;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Filtro que aplica un efecto sepia a la imagen.
 */
public class FiltroSepia implements FiltroImagen {
    
    private final String nombre = "Sepia";
    private final String descripcion = "Aplica un efecto sepia a la imagen";

    @Override
    public BufferedImage aplicar(BufferedImage imagenOriginal) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("La imagen original no puede ser nula");
        }

        int ancho = imagenOriginal.getWidth();
        int alto = imagenOriginal.getHeight();
        BufferedImage imagenSepia = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                Color color = new Color(imagenOriginal.getRGB(x, y));
                
                // Obtener componentes de color
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                // Aplicar matriz de sepia
                int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                
                // Asegurar que los valores estén en el rango correcto (0-255)
                tr = Math.min(255, tr);
                tg = Math.min(255, tg);
                tb = Math.min(255, tb);
                
                Color nuevoColor = new Color(tr, tg, tb);
                imagenSepia.setRGB(x, y, nuevoColor.getRGB());
            }
        }
        
        return imagenSepia;
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
