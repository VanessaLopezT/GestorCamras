package com.example.gestorcamras;

import com.example.gestorcamras.model.Informe;
import com.example.gestorcamras.model.InformeBuilder;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.repository.InformeRepository;
import com.example.gestorcamras.repository.UsuarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
public class InformeBuilderIntegrationTest {

    @Autowired
    private InformeRepository informeRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @Transactional
    public void testCrearYBorrarInformeBuilder() {
        // Paso 1: Crear un usuario base (o tomar uno existente)
        Usuario usuario = usuarioRepository.findAll().stream().findFirst().orElse(null);
        if (usuario == null) {
            // Si no hay usuarios, crea uno genérico temporal solo para el test
            usuario = new Usuario();
            usuario.setNombre("UsuarioTest");
            usuario.setCorreo("usuariotest@example.com");
            usuario.setContrasena("testpass");
            usuario = usuarioRepository.save(usuario);
        }
        // Mostrar usuarios tras posible inserción
        com.example.gestorcamras.TablaConsolaHelper.imprimirTablaUsuarios(usuarioRepository.findAll());

        // Paso 2: Crear informe por partes usando el builder
        System.out.println("[TEST] Paso 2: Comenzando a construir el informe por partes...");
        InformeBuilder builder = new InformeBuilder()
                .titulo("Informe de Prueba")
                .fechaGeneracion(LocalDateTime.now());
        System.out.println("[TEST] Título y fecha generados.");
        builder.tamaño(123.45)
                .contenido("Resumen inicial");
        System.out.println("[TEST] Tamaño y contenido inicial agregados.");
        builder.agregarAlContenido("• Video 1: OK");
        System.out.println("[TEST] Agregado: Video 1: OK");
        builder.agregarAlContenido("• Video 2: ALERTA");
        System.out.println("[TEST] Agregado: Video 2: ALERTA");
        builder.usuario(usuario);
        System.out.println("[TEST] Usuario asignado al informe.");
        Informe informe = builder.build();
        System.out.println("[TEST] Informe construido: " + informe);

        // Paso 3: Guardar el informe
        Informe guardado = informeRepository.save(informe);
        System.out.println("[TEST] Informe guardado en la BD: " + guardado);
        com.example.gestorcamras.TablaConsolaHelper.imprimirTablaInformes(informeRepository.findAll());
        Assertions.assertNotNull(guardado.getIdInfo(), "El informe debe tener un ID asignado");

        // Paso 4: Recuperar y verificar
        Informe recuperado = informeRepository.findById(guardado.getIdInfo()).orElse(null);
        System.out.println("[TEST] Informe recuperado de la BD: " + recuperado);
        Assertions.assertNotNull(recuperado);
        Assertions.assertEquals("Informe de Prueba", recuperado.getTitulo());
        Assertions.assertTrue(recuperado.getContenido().contains("Video 2: ALERTA"));

        // Paso 5: Borrar el informe
        informeRepository.deleteById(guardado.getIdInfo());
        System.out.println("[TEST] Informe eliminado de la BD (id=" + guardado.getIdInfo() + ")");
        com.example.gestorcamras.TablaConsolaHelper.imprimirTablaInformes(informeRepository.findAll());
        Assertions.assertFalse(informeRepository.findById(guardado.getIdInfo()).isPresent(), "El informe debe ser eliminado");
    }
}
