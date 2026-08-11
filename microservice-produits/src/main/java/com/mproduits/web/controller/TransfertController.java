/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;
import com.mproduits.dto.TransfertStockRequest;
import com.mproduits.dto.TransfertStockResponse;
import com.mproduits.model.Magasin;
import com.mproduits.model.TransfertStock;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.TransfertStockRepository;
import com.mproduits.services.TransfertStockService;
import com.mproduits.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/microservice-produits/transferts")
/**
 *
 * @author USER01
 */
public class TransfertController {
    @Autowired
    private TransfertStockService transfertStockService;

    @Autowired
    private TransfertStockRepository transfertStockRepository;
    @Autowired
    private TenantContext tenantContext;
    @Autowired
    private com.mproduits.utiles.PDFGeneratorBordereauLivraison pdfGeneratorBordereauLivraison;


    /**
     * Effectue un transfert de stock
     * 
     * Endpoint: POST /api/transferts/transferer
     * 
     * Exemple de requête:
     * {
     *   "produitId": 1,
     *   "magasinSourceId": 1,
     *   "magasinDestinationId": 2,
     *   "quantite": 50.0,
     *   "notes": "Transfert urgent pour approvisionnement"
     * }
     * 
     * @param requete Détails du transfert
     * @param result Résultats de la validation
     * @return TransfertStockResponse avec les détails du transfert effectué
     */
    @PreAuthorize("hasAnyRole('ADMIN','COMMERCIAL')")
    @PostMapping("/transferer")
    public ResponseEntity<?> effectuerTransfert(
            @Valid @RequestBody TransfertStockRequest requete,
            BindingResult result) {

        log.info("Requête de transfert reçue: {} -> {}, quantité: {}",
                requete.getMagasinSourceId(), requete.getMagasinDestinationId(), requete.getQuantite());

        // Validation des erreurs
        if (result.hasErrors()) {
            Map<String, String> erreurs = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    erreurs.put(error.getField(), error.getDefaultMessage())
            );
            log.warn("Erreurs de validation: {}", erreurs);
            return ResponseEntity.badRequest().body(erreurs);
        }

