package com.example.gestorcamras.builder;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO para la transferencia de datos de Informe.
 * Incluye toda la información necesaria para mostrar un informe en la interfaz de usuario.
 */
@Data
public class InformeDTO {
    private Long idInfo;
    private String titulo;
    private LocalDateTime fechaGeneracion;
    private double tamaño;
    private String contenido;
    
    // Información del usuario que generó el informe
    private Long usuarioId;
    private String nombreUsuario;
    
    // Información del equipo relacionado (si aplica)
    private String nombreEquipo;
    private String ipEquipo;
    private String identificadorEquipo;
    private LocalDateTime fechaRegistroEquipo;
    private boolean activo;
    private String configuracionEquipo;
    private String tipoEquipo;
    private String ubicacionEquipo;
    
    /**
     * Establece la información básica del equipo en el DTO.
     * @param nombre Nombre del equipo
     * @param identificador Identificador único del equipo
     * @param ip Dirección IP del equipo
     */
    public void setDatosEquipo(String nombre, String identificador, String ip) {
        this.nombreEquipo = nombre;
        this.identificadorEquipo = identificador;
        this.ipEquipo = ip;
    }
    
    /**
     * Establece el estado del equipo en el DTO.
     * @param activo Indica si el equipo está activo
     * @param fechaRegistro Fecha de registro del equipo
     */
    public void setEstadoEquipo(boolean activo, LocalDateTime fechaRegistro) {
        this.activo = activo;
        this.fechaRegistroEquipo = fechaRegistro;
    }
    
    /**
     * Establece la configuración del equipo en el DTO.
     * @param configuracion Configuración del equipo
     */
    public void setConfiguracion(String configuracion) {
        this.configuracionEquipo = configuracion;
    }
    
    /**
     * Establece la ubicación y tipo del equipo en el DTO.
     * @param tipo Tipo de equipo
     * @param ubicacion Ubicación física del equipo
     */
    public void setUbicacion(String tipo, String ubicacion) {
        this.tipoEquipo = tipo;
        this.ubicacionEquipo = ubicacion;
    }
}
