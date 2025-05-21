package com.example.gestorcamras.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(UsuarioCamaraId.class)
public class UsuarioCamara {
	@Id
	private Long usuarioId;

	@Id
	private Long camaraId;

	private LocalDateTime fechaAsignacion;

	private String permisoEspecial;

	@ManyToOne
	@JoinColumn(name = "usuarioId", insertable = false, updatable = false)
	private Usuario usuario;

	@ManyToOne
	@JoinColumn(name = "camaraId", insertable = false, updatable = false)
	private Camara camara;

}