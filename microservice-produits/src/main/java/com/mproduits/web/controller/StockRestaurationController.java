package com.mproduits.web.controller;

import com.mproduits.dto.ApercuImportStockDTO;
import com.mproduits.enums.ModeRestauration;
import com.mproduits.security.TenantContext;
import com.mproduits.services.StockRestaurationService;
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
}
