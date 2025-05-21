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
public class Filtro {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idFiltro;

private String tipo;

@Column(columnDefinition = "TEXT")
private String descripcion;

@OneToMany(mappedBy = "filtro")
private List<ImagenProcesada> imagenesProcesadas;

}