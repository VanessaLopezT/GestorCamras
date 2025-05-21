package com.example.gestorcamras.Escritorio;

/**
 * Clase DTO para representar un equipo en el sistema.
 */
public class EquipoDTO {
    private final long id;
    private final String nombre;
    private final String ip;
    private final boolean activo;
    
    /**
     * Constructor de la clase EquipoDTO.
     * 
     * @param id Identificador único del equipo
     * @param nombre Nombre descriptivo del equipo
     * @ip Dirección IP del equipo
     * @activo Estado de actividad del equipo
     */
    public EquipoDTO(long id, String nombre, String ip, boolean activo) {
        this.id = id;
        this.nombre = nombre != null ? nombre : "Equipo " + id;
        this.ip = ip != null ? ip : "";
        this.activo = activo;
    }
    
    // Getters
    public long getId() { 
        return id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public String getIp() { 
        return ip; 
    }
    
    public boolean isActivo() { 
        return activo; 
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %d, IP: %s, %s)", 
            nombre, id, ip, activo ? "Activo" : "Inactivo");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquipoDTO equipo = (EquipoDTO) obj;
        return id == equipo.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}