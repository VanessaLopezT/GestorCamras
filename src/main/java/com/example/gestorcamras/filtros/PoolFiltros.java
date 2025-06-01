package com.example.gestorcamras.filtros;

import com.example.gestorcamras.filtros.impl.FiltroEscalaGrises;
import com.example.gestorcamras.filtros.impl.FiltroSepia;
import com.example.gestorcamras.filtros.impl.FiltroBrillo;
import com.example.gestorcamras.filtros.impl.FiltroReducirTamano;
import com.example.gestorcamras.filtros.impl.FiltroRotar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Implementación de un Object Pool para la gestión eficiente de filtros de imagen.
 * Permite reutilizar instancias de filtros para mejorar el rendimiento.
 */
public class PoolFiltros {
    private static final Map<Class<? extends FiltroImagen>, Queue<FiltroImagen>> pool = new HashMap<>();
    private static final int DEFAULT_POOL_SIZE = 5;
    
    static {
        // Inicializar pools para cada tipo de filtro
        pool.put(FiltroEscalaGrises.class, new LinkedList<>());
        pool.put(FiltroSepia.class, new LinkedList<>());
        pool.put(FiltroBrillo.class, new LinkedList<>());
        pool.put(FiltroReducirTamano.class, new LinkedList<>());
        pool.put(FiltroRotar.class, new LinkedList<>());
        
        // Precargar instancias por defecto
        precargar(DEFAULT_POOL_SIZE, FiltroEscalaGrises.class);
        precargar(DEFAULT_POOL_SIZE, FiltroSepia.class);
        precargar(DEFAULT_POOL_SIZE, FiltroBrillo.class);
        precargar(DEFAULT_POOL_SIZE, FiltroReducirTamano.class);
        precargar(DEFAULT_POOL_SIZE, FiltroRotar.class);
    }
    
    /**
     * Obtiene una instancia de filtro del pool.
     * Si no hay instancias disponibles, crea una nueva.
     * 
     * @param tipo Clase del filtro a obtener
     * @return Instancia del filtro solicitado
     * @throws IllegalArgumentException si el tipo de filtro no es soportado
     */
    @SuppressWarnings("unchecked")
    public static <T extends FiltroImagen> T obtenerFiltro(Class<T> tipo) {
        Queue<FiltroImagen> filtros = pool.get(tipo);
        if (filtros == null) {
            throw new IllegalArgumentException("Tipo de filtro no soportado: " + tipo.getSimpleName());
        }
        
        synchronized (filtros) {
            FiltroImagen filtro = filtros.poll();
            if (filtro == null) {
                filtro = crearNuevaInstancia(tipo);
            }
            return (T) filtro;
        }
    }
    
    /**
     * Libera un filtro devolviéndolo al pool para su reutilización.
     * 
     * @param filtro Filtro a liberar
     */
    public static void liberarFiltro(FiltroImagen filtro) {
        if (filtro == null) return;
        
        Queue<FiltroImagen> filtros = pool.get(filtro.getClass());
        if (filtros != null) {
            synchronized (filtros) {
                filtro.reset(); // Reiniciar el estado del filtro
                filtros.offer(filtro);
            }
        }
    }
    
    /**
     * Precarga el pool con un número específico de instancias de un tipo de filtro.
     * 
     * @param cantidad Número de instancias a precargar
     * @param tipo Tipo de filtro a precargar
     */
    public static void precargar(int cantidad, Class<? extends FiltroImagen> tipo) {
        Queue<FiltroImagen> filtros = pool.get(tipo);
        if (filtros != null) {
            synchronized (filtros) {
                for (int i = 0; i < cantidad; i++) {
                    filtros.add(crearNuevaInstancia(tipo));
                }
            }
        }
    }
    
    /**
     * Crea una nueva instancia del tipo de filtro especificado.
     */
    private static FiltroImagen crearNuevaInstancia(Class<? extends FiltroImagen> tipo) {
        try {
            return tipo.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error al crear instancia de filtro: " + tipo.getSimpleName(), e);
        }
    }
    
    /**
     * Obtiene una descripción de todos los filtros disponibles.
     * 
     * @return Array de cadenas con los nombres y descripciones de los filtros
     */
    public static String[] obtenerFiltrosDisponibles() {
        String[] filtros = new String[5];
        filtros[0] = "escala_grises - Convierte la imagen a escala de grises";
        filtros[1] = "sepia - Aplica un efecto sepia a la imagen";
        filtros[2] = "brillo - Aumenta el brillo de la imagen en un 40%";
        filtros[3] = "reducir_tamano - Reduce el tamaño de la imagen a la mitad";
        filtros[4] = "rotar - Rota la imagen 90 grados en sentido horario";
        return filtros;
    }
    
    /**
     * Obtiene el número de instancias disponibles de un tipo de filtro.
     * 
     * @param tipo Tipo de filtro
     * @return Número de instancias disponibles
     */
    public static int getDisponibles(Class<? extends FiltroImagen> tipo) {
        Queue<FiltroImagen> filtros = pool.get(tipo);
        return filtros != null ? filtros.size() : 0;
    }
}
