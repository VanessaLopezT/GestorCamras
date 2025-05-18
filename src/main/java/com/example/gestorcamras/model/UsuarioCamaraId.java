package com.example.gestorcamras.model;

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