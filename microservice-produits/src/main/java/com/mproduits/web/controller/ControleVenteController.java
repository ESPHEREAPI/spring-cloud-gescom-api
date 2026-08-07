package com.mproduits.web.controller;

import com.mproduits.dto.ControleVenteDTO;
import com.mproduits.dto.ControleVenteSummaryDTO;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.ControleVenteService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour le contrôle des ventes.
 * Expose les endpoints pour générer les rapports de ventes consolidés.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-28
 */
@RestController
@RequestMapping("/microservice-produits/controle-vente")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contrôle Ventes", description = "API de contrôle et consolidation des ventes")
//@CrossOrigin(origins = {"http://localhost:4200", "https://votre-domaine.com"})
public class ControleVenteController {

    private final ControleVenteService controleVenteService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;

    /**
     * Génère le contrôle des ventes pour un mois donné.
     * Consolide toutes les recettes (caisse, clients, photocopies, ressources) par jour.
     *
     * @param moisId L'identifiant du mois
     * @param entrepriseId L'identifiant de l'entreprise
     * @return Liste des contrôles de vente par jour
     */
    @GetMapping
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Générer le contrôle des ventes",
        description = "Génère un rapport consolidé de toutes les recettes pour un mois donné"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrôle généré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Mois ou entreprise non trouvé")
    })
    public ResponseEntity<List<ControleVenteDTO>> generateControleVentes(
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid,  @RequestParam Long  boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête de génération du contrôle des ventes - Mois: {}, Entreprise: {}",
                moisId, anneeid,boutiqueid);

        List<ControleVenteDTO> controles = controleVenteService.generateControleVentes(
                moisId, anneeid,boutiqueid);

        return ResponseEntity.ok(controles);
    }

    /**
     * Génère le contrôle des ventes pour une période spécifique.
     *
     * @param moisId L'identifiant du mois
     * @param entrepriseId L'identifiant de l'entreprise
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @return Liste des contrôles de vente filtrés
     */
    @GetMapping("/period")
    //@PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Générer le contrôle pour une période",
        description = "Génère un rapport consolidé pour une période spécifique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrôle généré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Mois ou entreprise non trouvé")
    })
    public ResponseEntity<List<ControleVenteDTO>> generateControleVentesForPeriod(
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid,
            @RequestParam Long boutiqueid,
            @Parameter(description = "Date de début")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(description = "Date de fin")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête de génération du contrôle pour période - Mois: {}, Période: {} - {}",
                moisId, dateDebut, dateFin);

        List<ControleVenteDTO> controles = controleVenteService.generateControleVentesForPeriod(
                moisId,boutiqueid ,anneeid, dateDebut, dateFin);

        return ResponseEntity.ok(controles);
    }

    /**
     * Récupère le résumé global d'un mois.
     * Calcule les totaux de toutes les recettes.
     *
     * @param moisId L'identifiant du mois
     * @param entrepriseId L'identifiant de l'entreprise
     * @return Le résumé du mois
     */
    @GetMapping("/summary")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Résumé du contrôle des ventes",
        description = "Récupère le résumé global avec tous les totaux du mois"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résumé récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Mois ou entreprise non trouvé")
    })
    public ResponseEntity<ControleVenteSummaryDTO> getSummary(
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid, 
            @RequestParam Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête de résumé du contrôle des ventes - Mois: {}, Entreprise: {}",
                moisId, anneeid);

        ControleVenteSummaryDTO summary = controleVenteService.getSummary(moisId, boutiqueid, anneeid);

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
        description = "Endpoint pour vérifier que le service de contrôle des ventes est actif"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Contrôle Ventes service is running");
    }
}
