/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.EntrepriseCreateRequest;
import com.mproduits.dto.EntrepriseResponse;
import com.mproduits.services.EntrepriseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

/**
 * Contrôleur REST pour la gestion des entreprises
 * 
 * Endpoints:
 * - GET    /api/entreprises                  : Liste toutes les entreprises
 * - GET    /api/entreprises/{anneeId}/{employeurId} : Récupère une entreprise
 * - POST   /api/entreprises                  : Crée une entreprise
 * - PUT    /api/entreprises/{anneeId}/{employeurId} : Met à jour une entreprise
 * - DELETE /api/entreprises/{anneeId}/{employeurId} : Supprime une entreprise
 * - GET    /api/entreprises/active          : Liste les entreprises actives
 * - GET    /api/entreprises/annee/{anneeId} : Liste par année
 * - POST   /api/entreprises/{anneeId}/{employeurId}/activer : Active par défaut
 * - GET    /api/entreprises/search          : Recherche avec filtres
 * 
 * @author Système de Gestion
 */
@Slf4j
@RestController
@RequestMapping("/microservice-produits/entreprises")
/**
 *
 * @author USER01
 */
public class EntrepriseController {
     @Autowired
    private EntrepriseService entrepriseService;

    /**
     * Liste toutes les entreprises
     * 
     * GET /api/entreprises
     * 
     * @return Liste de toutes les entreprises
     */
    @GetMapping
    public ResponseEntity<List<EntrepriseResponse>> findAll() {
        log.info("📋 GET /api/entreprises - Liste toutes les entreprises");
        
        List<EntrepriseResponse> entreprises = entrepriseService.findAll();
        
        log.info("✅ {} entreprise(s) trouvée(s)", entreprises.size());
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Récupère une entreprise par sa clé composite
     * 
     * GET /api/entreprises/{anneeId}/{employeurId}
     * 
     * @param anneeId ID de l'année
     * @param employeurId ID de l'employeur
     * @return L'entreprise trouvée ou 404
     */
    @GetMapping("/{anneeId}/{employeurId}")
public ResponseEntity<Object> findById(
        @PathVariable("anneeId") Integer anneeId,
        @PathVariable("employeurId") Long employeurId) {

    log.info("📋 GET /api/entreprises/{}/{}", anneeId, employeurId);

    return entrepriseService.findById(anneeId, employeurId)
            .<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> {
                log.warn("⚠️ Entreprise non trouvée pour anneeId={} et employeurId={}", anneeId, employeurId);

                Map<String, Object> error = new HashMap<>();
                error.put("message", "Entreprise non trouvée");
                error.put("anneeId", anneeId);
                error.put("employeurId", employeurId);
                error.put("status", HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            });
}


    /**
     * Crée une nouvelle entreprise
     * 
     * POST /api/entreprises
     * 
     * Body:
     * {
     *   "anneeId": 1,
     *   "employeurId": 1,
     *   "directeur": "Jean Dupont",
     *   "activite": "Commerce",
     *   "conventionCollective": "Convention 2025",
     *   "actif": true,
     *   "dateCreation": "2025-01-01"
     * }
     * 
     * @param request Données de création
     * @param result Résultats de validation
     * @return L'entreprise créée ou erreurs de validation
     */
    @PostMapping
    public ResponseEntity<?> create(
            @Validated @RequestBody EntrepriseCreateRequest request,
            BindingResult result) {
        
        log.info("📝 POST /api/entreprises - Création entreprise");
        log.debug("Request: {}", request);

        // Validation des erreurs
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            log.warn("⚠️ Erreurs de validation: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EntrepriseResponse response = entrepriseService.create(request);
            log.info("✅ Entreprise créée avec succès");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Erreur métier: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de la création", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatusCode.valueOf(0)).body(error);
        }
    }

    /**
     * Met à jour une entreprise existante
     * 
     * PUT /api/entreprises/{anneeId}/{employeurId}
     * 
     * @param anneeId ID de l'année
     * @param employeurId ID de l'employeur
     * @param request Nouvelles données
     * @param result Résultats de validation
     * @return L'entreprise mise à jour ou erreurs
     */
    @PutMapping("/{anneeId}/{employeurId}")
    public ResponseEntity<?> update(
            @PathVariable Integer anneeId,
            @PathVariable Long employeurId,
            @Valid @RequestBody EntrepriseCreateRequest request,
            BindingResult result) {
        
        log.info("📝 PUT /api/entreprises/{}/{} - Mise à jour", anneeId, employeurId);

        // Validation
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            log.warn("⚠️ Erreurs de validation: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EntrepriseResponse response = entrepriseService.update(anneeId, employeurId, request);
            log.info("✅ Entreprise mise à jour avec succès");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Erreur métier: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de la mise à jour", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Supprime une entreprise
     * 
     * DELETE /api/entreprises/{anneeId}/{employeurId}
     * 
     * @param anneeId ID de l'année
     * @param employeurId ID de l'employeur
     * @return 204 No Content ou erreur
     */
    @DeleteMapping("/{anneeId}/{employeurId}")
    public ResponseEntity<?> delete(
            @PathVariable Integer anneeId,
            @PathVariable Long employeurId) {
        
        log.info("🗑️ DELETE /api/entreprises/{}/{}", anneeId, employeurId);

        try {
            entrepriseService.delete(anneeId, employeurId);
            log.info("✅ Entreprise supprimée avec succès");
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Erreur: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de la suppression", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Liste les entreprises actives
     * 
     * GET /api/entreprises/active
     * 
     * @return Liste des entreprises actives
     */
    @GetMapping("/active")
    public ResponseEntity<List<EntrepriseResponse>> findAllActive() {
        log.info("📋 GET /api/entreprises/active");
        
        List<EntrepriseResponse> entreprises = entrepriseService.findAllActive();
        
        log.info("✅ {} entreprise(s) active(s) trouvée(s)", entreprises.size());
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Liste les entreprises par année
     * 
     * GET /api/entreprises/annee/{anneeId}
     * 
     * @param anneeId ID de l'année
     * @return Liste des entreprises de l'année
     */
    @GetMapping("/annee/{anneeId}")
    public ResponseEntity<List<EntrepriseResponse>> findByAnnee(@PathVariable Integer anneeId) {
        log.info("📋 GET /api/entreprises/annee/{}", anneeId);
        
        List<EntrepriseResponse> entreprises = entrepriseService.findByAnnee(anneeId);
        
        log.info("✅ {} entreprise(s) trouvée(s) pour l'année {}", entreprises.size(), anneeId);
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Active une entreprise par défaut (désactive les autres)
     * 
     * POST /api/entreprises/{anneeId}/{employeurId}/activer
     * 
     * @param anneeId ID de l'année
     * @param employeurId ID de l'employeur
     * @return Message de succès ou erreur
     */
    @PostMapping("/{anneeId}/{employeurId}/activer")
    public ResponseEntity<?> activerParDefaut(
            @PathVariable Integer anneeId,
            @PathVariable Long employeurId) {
        
        log.info("🔄 POST /api/entreprises/{}/{}/activer", anneeId, employeurId);

        try {
            entrepriseService.activerParDefaut(anneeId, employeurId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Entreprise activée par défaut avec succès");
            
            log.info("✅ Entreprise activée par défaut");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Erreur: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de l'activation", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Recherche d'entreprises avec filtres
     * 
     * GET /api/entreprises/search?anneeId=1&actif=true&searchTerm=dupont
     * 
     * @param anneeId Filtre par année (optionnel)
     * @param employeurId Filtre par employeur (optionnel)
     * @param actif Filtre par statut actif (optionnel)
     * @param searchTerm Terme de recherche (optionnel)
     * @return Liste des entreprises correspondant aux critères
     */
    @GetMapping("/search")
    public ResponseEntity<List<EntrepriseResponse>> search(
            @RequestParam(required = false) Integer anneeId,
            @RequestParam(required = false) Long employeurId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String searchTerm) {
        
        log.info("🔍 GET /api/entreprises/search - Filtres: anneeId={}, employeurId={}, actif={}, term={}", 
                 anneeId, employeurId, actif, searchTerm);
        
        List<EntrepriseResponse> entreprises = entrepriseService.search(
            anneeId, employeurId, actif, searchTerm
        );
        
        log.info("✅ {} entreprise(s) trouvée(s)", entreprises.size());
        return ResponseEntity.ok(entreprises);
    }
    
    
}
