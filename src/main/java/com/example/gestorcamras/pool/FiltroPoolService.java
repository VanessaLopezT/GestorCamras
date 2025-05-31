package com.example.gestorcamras.pool;

import com.example.gestorcamras.filtros.FiltroImagen;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio que demuestra el uso del pool de filtros
 */
@Service
public class FiltroPoolService {
    
    private final FiltroObjectPool filtroObjectPool;
    
    @Autowired
    public FiltroPoolService(FiltroObjectPool filtroObjectPool) {
        this.filtroObjectPool = filtroObjectPool;
    }
    
    /**
     * Procesa una imagen usando un filtro del pool
     * @param tipoFiltro tipo de filtro a aplicar
     * @param descripcion descripción del filtro
     * @param imagenBytes bytes de la imagen a procesar
     * @return resultado del procesamiento
     */
    public byte[] procesarImagen(String tipoFiltro, String descripcion, byte[] imagenBytes) {
        FiltroImagen filtro = null;
        try {
            // Obtener un filtro del pool
            filtro = filtroObjectPool.obtenerFiltroConfigurado(tipoFiltro, descripcion);
            
            // Simular procesamiento de la imagen
            // En una implementación real, aquí se aplicaría el filtro a la imagen
            System.out.println("Aplicando filtro: " + tipoFiltro + " - " + descripcion);
            
            // En una implementación real, aquí se aplicaría el filtro a la imagen
            // byte[] imagenProcesada = filtro.aplicarFiltro(imagenBytes);
            
            // Simular operación costosa
            Thread.sleep(100);
            
            // Devolver la imagen procesada (en este ejemplo, la misma)
            return imagenBytes;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Procesamiento interrumpido", e);
        } finally {
            // Siempre devolver el filtro al pool
            if (filtro != null) {
                filtroObjectPool.devolverFiltro(filtro);
            }
        }
    }
    
    /**
     * Obtiene estadísticas del pool
     * @return estadísticas actuales del pool
     */
    public Object obtenerEstadisticasPool() {
        return Map.of(
            "tamanoPool", filtroObjectPool.getPoolSize(),
            "objetosCreados", filtroObjectPool.getCreatedObjectsCount()
        );
    }
}
