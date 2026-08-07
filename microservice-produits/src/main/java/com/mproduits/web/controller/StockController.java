/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;


import com.mproduits.dto.ProduitDto;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits/stock-facturation")
@Slf4j
@RequiredArgsConstructor
public class StockController {
    
    private final StockService stockService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;
     @Autowired
    MapperDtoImpl mapperdto;
    /**
     * Récupérer le stock total d'un produit
     * GET /api/v1/stock/produit/{produitId}
     * @param produitId
     * @return 
     */
    @GetMapping("/produit/{produitId}/{boutiqueid}/{anneeid}")
    public ResponseEntity<Map<String, Object>> getStockTotal(@PathVariable Long produitId,@PathVariable Long boutiqueid,@PathVariable int anneeid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        BigDecimal stock = stockService.getStockTotal(produitId,boutiqueid,anneeid);
        return ResponseEntity.ok(Map.of("stock", stock));
    }
    
    /**
     * Historique des mouvements de stock
     * GET /api/v1/stock/mouvements?produitId=&pointVenteId=
     */
    @GetMapping("/mouvements")
    public ResponseEntity<List<Map<String, Object>>> getMouvements(
        @RequestParam(required = false) Long produitId,
        @RequestParam(required = false) Long pointVenteId) {
        
        List<Map<String, Object>> mouvements = stockService.getMouvementsHistorique(
            produitId, pointVenteId);
        
        return ResponseEntity.ok(mouvements);
    }
    @GetMapping("/produit/{articleId}/{boutiqueid}")
    public ResponseEntity<ProduitDto>currentProduit(@PathVariable Long articleId,@PathVariable Long boutiqueid){
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        ProduitDto pdo=stockService.lastProduitForPointVente(articleId, boutiqueid);
          return ResponseEntity.ok(pdo);
     
    }
    
    
}
