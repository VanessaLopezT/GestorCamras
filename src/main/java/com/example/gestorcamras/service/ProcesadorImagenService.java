package com.example.gestorcamras.service;

import com.example.gestorcamras.dto.FiltroDTO;
import com.example.gestorcamras.dto.ImagenDTO;
import com.example.gestorcamras.dto.ImagenProcesadaDTO;
import com.example.gestorcamras.pool.FiltroBasicoFactory;
import com.example.gestorcamras.pool.FiltroOperacion;
import com.example.gestorcamras.pool.FiltroPool;
import com.example.gestorcamras.model.Filtro;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final FiltroPool filtroPool;
    private final ImagenProcesadaService imagenProcesadaService;

    @Autowired
    private FiltroBasicoFactory filtroBasicoFactory;

    public ImagenProcesadaDTO procesarImagen(ImagenDTO imagenOriginal, FiltroDTO filtroDTO) {
        // Obtener un filtro del pool
        Filtro filtro = filtroPool.obtenerFiltro(filtroDTO.getTipo(), filtroDTO.getDescripcion());

        if (filtro == null) {
            throw new IllegalArgumentException("No se pudo obtener un filtro del pool con los parámetros dados.");
        }

        try {
            // Crear el DTO de imagen procesada
            ImagenProcesadaDTO imagenProcesadaDTO = new ImagenProcesadaDTO();
            String nombreProcesado = "Procesada_" + UUID.randomUUID() + "_" + imagenOriginal.getNombre();
            imagenProcesadaDTO.setNombre(nombreProcesado);
            imagenProcesadaDTO.setRutaImagen("processed/" + nombreProcesado);

            imagenProcesadaDTO.setFechaProcesamiento(LocalDateTime.now());
            imagenProcesadaDTO.setTamaño(imagenOriginal.getTamaño()); // Podría variar según el procesamiento
            imagenProcesadaDTO.setRutaImagen("processed/" + imagenOriginal.getNombre());
            imagenProcesadaDTO.setImagenOriginalId(imagenOriginal.getIdImagen());
            imagenProcesadaDTO.setFiltroId(filtroDTO.getIdFiltro());

            // Aquí iría la lógica real de procesamiento de la imagen
            aplicarFiltro(imagenOriginal.getRutaAlmacenamiento(), imagenProcesadaDTO.getRutaImagen(), filtro);

            // Guardar la imagen procesada
            return imagenProcesadaService.guardarImagen(imagenProcesadaDTO);
        } finally {
            // Asegurarnos de devolver el filtro al pool después de usarlo
            filtroPool.devolverFiltro(filtro);
        }
    }

    private void aplicarFiltro(String rutaOriginal, String rutaDestino, Filtro filtro) {
        try {
            // Cargar imagen original
            BufferedImage original = ImageIO.read(new File(rutaOriginal));

            // Obtener el filtro real desde FiltroBasicoFactory (basado en el tipo del filtro del pool)
            FiltroOperacion filtroOperacion = filtroBasicoFactory.obtenerFiltro(filtro.getTipo());

            // Aplicar el filtro
            BufferedImage procesada = filtroOperacion.aplicar(original);

            // Crear el directorio si no existe
            File salida = new File(rutaDestino);
            salida.getParentFile().mkdirs();

            // Guardar imagen procesada
            ImageIO.write(procesada, "png", salida);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen con el filtro: " + filtro.getTipo(), e);
        }
    }



}