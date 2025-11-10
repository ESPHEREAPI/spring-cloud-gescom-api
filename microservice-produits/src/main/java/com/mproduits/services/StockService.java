/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.StockUpdateResponse;
import com.mproduits.enums.MovementType;
import com.mproduits.exceptions.MetierException;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.model.StockMovement;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.StockMovementRepository;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author USER01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    //ProduitRepositories produitRepositories;

    PrixArticlesRepositories prixArticlesRepositories;
    EntrepriseRepositories entrepriseRepositories;
     private final PointVenteRepositories pointVenteRepo;
    private final StockMovementRepository movementRepo;
    private final ProduitRepositories produitRepo;

//    @Autowired
//    public StockService(ProduitRepositories produitRepositories, PrixArticlesRepositories prixArticlesRepositories, EntrepriseRepositories entrepriseRepositories) {
//        this.produitRepo = produitRepositories;
//        this.prixArticlesRepositories = prixArticlesRepositories;
//        this.entrepriseRepositories = entrepriseRepositories;
//    }

    public StockUpdateResponse updateStockAfterSale(Long produitid, Integer quantite) {
        StockUpdateResponse response = new StockUpdateResponse();
        Produit p = produitRepo.findById(produitid)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p);
        if (pa.isPresent()) {
            PointVente pv = pa.get().getPointVente();
            // BigDecimal newStock = pv.getStockFinalTheorie().subtract(BigDecimal.valueOf(quantite));

            response.setMessage("update stock");
            response.setNewStock(pv.getStockFinalTheorie().intValue());
            response.setProductId(produitid);
            response.setSuccess(true);
        }
        return response;

    }

    public boolean isStockAvailable(Long produitid, Integer quantite) {
        // StockUpdateResponse response = new StockUpdateResponse();
        BigDecimal stockFinal = BigDecimal.ZERO;
        Produit p = produitRepo.findById(produitid)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p);
        if (pa.isPresent()) {
            stockFinal = pa.get().getPointVente().getStockFinalTheorie();
            return stockFinal.intValue() > quantite.intValue();

        }

        return Boolean.FALSE;

    }

    public Integer getCurrentStock(Long produitid) {
        Produit p = produitRepo.findById(produitid)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p);
        if (pa.isPresent()) {
            PointVente pv = pa.get().getPointVente();
            return pv.getStockFinalTheorie().intValue();
        }

        return 0;

    }
   /**
     * Augmente le stock (entrée)
     */
    @Transactional
    public void augmenterStock(Long produitId, Long pointVenteId, BigDecimal quantite,
            MovementType type, String motif, String username) {
        
        log.info("Augmentation stock: produit={}, quantite={}", produitId, quantite);
        
        PointVente pv = pointVenteRepo.findById(pointVenteId)
            .orElseThrow(() -> new MetierException("Point de vente non trouvé"));
        
        Produit produit = produitRepo.findById(produitId)
            .orElseThrow(() -> new MetierException("Produit non trouvé"));
        
        // Mettre à jour stock PointVente
        BigDecimal newStock = pv.getStockInitial() != null ? 
            pv.getStockInitial().add(quantite) : quantite;
        
        // Validation: stock ne peut pas être excessif
        if (newStock.compareTo(new BigDecimal("999999")) > 0) {
            throw new MetierException("Quantité invalide");
        }
        
        pv.setStockInitial(newStock);
        pointVenteRepo.save(pv);
        
        // Créer mouvement
        creerMouvement(produit, pv, quantite, type, motif, username);
    }
    
    /**
     * Diminue le stock (sortie)
     */
    @Transactional
    public void diminuerStock(Long produitId, Long pointVenteId, BigDecimal quantite,
MovementType type, String motif, String username) {
        
        log.info("Diminution stock: produit={}, quantite={}", produitId, quantite);
        
        PointVente pv = pointVenteRepo.findById(pointVenteId)
            .orElseThrow(() -> new MetierException("Point de vente non trouvé"));
        
        Produit produit = produitRepo.findById(produitId)
            .orElseThrow(() -> new MetierException("Produit non trouvé"));
        
        // Vérifier stock suffisant
        BigDecimal stockActuel = pv.getStockFinalTheorie()!= null ? 
            pv.getStockFinalTheorie() : BigDecimal.ZERO;
        
        if (stockActuel.compareTo(quantite) < 0) {
            throw new MetierException(
                String.format("Stock insuffisant: disponible=%.2f, demandé=%.2f",
                    stockActuel, quantite));
        }
        
        // Mettre à jour stock
        BigDecimal newStock = stockActuel.subtract(quantite);
        pv.setStockInitial(newStock);
        pointVenteRepo.save(pv);
        
        // Créer mouvement
        creerMouvement(produit, pv, quantite.negate(), type, motif, username);
    }
    
    /**
     * Récupère le stock total d'un produit (tous points de vente)
     */
    @Transactional(readOnly = true)
    public BigDecimal getStockTotal(Long produitId) {
        return pointVenteRepo.sumStockByProduit(produitId)
            .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Crée un mouvement de stock
     */
    private void creerMouvement(Produit produit, PointVente pv, BigDecimal quantite,
       MovementType type, String motif, String username) {
        
        StockMovement movement = new StockMovement();
        movement.setProduit(produit);
        movement.setPointVente(pv);
        movement.setQuantite(quantite);
        movement.setTypeMouvement(type);
        movement.setMotif(motif);
        movement.setUsernameCreate(username);
        movement.setDateCreation(LocalDateTime.now());
        
        movementRepo.save(movement);
        log.debug("Mouvement créé: {}", movement.getId());
    }
    
     /**
     * Récupère l'historique des mouvements de stock
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMouvementsHistorique(
        Long produitId, 
        Long pointVenteId) {
        
        log.debug("Récupération historique mouvements: produit={}, pointVente={}", 
            produitId, pointVenteId);
        
        List<StockMovement> mouvements = new ArrayList<>();
        
        if (produitId != null && pointVenteId == null) {
            mouvements = movementRepo.findByProduitId(produitId);
        } else if (pointVenteId != null && produitId == null) {
            mouvements = movementRepo.findByPointVenteId(pointVenteId);
        } else if (produitId != null && pointVenteId != null) {
            // Filtrer les deux
            mouvements = movementRepo.findByProduitId(produitId).stream()
                .filter(m -> m.getPointVente().getId().equals(pointVenteId))
                .collect(Collectors.toList());
        } else {
            mouvements = movementRepo.findAll();
        }
        
        // Convertir en Map pour retour
        return mouvements.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * Convertir StockMovement en Map
     */
    private Map<String, Object> convertToMap(StockMovement movement) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", movement.getId());
        map.put("type", movement.getTypeMouvement().getLabel());
        map.put("quantite", movement.getQuantite());
        map.put("produitId", movement.getProduit().getId());
        map.put("produitCode", movement.getProduit().getCode());
        map.put("pointVenteId", movement.getPointVente().getId());
        map.put("motif", movement.getMotif());
        map.put("username", movement.getUsernameCreate());
        map.put("dateCreation", movement.getDateCreation());
        
        return map;
    }
    
    /**
     * Récupère le stock par point de vente
     */
    @Transactional(readOnly = true)
    public BigDecimal getStockParPointVente(Long produitId, Long pointVenteId) {
        log.debug("Récupération stock: produit={}, pointVente={}", 
            produitId, pointVenteId);
        
        Optional<PointVente> pv = pointVenteRepo.findById(pointVenteId);
        
        if (pv.isPresent()) {
            BigDecimal stock = pv.get().getStockInitial();
            return stock != null ? stock : BigDecimal.ZERO;
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Récupère tous les mouvements pour un produit
     */
    @Transactional(readOnly = true)
    public List<StockMovement> getMouvementsProduit(Long produitId) {
        log.debug("Mouvements produit: {}", produitId);
        return movementRepo.findByProduitId(produitId);
    }
    
    /**
     * Récupère tous les mouvements pour un point de vente
     */
    @Transactional(readOnly = true)
    public List<StockMovement> getMouvementsPointVente(Long pointVenteId) {
        log.debug("Mouvements point de vente: {}", pointVenteId);
        return movementRepo.findByPointVenteId(pointVenteId);
    }
    
    /**
     * Récupère les 10 derniers mouvements de stock
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDerniersMouvements(int limite) {
        log.debug("Derniers {} mouvements", limite);
        
        List<StockMovement> mouvements = movementRepo.findAll().stream()
            .sorted(Comparator.comparing(StockMovement::getDateCreation).reversed())
            .limit(limite)
            .collect(Collectors.toList());
        
        return mouvements.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère les mouvements d'une facture
     */
    @Transactional(readOnly = true)
    public List<StockMovement> getMouvementsFacture(Long factureId) {
        log.debug("Mouvements facture: {}", factureId);
        
        return movementRepo.findAll().stream()
            .filter(m -> m.getFacture() != null && m.getFacture().getId().equals(factureId))
            .collect(Collectors.toList());
    }

}
