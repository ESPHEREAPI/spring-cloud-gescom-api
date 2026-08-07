package com.mproduits.web.controller;

import com.mproduits.enums.ModePaiement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Liste des modes de paiement acceptes - reference statique (enum), la meme
 * pour toutes les compagnies, aucune donnee a scoper.
 */
@RestController
@RequestMapping("/microservice-produits/modes-paiement")
public class ModePaiementController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getModesPaiement() {
        List<Map<String, Object>> modes = Arrays.stream(ModePaiement.values())
                .map(m -> Map.<String, Object>of(
                        "code", m.getCode(),
                        "nom", m.name(),
                        "libelle", m.getLibelle(),
                        "referenceObligatoire", m.isReferenceObligatoire(),
                        "paiementImmediat", m.isPaiementImmediat()
                ))
                .toList();
        return ResponseEntity.ok(modes);
    }
}
