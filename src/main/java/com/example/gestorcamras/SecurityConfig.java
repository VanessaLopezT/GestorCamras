package com.example.gestorcamras;

import com.example.gestorcamras.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authenticationProvider());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Lista de patrones de URL que no requieren autenticación
        String[] publicPaths = {
            "/", "/login", "/error", 
            "/css/**", "/js/**", "/images/**", "/webjars/**",
            "/api/auth/**",
            "/ws/**",
            "/ws/websocket/**",
            "/topic/**",
            "/queue/**",
            "/app/**",
            "/user/queue/**",
            "/sockjs/**",
            "/api/equipos/**",
            "/api/equipos/*/camaras",
            "/api/camaras/equipo/*"
        };

        return http
                // Deshabilitar CSRF para endpoints específicos
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/ws/**", "/ws/websocket/**", "/topic/**", "/queue/**", "/app/**", "/user/queue/**", "/sockjs/**")
                    .ignoringRequestMatchers("/api/equipos")
                    .ignoringRequestMatchers("/login")
                    .ignoringRequestMatchers("/api/camaras")
                )
                // Configuración CORS
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(java.util.List.of("*"));
                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setExposedHeaders(java.util.List.of("Authorization", "Cache-Control", "Content-Type"));
                    corsConfig.setAllowCredentials(true);
                    corsConfig.setMaxAge(3600L);
                    return corsConfig;
                }))
                // Configuración de autorización
                .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos
                    .requestMatchers(publicPaths).permitAll()
                    // Endpoints de administración
                    .requestMatchers("/ADMINISTRADOR/**").hasRole("ADMINISTRADOR")
                    // API REST (requiere autenticación de operador)
                    .requestMatchers("/api/**").hasAnyRole("OPERADOR", "ADMINISTRADOR")
                    // Cualquier otra solicitud requiere autenticación
                    .anyRequest().authenticated()
                )
                // Configuración del formulario de login
                .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true)
                    .permitAll()
                )
                // Configuración de logout
                .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )
                // Configuración de la sesión
                .sessionManagement(session -> session
                    .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation().migrateSession()
                    .maximumSessions(1)
                    .expiredUrl("/login?expired")
                )
                // Configuración de cabeceras de seguridad
                .headers(headers -> {
                    headers.frameOptions(frame -> frame.sameOrigin())
                          .httpStrictTransportSecurity(hsts -> hsts
                              .includeSubDomains(true)
                              .maxAgeInSeconds(31536000)
                          )
                          .contentSecurityPolicy(csp -> csp
                              .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self' ws: wss:;")
                          );
                })
                // Configuración para WebSocket
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                          .sessionFixation().migrateSession()
                          .maximumSessions(1)
                          .expiredUrl("/login?expired");
                })
                .build();
    }
}
