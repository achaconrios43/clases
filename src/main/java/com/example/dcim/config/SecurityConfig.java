package com.example.dcim.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de Spring Security
 *
 * Esta clase configura la seguridad de la aplicación:
 * - Protección de rutas (endpoints)
 * - Autenticación de usuarios
 * - Autorización por roles (ADMIN/USER)
 * - Encriptación de contraseñas con BCrypt
 */
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthSuccessHandler customAuthSuccessHandler;

    /**
     * Configuración de seguridad para API REST (aplicaciones móviles)
     * PRIORIDAD ALTA: Se procesa ANTES que filterChain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")  // Solo aplica a rutas /api/**
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // Permitir todas las peticiones API sin autenticación
            )
            .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF para API
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Sin sesiones (REST API)
            )
            .formLogin(form -> form.disable())  // Deshabilitar formLogin para API
            .httpBasic(basic -> basic.disable());  // Deshabilitar httpBasic

        return http.build();
    }

    /**
     * Configuración de la cadena de filtros de seguridad para aplicación web
     * Define qué URLs están protegidas y qué roles pueden acceder
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de autorización de peticiones
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos (CSS, JS, imágenes) - Acceso público
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()

                // Página de inicio y login - Acceso público
                .requestMatchers("/", "/index", "/home", "/login", "/error").permitAll()

                // Permitir acceso público a registro de usuarios y validación
                .requestMatchers("/user/create", "/user/exists", "/user/validate").permitAll()

                // Permitir acceso al health check de Actuator (para Koyeb)
                .requestMatchers("/actuator/health/**", "/actuator/health").permitAll()

                // TODAS LAS DEMÁS RUTAS: Requieren autenticación
                // Dashboard de estadísticas: accesible por ADMIN, USER, VIEWER y CLIENTE
                .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "USER", "VIEWER", "CLIENTE")

                // Módulos operativos: solo ADMIN y USER (VIEWER no tiene acceso)
                .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/ingresoap/**", "/ingresoap").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/ingreso/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/gestion/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/inventario/**", "/inventario").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/temperaturas/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/plano-sala/plantillas", "/plano-sala/plantillas/**").hasAnyRole("ADMIN", "USER", "VIEWER", "CLIENTE")
                .requestMatchers("/plano-sala/**", "/plano-sala").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/salas", "/salas/**").hasRole("ADMIN")

                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
                )

            // CsrfTokenRequestAttributeHandler: expone _csrf como atributo no-lazy en Thymeleaf (Spring Security 6).
            .csrf(csrf -> csrf
                .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )

            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")  // URL de la página de login personalizada
                .loginProcessingUrl("/login")  // URL donde se procesa el login
                .defaultSuccessUrl("/dashboard", true)  // fallback si no se usa successHandler
                .successHandler(customAuthSuccessHandler)  // handler personalizado (VIEWER=sesión infinita)
                .failureUrl("/login?error=true")  // Redirige al login con error si falla
                .usernameParameter("email")  // Campo del formulario para username (usamos email)
                .passwordParameter("password")  // Campo del formulario para password
                .permitAll()
            )

            // ALWAYS: crea la sesión ANTES de renderizar la vista,
            // evitando IllegalStateException al guardar el CSRF token cuando
            // el buffer HTTP ya está comprometido (respuesta grande en Render/proxy).
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )

            // Configuración del logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Bean de BCryptPasswordEncoder para encriptar contraseñas
     *
     * BCrypt es un algoritmo de hash de una sola vía que:
     * - Genera un salt aleatorio por cada contraseña
     * - Es resistente a ataques de fuerza bruta
     * - Es el estándar recomendado por Spring Security
     *
     * @return PasswordEncoder configurado con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
