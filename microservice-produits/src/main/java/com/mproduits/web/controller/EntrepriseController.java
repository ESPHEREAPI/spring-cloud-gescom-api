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
 * Contrôleur REST pour la gestion des entreprises (exercices) de la
 * compagnie de l'appelant. La compagnie n'est jamais un parametre de
 * l'API - toujours derivee du token JWT (voir EntrepriseService).
 *
 * Endpoints:
 * - GET    /entreprises                  : Liste les exercices de ma compagnie
 * - GET    /entreprises/{anneeId}        : Recupere l'exercice d'une annee
 * - POST   /entreprises                  : Cree un exercice
 * - PUT    /entreprises/{anneeId}        : Met a jour un exercice
 * - DELETE /entreprises/{anneeId}        : Supprime un exercice
 * - GET    /entreprises/active           : Exercice(s) actif(s) de ma compagnie
 * - GET    /entreprises/annee/{anneeId}  : Exercice de ma compagnie pour une annee
 * - POST   /entreprises/{anneeId}/activer : Active un exercice par defaut
 * - GET    /entreprises/search           : Recherche avec filtres
 *
 * @author Système de Gestion
 */
@Slf4j
@RestController
@RequestMapping("/microservice-produits/entreprises")
public class EntrepriseController {
     @Autowired
    private EntrepriseService entrepriseService;

    /**
     * Liste tous les exercices de la compagnie courante.
     */
    @GetMapping
    public ResponseEntity<List<EntrepriseResponse>> findAll() {
        log.info("📋 GET /entreprises - Liste les exercices de la compagnie");

        List<EntrepriseResponse> entreprises = entrepriseService.findAll();

        log.info("✅ {} entreprise(s) trouvée(s)", entreprises.size());
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Récupère l'exercice d'une année pour la compagnie courante.
     */
    @GetMapping("/{anneeId}")
public ResponseEntity<Object> findById(@PathVariable("anneeId") Integer anneeId) {

    log.info("📋 GET /entreprises/{}", anneeId);

    return entrepriseService.findById(anneeId)
            .<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> {
                log.warn("⚠️ Entreprise non trouvée pour anneeId={}", anneeId);

                Map<String, Object> error = new HashMap<>();
                error.put("message", "Entreprise non trouvée");
                error.put("anneeId", anneeId);
                error.put("status", HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            });
}


    /**
     * Crée un nouvel exercice pour la compagnie courante.
     *
     * POST /entreprises
     *
     * Body:
     * {
     *   "anneeId": 1,
     *   "directeur": "Jean Dupont",
     *   "activite": "Commerce",
     *   "conventionCollective": "Convention 2025",
     *   "actif": true,
     *   "dateCreation": "2025-01-01"
     * }
     */
    @PostMapping
    public ResponseEntity<?> create(
            @Validated @RequestBody EntrepriseCreateRequest request,
            BindingResult result) {

        log.info("📝 POST /entreprises - Création entreprise");
        log.debug("Request: {}", request);

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
     * Met à jour l'exercice d'une année pour la compagnie courante.
     */
    @PutMapping("/{anneeId}")
    public ResponseEntity<?> update(
            @PathVariable Integer anneeId,
            @Valid @RequestBody EntrepriseCreateRequest request,
            BindingResult result) {

        log.info("📝 PUT /entreprises/{} - Mise à jour", anneeId);

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            log.warn("⚠️ Erreurs de validation: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EntrepriseResponse response = entrepriseService.update(anneeId, request);
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
     * Supprime l'exercice d'une année pour la compagnie courante.
     */
    @DeleteMapping("/{anneeId}")
    public ResponseEntity<?> delete(@PathVariable Integer anneeId) {

        log.info("🗑️ DELETE /entreprises/{}", anneeId);

        try {
            entrepriseService.delete(anneeId);
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
     * Liste les exercices actifs de la compagnie courante.
     */
    @GetMapping("/active")
    public ResponseEntity<List<EntrepriseResponse>> findAllActive() {
        log.info("📋 GET /entreprises/active");

        List<EntrepriseResponse> entreprises = entrepriseService.findAllActive();

        log.info("✅ {} entreprise(s) active(s) trouvée(s)", entreprises.size());
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Exercice de la compagnie courante pour une année (au plus un résultat).
     */
    @GetMapping("/annee/{anneeId}")
    public ResponseEntity<List<EntrepriseResponse>> findByAnnee(@PathVariable Integer anneeId) {
        log.info("📋 GET /entreprises/annee/{}", anneeId);

        List<EntrepriseResponse> entreprises = entrepriseService.findByAnnee(anneeId);

        log.info("✅ {} entreprise(s) trouvée(s) pour l'année {}", entreprises.size(), anneeId);
        return ResponseEntity.ok(entreprises);
    }

    /**
     * Active un exercice par défaut pour la compagnie courante (désactive les autres).
     */
    @PostMapping("/{anneeId}/activer")
    public ResponseEntity<?> activerParDefaut(@PathVariable Integer anneeId) {

        log.info("🔄 POST /entreprises/{}/activer", anneeId);

        try {
            entrepriseService.activerParDefaut(anneeId);

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
     * Recherche d'exercices avec filtres, restreinte a la compagnie courante.
     *
     * GET /entreprises/search?anneeId=1&actif=true&searchTerm=dupont
     */
    @GetMapping("/search")
    public ResponseEntity<List<EntrepriseResponse>> search(
            @RequestParam(required = false) Integer anneeId,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String searchTerm) {

        log.info("🔍 GET /entreprises/search - Filtres: anneeId={}, actif={}, term={}",
                 anneeId, actif, searchTerm);

        List<EntrepriseResponse> entreprises = entrepriseService.search(anneeId, actif, searchTerm);

        log.info("✅ {} entreprise(s) trouvée(s)", entreprises.size());
        return ResponseEntity.ok(entreprises);
    }


}
