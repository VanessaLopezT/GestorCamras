package com.example.gestorcamras.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheImpl<T> implements IRedisCache<T> {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final long EXPIRACION_MINUTOS = 60; // Puedes ajustar tiempo de expiración

    @Override
    public void guardar(String clave, T valor) {
        redisTemplate.opsForValue().set(clave, valor, EXPIRACION_MINUTOS, TimeUnit.MINUTES);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> obtener(String clave) {
        T valor = (T) redisTemplate.opsForValue().get(clave);
        return Optional.ofNullable(valor);
    }

    @Override
    public void eliminar(String clave) {
        redisTemplate.delete(clave);
    }
}
