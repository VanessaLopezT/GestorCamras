package com.example.gestorcamras.redis;

import com.example.gestorcamras.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Import(JacksonConfig.class)
public class RedisConfig {

    private final ObjectMapper objectMapper;

    public RedisConfig(JacksonConfig jacksonConfig) {
        this.objectMapper = jacksonConfig.objectMapper();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configurar serializadores
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Usar el ObjectMapper ya configurado
        ObjectMapper redisObjectMapper = this.objectMapper.copy();
        
        // Asegurarse de que el módulo JavaTime esté registrado
        if (!redisObjectMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")) {
            redisObjectMapper.registerModule(new JavaTimeModule());
        }
        
        // Crear serializador con el ObjectMapper configurado
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setDefaultSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}
