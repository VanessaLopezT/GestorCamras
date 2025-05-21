package com.example.gestorcamras.redis;

import java.util.Optional;

public interface IRedisCache<T> {
    void guardar(String clave, T valor);
    Optional<T> obtener(String clave);
    void eliminar(String clave);
}
