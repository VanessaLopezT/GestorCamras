package com.example.gestorcamras.filtros.impl;

import com.example.gestorcamras.filtros.FiltroImagen;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Filtro que reduce el tamaño de una imagen a un porcentaje específico.
 * Un factor de 0.5 reducirá la imagen a la mitad, 0.25 a un cuarto, etc.
 */
public class FiltroReducirTamano implements FiltroImagen {
    private final double factorReduccion;
    
    /**
     * Crea un nuevo filtro de reducción de tamaño con el factor especificado.
     * @param factorReduccion Factor de reducción (ej: 0.5 para reducir a la mitad)
     */
    public FiltroReducirTamano(double factorReduccion) {
        if (factorReduccion <= 0 || factorReduccion >= 1.0) {
            throw new IllegalArgumentException("El factor de reducción debe estar entre 0 y 1");
        }
        this.factorReduccion = factorReduccion;
    }
    
    /**
     * Crea un filtro de reducción de tamaño con una reducción al 50% por defecto.
     */
    public FiltroReducirTamano() {
        this(0.5); // 50% de reducción por defecto
    }
    
    @Override
    public BufferedImage aplicar(BufferedImage imagenOriginal) {
        if (imagenOriginal == null) {
            return null;
        }
        
        // Calcular nuevas dimensiones
        int nuevoAncho = (int)(imagenOriginal.getWidth() * factorReduccion);
        int nuevoAlto = (int)(imagenOriginal.getHeight() * factorReduccion);
        
        // Asegurar un tamaño mínimo de 1x1 píxeles
        nuevoAncho = Math.max(1, nuevoAncho);
        nuevoAlto = Math.max(1, nuevoAlto);
        
        // Crear nueva imagen con las dimensiones reducidas
        BufferedImage imagenReducida = new BufferedImage(
            nuevoAncho, 
            nuevoAlto, 
            imagenOriginal.getType()
        );
        
        // Dibujar la imagen original escalada a las nuevas dimensiones
        Graphics2D g = imagenReducida.createGraphics();
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g.drawImage(imagenOriginal, 0, 0, nuevoAncho, nuevoAlto, null);
        g.dispose();
        
        return imagenReducida;
    }
    
    @Override
    public String getNombre() {
        return "Reducir tamaño (" + (int)((1 - factorReduccion) * 100) + "%)";
    }
    
    @Override
    public String getDescripcion() {
        return "Reduce el tamaño de la imagen al " + (int)(factorReduccion * 100) + "% del original";
    }
    
    @Override
    public void reset() {
        // No es necesario reiniciar nada en este filtro
    }
    
    @Override
    public String toString() {
        return "FiltroReducirTamano{" + "factorReduccion=" + factorReduccion + '}';
    }
}
