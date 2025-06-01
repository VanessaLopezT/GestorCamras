package com.example.gestorcamras.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "archivo_multimedia")
public class ArchivoMultimedia {
    
    public enum TipoArchivo {
        FOTO,
        VIDEO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idArchivo;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private String rutaArchivo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoArchivo tipo;

    @Column(nullable = false)
    private LocalDateTime fechaCaptura;

    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne
    @JoinColumn(name = "camara_id", nullable = false)
    private Camara camara;

    @ManyToOne
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;
    
    @OneToMany(mappedBy = "archivo")
    private List<FiltroAplicado> filtrosAplicados;
    
    /**
     * Obtiene una lista de nombres de filtros aplicados a este archivo
     * @return Lista de nombres de filtros
     */
    public List<String> getNombresFiltrosAplicados() {
        if (filtrosAplicados == null || filtrosAplicados.isEmpty()) {
            return List.of();
        }
        return filtrosAplicados.stream()
            .map(FiltroAplicado::getNombreFiltro)
            .distinct()
            .collect(Collectors.toList());
    }
} 