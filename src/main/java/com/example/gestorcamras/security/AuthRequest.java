package com.example.gestorcamras.security;

import lombok.Data;

@Data
public class AuthRequest {
    private String correo;
    private String contrasena;
}