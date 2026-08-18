package com.mproduits.web.controller;

import com.mproduits.dto.ApercuImportStockDTO;
import com.mproduits.enums.ModeRestauration;
import com.mproduits.security.TenantContext;
import com.mproduits.services.ProduitCleanupService;
import com.mproduits.services.StockRestaurationService;
import com.mproduits.repositories.ProduitRepositories;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Restauration de stock par boutique a partir d'un fichier Excel personnalise
 * par la compagnie (voir StockRestaurationService, StockImportFormat cote
 * microservice-administration). previsualiser ne modifie rien ; appliquer est
 * reserve a l'action VALIDER du menu "Initialisation Stock" (meme mecanisme
 * PERM_&lt;MENU&gt;_&lt;ACTION&gt; que Facture/Vente, voir EffectivePermissionService).
 */
@RestController
@RequestMapping("/microservice-produits/stock-restauration")
@RequiredArgsConstructor
public class StockRestaurationController {

    private final StockRestaurationService stockRestaurationService;
    private final ProduitCleanupService produitCleanupService;
    private final ProduitRepositories produitRepositories;
    private final TenantContext tenantContext;

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @GetMapping("/modele")
    public ResponseEntity<Resource> genererModele(@RequestParam(required = false) Long boutiqueId) {
        byte[] contenu = stockRestaurationService.genererModele(boutiqueId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modele-restauration-stock.xlsx")
                .contentType(XLSX)
                .contentLength(contenu.length)
                .body(new ByteArrayResource(contenu));
    }

    @PostMapping("/previsualiser")
    public ResponseEntity<ApercuImportStockDTO> previsualiser(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam(required = false) Long boutiqueId,
            @RequestParam ModeRestauration mode) {
        return ResponseEntity.ok(stockRestaurationService.previsualiserImport(fichier, boutiqueId, mode));
    }

    @PreAuthorize("hasAuthority('PERM_INITIALISATION_STOCK_VALIDER') or hasRole('COMPANY_ADMIN')")
    @PostMapping("/appliquer")
    public ResponseEntity<Map<String, String>> appliquer(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam(required = false) Long boutiqueId,
            @RequestParam ModeRestauration mode) {
        String batchId = stockRestaurationService.appliquerImport(fichier, boutiqueId, mode, tenantContext.currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("batchId", batchId));
    }

    /**
     * Remet une boutique a zero (stock + produits qui lui sont exclusifs)
     * pour reprendre une initialisation depuis un etat propre - outil de
     * maintenance, reserve aux administrateurs de la compagnie.
     *
     * La suppression des produits candidats est faite ICI, depuis ce
     * controleur (donc SANS transaction ambiante), en appelant
     * ProduitCleanupService (Propagation.REQUIRES_NEW) un produit a la fois -
     * necessaire pour qu'un produit non supprimable (reference FK ailleurs)
     * n'annule pas le reste du nettoyage (voir StockRestaurationService.viderStockBoutique).
     */
    // Diagnostic ponctuel - a retirer une fois la reinitialisation de
    // boutique stabilisee (voir StockRestaurationService.viderStockBoutique).
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @GetMapping("/diagnostic-fk-produit")
    public ResponseEntity<List<String>> diagnosticFkProduit() {
        return ResponseEntity.ok(produitRepositories.findForeignKeysReferencingProduitDiagnostic());
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @PostMapping("/reinitialiser-boutique")
    public ResponseEntity<Map<String, Object>> reinitialiserBoutique(@RequestParam Long boutiqueId) {
        Map<String, Object> resultatStock = stockRestaurationService.viderStockBoutique(boutiqueId);
        List<Long> produitIdsCandidats = (List<Long>) resultatStock.get("produitIdsCandidats");

        // ATTENTION : ne PAS retomber sur "tout produit de la compagnie sans
        // stock nulle part" ici - le catalogue de cette compagnie est un vrai
        // catalogue volumineux (centaines de references reelles), pas
        // seulement les produits de test. Si cette boutique est la seule a
        // avoir du stock, un tel repli capturerait quasiment tout le
        // catalogue reel des qu'il a deja ete vide par un essai precedent.
        // Se limiter strictement a PointVente pour cette boutique - si c'est
        // vide, il n'y a simplement plus de candidat sur ce faire.
        List<Long> aTenterDeSupprimer = produitIdsCandidats;

        int produitsSupprimes = 0;
        for (Long produitId : aTenterDeSupprimer) {
            if (produitCleanupService.essayerSupprimerProduitOrphelin(produitId)) {
                produitsSupprimes++;
            }
        }

        Map<String, Object> reponse = new HashMap<>(resultatStock);
        reponse.remove("produitIdsCandidats");
        reponse.put("produitsSupprimes", produitsSupprimes);
        reponse.put("produitsConserves", aTenterDeSupprimer.size() - produitsSupprimes);
        return ResponseEntity.ok(reponse);
    }
}
