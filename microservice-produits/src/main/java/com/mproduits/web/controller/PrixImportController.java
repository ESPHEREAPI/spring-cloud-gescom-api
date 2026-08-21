package com.mproduits.web.controller;

import com.mproduits.dto.ApercuPrixImportDTO;
import com.mproduits.services.PrixImportService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
 * Import de prix par lot (voir PrixImportService) - complement au filtre
 * "Sans prix"/edition en masse de l'ecran Gestion des Points de Vente, pour
 * corriger d'un coup des centaines de produits via un fichier Excel plutot
 * qu'un a un dans le navigateur.
 */
@RestController
@RequestMapping("/microservice-produits/prix-import")
@RequiredArgsConstructor
public class PrixImportController {

    private final PrixImportService prixImportService;

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @GetMapping("/modele")
    public ResponseEntity<Resource> genererModele(@RequestParam Long boutiqueId) {
        byte[] contenu = prixImportService.genererModele(boutiqueId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modele-import-prix.xlsx")
                .contentType(XLSX)
                .contentLength(contenu.length)
                .body(new ByteArrayResource(contenu));
    }

    @PostMapping("/previsualiser")
    public ResponseEntity<ApercuPrixImportDTO> previsualiser(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam Long boutiqueId) {
        return ResponseEntity.ok(prixImportService.previsualiser(fichier, boutiqueId));
    }

    @PreAuthorize("hasAuthority('PERM_INITIALISATION_STOCK_VALIDER') or hasRole('COMPANY_ADMIN')")
    @PostMapping("/appliquer")
    public ResponseEntity<Map<String, Object>> appliquer(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam Long boutiqueId) {
        int miseAJour = prixImportService.appliquer(fichier, boutiqueId);
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("miseAJour", miseAJour);
        return ResponseEntity.ok(reponse);
    }
}
