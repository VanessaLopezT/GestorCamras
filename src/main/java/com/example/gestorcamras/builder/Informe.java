package com.example.gestorcamras.builder;

import com.example.gestorcamras.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Clase que representa un informe generado para un equipo.
 * Incluye toda la información relevante del equipo en el momento de generación.
 */
@Data
@Entity
@Table(name = "informes")
public class Informe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInfo;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;
    
    @Column(name = "tamano_bytes")
    private double tamaño;
    
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;
    
    // Información del equipo
    @Column(name = "nombre_equipo", nullable = false)
    private String nombreEquipo;
    
    @Column(name = "ip_equipo")
    private String ipEquipo;
    
    @Column(name = "identificador_equipo", unique = true, nullable = false)
    private String identificadorEquipo;
    
    @Column(name = "fecha_registro_equipo")
    private LocalDateTime fechaRegistroEquipo;
    
    @Column(name = "activo")
    private boolean activo;
    
    @Lob
    @Column(columnDefinition = "TEXT", name = "configuracion_equipo")
    private String configuracionEquipo; // JSON con la configuración del equipo
    
    @Column(name = "tipo_equipo")
    private String tipoEquipo;
    
    @Column(name = "ubicacion_equipo")
    private String ubicacionEquipo;
    
    // Relación con el usuario que generó el informe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}