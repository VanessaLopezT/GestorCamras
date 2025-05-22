package com.example.gestorcamras.redis;

import com.example.gestorcamras.dto.EstadoEquipoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void guardarEstadoEquipo(Long equipoId, EstadoEquipoDTO estado) {
        try {
            String json = objectMapper.writeValueAsString(estado);
            redisTemplate.opsForValue().set("estado:equipo:" + equipoId, json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public EstadoEquipoDTO obtenerEstadoEquipo(Long equipoId) {
        String json = redisTemplate.opsForValue().get("estado:equipo:" + equipoId);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, EstadoEquipoDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
