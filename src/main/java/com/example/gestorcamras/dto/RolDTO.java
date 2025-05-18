package com.example.gestorcamras.dto;

import java.util.List;

public class RolDTO {
    private Long idRol;
    private String nombre;
    private String permisos;
    private List<Long> usuarioIds; // IDs de usuarios asociados a este rol

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }

    public List<Long> getUsuarioIds() { return usuarioIds; }
    public void setUsuarioIds(List<Long> usuarioIds) { this.usuarioIds = usuarioIds; }
}
