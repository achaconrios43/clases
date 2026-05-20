package com.example.dcim.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.dcim.dao.IUsuarioDao;
import com.example.dcim.dao.SitioRepository;
import com.example.dcim.entity.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @Autowired
    private IUsuarioDao usuarioDao;

    @Autowired
    private SitioRepository sitioRepository;

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(Authentication authentication) {
        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Usuario usuario = usuarioDao.findByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("usuario", usuario);
            }
        }
        model.addAttribute("sitios", sitioRepository.findByActivoTrueOrderByNombreAsc());
        return "dashboard";
    }

    @GetMapping("/session/keepalive")
    @ResponseBody
    public ResponseEntity<Void> keepAlive(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        // Accessing an attribute is enough to refresh last-accessed-time for inactivity timeout.
        session.getAttributeNames();
        return ResponseEntity.noContent().build();
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }
}