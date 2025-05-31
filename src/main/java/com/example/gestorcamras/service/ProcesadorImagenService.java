package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.FiltroDTO;
import com.example.gestorcamras.dto.ImagenDTO;
import com.example.gestorcamras.dto.ImagenProcesadaDTO;
import com.example.gestorcamras.filtros.FiltroImagen;
import com.example.gestorcamras.pool.FiltroObjectPool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcesadorImagenService {
    private final FiltroObjectPool filtroObjectPool;
    private final ImagenProcesadaService imagenProcesadaService;

    public ImagenProcesadaDTO procesarImagen(ImagenDTO imagenOriginal, FiltroDTO filtroDTO) {
        // Obtener un filtro del pool
        FiltroImagen filtro = filtroObjectPool.obtenerFiltro(filtroDTO.getTipo());

        try {
            // Crear el DTO de imagen procesada
            ImagenProcesadaDTO imagenProcesadaDTO = new ImagenProcesadaDTO();
            String nombreProcesado = "Procesada_" + UUID.randomUUID() + "_" + imagenOriginal.getNombre();
            String rutaProcesada = "processed/" + nombreProcesado;
            
            imagenProcesadaDTO.setNombre(nombreProcesado);
            imagenProcesadaDTO.setRutaImagen(rutaProcesada);
            imagenProcesadaDTO.setFechaProcesamiento(LocalDateTime.now());
            imagenProcesadaDTO.setTamaño(imagenOriginal.getTamaño()); // Podría variar según el procesamiento
            imagenProcesadaDTO.setImagenOriginalId(imagenOriginal.getIdImagen());
            imagenProcesadaDTO.setFiltroId(filtroDTO.getIdFiltro());

            // Procesar la imagen con el filtro
            aplicarFiltro(imagenOriginal.getRutaAlmacenamiento(), rutaProcesada, filtro);

            // Guardar la imagen procesada
            return imagenProcesadaService.guardarImagen(imagenProcesadaDTO);
        } finally {
            // Asegurarnos de devolver el filtro al pool después de usarlo
            if (filtro != null) {
                filtroObjectPool.devolverFiltro(filtro);
            }
        }
    }

    private void aplicarFiltro(String rutaOriginal, String rutaDestino, FiltroImagen filtro) {
        try {
            // Cargar imagen original
            BufferedImage original = ImageIO.read(new File(rutaOriginal));
            if (original == null) {
                throw new IOException("No se pudo cargar la imagen desde: " + rutaOriginal);
            }

            // Aplicar el filtro directamente usando la interfaz FiltroImagen
            BufferedImage procesada = filtro.aplicar(original);

            // Crear el directorio si no existe
            File salida = new File(rutaDestino);
            File directorioPadre = salida.getParentFile();
            if (directorioPadre != null && !directorioPadre.exists()) {
                if (!directorioPadre.mkdirs()) {
                    throw new IOException("No se pudo crear el directorio: " + directorioPadre.getAbsolutePath());
                }
            }

            // Guardar imagen procesada
            if (!ImageIO.write(procesada, "png", salida)) {
                throw new IOException("No se encontró un escritor de imagen adecuado para el formato PNG");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen: " + e.getMessage(), e);
        }
    }



}