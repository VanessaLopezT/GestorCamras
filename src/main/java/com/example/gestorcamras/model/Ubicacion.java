package com.example.gestorcamras.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID primario generado automáticamente

    private double latitud;

    private double longitud;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Camara> camaras;

    // Ya no es necesario constructor con id, Lombok se encarga
}
