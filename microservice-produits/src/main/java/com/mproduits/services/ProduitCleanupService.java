package com.mproduits.services;

import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.ProduitRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suppression best-effort d'un Produit orphelin (voir
 * StockRestaurationService.reinitialiserBoutique). Isolee dans sa propre
 * transaction (REQUIRES_NEW) : un DataIntegrityViolationException capture
 * dans la MEME transaction que l'appelant marque quand meme celle-ci
 * "rollback-only" cote Spring (le catch ne suffit pas a l'empecher), ce qui
 * annulait silencieusement TOUTES les suppressions deja faites par
 * reinitialiserBoutique des qu'un seul produit avait une reference bloquante
 * ailleurs (ex. Facturecommandefournisseur). Avec REQUIRES_NEW, l'echec
 * d'un produit reste confine a sa propre transaction et n'affecte pas le reste.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProduitCleanupService {

    private final ProduitRepositories produitRepositories;
    private final PrixAchatRepositories prixAchatRepositories;
    private final PointVenteRepositories pointVenteRepositories;

    /**
     * A appeler depuis un contexte SANS transaction ambiante (ex. un
     * controleur) - voir StockRestaurationService.viderStockBoutique pour le
     * pourquoi de cette contrainte.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean essayerSupprimerProduitOrphelin(Long produitId) {
        if (pointVenteRepositories.countByProduitId(produitId) > 0) {
            // Encore utilise dans une autre boutique - jamais supprime du catalogue.
            return false;
        }
        try {
            prixAchatRepositories.deleteByProduitIdBulk(produitId);
            produitRepositories.deleteByIdBulk(produitId);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.warn("Produit {} conserve dans le catalogue (references restantes, ex. facture fournisseur)", produitId);
            return false;
        }
    }
}
