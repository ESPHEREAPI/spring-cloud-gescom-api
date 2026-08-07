package com.mproduits.web.controller;

import com.entreprise.historique.dto.HistoriquePhotocopieDTO;
import com.mproduits.dto.HistoriquePhotocopieSummaryDTO;
import com.mproduits.dto.UserDTO;
import com.mproduits.model.Annee;
import com.mproduits.model.Mois;
import com.mproduits.model.Personne;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.HistoriqueCaisseService;
import com.mproduits.services.HistoriquePhotocopieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contrôleur REST pour l'historique des photocopies.
 * Expose les endpoints pour consulter l'historique avec différents filtres.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
@RestController
@RequestMapping("/microservice-produits/historique-photocopie")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Historique Photocopie", description = "API de consultation de l'historique des photocopies")
//@CrossOrigin(origins = {"http://localhost:4200", "https://votre-domaine.com"})
public class HistoriquePhotocopieController {

    private final HistoriquePhotocopieService historiqueService;

  private final  HistoriqueCaisseService historiqueCaisseService;
  private final BoutiqueAccessGuard boutiqueAccessGuard;

    /**
     * Récupère les dates disponibles pour un mois donné.
     * Utilisé pour remplir le sélecteur de date.
     *
     * @param entrepriseId ID de l'entreprise (obligatoire)
     * @param moisId ID du mois (obligatoire)
     * @return Liste des dates avec des opérations
     */
    @GetMapping("/dates")
    //@PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Récupérer les dates disponibles",
        description = "Liste les dates ayant des opérations de photocopie pour un mois donné"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dates récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Entreprise ou mois non trouvé")
    })
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid,
            
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId, @RequestParam Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête GET /api/historique-photocopie/dates - Entreprise: {}, Mois: {}",
                anneeid, moisId);

        List<LocalDate> dates = historiqueService.getAvailableDates(anneeid, moisId,boutiqueid);

        log.info("{} dates disponibles trouvées", dates.size());

        return ResponseEntity.ok(dates);
    }

    /**
     * Génère l'historique des photocopies pour une date donnée.
     *
     * @param entrepriseId ID de l'entreprise (obligatoire)
     * @param moisId ID du mois (obligatoire)
     * @param date Date sélectionnée (obligatoire)
     * @param personneId ID du caissier (optionnel, mode multi-caisse)
     * @param multiCaisse Mode multi-caisse activé (défaut: false)
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de page (défaut: 50)
     * @param sortBy Champ de tri (défaut: createdAt)
     * @param direction Direction du tri (défaut: DESC)
     * @return Page de DTOs d'historique
     */
    @GetMapping
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Consulter l'historique",
        description = "Récupère l'historique des photocopies pour une date donnée avec filtres optionnels"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée")
    })
    public ResponseEntity<Page<HistoriquePhotocopieDTO>> getHistorique(
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid,
            
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,@RequestParam Long boutiqueid,
            
            @Parameter(description = "Date sélectionnée (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "ID du caissier (mode multi-caisse)")
            @RequestParam(required = false) String username,
            
            @Parameter(description = "Mode multi-caisse activé")
            @RequestParam(defaultValue = "false") Boolean multiCaisse,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Taille de page")
            @RequestParam(defaultValue = "50") int size,
            
            @Parameter(description = "Champ de tri")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Direction du tri")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête GET /api/historique-photocopie - Entreprise: {}, Mois: {}, Date: {}, MultiCaisse: {}",
                anneeid, moisId, date, multiCaisse);

        // Construction de la pagination avec tri
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Appel au service
        Page<HistoriquePhotocopieDTO> historique = historiqueService.getHistorique(
                anneeid, moisId,boutiqueid, date, username, multiCaisse, pageable);

        log.info("Historique récupéré - {} opérations sur {} total", 
                historique.getNumberOfElements(), historique.getTotalElements());

        return ResponseEntity.ok(historique);
    }

    /**
     * Calcule le total des montants pour les paramètres donnés.
     *
     * @param entrepriseId ID de l'entreprise
     * @param moisId ID du mois
     * @param date Date sélectionnée
     * @param personneId ID du caissier (optionnel)
     * @param multiCaisse Mode multi-caisse activé
     * @return Total des montants
     */
    @GetMapping("/total")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Calculer le total",
        description = "Calcule le total des montants pour les filtres donnés"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total calculé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée")
    })
    public ResponseEntity<BigDecimal> getTotal(
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid,
            
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,  @RequestParam Long boutiqueid,
            
            @Parameter(description = "Date sélectionnée (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "ID du caissier (mode multi-caisse)")
            @RequestParam(required = false) String username,
            
            @Parameter(description = "Mode multi-caisse activé")
            @RequestParam(defaultValue = "false") Boolean multiCaisse) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête GET /api/historique-photocopie/total - Entreprise: {}, Mois: {}, Date: {}",
                anneeid, moisId, date);

        BigDecimal total = historiqueService.calculateTotal(
                anneeid, moisId,boutiqueid, date, username, multiCaisse);

        log.debug("Total calculé : {}", total);

        return ResponseEntity.ok(total);
    }

    /**
     * Récupère le résumé/statistiques de l'historique.
     *
     * @param entrepriseId ID de l'entreprise
     * @param moisId ID du mois
     * @param date Date sélectionnée
     * @param personneId ID du caissier (optionnel)
     * @param multiCaisse Mode multi-caisse activé
     * @return DTO contenant les statistiques
     */
    @GetMapping("/summary")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Résumé/Statistiques",
        description = "Récupère les statistiques de l'historique (total, moyenne, nombre d'opérations)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résumé récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée")
    })
    public ResponseEntity<HistoriquePhotocopieSummaryDTO> getSummary(
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int  anneeid,
            
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,
             @RequestParam Long boutiqueid,
            @Parameter(description = "Date sélectionnée (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "ID du caissier (mode multi-caisse)")
            @RequestParam(required = false) String username,
            
            @Parameter(description = "Mode multi-caisse activé")
            @RequestParam(defaultValue = "false") Boolean multiCaisse) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête GET /api/historique-photocopie/summary - Entreprise: {}, Mois: {}, Date: {}",
                anneeid, moisId, date);

        HistoriquePhotocopieSummaryDTO summary = historiqueService.getSummary(
                anneeid, moisId, date,boutiqueid, username, multiCaisse);

        return ResponseEntity.ok(summary);
    }

    /**
     * Recherche dans l'historique par libellé.
     *
     * @param entrepriseId ID de l'entreprise
     * @param moisId ID du mois
     * @param searchTerm Terme de recherche
     * @param pageable Paramètres de pagination
     * @return Page de résultats
     */
    @GetMapping("/search")
   // @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE', 'VIEWER')")
    @Operation(
        summary = "Rechercher dans l'historique",
        description = "Recherche dans l'historique par libellé"
    )
    public ResponseEntity<Page<HistoriquePhotocopieDTO>> search(
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam int anneeid,
            
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,@RequestParam Long boutiqueid,
            
            @Parameter(description = "Terme de recherche", required = true)
            @RequestParam String searchTerm,
            
            Pageable pageable) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("Requête GET /api/historique-photocopie/search - Terme: {}", searchTerm);

        Page<HistoriquePhotocopieDTO> results = historiqueService.searchByLibelle(
                anneeid, moisId,boutiqueid, searchTerm, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Exporte l'historique au format PDF.
     *
     * @param entrepriseId ID de l'entreprise
     * @param moisId ID du mois
     * @param date Date sélectionnée
     * @param personneId ID du caissier (optionnel)
     * @param multiCaisse Mode multi-caisse activé
     * @return Fichier PDF
     */
    @GetMapping("/export/pdf")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'CAISSE')")
    @Operation(
        summary = "Export PDF",
        description = "Génère un rapport PDF de l'historique"
    )
    public ResponseEntity<byte[]> exportPDF(
            @Parameter(description = "ID de l'entreprise", required = true)
            @RequestParam Long entrepriseId,
            
            @Parameter(description = "ID du mois", required = true)
            @RequestParam Long moisId,
            
            @Parameter(description = "Date sélectionnée (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "ID du caissier (mode multi-caisse)")
            @RequestParam(required = false) Long personneId,
            
            @Parameter(description = "Mode multi-caisse activé")
            @RequestParam(defaultValue = "false") Boolean multiCaisse) {
        
        log.info("Requête GET /api/historique-photocopie/export/pdf - Entreprise: {}, Mois: {}, Date: {}", 
                entrepriseId, moisId, date);

        byte[] pdfContent = historiqueService.generatePDFReport(
                entrepriseId, moisId, date, personneId, multiCaisse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                String.format("historique-photocopie-%s.pdf", date));

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    /**
     * Endpoint de santé pour vérifier que le service est opérationnel.
     *
     * @return Message de statut
     */
    @GetMapping("/health")
    @Operation(
        summary = "Vérification de santé",
        description = "Endpoint pour vérifier que le service d'historique photocopie est actif"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Historique Photocopie service is running");
    }
    
     @GetMapping("/allcaissier/{username}/{anneeid}")
    
    public ResponseEntity<List<Mois>> allCaissier(@PathVariable String username,@PathVariable int anneeid) {
        return ResponseEntity.ok(historiqueService.getAllMoisByAnneeForCaissier(anneeid, username));
    }
    
     @GetMapping("/allcaissier/{boutiqueid}")
    
    public ResponseEntity<List<UserDTO>> allCaissier(@PathVariable Long boutiqueid ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        return ResponseEntity.ok(historiqueService.getAllCaisssierByBoutique(boutiqueid));
    }
    
     @GetMapping("/annees/{boutiqueid}")
    public ResponseEntity<List<Annee>> listAnnee(@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        List<Annee> allAnnees = historiqueCaisseService.listeAnneeByVente(boutiqueid);
        return ResponseEntity.ok(allAnnees);
    }
}
