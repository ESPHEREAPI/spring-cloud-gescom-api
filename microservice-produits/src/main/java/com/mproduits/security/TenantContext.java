package com.mproduits.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * Boutique assignee a l'utilisateur courant (claim du JWT, pose par
     * JwtAuthFilter) - null pour un compte non rattache a une boutique
     * precise (admin, gerant, compte sans compagnie...). A utiliser
     * uniquement via BoutiqueAccessGuard.verifierBoutiqueUtilisateur().
     */
    public Long currentBoutiqueId() {
        return (Long) request.getAttribute("boutiqueId");
    }

    /**
     * Identite verifiee par le JWT (posee par JwtAuthFilter dans le
     * SecurityContext), a utiliser pour toute trace d'audit (qui a saisi/
     * modifie quoi). A ne jamais remplacer par une valeur fournie par le
     * client dans le corps de la requete - celle-ci est falsifiable.
     */
    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
