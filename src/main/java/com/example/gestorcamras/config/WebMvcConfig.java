package com.example.gestorcamras.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Configuración para servir archivos estáticos desde el directorio de la aplicación
        String uploadDir = Paths.get("archivos_multimedia").toAbsolutePath().toString();
        
        registry.addResourceHandler("/archivos_multimedia/**")
               .addResourceLocations("file:" + uploadDir + "/")
               .setCachePeriod(0);
    }
}
