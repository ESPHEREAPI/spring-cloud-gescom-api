package sid.service_admin.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authentifie les appels service-a-service sur /internal/** (ex. le controle
 * de licence appele par microservice-produits) via un secret partage, pas un
 * JWT utilisateur — l'appelant n'agit au nom de personne. Le secret est
 * fourni uniquement par APP_INTERNAL_SERVICE_SECRET (variable
 * d'environnement), jamais dans cloud-config-gescom.
 */
@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Service-Key";

    @Value("${app.internal.service-secret:}")
    private String expectedSecret;

    /**
     * Echoue au demarrage plutot que de laisser le filtre no-op silencieusement
     * (secret vide -> aucun appel /internal/** ne s'authentifie jamais, ce qui
     * s'est deja produit et a coute plusieurs heures de diagnostic).
     */
    @PostConstruct
    void verifierSecretConfigure() {
        if (expectedSecret == null || expectedSecret.isBlank()) {
            throw new IllegalStateException(
                    "APP_INTERNAL_SERVICE_SECRET n'est pas configure (variable d'environnement obligatoire, "
                    + "meme valeur que cote microservice-produits) - les appels internes ne peuvent pas s'authentifier.");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/internal/")
                && expectedSecret != null && !expectedSecret.isBlank()
                && expectedSecret.equals(request.getHeader(HEADER))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "internal-service", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))));
        }

        filterChain.doFilter(request, response);
    }
}
