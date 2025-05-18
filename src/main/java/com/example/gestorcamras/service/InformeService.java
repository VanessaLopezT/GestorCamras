package com.example.gestorcamras.service;

import com.example.gestorcamras.model.Informe;
import java.util.List;
import java.util.Optional;

public interface InformeService {
    List<Informe> obtenerTodos();
    Optional<Informe> obtenerPorId(Long id);
    Informe guardarInforme(Informe informe);
    void eliminarInforme(Long id);
}
