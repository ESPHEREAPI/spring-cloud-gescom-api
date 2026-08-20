/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.TransfertStockRequest;
import com.mproduits.dto.TransfertStockResponse;
import com.mproduits.model.Commande;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Magasin;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.PrixVente;
import com.mproduits.model.Produit;
import com.mproduits.model.TransfertStock;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.PrixVenteRepositories;
import com.mproduits.repositories.TransfertStockRepository;
import com.mproduits.security.TenantContext;
import java.awt.PointerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service
@Transactional
public class TransfertStockService {

    @Autowired
    private TransfertStockRepository transfertStockRepository;

    @Autowired
    private StockTransfertService stockService;

    @Autowired
    private CommandeRepositories commandeRepository;

    @Autowired
    private PointVenteRepositories pointVenteRepository;
    @Autowired
    private EntrepriseRepositories entrepriseRepositories;
    @Autowired
    private EntrepriseService entrepriseService;
    @Autowired
    private TenantContext tenantContext;

    @Autowired
    PrixVenteRepositories prixVenteRepositories;
    @Autowired
    PrixArticlesRepositories prixArticlesRepositories;

    /**
     * Effectue un transfert de stock complet
     *
     * Étapes: 1. Valider les données d'entrée 2. Récupérer les informations des
     * dépôts et du produit 3. Vérifier la disponibilité du stock 4. Mettre à
     * jour le dépôt source (déduction) 5. Mettre à jour le dépôt destination
     * (ajout) 6. Enregistrer l'historique du transfert
     *
     * @param requete Requête de transfert contenant tous les détails
     * @return Réponse avec détails du transfert effectué
     * @throws IllegalArgumentException si la requête est invalide
     */
    // Un article qui arrive dans une boutique par transfert doit apparaitre
    // immediatement en caisse - voir la meme justification sur
    // ServiceCommande.addProduitByPointVente.
    @CacheEvict(value = {"topArticles", "produits", "articleAutocomplete"}, allEntries = true)
    public TransfertStockResponse effectuerTransfert(TransfertStockRequest requete) {
        log.info("Début du transfert de stock: {} unités du produit {} de {} vers {}",
                requete.getQuantite(), requete.getProduitId(),
                requete.getMagasinSourceId(), requete.getMagasinDestinationId());

        //recuperation la companie
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        // ÉTAPE 1: Validations
        validationTransfert(requete);

        // ÉTAPE 2: Récupérer les données
        Magasin magasinSource = stockService.getMagasin(requete.getMagasinSourceId());
        Magasin magasinDestination = stockService.getMagasin(requete.getMagasinDestinationId());
        Produit produit = stockService.getProduit(requete.getProduitId());

        // ÉTAPE 3: Vérifier la disponibilité
        if (!stockService.verifierDisponibiliteStock(
                requete.getMagasinSourceId(),
                requete.getProduitId(),
                requete.getQuantite())) {
            throw new IllegalArgumentException(
                    "Stock insuffisant pour le produit " + produit.getLibelle()
                    + " dans le magasin " + magasinSource.getLibelle());
        }

        // ÉTAPE 4: Mettre à jour le stock source (déduction)
        BigDecimal stockSourceAvant = stockService.getStockActuel(
                requete.getMagasinSourceId(), requete.getProduitId());
        mettreAJourSourceTransfert(magasinSource, produit, requete.getQuantite());
        BigDecimal stockSourceApres = stockService.getStockActuel(
                requete.getMagasinSourceId(), requete.getProduitId());

        log.debug("Stock source avant: {}, après: {}", stockSourceAvant, stockSourceApres);

        // ÉTAPE 5: Mettre à jour le stock destination (ajout)
        BigDecimal stockDestinationAvant = stockService.getStockActuel(
                requete.getMagasinDestinationId(), requete.getProduitId());
        mettreAJourDestinationTransfert(magasinSource, magasinDestination, produit, requete.getQuantite());
        BigDecimal stockDestinationApres = stockService.getStockActuel(
                requete.getMagasinDestinationId(), requete.getProduitId());

        log.debug("Stock destination avant: {}, après: {}", stockDestinationAvant, stockDestinationApres);

        // ÉTAPE 6: Calculer la valeur estimée
        BigDecimal valeurEstimation = stockService.calculerValeurEstimee(
                requete.getMagasinSourceId(), requete.getProduitId(), requete.getQuantite());

        // ÉTAPE 7: Enregistrer l'historique du transfert
        TransfertStock transfert = new TransfertStock();
        transfert.setSource(magasinSource);
        transfert.setDestination(magasinDestination);
        transfert.setProduit(produit);
        transfert.setQuantite(requete.getQuantite());
        transfert.setDateTransfert(new Date());
        transfert.setValeurEstimation(valeurEstimation);
        transfert.setNotes(requete.getNotes());
        transfert.setEntreprise(e);
        // Identite verifiee par le JWT - jamais celle envoyee par le client
        // dans le corps de la requete (falsifiable).
        transfert.setUtilisateur(tenantContext.currentUsername());

        TransfertStock transfertSauve = transfertStockRepository.save(transfert);

        log.info("Transfert effectué avec succès. ID: {}, Valeur estimée: {}",
                transfertSauve.getId(), valeurEstimation);

        // ÉTAPE 8: Construire et retourner la réponse
        return construireReponseTransfert(transfertSauve, magasinSource, magasinDestination,
                produit, stockSourceApres, stockDestinationApres);
    }

