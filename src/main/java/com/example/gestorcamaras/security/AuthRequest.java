package com.example.gestorcamaras.security;

import lombok.Data;

@Data
public class AuthRequest {
    private String correo;
    private String contrasena;
}