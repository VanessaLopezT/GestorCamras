package com.example.gestorcamras.builder;

import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.model.Informe;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de informes.
 * Permite crear, recuperar y gestionar informes con información detallada de equipos.
 */
public interface InformeService {
    
    /**
     * Obtiene todos los informes existentes.
     */
    List<Informe> obtenerTodos();
    
    /**
     * Busca un informe por su ID.
     */
    Optional<Informe> obtenerPorId(Long id);
    
    /**
     * Guarda un informe en la base de datos.
     */
    Informe guardarInforme(Informe informe);
    
    /**
     * Elimina un informe por su ID.
     */
    void eliminarInforme(Long id);
    
    /**
     * Crea un nuevo informe con la información básica.
     * @param titulo Título del informe
     * @param contenido Contenido principal del informe
     * @param tamaño Tamaño en bytes del informe
     * @param usuario Usuario que genera el informe
     * @return El informe creado y guardado
     */
    Informe crearInformeBasico(String titulo, String contenido, double tamaño, Usuario usuario);
    
    /**
     * Crea un informe detallado para un equipo específico.
     * @param titulo Título del informe
     * @param descripcion Descripción detallada del informe
     * @param equipoId ID del equipo sobre el que se genera el informe
     * @param usuarioId ID del usuario que genera el informe
     * @return El informe creado y guardado
     */
    Informe generarInformeEquipo(String titulo, String descripcion, Long equipoId, Long usuarioId);
    
    /**
     * Genera un informe de actividad del sistema.
     * @param fechaInicio Fecha de inicio del período del informe
     * @param fechaFin Fecha de fin del período del informe
     * @param usuarioId ID del usuario que solicita el informe
     * @return El informe generado
     */
    Informe generarInformeActividad(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long usuarioId);
}
