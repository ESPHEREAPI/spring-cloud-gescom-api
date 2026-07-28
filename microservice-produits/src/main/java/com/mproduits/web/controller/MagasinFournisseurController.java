/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.MagasinFournisseurDTO;
import com.mproduits.model.Boutique;
import com.mproduits.model.Fournisseur;
import com.mproduits.repositories.FournisseurRepositories;
import com.mproduits.services.MagasinFournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller pour la gestion des associations Magasin-Fournisseur
 */
@RestController
@RequestMapping("/microservice-produits/magasin-fournisseur")
//@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class MagasinFournisseurController {

    private final MagasinFournisseurService magasinFournisseurService;
    private final FournisseurRepositories fournisseurRepositories;

    /**
     * Liste avec pagination
     */
    @GetMapping
    public ResponseEntity<Page<MagasinFournisseurDTO>> getAllAssociations(
        @PageableDefault(size = 10) Pageable pageable,  // ← Retirer sort et direction
        @RequestParam(required = false) String search) {
    
    log.info("Récupération des associations - page: {}, search: {}", pageable.getPageNumber(), search);
    
    Page<MagasinFournisseurDTO> associations = search != null && !search.isEmpty()
            ? magasinFournisseurService.searchAssociations(search, pageable)
            : magasinFournisseurService.getAllAssociations(pageable);
    
    return ResponseEntity.ok(associations);
    }

    /**
     * Détails d'une association
     */
    @GetMapping("/{depotId}/{fournisseurId}")
    public ResponseEntity<MagasinFournisseurDTO> getAssociationById(
            @PathVariable Long depotId,
            @PathVariable Long fournisseurId) {
        
        log.info("Récupération association Magasin {} - Fournisseur {}", depotId, fournisseurId);
        MagasinFournisseurDTO association = magasinFournisseurService.getAssociationById(depotId, fournisseurId);
        return ResponseEntity.ok(association);
    }

    /**
     * Créer une association
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAssociation(
            @Valid @RequestBody MagasinFournisseurDTO associationDTO) {
        
        log.info("Création association Magasin {} - Fournisseur {}", 
                 associationDTO.getDepotId(), associationDTO.getFournisseurId());
        
        MagasinFournisseurDTO created = magasinFournisseurService.createAssociation(associationDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Association créée avec succès");
        response.put("data", created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mettre à jour une association
     */
    @PutMapping("/{depotId}/{fournisseurId}")
    public ResponseEntity<Map<String, Object>> updateAssociation(
            @PathVariable Long depotId,
            @PathVariable Long fournisseurId,
            @Valid @RequestBody MagasinFournisseurDTO associationDTO) {
        
        log.info("Mise à jour association Magasin {} - Fournisseur {}", depotId, fournisseurId);
        
        MagasinFournisseurDTO updated = magasinFournisseurService.updateAssociation(
                depotId, fournisseurId, associationDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Association mise à jour avec succès");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une association
     */
    @DeleteMapping("/{depotId}/{fournisseurId}")
    public ResponseEntity<Map<String, Object>> deleteAssociation(
            @PathVariable Long depotId,
            @PathVariable Long fournisseurId) {
        
        log.info("Suppression association Magasin {} - Fournisseur {}", depotId, fournisseurId);
        
        magasinFournisseurService.deleteAssociation(depotId, fournisseurId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Association supprimée avec succès");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Associations par magasin
     */
    @GetMapping("/magasin/{depotId}")
    public ResponseEntity<List<MagasinFournisseurDTO>> getAssociationsByMagasin(@PathVariable Long depotId) {
        log.info("Récupération associations pour magasin ID: {}", depotId);
        List<MagasinFournisseurDTO> associations = magasinFournisseurService.getAssociationsByMagasin(depotId);
        return ResponseEntity.ok(associations);
    }

    /**
     * Associations par fournisseur
     */
    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<List<MagasinFournisseurDTO>> getAssociationsByFournisseur(
            @PathVariable Long fournisseurId) {
        log.info("Récupération associations pour fournisseur ID: {}", fournisseurId);
        List<MagasinFournisseurDTO> associations = magasinFournisseurService.getAssociationsByFournisseur(fournisseurId);
        return ResponseEntity.ok(associations);
    }

    /**
     * Vérifier si association existe
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkAssociationExists(
            @RequestParam Long depotId,
            @RequestParam Long fournisseurId) {
        
        log.info("Vérification existence association Magasin {} - Fournisseur {}", depotId, fournisseurId);
        
        boolean exists = magasinFournisseurService.associationExists(depotId, fournisseurId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Statistiques d'une association
     */
    @GetMapping("/{depotId}/{fournisseurId}/stats")
    public ResponseEntity<Map<String, Object>> getAssociationStats(
            @PathVariable Long depotId,
            @PathVariable Long fournisseurId) {
        
        log.info("Statistiques association Magasin {} - Fournisseur {}", depotId, fournisseurId);
        Map<String, Object> stats = magasinFournisseurService.getAssociationStatistics(depotId, fournisseurId);
        return ResponseEntity.ok(stats);
    }
    
     
    @GetMapping("/fournisseurs/all")
    public ResponseEntity<List<Fournisseur>> getAllFournisseur() {
        List<Fournisseur> fournisseur = fournisseurRepositories.findAll();
        return ResponseEntity.ok(fournisseur);
    }
    
}
