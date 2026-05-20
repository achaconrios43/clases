package com.example.dcim.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Handler personalizado ejecutado tras login exitoso.
 * - VIEWER: sesión sin tiempo de expiración (pantalla de monitoreo 24/7)
 * - Todos: redirigen al dashboard
 */
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String role = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_USER");

        HttpSession session = request.getSession(false);
        if (session != null) {
            switch (role) {
                case "ROLE_VIEWER"  -> session.setMaxInactiveInterval(-1);    // Sin expiración (pantalla 24/7)
                case "ROLE_USER"    -> session.setMaxInactiveInterval(600);   // 10 minutos
                case "ROLE_ADMIN"   -> session.setMaxInactiveInterval(300);   // 5 minutos
                case "ROLE_CLIENTE" -> session.setMaxInactiveInterval(28800); // 8 horas
                default             -> session.setMaxInactiveInterval(180);   // 3 minutos por defecto
            }
        }

        String redirectUrl = "ROLE_CLIENTE".equals(role)
                ? request.getContextPath() + "/estadisticas"
                : request.getContextPath() + "/dashboard";
        response.sendRedirect(redirectUrl);
    }
}
