package com.mproduits.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mproduits.exceptions.ErrorDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Place juste apres LicenceCheckFilter (qui a deja lu compagnieId depuis le
 * token). Un compte sans compagnieId n'est jamais un utilisateur de
 * compagnie (SUPER_ADMIN/SYSTEM_ADMIN) - ces comptes n'ont pas le droit
 * d'acceder aux donnees de gestion d'une compagnie via ce module, meme par
 * appel direct a l'API (le filtre bloque explicitement en 403 plutot que de
 * laisser des requetes/services non scopes retourner des donnees).
 */
@Component
public class TenantScopeFilter extends OncePerRequestFilter {

    private static final int STATUS_ACCES_INTERDIT = 403;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean authentifie = SecurityContextHolder.getContext().getAuthentication() != null;
        boolean sansCompagnie = request.getAttribute("compagnieId") == null;
        String uri = request.getRequestURI();
        // /microservice-produits/health : simple heartbeat de connectivite (voir
        // HealthController), aucune donnee metier - accessible a tout compte
        // authentifie, y compris sans compagnie.
        // /microservice-produits/admin/dashboard/** : vue plateforme reservee aux
        // administrateurs systeme (voir PlatformDashboardController) - chiffres
        // agreges uniquement, protegee par @PreAuthorize plutot que par ce filtre.
        boolean endpointSysteme = uri.startsWith("/actuator/")
                || uri.equals("/microservice-produits/health")
                || uri.startsWith("/microservice-produits/admin/dashboard/");

        if (authentifie && sansCompagnie && !endpointSysteme) {
            ErrorDetails errorDetails = new ErrorDetails(
                    LocalDateTime.now(),
                    "Acces refuse : les comptes administrateur systeme n'ont pas le droit de consulter les donnees de gestion d'une compagnie.",
                    request.getRequestURI(),
                    STATUS_ACCES_INTERDIT);

            response.setStatus(STATUS_ACCES_INTERDIT);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