    /**
     * Valide les données du transfert
     *
     * Vérifie: - Les IDs ne sont pas null - La quantité est positive - Les
     * magasins sont différents - Les magasins existent - Le produit existe
     *
     * @param requete Requête à valider
     * @throws IllegalArgumentException si la validation échoue
     */
    private void validationTransfert(TransfertStockRequest requete) {
        if (!requete.isSourceDifferentFromDestination()) {
            throw new IllegalArgumentException(
                    "Le magasin source et destination doivent être différents");
        }

        // Vérifier que les dépôts existent
        try {
            stockService.getMagasin(requete.getMagasinSourceId());
            stockService.getMagasin(requete.getMagasinDestinationId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Magasin invalide: " + e.getMessage());
        }

        // Vérifier que le produit existe
        try {
            stockService.getProduit(requete.getProduitId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Produit invalide: " + e.getMessage());
        }

        log.debug("Validation du transfert réussie");
    }

    /**
     * Met à jour le stock source lors d'un transfert
     *
     * Règles de mise à jour: - sortiProduit += quantite - stockFinalTheorie -=
     * quantite
     *
     * @param magasinSource Magasin source
     * @param produit Produit transféré
     * @param quantite Quantité transférée
     */
    private void mettreAJourSourceTransfert(Magasin magasinSource, Produit produit, BigDecimal quantite) {
        boolean isMagasin = stockService.isMagasinDeStock(magasinSource.getId());

        if (isMagasin) {
            // Mise à jour d'une Commande (magasin de stock)
            Commande commande = stockService.getCommandeActuelle(magasinSource.getId(), produit.getId());
            if (commande != null) {
                // Augmenter sortie
                commande.setSortiProduit(
                        (commande.getSortiProduit() != null ? commande.getSortiProduit() : BigDecimal.ZERO)
                                .add(quantite));
                // Diminuer stock final
                commande.setStockFinalTheorie(
                        (commande.getStockFinalTheorie() != null ? commande.getStockFinalTheorie() : BigDecimal.ZERO)
                                .subtract(quantite));
                commandeRepository.save(commande);
                log.debug("Commande mise à jour: ID={}, nouvelle sortie={}, nouveau stock={}",
                        commande.getId(), commande.getSortiProduit(), commande.getStockFinalTheorie());
            } else {
                log.warn("Aucune commande trouvée pour le magasin {} et le produit {}",
                        magasinSource.getId(), produit.getId());
            }
        } else {
            // Mise à jour d'un PointVente (point de vente)
            PointVente pointVente = stockService.getPointVenteActuelle(magasinSource.getId(), produit.getId());
            if (pointVente != null) {
                // Augmenter sortie
                pointVente.setSortiProduit(
                        (pointVente.getSortiProduit() != null ? pointVente.getSortiProduit() : BigDecimal.ZERO)
                                .add(quantite));
                // Diminuer stock final
                pointVente.setStockFinalTheorie(
                        (pointVente.getStockFinalTheorie() != null ? pointVente.getStockFinalTheorie() : BigDecimal.ZERO)
                                .subtract(quantite));
                pointVenteRepository.save(pointVente);
                log.debug("PointVente mise à jour: ID={}, nouvelle sortie={}, nouveau stock={}",
                        pointVente.getId(), pointVente.getSortiProduit(), pointVente.getStockFinalTheorie());
            } else {
                log.warn("Aucun PointVente trouvé pour le magasin {} et le produit {}",
                        magasinSource.getId(), produit.getId());
            }
        }
    }

    /**
     * Met à jour le stock destination lors d'un transfert
     *
     * Règles de mise à jour: - entreeProduit += quantite - stockFinalTheorie +=
     * quantite
     *
     * @param magasinSource Magasin source (pour recuperer un prix de reference
     * si la destination n'a encore jamais vendu ce produit - voir
     * assurerPrixArticlesDestination)
     * @param magasinDestination Magasin destination
     * @param produit Produit transféré
     * @param quantite Quantité transférée
     */
    private void mettreAJourDestinationTransfert(Magasin magasinSource, Magasin magasinDestination, Produit produit, BigDecimal quantite) {
        boolean isMagasin = stockService.isMagasinDeStock(magasinDestination.getId());

        if (isMagasin) {
            // Mise à jour d'une Commande (magasin de stock)
            Commande commande = stockService.getCommandeActuelle(magasinDestination.getId(), produit.getId());
            if (commande != null) {
                // Augmenter entrée
                commande.setEntreeProduit(
                        (commande.getEntreeProduit() != null ? commande.getEntreeProduit() : BigDecimal.ZERO)
                                .add(quantite));
                // Augmenter stock final
                commande.setStockFinalTheorie(
                        (commande.getStockFinalTheorie() != null ? commande.getStockFinalTheorie() : BigDecimal.ZERO)
                                .add(quantite));
                commandeRepository.save(commande);
                log.debug("Commande destination mise à jour: ID={}, nouvelle entrée={}, nouveau stock={}",
                        commande.getId(), commande.getEntreeProduit(), commande.getStockFinalTheorie());
            } else {
                log.warn("Aucune commande trouvée pour créer/mettre à jour le magasin destination");
            }
        } else {
            // Mise à jour d'un PointVente (point de vente)
            Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
            PointVente pointVente = stockService.getPointVenteActuelle(magasinDestination.getId(), produit.getId());
            if (pointVente != null) {
                // Augmenter entrée
                pointVente.setEntreeProduit(
                        (pointVente.getEntreeProduit() != null ? pointVente.getEntreeProduit() : BigDecimal.ZERO)
                                .add(quantite));
                // Augmenter stock final
                pointVente.setStockFinalTheorie(
                        (pointVente.getStockFinalTheorie() != null ? pointVente.getStockFinalTheorie() : BigDecimal.ZERO)
                                .add(quantite));
                pointVenteRepository.save(pointVente);
                log.debug("PointVente destination mise à jour: ID={}, nouvelle entrée={}, nouveau stock={}",
                        pointVente.getId(), pointVente.getEntreeProduit(), pointVente.getStockFinalTheorie());
            } else {
                PointVente pv = new PointVente();
                pv.setEntreeProduit(quantite);
                pv.setStockFinalTheorie(quantite);
                pv.setEntreprise(entreprise);
                pv.setBoutique(magasinDestination.getBoutiqueId());
                pv.setDepotId(magasinDestination);
                pv.setProduit(produit);
                pv.setDateReception(new Date());
                pointVente = pointVenteRepository.save(pv);
                log.debug("PointVente destination créé: ID={}, entrée={}, stock={}",
                        pointVente.getId(), pointVente.getEntreeProduit(), pointVente.getStockFinalTheorie());
            }
            // Que le PointVente vienne d'etre cree ou qu'il existait deja,
            // s'assurer qu'il a un PrixArticles actif - sans ca, le produit
            // reste invisible dans "Approvisionnement"/"Liste des Prix
            // Articles" (ecran cote-a-cote avec ce meme stock) meme si son
            // stock est correct. Avant ce correctif, la creation etait
            // seulement tentee au moment de la creation du PointVente ET
            // seulement si un PrixVente "maitre" existait pour ce produit -
            // silencieusement ignoree sinon, un ecart jamais rattrape par la
            // suite (chaque transfert ulterieur ne fait que mettre a jour le
            // PointVente existant, jamais son PrixArticles).
            assurerPrixArticlesDestination(pointVente, produit, magasinSource, entreprise);
        }
    }

    /**
     * Garantit qu'un PointVente a un PrixArticles actif, en le creant si
     * besoin. Prix de reference, par ordre de preference : (1) le prix actif
     * de ce meme produit dans le magasin source du transfert (le plus fiable
     * : le produit y est deja effectivement vendu) ; (2) a defaut, le dernier
     * PrixVente "maitre" du produit ; (3) a defaut, zero (mieux qu'un produit
     * invisible dans les listings faute de PrixArticles).
     */
    private void assurerPrixArticlesDestination(PointVente pointVente, Produit produit, Magasin magasinSource, Entreprise entreprise) {
        if (prixArticlesRepositories.findActivePriceByPointVente(pointVente.getId()).isPresent()) {
            return;
        }

        BigDecimal prix = null;
        PointVente pointVenteSource = stockService.getPointVenteActuelle(magasinSource.getId(), produit.getId());
        if (pointVenteSource != null) {
            prix = prixArticlesRepositories.findActivePriceByPointVente(pointVenteSource.getId())
                    .map(PrixArticles::getPrixVenteTTC)
                    .orElse(null);
        }
        if (prix == null) {
            prix = prixVenteRepositories.findLastPrixventeByProduit(produit)
                    .map(PrixVente::getPrix)
                    .orElse(BigDecimal.ZERO);
        }

        PrixArticles pa = new PrixArticles();
        pa.setActif(true);
        pa.setDateCreation(new Date());
        pa.setEntreprise(entreprise);
        pa.setPointVente(pointVente);
        pa.setPrixVenteNet(prix);
        pa.setPrixVenteTTC(prix);
        // Sans ceci, tva/remise restent null -> "NaN%"/"NaN FCFA" a
        // l'affichage (ex. ecran Gestion des Points de Vente).
        pa.setTva(BigDecimal.ZERO);
        pa.setRemise(BigDecimal.ZERO);
        prixArticlesRepositories.save(pa);
        log.debug("PrixArticles cree pour PointVente destination ID={}, prix={}", pointVente.getId(), prix);
    }

    /**
     * Repare les PointVente de la compagnie courante qui ont du stock reel
     * mais aucun PrixArticles actif - consequence d'un ecart deja present
     * dans les donnees avant le correctif de assurerPrixArticlesDestination
     * (qui empeche desormais le probleme mais ne rattrape pas les PointVente
     * deja affectes, puisqu'un PointVente existant n'est plus jamais recree).
     * Idempotent : ne fait rien sur un PointVente qui a deja un PrixArticles
     * actif.
     *
     * @return nombre de PointVente reparés
     */
    public int reparerPrixArticlesManquants() {
        Long compagnieId = tenantContext.currentCompagnieId();
        Entreprise entreprise = entrepriseService.obtenirOuCreerExerciceActif(compagnieId);
        java.util.List<PointVente> aReparer = pointVenteRepository.findSansPrixArticlesActifAvecStock(compagnieId);

        for (PointVente pv : aReparer) {
            BigDecimal prix = prixArticlesRepositories
                    .findAnyActifByProduitAndCompagnie(pv.getProduit().getId(), compagnieId)
                    .map(PrixArticles::getPrixVenteTTC)
                    .orElse(null);
            if (prix == null) {
                prix = prixVenteRepositories.findLastPrixventeByProduit(pv.getProduit())
                        .map(PrixVente::getPrix)
                        .orElse(BigDecimal.ZERO);
            }

            PrixArticles pa = new PrixArticles();
            pa.setActif(true);
            pa.setDateCreation(new Date());
            pa.setEntreprise(entreprise);
            pa.setPointVente(pv);
            pa.setPrixVenteNet(prix);
            pa.setPrixVenteTTC(prix);
            pa.setTva(BigDecimal.ZERO);
            pa.setRemise(BigDecimal.ZERO);
            prixArticlesRepositories.save(pa);
        }

        log.info("Reparation PrixArticles manquants: {} PointVente reparés pour compagnie {}",
                aReparer.size(), compagnieId);
        return aReparer.size();
    }

    /**
     * Construit la réponse de transfert
     *
     * @param transfert Transfert sauvegardé
     * @param magasinSource Magasin source
     * @param magasinDestination Magasin destination
     * @param produit Produit
     * @param stockSourceApres Stock source après transfert
     * @param stockDestinationApres Stock destination après transfert
     * @return Réponse formatée
     */
    private TransfertStockResponse construireReponseTransfert(
            TransfertStock transfert,
            Magasin magasinSource,
            Magasin magasinDestination,
            Produit produit,
            BigDecimal stockSourceApres,
            BigDecimal stockDestinationApres) {

        return TransfertStockResponse.builder()
                .transfertId(transfert.getId())
                .produit(TransfertStockResponse.ProduitDTO.builder()
                        .id(produit.getId())
                        .code(produit.getCode())
                        .libelle(produit.getLibelle())
                        .reference(produit.getReference())
                        .build())
                .source(TransfertStockResponse.MagasinDTO.builder()
                        .id(magasinSource.getId())
                        .code(magasinSource.getCode())
                        .libelle(magasinSource.getLibelle())
                        .ville(magasinSource.getVilleid() != null ? magasinSource.getVilleid().toString() : "")
                        .type(magasinSource.getBoutiqueId() == null ? "MAGASIN" : "POINT_VENTE")
                        .stockFinal(stockSourceApres)
                        .build())
                .destination(TransfertStockResponse.MagasinDTO.builder()
                        .id(magasinDestination.getId())
                        .code(magasinDestination.getCode())
                        .libelle(magasinDestination.getLibelle())
                        .ville(magasinDestination.getVilleid() != null ? magasinDestination.getVilleid().toString() : "")
                        .type(magasinDestination.getBoutiqueId() == null ? "MAGASIN" : "POINT_VENTE")
                        .stockFinal(stockDestinationApres)
                        .build())
                .quantite(transfert.getQuantite())
                .valeurEstimation(transfert.getValeurEstimation())
                .dateTransfert(transfert.getDateTransfert())
                .message("Transfert effectué avec succès")
                .status("SUCCESS")
                .build();
    }

}
