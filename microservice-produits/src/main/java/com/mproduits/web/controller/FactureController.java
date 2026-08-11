package com.mproduits.web.controller;

import com.mproduits.dto.DevisToFactureRequest;
import com.mproduits.dto.FactureAnnulationRequest;
import com.mproduits.dto.FactureCreateRequest;
import com.mproduits.dto.FactureResponse;
import com.mproduits.dto.FactureSearchCriteria;
import com.mproduits.dto.FactureStatistiques;
import com.mproduits.dto.FactureSummary;
import com.mproduits.dto.FactureUpdateRequest;
import com.mproduits.dto.FactureValidationRequest;
import com.mproduits.dto.ProduitDto;

import com.mproduits.enums.StatutFacture;

import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.security.TenantContext;
import com.mproduits.services.FactureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Contrôleur REST pour la gestion des factures
 * 
 * ENDPOINTS:
 * - POST   /api/factures                    → Créer une facture
 * - POST   /api/factures/from-devis         → Convertir devis en facture
 * - PUT    /api/factures/{id}               → Modifier une facture (brouillon)
 * - POST   /api/factures/{id}/valider       → Valider une facture
 * - POST   /api/factures/{id}/annuler       → Annuler une facture
 * - GET    /api/factures/{id}               → Récupérer une facture
 * - GET    /api/factures/numero/{numero}    → Récupérer par numéro
 * - GET    /api/factures                    → Lister avec filtres
 * - GET    /api/factures/client/{clientId}  → Factures d'un client
 * - GET    /api/factures/statistiques       → Statistiques
 * 
 * SÉCURITÉ:
 * - Authentification requise pour tous les endpoints
 * - ROLE_USER: Lecture seule
 * - ROLE_GESTIONNAIRE: Création, modification, validation
 * - ROLE_ADMIN: Toutes opérations y compris annulation
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
@RestController
@RequestMapping("/microservice-produits/factures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Factures", description = "API de gestion des factures clients")
public class FactureController {

    private final FactureService factureService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;
    private final TenantContext tenantContext;

    /**
     * Crée une nouvelle facture (création directe)
     * 
     * @param request Données de la facture
     * @return Facture créée
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    @PostMapping
    @Operation(summary = "Créer une facture",
               description = "Crée une nouvelle facture directement (sans passer par un devis)")
    public ResponseEntity<FactureResponse> creerFacture(@Valid @RequestBody FactureCreateRequest request) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(request.getBoutiqueid());
        log.info("Création d'une nouvelle facture pour le client ID: {}", request.getClientId());
        FactureResponse response = factureService.creerFacture(request, tenantContext.currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Convertit un devis accepté en facture
     * 
     * @param request Données de conversion
     * @return Facture créée
     */
    @PostMapping("/from-devis")
   
