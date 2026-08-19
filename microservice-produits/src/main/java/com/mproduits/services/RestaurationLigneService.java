package com.mproduits.services;

import com.mproduits.enums.ModeRestauration;
import com.mproduits.enums.MovementType;
import com.mproduits.enums.TypeMagasin;
import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import com.mproduits.model.Compagnie;
import com.mproduits.model.Entreprise;
import com.mproduits.model.HistoriqueRestaurationStock;
import com.mproduits.model.Magasin;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixAchat;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.model.StockMovement;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.HistoriqueRestaurationStockRepository;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.StockMovementRepository;
import com.mproduits.security.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applique UNE ligne resolue de restauration de stock dans sa PROPRE
 * transaction (REQUIRES_NEW) - voir StockRestaurationService.appliquerImport.
 *
 * Necessaire pour les gros fichiers reels : traiter des centaines de lignes
 * (Produit, PointVente, PrixArticles, StockMovement,
 * HistoriqueRestaurationStock) dans UNE seule transaction/session Hibernate
 * finissait par planter au flush avec "Found shared references to a
 * collection" sur com.mproduits.model.Produit.facturecommandefournisseurCollection
 * - jamais reproduit avec quelques dizaines de lignes, systematique avec un
 * fichier reel de ~1000 lignes/~500 references distinctes. Une transaction
 * par ligne garde chaque session Hibernate courte (peu d'entites Produit
 * geree simultanement) et evite le probleme, au prix de l'atomicite globale
 * du lot : chaque ligne reussit ou echoue independamment des autres -
 * batchId dans HistoriqueRestaurationStock permet de retrouver exactement
 * lesquelles sont passees.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurationLigneService {

    private final ProduitRepositories produitRepositories;
    private final CategorieRepositories categorieRepositories;
    private final MagasinRepositories magasinRepositories;
    private final PointVenteRepositories pointVenteRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final PrixAchatRepositories prixAchatRepositories;
    private final StockMovementRepository stockMovementRepository;
    private final HistoriqueRestaurationStockRepository historiqueRestaurationStockRepository;
    private final TenantContext tenantContext;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void appliquerLigne(StockRestaurationService.LigneResolue ligne, String batchId, ModeRestauration mode,
            String username, Date maintenant, LocalDateTime maintenantLdt, Entreprise entrepriseActive) {
        Produit produit = ligne.nouveauProduit() ? creerProduit(ligne) : ligne.produit();
        PointVente pointVente;

        if (ligne.nouveauPointVente()) {
            // Premiere reception pour ce produit dans cette boutique - vrai
            // aussi bien pour un produit tout neuf que pour un produit deja
            // au catalogue mais jamais stocke ici (catalogue partage entre
            // boutiques). stockInitial reste a zero et entreeProduit porte
            // la quantite (meme convention que ServiceCommande, "premiere
            // reception") - stockInitial ET entreeProduit mis tous les deux
            // a la quantite du fichier faisait apparaitre le double de la
            // vraie quantite recue sur l'ecran Approvisionnement (Quantite =
            // entreeProduit + stockInitial).
            Magasin magasin = resolveOuCreerMagasinPointDeVente(ligne.boutique());
            pointVente = new PointVente();
            pointVente.setBoutique(ligne.boutique());
            pointVente.setDepotId(magasin);
            pointVente.setEntreprise(entrepriseActive);
            pointVente.setProduit(produit);
            pointVente.setDateReception(maintenant);
            pointVente.setStockInitial(BigDecimal.ZERO);
            pointVente.setStockFinalTheorie(ligne.nouvelleQuantite());
            pointVente.setEntreeProduit(ligne.nouvelleQuantite());
            pointVente.setSortiProduit(BigDecimal.ZERO);
            pointVente = pointVenteRepositories.save(pointVente);

            // pa.actif = TRUE est requis par findLatestActiveByProduitBoutiqueAndEntreprise
            // (join sur prixarticlesCollection) - sans cette ligne, ce
            // PointVente resterait invisible a toute future recherche de
            // stock actif pour ce produit/boutique.
            PrixArticles prixArticles = new PrixArticles();
            prixArticles.setActif(true);
            prixArticles.setDateCreation(maintenant);
            prixArticles.setEntreprise(entrepriseActive);
            prixArticles.setPointVente(pointVente);
            prixArticles.setPrixVenteNet(ligne.prixVente());
            prixArticles.setPrixVenteTTC(ligne.prixVente());
            prixArticles.setRemise(BigDecimal.ZERO);
            prixArticles.setTva(BigDecimal.ZERO);
            prixArticlesRepositories.save(prixArticles);

            if (ligne.prixAchat() != null && ligne.prixAchat().compareTo(BigDecimal.ZERO) > 0) {
                PrixAchat prixAchat = new PrixAchat();
                prixAchat.setProduit(produit);
                prixAchat.setDatedebut(maintenant);
                prixAchat.setPrix(ligne.prixAchat());
                prixAchat.setUsercreat(username);
                prixAchatRepositories.save(prixAchat);
            }
        } else {
            pointVente = pointVenteRepositories
                    .findLatestActiveByProduitBoutiqueAndEntreprise(produit, ligne.boutique(), entrepriseActive)
                    .orElseThrow(() -> new IllegalStateException(
                            "Aucun point de vente actif pour " + ligne.reference() + " / " + ligne.boutiqueNom()));
            // entreeProduit REMPLACE par la nouvelle quantite (pas accumule,
            // comme InventairesService.saveStock) et sortiProduit remis a
            // zero. stockInitial remis a zero aussi (contrairement a
            // InventairesService, qui n'y touche pas) : une ligne deja creee
            // par une restauration anterieure au correctif du
            // double-comptage y avait la quantite entiere, et sans cette
            // remise a zero ici, rejouer la restauration ne suffisait pas a
            // corriger l'affichage (Quantite = entreeProduit + stockInitial)
            // sur l'ecran Approvisionnement.
            pointVente.setStockFinalTheorie(ligne.nouvelleQuantite());
            pointVente.setEntreeProduit(ligne.nouvelleQuantite());
            pointVente.setStockInitial(BigDecimal.ZERO);
            pointVente.setSortiProduit(BigDecimal.ZERO);
            pointVenteRepositories.save(pointVente);
        }

        StockMovement mouvement = StockMovement.builder()
                .produit(produit)
                .pointVente(pointVente)
                .quantite(ligne.nouvelleQuantite().subtract(ligne.ancienneQuantite()))
                .stockAvant(ligne.ancienneQuantite())
                // MovementType.INITIALISATION serait plus precis mais
                // type_mouvement est une colonne ENUM MySQL native creee
                // avant l'ajout de toute nouvelle valeur (meme piege que
                // Permission.operationType/PRINT) - reutilise AJUSTEMENT,
                // deja present en base, le motif ci-dessous precise le
                // contexte reel (restauration + lot).
                .typeMouvement(MovementType.AJUSTEMENT)
                .motif("Restauration de stock (" + mode + "), lot " + batchId)
                .usernameCreate(username)
                .dateCreation(maintenantLdt)
                .build();
        stockMovementRepository.save(mouvement);

        HistoriqueRestaurationStock historique = new HistoriqueRestaurationStock();
        historique.setBatchId(batchId);
        historique.setProduit(produit);
        historique.setBoutique(ligne.boutique());
        historique.setCompagnie(ligne.boutique().getCompagnie());
        historique.setAncienneQuantite(ligne.ancienneQuantite());
        historique.setNouvelleQuantite(ligne.nouvelleQuantite());
        historique.setMode(mode);
        historique.setUtilisateur(username);
        historique.setDateRestauration(maintenant);
        historiqueRestaurationStockRepository.save(historique);
    }

    /**
     * Cree le produit manquant a partir des colonnes informatives du fichier
     * (Produit/Categorie servent ici, pas seulement d'affichage). Reference
     * et compagnie sont les seuls champs garantis ; Produit (libelle) retombe
     * sur la reference si absente du format ou vide.
     */
    private Produit creerProduit(StockRestaurationService.LigneResolue ligne) {
        Long compagnieId = tenantContext.currentCompagnieId();
        Produit produit = new Produit();
        produit.setReference(ligne.reference());
        produit.setLibelle(ligne.produitLibelle() != null && !ligne.produitLibelle().isBlank()
                ? ligne.produitLibelle() : ligne.reference());
        produit.setDeletes(Boolean.FALSE);
        produit.setPrixVenteModifiable(Boolean.FALSE);
        produit.setUsername(tenantContext.currentUsername());
        produit.setCompagnie(new Compagnie(compagnieId));
        if (ligne.categorieLibelle() != null && !ligne.categorieLibelle().isBlank()) {
            produit.setCategorie(resolveOuCreerCategorie(ligne.categorieLibelle(), compagnieId));
        }
        return produitRepositories.save(produit);
    }

    private Categories resolveOuCreerCategorie(String libelle, Long compagnieId) {
        return categorieRepositories.findByLibelleIgnoreCaseAndCompagnie_Id(libelle, compagnieId)
                .orElseGet(() -> {
                    Categories categorie = new Categories();
                    categorie.setLibelle(libelle);
                    categorie.setCompagnieId(compagnieId);
                    return categorieRepositories.save(categorie);
                });
    }

    /**
     * Depot "point de vente" de la boutique - cree une fois pour toutes s'il
     * n'en existe encore aucun, pour qu'une compagnie qui initialise une
     * nouvelle boutique de A a Z n'ait pas a configurer un Magasin a la main
     * au prealable (voir Boutique/Magasin, aucune creation automatique
     * n'existait avant pour ce cas).
     */
    private Magasin resolveOuCreerMagasinPointDeVente(Boutique boutique) {
        Long compagnieId = tenantContext.currentCompagnieId();
        return magasinRepositories.findFirstByBoutique_IdAndCompagnie_Id(boutique.getId(), compagnieId)
                .orElseGet(() -> {
                    Magasin magasin = new Magasin();
                    magasin.setLibelle("Point de vente - " + boutique.getNom());
                    magasin.setCode(boutique.getCode());
                    magasin.setBoutiqueId(boutique);
                    magasin.setTypeMagasin(TypeMagasin.POINT_DE_VENTE);
                    magasin.setCompagnieId(compagnieId);
                    return magasinRepositories.save(magasin);
                });
    }
}
