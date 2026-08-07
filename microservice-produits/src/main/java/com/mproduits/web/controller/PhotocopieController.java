package com.mproduits.web.controller;


import com.entreprise.recette.dto.PhotocopieSummaryDTO;
import com.mproduits.dto.PhotocopieDTO;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.PhotocopieService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Contrôleur REST pour la gestion des photocopies.
 * Expose les endpoints de l'API pour les opérations CRUD et les statistiques.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-27
 */
@RestController
@RequestMapping("/microservice-produits/photocopie")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Photocopie", description = "API de gestion des recettes photocopie")
//@CrossOrigin(origins = {"http://localhost:4200", "https://votre-domaine.com"})
public class PhotocopieController {

    private final PhotocopieService photocopieService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;

    /**
     * Crée une nouvelle photocopie.
     *
     * @param dto Les données de la photocopie à créer
     * @return La photocopie créée avec le code HTTP 201 (CREATED)
     */
    @PostMapping
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE')")
    @Operation(
        summary = "Créer une photocopie",
        description = "Crée une nouvelle entrée de recette photocopie"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Photocopie créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Non autorisé"),
        @ApiResponse(responseCode = "404", description = "Entreprise ou mois non trouvé"),
        @ApiResponse(responseCode = "409", description = "Conflit - Doublon détecté")
    })
    public ResponseEntity<PhotocopieDTO> create(
            @Valid @RequestBody PhotocopieDTO dto) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(dto.getBoutiqueid());
        log.info("Requête de création d'une photocopie : {}", dto.getLibelle());
        
        PhotocopieDTO created = photocopieService.create(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Met à jour une photocopie existante.
     *
     * @param id L'identifiant de la photocopie
     * @param dto Les nouvelles données
     * @return La photocopie mise à jour
     */
    @PutMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'CAISSE')")
    @Operation(
        summary = "Modifier une photocopie",
        description = "Met à jour les informations d'une photocopie existante"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photocopie modifiée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Non autorisé"),
        @ApiResponse(responseCode = "404", description = "Photocopie non trouvée")
    })
    public ResponseEntity<PhotocopieDTO> update(
            @Parameter(description = "ID de la photocopie", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PhotocopieDTO dto) {
        log.info("Requête de modification de la photocopie ID: {}", id);
        
        PhotocopieDTO updated = photocopieService.update(id, dto);
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Récupère une photocopie par son identifiant.
     *
     * @param id L'identifiant de la photocopie
     * @return La photocopie demandée
     */
    @GetMapping("/{id}")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Récupérer une photocopie",
        description = "Récupère les détails d'une photocopie par son ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photocopie trouvée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Photocopie non trouvée")
    })
    public ResponseEntity<PhotocopieDTO> findById(
            @Parameter(description = "ID de la photocopie", required = true)
            @PathVariable Long id) {
        log.debug("Requête de récupération de la photocopie ID: {}", id);
        
        PhotocopieDTO photocopie = photocopieService.findById(id);
        
        return ResponseEntity.ok(photocopie);
    }

    /**
     * Récupère toutes les photocopies d'un mois avec pagination.
     *
     * @param moisId L'identifiant du mois
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Liste des photocopies",
        description = "Récupère la liste paginée des photocopies d'un mois"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Mois non trouvé")
    })
    public ResponseEntity<Page<PhotocopieDTO>> findByMois(
            @Parameter(description = "ID du mois", required = true)
            @RequestParam int anneeid, @RequestParam Long boutiqueid,@RequestParam String userName,
            @PageableDefault(size = 15, sort = "dateReception", direction = Sort.Direction.DESC)
            Pageable pageable) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        log.debug("Requête de liste des photocopies du mois ID: {}", anneeid);

        Page<PhotocopieDTO> photocopies = photocopieService.findByMois(anneeid,boutiqueid,userName, pageable);
        
        return ResponseEntity.ok(photocopies);
    }

    /**
     * Recherche des photocopies avec filtres.
     *
     * @param moisId Identifiant du mois (obligatoire)
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @param libelle Libellé à rechercher (optionnel)
     * @param pageable Paramètres de pagination
     * @return Une page de photocopies filtrées
     */
    @GetMapping("/search")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Rechercher des photocopies",
        description = "Recherche des photocopies avec filtres multiples"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Page<PhotocopieDTO>> search(
            @Parameter(description = "ID du mois") @RequestParam int anneeid,
               @Parameter(description = "boutiqueid") @RequestParam Long boutiqueid,@RequestParam String userName,
            @Parameter(description = "Date de début") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @Parameter(description = "Libellé à rechercher") 
            @RequestParam(required = false) String libelle,
            @PageableDefault(size = 15, sort = "dateReception", direction = Sort.Direction.DESC)
            Pageable pageable) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        log.debug("Requête de recherche - Mois: {}, Libellé: {}", anneeid, libelle);

        Page<PhotocopieDTO> photocopies = photocopieService.findWithFilters(
                anneeid,boutiqueid, userName, dateDebut, dateFin, libelle, pageable);
        
        return ResponseEntity.ok(photocopies);
    }

    /**
     * Supprime logiquement une photocopie.
     *
     * @param id L'identifiant de la photocopie à supprimer
     * @return Réponse HTTP 204 (NO CONTENT)
     */
    @DeleteMapping("/{id}")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE')")
    @Operation(
        summary = "Supprimer une photocopie",
        description = "Supprime logiquement une photocopie (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Photocopie supprimée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Non autorisé"),
        @ApiResponse(responseCode = "404", description = "Photocopie non trouvée")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la photocopie", required = true)
            @PathVariable Long id) {
        log.info("Requête de suppression de la photocopie ID: {}", id);
        
        photocopieService.delete(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Restaure une photocopie supprimée.
     *
     * @param id L'identifiant de la photocopie à restaurer
     * @return La photocopie restaurée
     */
    @PostMapping("/{id}/restore")
   // @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Restaurer une photocopie",
        description = "Restaure une photocopie supprimée logiquement"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photocopie restaurée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Non autorisé"),
        @ApiResponse(responseCode = "404", description = "Photocopie non trouvée")
    })
    public ResponseEntity<PhotocopieDTO> restore(
            @Parameter(description = "ID de la photocopie", required = true)
            @PathVariable Long id) {
        log.info("Requête de restauration de la photocopie ID: {}", id);
        
        PhotocopieDTO restored = photocopieService.restore(id);
        
        return ResponseEntity.ok(restored);
    }

    /**
     * Récupère le résumé statistique d'un mois.
     *
     * @param moisId L'identifiant du mois
     * @return Les statistiques du mois
     */
    @GetMapping("/summary")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Résumé des photocopies",
        description = "Récupère les statistiques et résumé d'un mois"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résumé récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Mois non trouvé")
    })
    public ResponseEntity<PhotocopieSummaryDTO> getSummary(
            @Parameter(description = "ID du mois", required = true)
            @RequestParam int anneeid, @RequestParam Long boutiqueid,@RequestParam String userName  ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        log.debug("Requête de résumé pour le anneeid ID: {}", anneeid);
        
        PhotocopieSummaryDTO summary = photocopieService.getSummary(anneeid,boutiqueid,userName);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Endpoint de santé pour vérifier que le service est opérationnel.
     *
     * @return Message de statut
     */
    @GetMapping("/health")
    @Operation(
        summary = "Vérification de santé",
        description = "Endpoint pour vérifier que le service est actif"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Photocopie service is running");
    }
}
