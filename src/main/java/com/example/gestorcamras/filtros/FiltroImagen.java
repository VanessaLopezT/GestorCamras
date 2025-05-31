package com.example.gestorcamras.filtros;

import java.awt.image.BufferedImage;

/**
 * Interfaz que define el contrato para todos los filtros de imagen.
 */
public interface FiltroImagen {
    
    /**
     * Aplica el filtro a una imagen y devuelve el resultado.
     * @param imagenOriginal Imagen original a la que se aplicará el filtro
     * @return Imagen con el filtro aplicado
     */
    BufferedImage aplicar(BufferedImage imagenOriginal);
    
    /**
     * Obtiene el nombre del filtro.
     * @return Nombre del filtro
     */
    String getNombre();
    
    /**
     * Obtiene la descripción del filtro.
     * @return Descripción del filtro
     */
    String getDescripcion();
    
    /**
     * Reinicia el estado del filtro si es necesario.
     */
    void reset();
}
