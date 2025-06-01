package com.example.gestorcamras.filtros.impl;

import com.example.gestorcamras.filtros.FiltroImagen;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

/**
 * Filtro que rota una imagen en grados especificados.
 * La rotación se realiza en sentido horario.
 */
public class FiltroRotar implements FiltroImagen {
    private final double anguloGrados;
    
    /**
     * Crea un nuevo filtro de rotación con el ángulo especificado.
     * @param anguloGrados Ángulo de rotación en grados (positivo para rotación horaria)
     */
    public FiltroRotar(double anguloGrados) {
        this.anguloGrados = anguloGrados % 360; // Normalizar el ángulo
    }
    
    /**
     * Crea un filtro de rotación con un ángulo de 90 grados por defecto.
     */
    public FiltroRotar() {
        this(90.0); // 90 grados por defecto
    }
    
    @Override
    public BufferedImage aplicar(BufferedImage imagenOriginal) {
        if (imagenOriginal == null) {
            return null;
        }
        
        // Convertir el ángulo a radianes
        double anguloRadianes = Math.toRadians(anguloGrados);
        
        // Calcular las nuevas dimensiones de la imagen
        double sin = Math.abs(Math.sin(anguloRadianes));
        double cos = Math.abs(Math.cos(anguloRadianes));
        int nuevoAncho = (int) Math.floor(imagenOriginal.getWidth() * cos + imagenOriginal.getHeight() * sin);
        int nuevoAlto = (int) Math.floor(imagenOriginal.getHeight() * cos + imagenOriginal.getWidth() * sin);
        
        // Crear la nueva imagen con las dimensiones calculadas
        BufferedImage imagenRotada = new BufferedImage(
            Math.max(1, nuevoAncho), 
            Math.max(1, nuevoAlto), 
            imagenOriginal.getType()
        );
        
        // Configurar la transformación
        Graphics2D g = imagenRotada.createGraphics();
        AffineTransform at = new AffineTransform();
        
        // Mover al centro de la nueva imagen
        at.translate(nuevoAncho / 2.0, nuevoAlto / 2.0);
        // Aplicar rotación
        at.rotate(anguloRadianes);
        // Mover el punto de origen de vuelta para que la imagen quede centrada
        at.translate(-imagenOriginal.getWidth() / 2.0, -imagenOriginal.getHeight() / 2.0);
        
        // Aplicar la transformación y dibujar la imagen
        g.drawImage(imagenOriginal, at, null);
        g.dispose();
        
        return imagenRotada;
    }
    
    @Override
    public String getNombre() {
        return "Rotar " + (int)anguloGrados + "°";
    }
    
    @Override
    public String getDescripcion() {
        return "Rota la imagen " + (int)anguloGrados + " grados en sentido horario";
    }
    
    @Override
    public void reset() {
        // No es necesario reiniciar nada en este filtro
    }
    
    @Override
    public String toString() {
        return "FiltroRotar{" + "anguloGrados=" + anguloGrados + '}';
    }
    
    /**
     * Obtiene el ángulo de rotación actual.
     * @return Ángulo en grados
     */
    public double getAnguloGrados() {
        return anguloGrados;
    }
}
