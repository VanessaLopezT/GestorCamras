package com.example.gestorcamras.dto;

import com.example.gestorcamras.model.Equipo;
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
