package com.example.gestorcamras.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idVideo;

private String nombre;

private double tamaño;

private LocalDateTime fechaCaptura;

private Duration duracion;

@Column(columnDefinition = "TEXT")
private String rutaAlmacenamiento;

@ManyToOne
@JoinColumn(name = "camara_id")
private Camara camara;
}