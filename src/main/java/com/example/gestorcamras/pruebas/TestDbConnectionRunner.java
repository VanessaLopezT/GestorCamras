package com.example.gestorcamras.pruebas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestDbConnectionRunner implements CommandLineRunner {


        private final JdbcTemplate jdbcTemplate;

        public TestDbConnectionRunner(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public void run(String... args) throws Exception {
            // Consulta simple para probar la conexión (puedes cambiar la consulta a la que tenga sentido para tu BD)
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("Prueba de conexión a BD exitosa, resultado: " + result);
        }
    }
