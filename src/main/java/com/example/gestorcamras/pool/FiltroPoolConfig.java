package com.example.gestorcamras.pool;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiltroPoolConfig {

    @Bean
    public FiltroPool filtroPool() {
        return new FiltroPool();
    }
}