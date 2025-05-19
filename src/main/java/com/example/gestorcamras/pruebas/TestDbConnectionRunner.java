package com.example.gestorcamras.pruebas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.sql.SQLException;

@Component
public class TestDbConnectionRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public TestDbConnectionRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // Intentamos extraer el host y puerto de la URL JDBC
            String hostPort = dbUrl
                    .replace("jdbc:mysql://", "")
                    .split("/")[0]; // "localhost:3306"

            String host = hostPort.split(":")[0];
            int port = Integer.parseInt(hostPort.split(":")[1]);

            // Verificamos si el puerto está abierto
            try (Socket socket = new Socket(host, port)) {
                System.out.println("✅ Puerto " + port + " en " + host + " está abierto.");
            } catch (Exception e) {
                System.err.println("❌ No se puede conectar al puerto " + port + " de " + host + ": " + e.getMessage());
                return;
            }

            // Ejecutamos consulta de prueba
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println("✅ Prueba de conexión a BD exitosa, resultado: " + result);

        } catch (Exception e) {
            System.err.println("❌ Error al probar conexión con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
