package com.example.dcim.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.dcim.dao.IUsuarioDao;

/**
 * Agrega al modelo de cada vista el conjunto de secciones habilitadas
 * para usuarios VIEWER y CLIENTE, basado en su campo modulosPermitidos.
 *
 * La variable 'usuarioSecciones' es un Set<String> con los prefijos de grupo
 * (ej: "dashboard", "inventario", "ingresos", "gestion", "temperaturas", "planos").
 * Las plantillas Thymeleaf la usan para mostrar/ocultar enlaces de navegación.
 */
@ControllerAdvice
public class ModulosPermitidosAdvice {

    @Autowired
    private IUsuarioDao usuarioDao;

    @ModelAttribute
    public void agregarSeccionesAlModelo(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return;

        boolean esRestringido = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_VIEWER".equals(a.getAuthority())
                            || "ROLE_CLIENTE".equals(a.getAuthority()));
        if (!esRestringido) return;

        usuarioDao.findByEmail(auth.getName()).ifPresent(usuario -> {
            String raw = usuario.getModulosPermitidos();
            if (raw == null || raw.isBlank()) return;

            Set<String> secciones = new HashSet<>();
            for (String modulo : Arrays.asList(raw.split(","))) {
                String m = modulo.trim();
                if (m.isEmpty()) continue;
                int dot = m.indexOf('.');
                if (dot > 0) {
                    secciones.add(m.substring(0, dot)); // grupo (ej: "inventario")
                }
                secciones.add(m); // clave completa (ej: "inventario.ver")
            }
            model.addAttribute("usuarioSecciones", secciones);
        });
    }
}
