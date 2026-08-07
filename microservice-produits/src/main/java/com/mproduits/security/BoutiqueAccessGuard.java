package com.mproduits.security;

import com.mproduits.repositories.BoutiqueRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Verifie qu'une boutique demandee par un controleur (parametre boutiqueId)
 * appartient bien a la compagnie de l'utilisateur courant, avant d'executer
 * la requete. Necessaire car la plupart des entites metier (Facture, Devis,
 * Vente, Commande, ...) n'ont pas de colonne compagnie directe : leur scope
 * compagnie se deduit de Boutique.compagnie, mais rien n'empechait jusqu'ici
 * un appel de passer le boutiqueId d'une AUTRE compagnie pour lire ses
 * donnees. A appeler en tout debut de chaque methode de controleur qui
 * recoit un boutiqueId.
 */
@Component
public class BoutiqueAccessGuard {

    @Autowired
    private BoutiqueRepositories boutiqueRepository;

    @Autowired
    private TenantContext tenantContext;

    /**
     * Leve TenantScopeException (-> 403) si boutiqueId n'appartient pas a la
     * compagnie de l'utilisateur courant. Ne fait rien pour un compte sans
     * compagnie (SUPER_ADMIN/SYSTEM_ADMIN) : ces endpoints sont deja bloques
     * en amont par TenantScopeFilter.
     */
    public void verifierAppartientALaCompagnieCourante(Long boutiqueId) {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null || boutiqueId == null) {
            return;
        }
        boolean appartient = boutiqueRepository.findById(boutiqueId)
                .map(b -> compagnieId.equals(b.getCompagnieId()))
                .orElse(false);
        if (!appartient) {
            throw new TenantScopeException("Cette boutique n'appartient pas a votre compagnie.");
        }
    }
}
