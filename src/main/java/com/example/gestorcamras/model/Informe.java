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
public class Informe {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idInfo;

private String titulo;

private LocalDateTime fechaGeneracion;

private double tamaño;

@Column(columnDefinition = "TEXT")
private String contenido;

@ManyToOne
@JoinColumn(name = "usuario_id")
private Usuario usuario;
}