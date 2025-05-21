package com.example.gestorcamras.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "equipo")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipo;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String identificador; // Identificador único del equipo

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private Integer puerto;

    @Column(nullable = false)
    private LocalDateTime ultimaConexion;

    @Column(nullable = false)
    private Boolean activo;

    @ManyToMany
    @JoinTable(
        name = "equipo_camara",
        joinColumns = @JoinColumn(name = "equipo_id"),
        inverseJoinColumns = @JoinColumn(name = "camara_id")
    )
    private Set<Camara> camaras = new HashSet<>();
}
