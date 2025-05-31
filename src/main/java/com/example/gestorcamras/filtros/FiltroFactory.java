package com.example.gestorcamras.filtros;

import com.example.gestorcamras.filtros.impl.FiltroEscalaGrises;
import com.example.gestorcamras.filtros.impl.FiltroSepia;

/**
 * Fábrica para crear instancias de filtros de imagen.
 */
public class FiltroFactory {
    
    /**
     * Crea una nueva instancia de un filtro según el tipo especificado.
     * @param tipo Tipo de filtro a crear
     * @return Instancia del filtro solicitado
     * @throws IllegalArgumentException si el tipo de filtro no es soportado
     */
    public static FiltroImagen crearFiltro(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de filtro no puede ser nulo o vacío");
        }
        
        switch (tipo.toLowerCase()) {
            case "escala_grises":
            case "escala grises":
            case "grises":
                return new FiltroEscalaGrises();
                
            case "sepia":
                return new FiltroSepia();
                
            // Agregar más casos para otros filtros aquí
                
            default:
                throw new IllegalArgumentException("Tipo de filtro no soportado: " + tipo);
        }
    }
    
    /**
     * Obtiene una descripción de todos los filtros disponibles.
     * @return Array de cadenas con los nombres y descripciones de los filtros
     */
    public static String[] obtenerFiltrosDisponibles() {
        return new String[] {
            "escala_grises - Convierte la imagen a escala de grises",
            "sepia - Aplica un efecto sepia a la imagen"
            // Agregar más descripciones de filtros aquí
        };
    }
}
