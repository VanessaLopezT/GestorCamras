package com.example.gestorcamras.pool;

import com.example.gestorcamras.filtros.FiltroImagen;
import com.example.gestorcamras.filtros.PoolFiltros;
import com.example.gestorcamras.filtros.impl.FiltroEscalaGrises;
import com.example.gestorcamras.filtros.impl.FiltroSepia;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Implementación concreta de un pool de objetos para filtros de imagen.
 * Utiliza la implementación base AbstractObjectPool para manejar los objetos disponibles.
 */
@Component
public class FiltroObjectPool extends AbstractObjectPool<FiltroImagen> {
    
    private final Map<String, BlockingQueue<FiltroImagen>> pools = new ConcurrentHashMap<>();
    
    /**
     * Constructor por defecto que inicializa el pool con un tamaño máximo de 10 objetos
     */
    public FiltroObjectPool() {
        super(10); // Tamaño máximo por defecto del pool
    }
    
    /**
     * Constructor que permite especificar el tamaño máximo del pool
     * @param maxSize Tamaño máximo del pool
     */
    public FiltroObjectPool(int maxSize) {
        super(maxSize);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public FiltroImagen crearFiltro(String tipo) {
        try {
            // Mapear el tipo de filtro a la clase correspondiente
            Class<? extends FiltroImagen> filtroClass = (Class<? extends FiltroImagen>) switch (tipo.toLowerCase()) {
                case "escala_grises" -> FiltroEscalaGrises.class;
                case "sepia" -> FiltroSepia.class;
                default -> throw new IllegalArgumentException("Tipo de filtro no soportado: " + tipo);
            };
            
            // Obtener el filtro del pool
            return PoolFiltros.obtenerFiltro(filtroClass);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo crear el filtro de tipo: " + tipo, e);
        }
    }
    
    @Override
    public boolean validarFiltro(FiltroImagen filtro) {
        return filtro != null;
    }
    
    @Override
    public void limpiarFiltro(FiltroImagen filtro) {
        if (filtro != null) {
            PoolFiltros.liberarFiltro(filtro);
            filtro.reset();
        }
    }
    
    @Override
    @PreDestroy
    public void cerrar() {
        super.cerrar();
        // No es necesario limpiar manualmente ya que PoolFiltros maneja su propio ciclo de vida
        // Los filtros serán recolectados por el recolector de basura cuando sea apropiado
    }
    
    /**
     * Obtiene estadísticas del pool
     * @return Mapa con estadísticas
     */
    public Map<String, Object> obtenerEstadisticas() {
        return Map.of(
            "tamanoPool", getPoolSize(),
            "objetosCreados", getCreatedObjectsCount()
        );
    }
    
    /**
     * Obtiene el tamaño actual del pool para un tipo de filtro específico
     * @param tipo Tipo de filtro
     * @return Tamaño actual del pool para el tipo especificado
     */
    public int getPoolSize(String tipo) {
        BlockingQueue<FiltroImagen> pool = pools.get(tipo);
        return pool != null ? pool.size() : 0;
    }
    
    /**
     * Obtiene el número total de objetos creados para un tipo de filtro
     * @param tipo Tipo de filtro
     * @return Número total de objetos creados
     */
    public int getCreatedObjectsCount(String tipo) {
        // En esta implementación simple, no llevamos un conteo de objetos creados
        BlockingQueue<FiltroImagen> pool = pools.get(tipo);
        return pool != null ? pool.size() : 0;
    }
    
    /**
     * Obtiene un filtro configurado con el tipo y descripción especificados
     * @param tipo Tipo de filtro
     * @param descripcion Descripción del filtro
     * @return Filtro configurado
     */
    public FiltroImagen obtenerFiltroConfigurado(String tipo, String descripcion) {
        FiltroImagen filtro = obtenerFiltro(tipo);
        // Aquí podrías configurar propiedades adicionales del filtro si fuera necesario
        return filtro;
    }
}
