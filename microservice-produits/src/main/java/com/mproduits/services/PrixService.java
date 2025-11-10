/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

/**
 *
 * @author USER01
 */

import com.mproduits.model.*;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Service de Gestion des Prix
 * Responsabilités:
 * - Récupérer prix courant avec cache
 * - Créer historique prix
 * - Valider cohérence prix
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrixService {
    
    private final PrixArticlesRepositories prixArticlesRepo;
    private final PrixHistoriqueRepository prixHistoriqueRepo;
    
    /**
     * Récupère le prix courant (cache 1 heure)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "prixArticles", key = "#produitId")
    public PrixArticles getPrixCourant(Long produitId) {
        return prixArticlesRepo.findCurrentPrice(produitId).orElse(null);
    }
    
    /**
     * Crée un historique de prix
     */
    @Transactional
    public PrixHistorique creerHistoriquePrix(Produit produit, PointVente pv,
        BigDecimal prixNet, BigDecimal tauxTVA, String username) {
        
        // Désactiver anciens prix actifs
        prixHistoriqueRepo.findActiveByProduit(produit.getId()).stream()
            .forEach(p -> {
                p.setActif(false);
                prixHistoriqueRepo.save(p);
            });
        
        // Créer nouveau prix
        PrixHistorique prix = new PrixHistorique();
        prix.setProduit(produit);
        prix.setPointVente(pv);
        prix.setPrixVenteNet(prixNet);
        prix.setTauxTVA(tauxTVA);
        prix.calculerPrixTTC();
        prix.setActif(true);
        prix.setUsernameCreate(username);
        prix.setDateCreation(LocalDateTime.now());
        
        return prixHistoriqueRepo.save(prix);
    }
}