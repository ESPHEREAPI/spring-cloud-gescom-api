package com.mproduits.services;

import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.ProduitRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suppression best-effort d'un Produit orphelin (voir
 * StockRestaurationService.viderStockBoutique). Verifie AVANT de supprimer
 * (ProduitRepositories.estSupprimableSansReferenceRestante) plutot que
 * d'essayer puis d'attraper l'echec : en JPA, des qu'une exception survient
 * pendant l'execution d'une requete, la transaction est marquee
 * "rollback-only" par le fournisseur JPA lui-meme, independamment de tout
 * catch cote Java - un catch autour du DELETE ne l'empeche pas, le commit
 * echoue quand meme ensuite avec UnexpectedRollbackException et annule tout
 * ce que la transaction avait fait, meme les operations reussies avant
 * l'echec. REQUIRES_NEW isole quand meme chaque tentative des autres (utile
 * si un jour cette methode est rappelee en boucle) mais n'est PAS ce qui
 * evite le probleme ci-dessus - c'est bien la verification prealable.
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
        if (!produitRepositories.estSupprimableSansReferenceRestante(produitId)) {
            log.info("Produit {} conserve dans le catalogue (references restantes ailleurs - vente, facture, devis, etc.)", produitId);
            return false;
        }
        prixAchatRepositories.deleteByProduitIdBulk(produitId);
        produitRepositories.deleteByIdBulk(produitId);
        return true;
    }
}
