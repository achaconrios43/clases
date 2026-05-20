package com.example.dcim.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Fuerza creación de sesión HTTP ANTES de que Thymeleaf empiece a renderizar.
 *
 * Problema raíz: login.html tiene ~36KB de nav HTML antes del <form>.
 * Eso llena el buffer Tomcat (8KB) y fuerza varios flushes → respuesta committed.
 * Cuando th:action="@{/login}" dispara CSRF lazy loading + getSession() → falla
 * porque no se puede agregar Set-Cookie después de commit → form truncado.
 *
 * Solución: llamar request.getSession(true) aquí, ANTES de cualquier flush,
 * de modo que JSESSIONID esté en headers (aún no enviados) y el CSRF token
 * se pueda guardar en sesión sin necesidad de modificar headers.
 */
@Component
@Order(1)
public class EagerSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Solo para páginas web (no APIs, no recursos estáticos)
        if (!uri.startsWith("/api/")
                && !uri.startsWith("/actuator/")
                && !uri.contains(".")) {
            request.getSession(true);
        }

        filterChain.doFilter(request, response);
    }
}
