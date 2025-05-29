package com.example.gestorcamras.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCamara;

    private String nombre;

    private String ip;

    private boolean activa;

    private String tipo;

    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario propietario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @OneToMany(mappedBy = "camara", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos;

    @OneToMany(mappedBy = "camara", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes;
}
