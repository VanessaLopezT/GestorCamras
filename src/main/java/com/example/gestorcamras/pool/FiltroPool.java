package com.example.gestorcamras.pool;

import com.example.gestorcamras.model.Filtro;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class FiltroPool {
    private static final int MAX_POOL_SIZE = 20;
    private final Map<String, ConcurrentLinkedQueue<Filtro>> filterPool;

    public FiltroPool() {
        this.filterPool = new ConcurrentHashMap<>();
    }

    public synchronized Filtro obtenerFiltro(String tipo, String descripcion) {
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("Tipo de filtro inválido");

        ConcurrentLinkedQueue<Filtro> queue = filterPool.computeIfAbsent(tipo,
                k -> new ConcurrentLinkedQueue<>());

        Filtro filtro = queue.poll();
        if (filtro == null) {
            // Crear nuevo filtro si no hay ninguno disponible en el pool
            filtro = new Filtro();
            filtro.setTipo(tipo);
            filtro.setDescripcion(descripcion);
        }

        return filtro;
    }

    public synchronized void devolverFiltro(Filtro filtro) {
        if (filtro == null) return;

        ConcurrentLinkedQueue<Filtro> queue = filterPool.get(filtro.getTipo());
        if (queue != null && queue.size() < MAX_POOL_SIZE) {
            // Limpiar el estado del filtro antes de devolverlo al pool
            filtro.setImagenesProcesadas(null);
            queue.offer(filtro);
        }
    }

    public synchronized void limpiarPool() {
        filterPool.clear();
    }

    public Map<String, Integer> obtenerEstadoPool() {
        return filterPool.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
    }

}