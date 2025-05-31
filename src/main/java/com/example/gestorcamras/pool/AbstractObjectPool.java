package com.example.gestorcamras.pool;

import com.example.gestorcamras.filtros.FiltroImagen;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Implementación base abstracta de ObjectPool para filtros de imagen
 * @param <T> Tipo de filtro que manejará el pool (debe extender de FiltroImagen)
 */
public abstract class AbstractObjectPool<T extends FiltroImagen> implements ObjectPool<T> {
    private final int maxSize;
    private final Semaphore available;
    private final Queue<T> objects;
    private int createdObjects = 0;
    private volatile boolean isShutdown = false;

    protected AbstractObjectPool(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("El tamaño del pool debe ser mayor que cero");
        }
        this.maxSize = maxSize;
        this.available = new Semaphore(maxSize, true);
        this.objects = new ConcurrentLinkedQueue<>();
    }

    @Override
    public T obtenerFiltro(String tipo) {
        if (isShutdown) {
            throw new IllegalStateException("El pool ha sido cerrado");
        }

        try {
            if (!available.tryAcquire(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Tiempo de espera agotado al obtener un filtro del pool");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupción al obtener filtro del pool", e);
        }

        T filtro = objects.poll();
        if (filtro == null) {
            filtro = crearFiltro(tipo);
            createdObjects++;
        }

        return filtro;
    }

    @Override
    public void devolverFiltro(T filtro) {
        if (filtro == null) {
            return;
        }

        if (isShutdown) {
            limpiarFiltro(filtro);
            return;
        }

        if (validarFiltro(filtro)) {
            limpiarFiltro(filtro);
            objects.offer(filtro);
        } else {
            limpiarFiltro(filtro);
            createdObjects--;
        }
        available.release();
    }

    @Override
    public void cerrar() {
        isShutdown = true;
        synchronized (this) {
            T filtro;
            while ((filtro = objects.poll()) != null) {
                limpiarFiltro(filtro);
            }
            createdObjects = 0;
        }
    }

    @Override
    public abstract T crearFiltro(String tipo);
    
    @Override
    public boolean validarFiltro(T filtro) {
        return filtro != null;
    }
    
    @Override
    public void limpiarFiltro(T filtro) {
        if (filtro != null) {
            filtro.reset();
        }
    }
    
    /**
     * Obtiene el tamaño actual del pool
     * @return Número de objetos actualmente en el pool
     */
    public int getPoolSize() {
        return objects.size();
    }
    
    /**
     * Obtiene el número total de objetos creados
     * @return Número total de objetos creados
     */
    public int getCreatedObjectsCount() {
        return createdObjects;
    }
    
    /**
     * Obtiene el tamaño máximo del pool
     * @return Tamaño máximo del pool
     */
    public int getMaxSize() {
        return maxSize;
    }
}