        try {
            TransfertStockResponse reponse = transfertStockService.effectuerTransfert(requete);
            log.info("Transfert effectué avec succès. ID: {}", reponse.getTransfertId());
            return ResponseEntity.ok(reponse);
        } catch (IllegalArgumentException e) {
            log.error("Erreur métier lors du transfert: {}", e.getMessage());
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(erreur);
        } catch (Exception e) {
            log.error("Erreur inattendue lors du transfert", e);
            Map<String, String> erreur = new HashMap<>();
            erreur.put("error", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erreur);
        }
    }

    /**
     * Récupère un transfert par son ID
     * 
     * Endpoint: GET /api/transferts/{id}
     * 
     * @param id ID du transfert
     * @return Transfert trouvé ou 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirTransfert(@PathVariable Long id) {
        log.debug("Requête pour récupérer le transfert: {}", id);

        Optional<TransfertStock> transfert = transfertStockRepository.findByIdAndEntreprise_EntreprisePK_CompagnieId(id, tenantContext.currentCompagnieId());
        if (transfert.isEmpty()) {
            log.warn("Transfert non trouvé: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(transfert.get());
    }

    // Bordereau de livraison PDF - piece justificative de la reception a
    // faire signer par le depot destinataire (voir tache stock : chaque
    // transfert doit generer un bordereau).
    @GetMapping("/{id}/bordereau")
    public ResponseEntity<byte[]> telechargerBordereau(@PathVariable Long id) {
        TransfertStock transfert = transfertStockRepository
                .findByIdAndEntreprise_EntreprisePK_CompagnieId(id, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Transfert non trouvé avec l'ID: " + id));

        com.mproduits.model.Compagnie compagnie = transfert.getEntreprise() != null
                ? transfert.getEntreprise().getCompagnie() : null;
        byte[] pdf = pdfGeneratorBordereauLivraison.genererBordereau(transfert, compagnie);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "bordereau-livraison-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    /**
     * Liste tous les transferts
     * 
     * Endpoint: GET /api/transferts
     * 
     * @return Liste de tous les transferts
     */
    @GetMapping
    public ResponseEntity<?> listerTransferts() {
        log.debug("Requête pour lister tous les transferts");
        List<TransfertStock> transferts = transfertStockRepository.findAllByCompagnieId(tenantContext.currentCompagnieId());
        return ResponseEntity.ok(transferts);
    }

    /**
     * Récupère les transferts sortants d'un magasin
     * 
     * Endpoint: GET /api/transferts/source/{magasinId}
     * 
     * @param magasinId ID du magasin source
     * @return Transferts d'où le magasin est la source
     */
    @GetMapping("/source/{magasinId}")
    public ResponseEntity<?> obtenirTransfertsSource(@PathVariable Long magasinId) {
        log.debug("Requête pour les transferts sortants du magasin: {}", magasinId);
        List<TransfertStock> transferts = transfertStockRepository.findBySourceIdAndCompagnieId(magasinId, tenantContext.currentCompagnieId());
        return ResponseEntity.ok(transferts);
    }

    /**
     * Récupère les transferts entrants vers un magasin
     * 
     * Endpoint: GET /api/transferts/destination/{magasinId}
     * 
     * @param magasinId ID du magasin destination
     * @return Transferts où le magasin est la destination
     */
    @GetMapping("/destination/{magasinId}")
    public ResponseEntity<?> obtenirTransfertsDestination(@PathVariable Long magasinId) {
        log.debug("Requête pour les transferts entrants du magasin: {}", magasinId);
        List<TransfertStock> transferts = transfertStockRepository.findByDestinationIdAndCompagnieId(magasinId, tenantContext.currentCompagnieId());
        return ResponseEntity.ok(transferts);
    }

    /**
     * Récupère les transferts d'un produit
     * 
     * Endpoint: GET /api/transferts/produit/{produitId}
     * 
     * @param produitId ID du produit
     * @return Transferts du produit
     */
    @GetMapping("/produit/{produitId}")
    public ResponseEntity<?> obtenirTransfertsProduit(@PathVariable Long produitId) {
        log.debug("Requête pour les transferts du produit: {}", produitId);
        List<TransfertStock> transferts = transfertStockRepository.findByProduitIdAndCompagnieId(produitId, tenantContext.currentCompagnieId());
        return ResponseEntity.ok(transferts);
    }

    /**
     * Récupère l'historique des transferts avec pagination et filtres
     * 
     * Endpoint: GET /api/transferts/historique?page=0&size=20&magasinId=1&produitId=1
     * 
     * Paramètres de requête:
     * - page: Numéro de la page (défaut: 0)
     * - size: Nombre d'éléments par page (défaut: 20)
     * - magasinId: Filtrer par magasin (optionnel)
     * - produitId: Filtrer par produit (optionnel)
     * 
     * @param page Numéro de la page
     * @param size Taille de la page
     * @param magasinId Filtrer par magasin (optionnel)
     * @param produitId Filtrer par produit (optionnel)
     * @return Page d'historique des transferts
     */
    @GetMapping("/historique")
    public ResponseEntity<?> obtenirHistorique(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long magasinId,
            @RequestParam(required = false) Long produitId) {

        log.debug("Requête d'historique avec pagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateTransfert").descending());

        // TODO: Implémenter les filtres avec magasinId et produitId
        // Pour l'instant, retourner tous les transferts de la compagnie avec pagination

        List<TransfertStock> transferts = transfertStockRepository.findAllByCompagnieId(tenantContext.currentCompagnieId());
        int total = transferts.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("content", transferts.subList(start, end));
        reponse.put("totalElements", total);
        reponse.put("totalPages", (total + size - 1) / size);
        reponse.put("currentPage", page);
        reponse.put("pageSize", size);

        return ResponseEntity.ok(reponse);
    }
    
    
}
