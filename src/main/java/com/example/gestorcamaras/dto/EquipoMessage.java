package com.example.gestorcamaras.dto;

import com.example.gestorcamaras.model.Equipo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoMessage {
    private String type; // "ADD", "UPDATE", "DELETE"
    private Equipo equipo;
}
