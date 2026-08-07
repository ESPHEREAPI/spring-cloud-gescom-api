package com.mproduits.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Point d'acces unique, depuis les services/repositories, a la compagnie de
 * l'utilisateur authentifie sur la requete courante (attribut pose par
 * JwtAuthFilter). Evite de re-lire "compagnieId" en dur un peu partout.
 * Null pour un compte sans compagnie (SUPER_ADMIN/SYSTEM_ADMIN) - ces
 * endpoints sont deja bloques en amont par TenantScopeFilter pour les
 * routes metier, donc currentCompagnieId() ne doit normalement pas etre
 * appele avec un resultat null en dehors des routes explicitement exemptees.
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
