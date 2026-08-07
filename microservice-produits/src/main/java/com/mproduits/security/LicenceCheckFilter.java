package com.mproduits.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mproduits.exceptions.ErrorDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Verifie la licence de la compagnie de l'utilisateur authentifie (place
 * juste apres JwtAuthFilter, qui pose l'attribut compagnieId). Sans
 * compagnieId (SUPER_ADMIN/SYSTEM_ADMIN, comptes systeme), la requete passe
 * sans verification. 402 (pas 401/403) pour que le frontend distingue une
 * licence expiree d'un probleme d'authentification/autorisation.
 */
@Component
public class LicenceCheckFilter extends OncePerRequestFilter {

    private static final int STATUS_LICENCE_INVALIDE = 402;

    @Autowired
    private LicenceStatusService licenceStatusService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Object compagnieIdAttr = request.getAttribute("compagnieId");
        if (compagnieIdAttr == null || estExempte(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        LicenceCheckResult result = licenceStatusService.check((Long) compagnieIdAttr);
        if (!result.isAllowed()) {
            ErrorDetails errorDetails = new ErrorDetails(
                    LocalDateTime.now(),
                    result.getMessage(),
                    request.getRequestURI(),
                    STATUS_LICENCE_INVALIDE);

            response.setStatus(STATUS_LICENCE_INVALIDE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Lister/creer une boutique reste accessible sans licence active : une
     * compagnie fraichement creee doit pouvoir configurer sa premiere
     * boutique avant meme d'avoir active une licence (sinon blocage total -
     * impossible de creer la boutique qui permettrait de sortir du dialogue
     * de selection affiche a la connexion).
     */
    private boolean estExempte(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String methode = request.getMethod();
        if ("GET".equals(methode) && uri.endsWith("/microservice-produits/all")) {
            return true;
        }
        return "POST".equals(methode) && uri.endsWith("/microservice-produits");
    }
}
