package com.example.gestorcamaras.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCamaraId implements Serializable {
private Long usuarioId;
private Long camaraId;
}