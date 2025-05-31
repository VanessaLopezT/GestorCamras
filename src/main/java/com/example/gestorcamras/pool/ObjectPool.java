package com.example.gestorcamras.pool;

import com.example.gestorcamras.filtros.FiltroImagen;

/**
 * Interfaz base para el pool de filtros de imagen
 */
public interface ObjectPool<T extends FiltroImagen> {
    
    /**
     * Obtiene un filtro del pool
     * @param tipo Tipo de filtro a obtener
     * @return Filtro listo para usar
     */
    T obtenerFiltro(String tipo);
    
    /**
     * Devuelve un filtro al pool
     * @param filtro Filtro a devolver
     */
    void devolverFiltro(T filtro);
    
    /**
     * Crea una nueva instancia de un filtro
     * @param tipo Tipo de filtro a crear
     * @return Nueva instancia del filtro
     */
    T crearFiltro(String tipo);
    
    /**
     * Valida que un filtro pueda ser reutilizado
     * @param filtro Filtro a validar
     * @return true si el filtro puede ser reutilizado
     */
    boolean validarFiltro(T filtro);
    
    /**
     * Limpia el estado de un filtro antes de devolverlo al pool
     * @param filtro Filtro a limpiar
     */
    void limpiarFiltro(T filtro);
    
    /**
     * Cierra el pool y libera recursos
     */
    void cerrar();
}
