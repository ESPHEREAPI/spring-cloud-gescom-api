/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;
import com.mproduits.dto.*;
import com.mproduits.ecommerce.dto.service.ReconductionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion de la reconduction annuelle.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/microservice-produits/reconduction")
@RequiredArgsConstructor
public class ReconductionController {
     private final ReconductionService reconductionService;

    /**
     * Valide les pré-requis avant de lancer une reconduction.
     */
    @PostMapping("/valider")
    public ResponseEntity<ValidationReconductionResponse> valider(
            @Valid @RequestBody ValidationReconductionRequest request) {
        
        log.info("API: Validation reconduction - année {} -> {}, employeur {}",
                request.getAnneeSourceId(), request.getAnneeCibleId(), request.getEmployeurId());
        
        try {
            ValidationReconductionResponse response = reconductionService.validerPreRequis(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la validation", e);
            ValidationReconductionResponse errorResponse = ValidationReconductionResponse.builder()
                    .valide(false)
                    .build();
            errorResponse.ajouterErreur("Erreur interne: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lance l'exécution d'une reconduction.
     */
    @PostMapping("/executer")
    public ResponseEntity<ExecutionReconductionResponse> executer(
            @Valid @RequestBody ExecutionReconductionRequest request) {
        
        log.info("API: Exécution reconduction - année {} -> {}, employeur {}",
                request.getAnneeSourceId(), request.getAnneeCibleId(), request.getEmployeurId());
        
        try {
            ExecutionReconductionResponse response = reconductionService.executerReconduction(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution", e);
            ExecutionReconductionResponse errorResponse = ExecutionReconductionResponse.builder()
                    .success(false)
                    .messageErreur("Erreur: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupère la progression d'une reconduction en cours.
     */
    @GetMapping("/{reconductionId}/progression")
    public ResponseEntity<ProgressionReconductionResponse> getProgression(
            @PathVariable String reconductionId) {
        
        log.debug("API: Récupération progression reconduction {}", reconductionId);
        
        try {
            ProgressionReconductionResponse progression = reconductionService.getProgression(reconductionId);
            return ResponseEntity.ok(progression);
        } catch (Exception e) {
            log.warn("Reconduction introuvable: {}", reconductionId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Annule une reconduction en cours.
     */
    @PostMapping("/{reconductionId}/annuler")
    public ResponseEntity<Map<String, Object>> annuler(@PathVariable String reconductionId) {
        
        log.info("API: Annulation reconduction {}", reconductionId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            // TODO: Implémenter l'annulation
            response.put("success", true);
            response.put("message", "Reconduction annulée");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'annulation", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Health check pour vérifier que le service est opérationnel.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Reconduction Service");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(status);
    }
}
