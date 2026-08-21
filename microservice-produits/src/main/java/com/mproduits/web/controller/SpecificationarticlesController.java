package com.mproduits.web.controller;

import com.mproduits.dto.SpecificationArticleDTO;
import com.mproduits.services.SpecificationarticlesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CRUD des valeurs de specification d'un produit (voir
 * SpecificationarticlesService pour les garde-fous d'appartenance
 * compagnie/categorie). URLs alignees sur celles deja attendues par
 * article.service.ts cote frontend (getArticleSpecifications/
 * updateArticleSpecification existaient deja, sans route pour y
 * repondre).
 */
@RestController
@RequestMapping("/microservice-produits")
@RequiredArgsConstructor
public class SpecificationarticlesController {

    private final SpecificationarticlesService specificationarticlesService;

    @GetMapping("/articles/{produitId}/specifications")
    public ResponseEntity<List<SpecificationArticleDTO>> lister(@PathVariable Long produitId) {
        return ResponseEntity.ok(specificationarticlesService.lister(produitId));
    }

    @PostMapping("/articles/{produitId}/specifications")
    public ResponseEntity<SpecificationArticleDTO> ajouter(@PathVariable Long produitId,
            @RequestBody Map<String, Object> body) {
        Long specifiqueId = Long.valueOf(String.valueOf(body.get("specifiqueId")));
        String valeur = String.valueOf(body.get("valeur"));
        SpecificationArticleDTO cree = specificationarticlesService.ajouter(produitId, specifiqueId, valeur);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PutMapping("/specifications/{id}")
    public ResponseEntity<SpecificationArticleDTO> modifier(@PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String valeur = String.valueOf(body.get("valeur") != null ? body.get("valeur") : body.get("libelle"));
        return ResponseEntity.ok(specificationarticlesService.modifier(id, valeur));
    }

    @DeleteMapping("/specifications/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        specificationarticlesService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
