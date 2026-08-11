/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.ApiResponse;
import com.mproduits.dto.CorrectionStockRequestDto;
import com.mproduits.dto.InventaireDto;
import com.mproduits.dto.PointVenteDto;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.InventairesService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits")
public class InventairesControllers {
    @Autowired
    InventairesService inventairesService;
    @Autowired
    BoutiqueAccessGuard boutiqueAccessGuard;
     //recuperation des annee pour les ventes
    @GetMapping("/inventaires/boutique/{boutiqueid}/categorie/{categorieid}")
    public ResponseEntity<List<InventaireDto>> listePointVente(@PathVariable("boutiqueid") Long  boutiqueid,@PathVariable("categorieid") Long  categorieid) {
     boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
     List<InventaireDto> allStockPv=inventairesService.chargeInventaire(boutiqueid, categorieid);
        return ResponseEntity.ok(allStockPv);
    }

    // Inventaire "Par depot" - le scope compagnie est verifie dans
    // InventairesService.chargeInventaireParDepot (pas de BoutiqueAccessGuard
    // ici, un depot n'a pas de boutique a verifier).
    @GetMapping("/inventaires/depot/{depotid}")
    public ResponseEntity<List<InventaireDto>> listeParDepot(@PathVariable("depotid") Long depotid) {
        List<InventaireDto> allStock = inventairesService.chargeInventaireParDepot(depotid);
        return ResponseEntity.ok(allStock);
    }
    
     // Correction manuelle de stock : operation sensible (peut masquer un
     // vol/une erreur non tracee), reservee aux comptes ADMIN et desormais
     // tracee (voir HistoriqueCorrectionStock, motif obligatoire).
     @PreAuthorize("hasAnyRole('COMPANY_ADMIN','ADMIN')")
     @PostMapping("/corrections-stock")
    public ResponseEntity<ApiResponse<Void>> saveCorrections(@RequestBody CorrectionStockRequestDto request) {
        try {
            List<PointVenteDto> pointsVente = request.getLignes();
            // Chaque ligne doit porter sur une boutique de la compagnie courante -
            // sans ce controle, un client pourrait corriger le stock d'une autre compagnie.
            pointsVente.forEach(pv -> boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(pv.getBoutique().getId()));
            this.inventairesService.saveStockInventaire(pointsVente, request.getMotif());

            ApiResponse<Void> response = new ApiResponse<>(true, "Corrections enregistrées avec succès", null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<Void> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Échec de l'enregistrement des corrections");
            response.setErrors(List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
      //recuperation des annee pour les ventes
    @GetMapping("/points-vente/{boutiqueid}/{categorieid}")
    public ResponseEntity<List<PointVenteDto>> chargechargeStockForUpdate(@PathVariable("boutiqueid") Long  boutiqueid,@PathVariable("categorieid") Long  categorieid) {
     boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
     List<PointVenteDto> allStockPv=inventairesService.chargeStockeForUpdate(boutiqueid, categorieid);
        return ResponseEntity.ok(allStockPv);
    }
    
}
    

