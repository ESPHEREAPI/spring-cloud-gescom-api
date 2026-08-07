package sid.service_admin.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Point d'acces unique, depuis les services, a la compagnie de l'utilisateur
 * authentifie sur la requete courante (attribut pose par JwtAuthFilter).
 * Null pour un compte sans compagnie (SUPER_ADMIN/SYSTEM_ADMIN).
 * HttpServletRequest est injecte via le proxy request-scope standard de
 * Spring MVC - fonctionne tel quel dans un bean singleton.
 */
@Component
public class TenantContext {

    @Autowired
    private HttpServletRequest request;

    public Long currentCompagnieId() {
        return (Long) request.getAttribute("compagnieId");
    }
}
