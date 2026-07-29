package sid.service_admin.security;

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

    @Value("${app.internal.serviceSecret:}")
    private String expectedSecret;

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
