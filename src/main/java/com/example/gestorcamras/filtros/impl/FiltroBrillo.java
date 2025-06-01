package com.example.gestorcamras.filtros.impl;

import com.example.gestorcamras.filtros.FiltroImagen;
import java.awt.image.BufferedImage;

/**
 * Filtro que ajusta el brillo de una imagen.
 * Un factor mayor que 1.0 aumenta el brillo, mientras que un factor menor que 1.0 lo disminuye.
 */
public class FiltroBrillo implements FiltroImagen {
    private final float factorBrillo;
    
    /**
     * Crea un nuevo filtro de brillo con el factor especificado.
     * @param factorBrillo Factor de brillo (1.0 = sin cambios, >1.0 = aumentar brillo, <1.0 = disminuir brillo)
     */
    public FiltroBrillo(float factorBrillo) {
        this.factorBrillo = factorBrillo;
    }
    
    /**
     * Crea un filtro de brillo con un aumento del 20% por defecto.
     */
    public FiltroBrillo() {
        this(1.4f); // 40% más brillo por defecto
    }
    
    @Override
    public BufferedImage aplicar(BufferedImage imagenOriginal) {
        if (imagenOriginal == null) {
            return null;
        }
        
        int ancho = imagenOriginal.getWidth();
        int alto = imagenOriginal.getHeight();
        
        // Crear una copia de la imagen original
        BufferedImage imagenFiltrada = new BufferedImage(
            ancho, alto, imagenOriginal.getType());
        
        // Aplicar el filtro de brillo
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                // Obtener el color del píxel
                int rgb = imagenOriginal.getRGB(x, y);
                
                // Extraer componentes de color
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                // Aplicar brillo
                r = ajustarRango((int)(r * factorBrillo));
                g = ajustarRango((int)(g * factorBrillo));
                b = ajustarRango((int)(b * factorBrillo));
                
                // Combinar componentes y establecer el nuevo píxel
                int nuevoRGB = (a << 24) | (r << 16) | (g << 8) | b;
                imagenFiltrada.setRGB(x, y, nuevoRGB);
            }
        }
        
        return imagenFiltrada;
    }
    
    @Override
    public String getNombre() {
        return "Brillo (" + (int)((factorBrillo - 1) * 100) + "%)";
    }
    
    @Override
    public String getDescripcion() {
        return "Ajusta el brillo de la imagen en un " + (int)((factorBrillo - 1) * 100) + "%";
    }
    
    @Override
    public void reset() {
        // No es necesario reiniciar nada en este filtro
    }
    
    /**
     * Asegura que el valor esté en el rango [0, 255]
     */
    private int ajustarRango(int valor) {
        return Math.min(255, Math.max(0, valor));
    }
    
    @Override
    public String toString() {
        return "FiltroBrillo{" + "factorBrillo=" + factorBrillo + '}';
    }
}