    @Operation(summary = "Convertir un devis en facture",
               description = "Convertit un devis accepté en facture")
    public ResponseEntity<FactureResponse> convertirDevisEnFacture(
            @Valid @RequestBody DevisToFactureRequest request) {
        log.info("Conversion du devis ID: {} en facture", request.getDevisId());
        FactureResponse response = factureService.convertirDevisEnFacture(request, tenantContext.currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Modifie une facture (uniquement si statut BROUILLON)
     * 
     * @param id ID de la facture
     * @param request Données de modification
     * @return Facture modifiée
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une facture",
               description = "Modifie une facture en brouillon")
    public ResponseEntity<FactureResponse> modifierFacture(
            @PathVariable Long id,
            @Valid @RequestBody FactureUpdateRequest request) {
        log.info("Modification de la facture ID: {}", id);
        request.setId(id);
        FactureResponse response = factureService.modifierFacture(request, tenantContext.currentUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Valide une facture (BROUILLON → NON_PAYEE)
     * CRITIQUE: Effectue la sortie de stock
     * 
     * @param id ID de la facture
     * @param request Données de validation
     * @return Facture validée
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    @PostMapping("/{id}/valider")
    @Operation(summary = "Valider une facture",
               description = "Valide une facture brouillon et effectue la sortie de stock")
    public ResponseEntity<FactureResponse> validerFacture(
            @PathVariable Long id,
            @Valid @RequestBody FactureValidationRequest request) {
        log.info("Validation de la facture ID: {}", id);
        request.setFactureId(id);
        FactureResponse response = factureService.validerFacture(request, tenantContext.currentUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Annule une facture
     * CRITIQUE: Réintègre le stock
     * 
     * @param id ID de la facture
     * @param request Données d'annulation
     * @return Facture annulée
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler une facture",
               description = "Annule une facture et réintègre le stock")
    public ResponseEntity<FactureResponse> annulerFacture(
            @PathVariable Long id,
            @Valid @RequestBody FactureAnnulationRequest request) {
        log.info("Annulation de la facture ID: {}", id);
        request.setFactureId(id);
        FactureResponse response = factureService.annulerFacture(request, tenantContext.currentUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère une facture par son ID
     * 
     * @param id ID de la facture
     * @return Facture trouvée
     */
    @GetMapping("/{id}")

    @Operation(summary = "Récupérer une facture",
               description = "Récupère les détails d'une facture par son ID")
    public ResponseEntity<FactureResponse> getFacture(@PathVariable Long id) {
        log.info("Récupération de la facture ID: {}", id);
        FactureResponse response = factureService.getFacture(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère une facture par son numéro
     * 
     * @param numero Numéro de la facture
     * @return Facture trouvée
     */
    @GetMapping("/numero/{numero}")

    @Operation(summary = "Récupérer une facture par numéro",
               description = "Récupère les détails d'une facture par son numéro")
    public ResponseEntity<FactureResponse> getFactureParNumero(@PathVariable String numero) {
        log.info("Récupération de la facture numéro: {}", numero);
        FactureResponse response = factureService.getFactureParNumero(numero);
        return ResponseEntity.ok(response);
    }

    /**
     * Liste les factures avec filtres et pagination
     * 
     * @param criteria Critères de recherche
     * @return Page de factures
     */
    @GetMapping("")

    @Operation(summary = "Lister les factures",
               description = "Liste les factures avec filtres et pagination")
    public ResponseEntity<Page<FactureSummary>> listerFactures(
            @Parameter(hidden = true) FactureSearchCriteria criteria) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(criteria.getBoutiqueid());
        log.info("Liste des factures avec critères: {}", criteria);
        Page<FactureSummary> response = factureService.listerFactures(criteria);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les factures d'un client
     * 
     * @param clientId ID du client
     * @return Liste des factures du client
     */
    @GetMapping("/client/{clientId}/{boutiqueid}")

    @Operation(summary = "Factures d'un client",
               description = "Récupère toutes les factures d'un client")
    public ResponseEntity<List<FactureSummary>> getFacturesClient(@PathVariable Long clientId,@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        log.info("Récupération des factures du client ID: {}", clientId);
        List<FactureSummary> response = factureService.getFacturesClient(clientId,boutiqueid);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les statistiques de facturation
     * 
     * @param dateDebut Date de début (optionnelle)
     * @param dateFin Date de fin (optionnelle)
     * @return Statistiques
     */
    @GetMapping("/statistiques")
   
    @Operation(summary = "Statistiques de facturation",
               description = "Récupère les statistiques de facturation sur une période")
    public ResponseEntity<FactureStatistiques> getStatistiques(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        log.info("Récupération des statistiques de facturation");
        FactureStatistiques response = factureService.getStatistiques(dateDebut, dateFin);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/getStatistiquesAll")
   
    @Operation(summary = "Statistiques de facturation",
               description = "Récupère les statistiques de facturation sur une période")
    public ResponseEntity<FactureStatistiques> getStatistiques() {
        log.info("Récupération des statistiques de facturation");
        FactureStatistiques response = factureService.getStatistiques();
        return ResponseEntity.ok(response);
    }

    /**
     * Recherche de factures par critères avancés
     * 
     * Paramètres de recherche:
     * - numeroFacture: Numéro de facture
     * - clientId: ID du client
     * - statut: Statut de la facture
     * - dateFactureDebut/Fin: Période de facturation
     * - dateEcheanceDebut/Fin: Période d'échéance
     * - montantMin/Max: Fourchette de montant
     * - enRetard: true/false
     * - page, size: Pagination
     * - sortBy, sortDirection: Tri
     */
    @GetMapping("/search")
  
    @Operation(summary = "Recherche avancée",
               description = "Recherche de factures avec critères avancés")
    public ResponseEntity<Page<FactureSummary>> rechercherFactures(
            @RequestParam(required = false) String numeroFacture,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFactureDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFactureFin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEcheanceDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEcheanceFin,
            @RequestParam(required = false) Boolean enRetard,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "dateFacture") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("Recherche avancée de factures");
        
        FactureSearchCriteria criteria = FactureSearchCriteria.builder()
                .numeroFacture(numeroFacture)
                .clientId(clientId)
                .statut(statut != null ? StatutFacture.valueOf(statut) : null)
                .dateFactureDebut(dateFactureDebut)
                .dateFactureFin(dateFactureFin)
                .dateEcheanceDebut(dateEcheanceDebut)
                .dateEcheanceFin(dateEcheanceFin)
                .enRetard(enRetard)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        Page<FactureSummary> response = factureService.listerFactures(criteria);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/pdf")
public ResponseEntity<Resource> genererPDF(@PathVariable Long id) {
    byte[] pdfContent = factureService.genererPDF(id);
    ByteArrayResource resource = new ByteArrayResource(pdfContent);
    
    FactureResponse facture = factureService.getFacture(id);
    
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=" + facture.getNumeroFacture() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfContent.length)
            .body(resource);
}

@GetMapping("/produits/have-stock/{anneeId}/{boutiqueId}")
public ResponseEntity<List<ProduitDto>> getProduitsHaveStock(
        @PathVariable Integer anneeId,
        @PathVariable Long boutiqueId
) {
    boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueId);
    return ResponseEntity.ok(factureService.listStockHaveQuantiteForBoutiqueAndAnneei(anneeId, boutiqueId));
}
}