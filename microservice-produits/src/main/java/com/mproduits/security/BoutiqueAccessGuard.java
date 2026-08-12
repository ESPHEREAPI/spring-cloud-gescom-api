package com.mproduits.security;

import com.mproduits.repositories.BoutiqueRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /**
     * Verification stricte pour les ecrans de caisse/vente (argent, tickets,
     * annulations) : en plus de l'appartenance a la compagnie, un compte
     * CAISSIER ne peut agir que sur SA boutique assignee (JWT), pas
     * seulement une boutique de la meme compagnie - empeche un caissier de
     * la boutique A de consulter/agir sur la caisse de la boutique B.
     * Les autres roles (admin, gerant...) ne sont pas restreints ici, pour
     * ne pas casser les ecrans de supervision multi-boutique (Dashboard,
     * Approvisionnement, Historique Caisse consulte par un gerant...).
     */
    public void verifierBoutiqueUtilisateur(Long boutiqueId) {
        verifierAppartientALaCompagnieCourante(boutiqueId);

        boolean estCaissier = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(a -> "ROLE_CAISSIER".equals(a.getAuthority()));
        if (!estCaissier) {
            return;
        }

        Long boutiqueUtilisateur = tenantContext.currentBoutiqueId();
        if (boutiqueUtilisateur == null || !boutiqueUtilisateur.equals(boutiqueId)) {
            throw new TenantScopeException("Acces refuse : vous ne pouvez agir que sur votre boutique assignee.");
        }
    }

    /**
     * Verification pour les ecrans de reporting multi-boutique (Controle
     * Vente, Marge Caisse, Historique vente) qui acceptent desormais une
     * boutique, plusieurs boutiques, ou aucune (= toute la compagnie).
     * Un CAISSIER reste pinne a sa propre boutique (JWT) : il ne peut ni
     * choisir "toute la compagnie" (liste vide/nulle), ni consulter une
     * autre boutique que la sienne. Les autres roles peuvent demander
     * n'importe quel sous-ensemble de boutiques, du moment qu'elles
     * appartiennent toutes a leur compagnie.
     */
    public void verifierBoutiquesUtilisateur(List<Long> boutiqueIds) {
        boolean estCaissier = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(a -> "ROLE_CAISSIER".equals(a.getAuthority()));

        if (estCaissier) {
            Long boutiqueUtilisateur = tenantContext.currentBoutiqueId();
            boolean estUniquementSaBoutique = boutiqueIds != null && boutiqueIds.size() == 1
                    && boutiqueIds.get(0).equals(boutiqueUtilisateur);
            if (boutiqueUtilisateur == null || !estUniquementSaBoutique) {
                throw new TenantScopeException("Acces refuse : vous ne pouvez agir que sur votre boutique assignee.");
            }
            return;
        }

        if (boutiqueIds == null || boutiqueIds.isEmpty()) {
            return;
        }
        for (Long boutiqueId : boutiqueIds) {
            verifierAppartientALaCompagnieCourante(boutiqueId);
        }
    }
}
