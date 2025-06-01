package com.example.gestorcamras.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "filtros_aplicados")
public class FiltroAplicado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_archivo", nullable = false)
    private ArchivoMultimedia archivo;
    
    @Column(name = "nombre_filtro", nullable = false)
    private String nombreFiltro;
    
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDateTime fechaAplicacion;
    
    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArchivoMultimedia getArchivo() {
        return archivo;
    }

    public void setArchivo(ArchivoMultimedia archivo) {
        this.archivo = archivo;
    }

    public String getNombreFiltro() {
        return nombreFiltro;
    }

    public void setNombreFiltro(String nombreFiltro) {
        this.nombreFiltro = nombreFiltro;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }
    
    @Override
    public String toString() {
        return "FiltroAplicado{" +
                "id=" + id +
                ", archivo=" + (archivo != null ? archivo.getIdArchivo() : null) +
                ", nombreFiltro='" + nombreFiltro + '\'' +
                ", fechaAplicacion=" + fechaAplicacion +
                '}';
    }
}
