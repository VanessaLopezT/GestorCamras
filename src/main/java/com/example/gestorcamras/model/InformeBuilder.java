package com.example.gestorcamras.model;

import java.time.LocalDateTime;

/**
 * Builder para crear objetos Informe de manera flexible y reutilizable.
 */
public class InformeBuilder {
    private String titulo;
    private LocalDateTime fechaGeneracion;
    private double tamaño;
    private String contenido;
    private Usuario usuario;

    public InformeBuilder() {}

    public InformeBuilder titulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public InformeBuilder fechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
        return this;
    }

    public InformeBuilder tamaño(double tamaño) {
        this.tamaño = tamaño;
        return this;
    }

    public InformeBuilder contenido(String contenido) {
        this.contenido = contenido;
        return this;
    }

    public InformeBuilder usuario(Usuario usuario) {
        this.usuario = usuario;
        return this;
    }

    /**
     * Permite agregar información adicional al contenido del informe (por ejemplo, resumen de videos, datos extra, etc).
     */
    public InformeBuilder agregarAlContenido(String extra) {
        if (this.contenido == null) {
            this.contenido = "";
        }
        this.contenido += "\n" + extra;
        return this;
    }

    public Informe build() {
        Informe informe = new Informe();
        informe.setTitulo(this.titulo);
        informe.setFechaGeneracion(this.fechaGeneracion);
        informe.setTamaño(this.tamaño);
        informe.setContenido(this.contenido);
        informe.setUsuario(this.usuario);
        // idInfo es autogenerado por la base de datos
        return informe;
    }
}
