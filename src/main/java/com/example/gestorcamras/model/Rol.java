package com.example.gestorcamras.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idRol;

private String nombre;

@Column(columnDefinition = "TEXT")
private String permisos;

@OneToMany(mappedBy = "rol")
private List<Usuario> usuarios;
}