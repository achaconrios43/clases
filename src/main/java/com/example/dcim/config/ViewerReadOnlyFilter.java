package com.example.dcim.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de seguridad para el rol VIEWER.
 *
 * VIEWER puede navegar libremente en GET (lectura de páginas/datos),
 * pero no puede ejecutar operaciones de escritura (POST, PUT, DELETE, PATCH).
 * Cualquier intento de escritura redirige al dashboard con un mensaje de error.
 */
@Component
public class ViewerReadOnlyFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        if (WRITE_METHODS.contains(request.getMethod().toUpperCase())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                boolean isViewer = auth.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_VIEWER".equals(a.getAuthority()));
                if (isViewer) {
                    // Redirige con parámetro para mostrar mensaje en el dashboard
                    response.sendRedirect(request.getContextPath() + "/dashboard?errorViewer=1");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
