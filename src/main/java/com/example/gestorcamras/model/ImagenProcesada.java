package com.example.gestorcamras.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenProcesada {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idImgProcesada;

private String nombre;

private LocalDateTime fechaProcesamiento;

private double tamaño;

@Column(columnDefinition = "TEXT")
private String rutaImagen;

@ManyToOne
@JoinColumn(name = "imagen_id")
private Imagen imagenOriginal;

@ManyToOne
@JoinColumn(name = "filtro_id")
private Filtro filtro;
}