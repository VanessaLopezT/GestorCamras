package com.example.gestorcamras.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;

import com.example.gestorcamras.model.Informe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idUsuario;

private String nombre;

private String correo;

private String contrasena;

private LocalDateTime fechaRegistro;

@ManyToOne
@JoinColumn(name = "rol_id")
private Rol rol;

@OneToMany(mappedBy = "propietario")
private List<Camara> camaras;

@OneToMany(mappedBy = "usuario")
private List<Informe> informes;
}