/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;
import com.mproduits.dto.DashboardStockDTO;
import com.mproduits.services.DashboardService;
import com.mproduits.services.StockTransfertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author USER01
 */
@Slf4j
@RestController
@RequestMapping("/microservice-produits/dashboard-transfert-stock")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private StockTransfertService stockService;

    /**
     * Récupère le dashboard complet avec tous les KPIs
     * 
     * Endpoint: GET /api/dashboard/stock
     * 
     * Réponse:
     * {
     *   "totalValeurStock": 125000.00,
     *   "totalValeurMagasins": 100000.00,
     *   "totalValeurPointsVente": 25000.00,
     *   "nombreProduitsFaibles": 3,
     *   "nombreMouvements": 42,
     *   "valeurMagasins": [...],
     *   "valeurPointsVente": [...],
     *   "produitsFaibleStock": [...],
     *   "evolutionStock": [...]
     * }
     * 
     * @return Dashboard avec toutes les données agrégées
     */
    @GetMapping("/stock")
    public ResponseEntity<?> obtenirDashboard() {
        log.info("Requête du dashboard de stock");
        try {
            DashboardStockDTO dashboard = dashboardService.genererDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du dashboard", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération du dashboard");
            return ResponseEntity.internalServerError().body(erreur);
        }
    }

    /**
     * Récupère les valeurs de stock par magasin
     * 
     * Endpoint: GET /api/dashboard/stock/magasins
     * 
     * @return Liste des valeurs par magasin
     */
    @GetMapping("/stock/magasins")
    public ResponseEntity<?> obtenirValeurMagasins() {
        log.debug("Requête des valeurs par magasin");
        try {
            DashboardStockDTO dashboard = dashboardService.genererDashboard();
            return ResponseEntity.ok(dashboard.getValeurMagasins());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des valeurs magasins", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération");
            return ResponseEntity.internalServerError().body(erreur);
        }
    }

    /**
     * Récupère les valeurs de stock par point de vente
     * 
     * Endpoint: GET /api/dashboard/stock/points-vente
     * 
     * @return Liste des valeurs par point de vente
     */
    @GetMapping("/stock/points-vente")
    public ResponseEntity<?> obtenirValeurPointsVente() {
        log.debug("Requête des valeurs par point de vente");
        try {
            DashboardStockDTO dashboard = dashboardService.genererDashboard();
            return ResponseEntity.ok(dashboard.getValeurPointsVente());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des valeurs points de vente", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération");
            return ResponseEntity.internalServerError().body(erreur);
        }
    }

    /**
     * Récupère les produits en stock faible
     * 
     * Endpoint: GET /api/dashboard/stock/faible
     * 
     * Colorisation selon la quantité:
     * - quantité <= 5: "danger" (rouge)
     * - 5 < quantité <= 10: "warning" (orange)
     * 
     * @return Liste des produits en stock faible avec alertes
     */
    @GetMapping("/stock/faible")
    public ResponseEntity<?> obtenirProduitsFaibles() {
        log.debug("Requête des produits en stock faible");
        try {
            DashboardStockDTO dashboard = dashboardService.genererDashboard();
            return ResponseEntity.ok(dashboard.getProduitsFaibleStock());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits faibles", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération");
            return ResponseEntity.internalServerError().body(erreur);
        }
    }

    /**
     * Récupère l'évolution du stock pour les graphiques
     * 
     * Endpoint: GET /api/dashboard/stock/evolution
     * 
     * @return Données d'évolution mensuelles
     */
    @GetMapping("/stock/evolution")
    public ResponseEntity<?> obtenirEvolution() {
        log.debug("Requête de l'évolution du stock");
        try {
            DashboardStockDTO dashboard = dashboardService.genererDashboard();
            return ResponseEntity.ok(dashboard.getEvolutionStock());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'évolution", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Erreur lors de la récupération");
            return ResponseEntity.internalServerError().body(erreur);
        }
    }
}


