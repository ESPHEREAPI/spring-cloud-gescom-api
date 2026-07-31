package sid.service_admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sid.service_admin.exceptions.ErrorDetails;
import sid.service_admin.model.Personne;
import sid.service_admin.repository.PersonneRepository;

/**
 * Defense en profondeur : si le compte authentifie a un mot de passe assigne
 * (jamais choisi par son titulaire), bloque tout sauf le strict necessaire
 * pour le changer. Le frontend redirige deja proactivement apres le login ;
 * ce filtre empeche un contournement par appel direct a l'API.
 */
@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final List<String> ROUTES_AUTORISEES = List.of(
            "/auth/login", "/auth/logout", "/users/change-password", "/actuator/health");

    @Autowired
    private PersonneRepository personneRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (ROUTES_AUTORISEES.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Personne personne = personneRepository.findByUserName(authentication.getName()).orElse(null);
            if (personne != null && Boolean.TRUE.equals(personne.getMustChangePassword())) {
                ErrorDetails errorDetails = new ErrorDetails(
                        LocalDateTime.now(),
                        "Vous devez changer votre mot de passe avant de continuer",
                        path,
                        HttpStatus.FORBIDDEN.value());
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
