package com.example.gestorcamras.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Configuración para servir archivos estáticos desde el directorio de la aplicación
        String uploadDir = Paths.get("archivos_multimedia").toAbsolutePath().toString();
        
        // Asegurarse de que el directorio exista
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Created directory: " + uploadDir);
            } else {
                logger.warn("Could not create directory: " + uploadDir);
            }
        }
        
        // Registrar el manejador de recursos
        String resourcePath = "file:" + uploadDir.replace("\\", "/") + "/";
        logger.info("Serving static resources from: " + resourcePath);
        
        registry.addResourceHandler("/archivos_multimedia/**")
               .addResourceLocations(resourcePath)
               .setCachePeriod(0);
        
        // Para desarrollo: registrar también el directorio de recursos estáticos de la aplicación
        registry.addResourceHandler("/static/**")
               .addResourceLocations("classpath:/static/")
               .setCachePeriod(0);
    }
}
