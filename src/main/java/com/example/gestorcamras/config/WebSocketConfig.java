package com.example.gestorcamras.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
              .setTaskScheduler(heartBeatScheduler())
              .setHeartbeatValue(new long[] {10000, 10000}); // 10 seconds
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
        config.setPathMatcher(new AntPathMatcher("."));
    }
    
    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        scheduler.setPoolSize(1);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Registrar el endpoint WebSocket con soporte para SockJS
        registry.addEndpoint("/ws") // Ruta del endpoint WebSocket
                .setAllowedOriginPatterns("*") // Permitir todos los orígenes
                .setHandshakeHandler(new DefaultHandshakeHandler()) // Manejador de handshake predeterminado
                .withSockJS() // Habilitar compatibilidad con SockJS
                .setHeartbeatTime(10000); // Configurar el intervalo de latido a 10 segundos
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Configurar el canal de entrada de mensajes
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                // Interceptar mensajes entrantes
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                
                // Obtener el comando de manera segura
                StompCommand command = accessor.getCommand();
                if (command == null) {
                    return message; // Ignorar mensajes sin comando
                }
                
                // Registrar eventos de conexión/desconexión
                String sessionId = accessor.getSessionId();
                switch (command) {
                    case CONNECT:
                        System.out.println("Cliente conectado: " + sessionId);
                        break;
                    case DISCONNECT:
                        System.out.println("Cliente desconectado: " + sessionId);
                        break;
                    case SUBSCRIBE:
                        String destination = accessor.getDestination();
                        System.out.println("Nueva suscripción: " + destination + 
                                         " (Sesión: " + sessionId + ")");
                        break;
                    default:
                        // No se requiere acción para otros comandos
                        break;
                }
                
                return message;
            }
        });
    }
}
