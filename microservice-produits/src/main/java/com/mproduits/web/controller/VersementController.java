package com.mproduits.web.controller;

import com.mproduits.dto.ClientDto;
import com.mproduits.dto.HistoriquePaiementClient;
import com.mproduits.dto.RapportEmailRequest;
import com.mproduits.dto.RecuPaiementDTO;

import com.mproduits.dto.VersementAnnulationRequest;
import com.mproduits.dto.VersementCreateRequest;
import com.mproduits.dto.VersementMultipleRequest;
import com.mproduits.dto.VersementResponse;
import com.mproduits.dto.VersementSearchCriteria;
import com.mproduits.dto.VersementStatistiques;
import com.mproduits.dto.VersementSummary;
import com.mproduits.dto.VersementValidationRequest;
import com.mproduits.enums.ModePaiement;

import com.mproduits.security.TenantContext;
import com.mproduits.services.VersementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;




/**
 * Contrôleur REST pour la gestion des versements clients
 * 
 * ENDPOINTS:
 * - POST   /api/versements                         → Créer un versement
 * - POST   /api/versements/multiple                → Créer plusieurs versements
 * - POST   /api/versements/{id}/valider            → Valider un versement
 * - POST   /api/versements/{id}/annuler            → Annuler un versement
 * - GET    /api/versements/{id}                    → Récupérer un versement
 * - GET    /api/versements/numero/{numero}         → Récupérer par numéro
 * - GET    /api/versements                         → Lister avec filtres
 * - GET    /api/versements/facture/{factureId}     → Versements d'une facture
 * - GET    /api/versements/client/{clientId}       → Versements d'un client
 * - GET    /api/versements/statistiques            → Statistiques
 * - GET    /api/versements/{id}/recu               → Générer reçu PDF
 * 
 * SÉCURITÉ:
 * - Authentification requise pour tous les endpoints
 * - ROLE_USER: Lecture seule
 * - ROLE_CAISSIER: Création et validation de versements
 * - ROLE_GESTIONNAIRE: Toutes opérations sauf annulation
 * - ROLE_ADMIN: Toutes opérations y compris annulation
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
@RestController
@RequestMapping("/microservice-produits/versement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Versements", description = "API de gestion des paiements clients")
public class VersementController {

    private final VersementService versementService;
    private final TenantContext tenantContext;

    /**
     * Crée un nouveau versement pour une facture
     * 
     * @param request Données du versement
     * @return Versement créé
     */
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN', 'COMPTABLE', 'CAISSIER')")
    @PostMapping
    @Operation(summary = "Créer un versement",
               description = "Enregistre un nouveau paiement pour une facture")
    public ResponseEntity<VersementResponse> creerVersement(@Valid @RequestBody VersementCreateRequest request) {
        log.info("Création d'un versement pour la facture ID: {}", request.getFactureId());
        VersementResponse response = versementService.creerVersement(request, tenantContext.currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Crée plusieurs versements pour une facture
     * 
     * @param request Données des versements
     * @return Liste des versements créés
     */
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN', 'COMPTABLE', 'CAISSIER')")
    @PostMapping("/multiple")
    @Operation(summary = "Créer plusieurs versements",
               description = "Enregistre plusieurs paiements pour une facture")
    public ResponseEntity<List<VersementResponse>> creerVersementsMultiples(
            @Valid @RequestBody VersementMultipleRequest request) {
        log.info("Création de {} versements pour la facture ID: {}", 
                request.getVersements().size(), request.getFactureId());
        List<VersementResponse> response = versementService.creerVersementsMultiples(request, tenantContext.currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Valide un versement
     * CRITIQUE: Met à jour le solde de la facture et son statut
     * 
     * @param id ID du versement
     * @param request Données de validation
     * @return Versement validé
     */
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN', 'COMPTABLE', 'CAISSIER')")
    @PostMapping("/{id}/valider")
    @Operation(summary = "Valider un versement",
               description = "Valide un versement et met à jour la facture")
    public ResponseEntity<VersementResponse> validerVersement(
            @PathVariable Long id,
            @Valid @RequestBody VersementValidationRequest request) {
        log.info("Validation du versement ID: {}", id);
        request.setVersementId(id);
        VersementResponse response = versementService.validerVersement(request, tenantContext.currentUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Annule un versement
     * 
     * @param id ID du versement
     * @param request Données d'annulation
     * @return Versement annulé
     */
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler un versement",
               description = "Annule un versement et met à jour la facture")
    public ResponseEntity<VersementResponse> annulerVersement(
            @PathVariable Long id,
            @Valid @RequestBody VersementAnnulationRequest request) {
        log.info("Annulation du versement ID: {}", id);
        request.setVersementId(id);
        VersementResponse response = versementService.annulerVersement(request, tenantContext.currentUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère un versement par son ID
     * 
     * @param id ID du versement
     * @return Versement trouvé
     */
    @GetMapping("/{id}")
 
    @Operation(summary = "Récupérer un versement",
               description = "Récupère les détails d'un versement par son ID")
    public ResponseEntity<VersementResponse> getVersement(@PathVariable Long id) {
        log.info("Récupération du versement ID: {}", id);
        VersementResponse response = versementService.getVersement(id);
        return ResponseEntity.ok(response);
    }
    
     @GetMapping("/all-client")
 
    @Operation(summary = "Récupérer un versement",
               description = "Récupère les détails d'un versement par son ID")
    public ResponseEntity<List<ClientDto>> listeClient() {
        //og.info("Récupération du client ID: {}", clientId);
        List<ClientDto> response = versementService.listeClientVersement();
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère un versement par son numéro
     * 
     * @param numero Numéro du versement
     * @return Versement trouvé
     */
    @GetMapping("/numero/{numero}")
 
    @Operation(summary = "Récupérer un versement par numéro",
               description = "Récupère les détails d'un versement par son numéro")
    public ResponseEntity<VersementResponse> getVersementParNumero(@PathVariable String numero) {
        log.info("Récupération du versement numéro: {}", numero);
        VersementResponse response = versementService.getVersementParNumero(numero);
        return ResponseEntity.ok(response);
    }

    /**
     * Liste les versements avec filtres et pagination
     * 
     * @param criteria Critères de recherche
     * @return Page de versements
     */
    @GetMapping
 
    @Operation(summary = "Lister les versements",
               description = "Liste les versements avec filtres et pagination")
    public ResponseEntity<Page<VersementSummary>> listerVersements(
            @Parameter(hidden = true) VersementSearchCriteria criteria) {
        log.info("Liste des versements avec critères: {}", criteria);
        Page<VersementSummary> response = versementService.listerVersements(criteria);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les versements d'une facture
     * 
     * @param factureId ID de la facture
     * @return Liste des versements de la facture
     */
    @GetMapping("/facture/{factureId}")
 
    @Operation(summary = "Versements d'une facture",
               description = "Récupère tous les versements d'une facture")
    public ResponseEntity<List<VersementResponse>> getVersementsFacture(@PathVariable Long factureId) {
        log.info("Récupération des versements de la facture ID: {}", factureId);
        List<VersementResponse> response = versementService.getVersementsFacture(factureId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les versements d'un client
     * 
     * @param clientId ID du client
     * @return Liste des versements du client
     */
    @GetMapping("/client/{clientId}")
   
    @Operation(summary = "Versements d'un client",
               description = "Récupère tous les versements d'un client")
    public ResponseEntity<List<VersementResponse>> getVersementsClient(@PathVariable Long clientId) {
        log.info("Récupération des versements du client ID: {}", clientId);
        List<VersementResponse> response = versementService.getVersementsClient(clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les statistiques de versements
     * 
     * @param dateDebut Date de début (optionnelle)
     * @param dateFin Date de fin (optionnelle)
     * @return Statistiques
     */
//    @GetMapping("/statistiques")
//
//    @Operation(summary = "Statistiques de versements",
//               description = "Récupère les statistiques de paiements sur une période")
//    public ResponseEntity<VersementStatistiques> getStatistiques(
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
//        log.info("Récupération des statistiques de versements");
//        VersementStatistiques response = versementService.getStatistiques(dateDebut, dateFin);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Récupère l'historique de paiement d'un client
     * 
     * @param clientId ID du client
     * @return Historique de paiement
     */
    @GetMapping("/client/{clientId}/historique")
 
    @Operation(summary = "Historique de paiement d'un client",
               description = "Récupère l'historique complet des paiements d'un client")
    public ResponseEntity<HistoriquePaiementClient> getHistoriquePaiementClient(@PathVariable Long clientId) {
        log.info("Récupération de l'historique de paiement du client ID: {}", clientId);
        HistoriquePaiementClient response = versementService.getHistoriquePaiementClient(clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Génère un reçu de paiement PDF
     * 
     * @param id ID du versement
     * @return Reçu de paiement
     */
    @GetMapping("/{id}/recu")
  
    @Operation(summary = "Générer un reçu de paiement",
               description = "Génère un reçu de paiement en PDF")
    public ResponseEntity<RecuPaiementDTO> genererRecuPaiement(@PathVariable Long id) {
        log.info("Génération du reçu de paiement pour le versement ID: {}", id);
        RecuPaiementDTO response = versementService.genererRecuPaiement(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Télécharge un reçu de paiement PDF
     * 
     * @param id
     * @param id
     * @param id ID du versement
     * @return Fichier PDF
     * @return 
     */
    @GetMapping(value = "/{id}/recu/download", produces = MediaType.APPLICATION_PDF_VALUE)
  
    @Operation(summary = "Télécharger le reçu de paiement",
               description = "Télécharge le reçu de paiement en PDF")
    public ResponseEntity<byte[]> telechargerRecuPaiement(@PathVariable Long id) {
        log.info("Téléchargement du reçu de paiement pour le versement ID: {}", id);
        byte[] pdf = versementService.genererRecuPdf(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=recu-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Recherche de versements par critères avancés
     * 
     * Paramètres de recherche:
     * - numeroVersement: Numéro du versement
     * - factureId: ID de la facture
     * - clientId: ID du client
     * - modePaiement: Mode de paiement
     * - statut: Statut du versement
     * - dateVersementDebut/Fin: Période de versement
     * - montantMin/Max: Fourchette de montant
     * - referencePaiement: Référence de paiement
     * - page, size: Pagination
     * - sortBy, sortDirection: Tri
     */
    @GetMapping("/search")

    @Operation(summary = "Recherche avancée",
               description = "Recherche de versements avec critères avancés")
    public ResponseEntity<Page<VersementSummary>> rechercherVersements(
            @RequestParam(required = false) String numeroVersement,
            @RequestParam(required = false) Long factureId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String modePaiement,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateVersementDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateVersementFin,
            @RequestParam(required = false) String referencePaiement,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "dateVersement") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("Recherche avancée de versements");
        
        VersementSearchCriteria criteria = VersementSearchCriteria.builder()
                .numeroVersement(numeroVersement)
                .factureId(factureId)
                .clientId(clientId)
                .modePaiement(modePaiement != null ? ModePaiement.valueOf(modePaiement) : null)
                .statut(statut)
                .dateVersementDebut(dateVersementDebut)
                .dateVersementFin(dateVersementFin)
                .referencePaiement(referencePaiement)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        Page<VersementSummary> response = versementService.listerVersements(criteria);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Télécharge le reçu de paiement en PDF
     */
//    @GetMapping("/{id}/recu")
//    public ResponseEntity<Resource> telechargerRecuPaiement(@PathVariable Long id) {
//        log.info("Génération du reçu de paiement pour le versement {}", id);
//        
//        byte[] pdfContent = versementService.genererRecuPaiement(id);
//        ByteArrayResource resource = new ByteArrayResource(pdfContent);
//        
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu-versement-" + id + ".pdf")
//                .contentType(MediaType.APPLICATION_PDF)
//                .contentLength(pdfContent.length)
//                .body(resource);
//    }

    /**
     * Génère un rapport client en PDF
     */
    @GetMapping("/rapport/client/{clientId}/pdf")
   
    public ResponseEntity<Resource> genererRapportClientPDF(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        
        log.info("Génération du rapport PDF pour le client {} entre {} et {}", clientId, dateDebut, dateFin);
        
        byte[] pdfContent = versementService.genererRapportClientPDF(clientId, dateDebut, dateFin);
        ByteArrayResource resource = new ByteArrayResource(pdfContent);
        
        String filename = String.format("rapport-client-%d-%s-%s.pdf", 
                clientId, 
                dateDebut.toLocalDate(), 
                dateFin.toLocalDate());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfContent.length)
                .body(resource);
    }
    
    

    /**
     * Envoie le rapport par email
     */
    @PostMapping("/rapport/client/{clientId}/email")
 
    public ResponseEntity<Void> envoyerRapportParEmail(
            @PathVariable Long clientId,
            @Valid @RequestBody RapportEmailRequest request) {
        
        log.info("Envoi du rapport par email au client {} à l'adresse {}", clientId, request.getEmail());
        versementService.envoyerRapportParEmail(clientId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Exporte le rapport en Excel
     */
    @GetMapping("/rapport/client/{clientId}/excel")

    public ResponseEntity<Resource> exporterRapportExcel(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        
        log.info("Export Excel du rapport pour le client {} entre {} et {}", clientId, dateDebut, dateFin);
        
        byte[] excelContent = versementService.exporterRapportExcel(clientId, dateDebut, dateFin);
        ByteArrayResource resource = new ByteArrayResource(excelContent);
        
        String filename = String.format("rapport-client-%d-%s-%s.xlsx", 
                clientId, 
                dateDebut.toLocalDate(), 
                dateFin.toLocalDate());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelContent.length)
                .body(resource);
    }

    /**
     * Récupère les statistiques globales des versements
     */
    @GetMapping("/statistiques")

    public ResponseEntity<VersementStatistiques> getStatistiquesGlobales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        
        log.info("Récupération des statistiques globales des versements");
        VersementStatistiques stats = versementService.getStatistiquesGlobales(dateDebut, dateFin);
        return ResponseEntity.ok(stats);
    }
}