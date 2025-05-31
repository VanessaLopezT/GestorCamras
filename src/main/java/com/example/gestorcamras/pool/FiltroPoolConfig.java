package com.example.gestorcamras.pool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FiltroPoolConfig {

    @Bean
    @Scope("singleton")
    public FiltroObjectPool filtroObjectPool() {
        // Configuración del pool con un tamaño máximo de 20 objetos
        return new FiltroObjectPool(20);
    }
}