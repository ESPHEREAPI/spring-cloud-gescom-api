package com.mproduits.web.controller;

import com.mproduits.model.Ressource;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.RessourceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/microservice-produits/ressources")
@RequiredArgsConstructor
public class RessourceController {

    private final RessourceService service;
    private final BoutiqueAccessGuard boutiqueAccessGuard;

    @Data
    public static class RessourceCreateRequest {
        private Long boutiqueId;
        private Long typeResourceId;
        private BigDecimal montant;
        private LocalDate dateRessource;
        private String commentaire;
    }

    @GetMapping
    public ResponseEntity<List<Ressource>> findByBoutiqueAndPeriode(
            @RequestParam Long boutiqueid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        return ResponseEntity.ok(service.findByBoutiqueAndPeriode(boutiqueid, debut, fin));
    }

    @PostMapping
    public ResponseEntity<Ressource> create(@RequestBody RessourceCreateRequest request) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(request.getBoutiqueId());
        Ressource ressource = new Ressource();
        ressource.setMontant(request.getMontant());
        ressource.setDateRessource(request.getDateRessource());
        ressource.setCommentaire(request.getCommentaire());
        Ressource saved = service.create(request.getBoutiqueId(), request.getTypeResourceId(), ressource);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ressource> update(@PathVariable Long id, @RequestBody Ressource ressource) {
        return ResponseEntity.ok(service.update(id, ressource));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
