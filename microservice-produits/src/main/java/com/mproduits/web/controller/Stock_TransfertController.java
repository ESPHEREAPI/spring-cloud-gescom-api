/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.model.Commande;
import com.mproduits.model.Magasin;
import com.mproduits.model.PointVente;
import com.mproduits.model.Produit;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.security.TenantContext;
import com.mproduits.services.StockTransfertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 *
 * @author USER01
 */
@Slf4j
@RestController
@RequestMapping("/microservice-produits/stock-transfert")
public class Stock_TransfertController {
      @Autowired
    private StockTransfertService stockService;
        @Autowired
    private CommandeRepositories commandeRepository;
    
    @Autowired
    private PointVenteRepositories pointVenteRepository;
    
    @Autowired
    private MagasinRepositories magasinRepository;
    @Autowired
    private BoutiqueAccessGuard boutiqueAccessGuard;
    @Autowired
    private TenantContext tenantContext;

    /**
     * Récupère les détails du stock d'un produit dans un magasin
     * 
     * Endpoint: GET /api/stock/details/{produitId}/{magasinId}
     * 
     * Reponse:
     * {
     *   "produit": {...},
     *   "magasin": {...},
     *   "stockFinal": 45.50,
     *   "isMagasin": true,
     *   "typeDepot": "MAGASIN",
     *   "commande": {...},  // pour magasin
     *   "pointVente": {...} // pour point de vente
     * }
     * 
     * @param produitId ID du produit
     * @param magasinId ID du magasin/point de vente
     * @return Détails du stock
     */
    @GetMapping("/details/{produitId}/{magasinId}")
    public ResponseEntity<?> obtenirDetailsStock(
            @PathVariable Long produitId,
            @PathVariable Long magasinId) {

        log.info("Requête des détails de stock pour produit {} dans magasin {}", produitId, magasinId);

        try {
            Produit produit = stockService.getProduit(produitId);
            Magasin magasin = stockService.getMagasin(magasinId);
            
            Map<String, Object> details = new HashMap<>();
            details.put("produit", produit);
            details.put("magasin", magasin);
            details.put("stockFinal", stockService.getStockActuel(magasinId, produitId));
            
            boolean isMagasin = stockService.isMagasinDeStock(magasinId);
            details.put("isMagasin", isMagasin);
            details.put("typeDepot", isMagasin ? "MAGASIN" : "POINT_VENTE");

            if (isMagasin) {
                Commande commande = stockService.getCommandeActuelle(magasinId, produitId);
                details.put("commande", commande);
            } else {
                PointVente pointVente = stockService.getPointVenteActuelle(magasinId, produitId);
                details.put("pointVente", pointVente);
            }

            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la récupération des détails: {}", e.getMessage());
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(erreur);
        } catch (Exception e) {
            log.error("Erreur inattendue", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Une erreur inattendue s'est produite");
            return ResponseEntity.internalServerError().body(erreur);
        }
    }

    /**
     * Récupère le stock disponible d'un produit dans un magasin
     * 
     * Endpoint: GET /api/stock/{magasinId}/{produitId}
     * 
     * @param magasinId ID du magasin
     * @param produitId ID du produit
     * @return Quantité en stock
     */
    @GetMapping("/{magasinId}/{produitId}")
    public ResponseEntity<?> obtenirStock(
            @PathVariable Long magasinId,
            @PathVariable Long produitId) {

        log.debug("Requête du stock pour produit {} dans magasin {}", produitId, magasinId);

        if (magasinRepository.findByIdAndCompagnie_Id(magasinId, tenantContext.currentCompagnieId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            BigDecimal stock = stockService.getStockActuel(magasinId, produitId);
            
            Map<String, Object> reponse = new HashMap<>();
            reponse.put("magasinId", magasinId);
            reponse.put("produitId", produitId);
            reponse.put("quantite", stock);
            
            return ResponseEntity.ok(reponse);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du stock", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération du stock");
            return ResponseEntity.internalServerError().body(erreur);
        }
        
        
}
    
    
    /**
     * Récupère les produits disponibles dans un dépôt spécifique avec leur stock
     * 
     * Endpoint: GET /api/stock-transfert/produits-disponibles/{depotId}?anneeid=X&boutiqueid=Y
     * 
     * Logique:
     * - Si le dépôt a boutique = null → stock depuis table COMMANDE (stock_final_theorie)
     * - Si le dépôt a boutique != null → stock depuis table POINTVENTE (stock_final_theorie)
     * 
     * @param depotId ID du dépôt source
     * @param anneeid ID de l'année
     * @param boutiqueid ID de la boutique
     * @return Liste des produits avec leur stock disponible (stock > 0)
     */
    @GetMapping("/produits-disponibles/{depotId}")
    public ResponseEntity<?> getProduitsDisponiblesParDepot(
            @PathVariable Long depotId,
            @RequestParam Long anneeid,
            @RequestParam Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("📦 Récupération des produits disponibles pour le dépôt: {}", depotId);
        
        try {
            // Récupérer le dépôt pour déterminer son type - verifie au passage
            // qu'il appartient a la compagnie courante (protection IDOR).
            Optional<Magasin> depotOpt = magasinRepository.findByIdAndCompagnie_Id(depotId, tenantContext.currentCompagnieId());
            if (depotOpt.isEmpty()) {
                log.warn("❌ Dépôt non trouvé: {}", depotId);
                return ResponseEntity.notFound().build();
            }
            
            Magasin depot = depotOpt.get();
            List<Map<String, Object>> produitsAvecStock = new ArrayList<>();
            
            // Dépôt de type MAGASIN (boutique = null) → Stock dans COMMANDE
            if (depot.getBoutiqueId()== null) {
                log.info("🏢 Dépôt MAGASIN détecté - Recherche dans table COMMANDE");
                
                List<Object[]> resultats = commandeRepository.findProduitsWithStockByDepot(
                    depotId, anneeid
                );
                
                for (Object[] row : resultats) {
                    Map<String, Object> produit = new HashMap<>();
                    produit.put("id", row[0]);                    // produit.id
                    produit.put("code", row[1]);                  // produit.code
                    produit.put("libelle", row[2]);               // produit.libelle
                    produit.put("reference", row[3]);             // produit.reference
                    produit.put("stockDisponible", row[4]);       // stock_final_theorie
                    produit.put("prixAchat", row[5]);            // prix_achat
                    produit.put("typeDepot", "MAGASIN");
                    
                    produitsAvecStock.add(produit);
                }
                
            } 
            // Dépôt de type POINT DE VENTE (boutique != null) → Stock dans POINTVENTE
            else {
                log.info("🏪 Dépôt POINT DE VENTE détecté - Recherche dans table POINTVENTE");
                
                List<Object[]> resultats = pointVenteRepository.findProduitsWithStockByDepot(
                    depotId, anneeid, boutiqueid
                );
                
                for (Object[] row : resultats) {
                    Map<String, Object> produit = new HashMap<>();
                    produit.put("id", row[0]);                    // produit.id
                    produit.put("code", row[1]);                  // produit.code
                    produit.put("libelle", row[2]);               // produit.libelle
                    produit.put("reference", row[3]);             // produit.reference
                    produit.put("stockDisponible", row[4]);       // stock_final_theorie
                    produit.put("prixVente", row[5]);            // prix_vente (depuis PrixArticles)
                    produit.put("typeDepot", "POINT_VENTE");
                    
                    produitsAvecStock.add(produit);
                }
            }
            
            // Filtrer les produits avec stock > 0
            produitsAvecStock = produitsAvecStock.stream()
                .filter(p -> {
                    BigDecimal stock = (BigDecimal) p.get("stockDisponible");
                    return stock != null && stock.compareTo(BigDecimal.ZERO) > 0;
                })
                .toList();
            
            log.info("✅ {} produits trouvés avec stock disponible", produitsAvecStock.size());
            
            return ResponseEntity.ok(produitsAvecStock);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des produits: {}", e.getMessage(), e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération des produits: " + e.getMessage());
            return ResponseEntity.internalServerError().body(erreur);
        }
    }

    // Deux methodes "getStockProduit"/"getDetailsStock" existaient ici avec
    // exactement les memes templates d'URL que obtenirStock()/obtenirDetailsStock()
    // ci-dessus (seuls les noms de variables de chemin differaient, ex.
    // "/{depotId}/{produitId}" vs "/{magasinId}/{produitId}") - Spring MVC les
    // enregistre sans erreur au demarrage (les patterns sont compares comme
    // chaines, pas structurellement) mais laquelle des deux repond a une
    // requete donnee est indeterministe. Elles utilisaient en plus des
    // requetes JPQL sans LIMIT 1 (getStockByDepotAndProduit/
    // getStockDetailsFromCommande/PointVente) qui levaient une exception des
    // qu'un depot+produit avait plus d'une ligne Commande/PointVente - capturee
    // en 500 silencieux, affiche comme "Stock actuel: 0,00" cote formulaire de
    // transfert alors que le transfert avait reellement abouti. Supprimees :
    // obtenirStock()/obtenirDetailsStock() couvrent deja le meme besoin via
    // stockService.getStockActuel(), qui utilise les requetes LIMIT 1 correctes.
}
    
