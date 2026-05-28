package com.example.dcim.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.dcim.dao.IUsuarioDao;
import com.example.dcim.entity.Usuario;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que restringe el acceso de VIEWER y CLIENTE
 * exclusivamente a los módulos seleccionados en su perfil (modulosPermitidos).
 *
 * No existe ningún otro bloque de acceso por URL para estos roles:
 * la única fuente de privilegios es el campo modulosPermitidos.
 */
@Component
public class ModulosPermitidosFilter extends OncePerRequestFilter {

    @Autowired
    private IUsuarioDao usuarioDao;

    private static final Set<String> ROLES_RESTRINGIDOS = Set.of("ROLE_VIEWER", "ROLE_CLIENTE");

    // URL prefix → grupo de módulo (el prefijo antes del primer punto en modulosPermitidos)
    // Orden: rutas más específicas primero
    private static final Map<String, String> URL_A_GRUPO = new LinkedHashMap<>();
    static {
        URL_A_GRUPO.put("/plano-sala-plantillas", "planos");
        URL_A_GRUPO.put("/plano-sala-ver",         "planos");
        URL_A_GRUPO.put("/plano-sala",             "planos");
        URL_A_GRUPO.put("/layout-vertical",        "inventario");
        URL_A_GRUPO.put("/inventario-import",      "inventario");
        URL_A_GRUPO.put("/inventario",             "inventario");
        URL_A_GRUPO.put("/ingresoap",              "ingresos");
        URL_A_GRUPO.put("/ingreso",                "ingresos");
        URL_A_GRUPO.put("/gestion",                "gestion");
        URL_A_GRUPO.put("/temperaturas",           "temperaturas");
        URL_A_GRUPO.put("/estadisticas",           "dashboard");
        URL_A_GRUPO.put("/dashboard",              "dashboard");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        boolean esRestringido = auth.getAuthorities().stream()
                .anyMatch(a -> ROLES_RESTRINGIDOS.contains(a.getAuthority()));

        if (!esRestringido) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }

        String grupo = resolverGrupo(path);
        if (grupo == null) {
            // URL fuera del alcance de módulos → dejar pasar (ej: /dashboard directo)
            chain.doFilter(request, response);
            return;
        }

        // Obtener módulos permitidos del usuario desde BD
        Usuario usuario = usuarioDao.findByEmail(auth.getName()).orElse(null);
        if (usuario == null) {
            response.sendRedirect(contextPath + "/dashboard?errorAcceso=1");
            return;
        }

        String raw = usuario.getModulosPermitidos();
        if (raw == null || raw.isBlank()) {
            response.sendRedirect(contextPath + "/dashboard?errorAcceso=1");
            return;
        }

        List<String> modulos = Arrays.asList(raw.split(","));
        String prefijo = grupo + ".";
        boolean tieneAcceso = modulos.stream().anyMatch(m -> m.trim().startsWith(prefijo));

        if (!tieneAcceso) {
            response.sendRedirect(contextPath + "/dashboard?errorAcceso=1");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolverGrupo(String path) {
        for (Map.Entry<String, String> entry : URL_A_GRUPO.entrySet()) {
            if (path.equals(entry.getKey()) || path.startsWith(entry.getKey() + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }
}
