package com.mproduits.web.controller;

import com.mproduits.model.Charge;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.ChargeService;
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
@RequestMapping("/microservice-produits/charges")
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService service;
    private final BoutiqueAccessGuard boutiqueAccessGuard;

    @Data
    public static class ChargeCreateRequest {
        private Long boutiqueId;
        private Long typeDepenseId;
        private BigDecimal montant;
        private LocalDate dateCharge;
        private String commentaire;
    }

    @GetMapping
    public ResponseEntity<List<Charge>> findByBoutiqueAndPeriode(
            @RequestParam Long boutiqueid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        return ResponseEntity.ok(service.findByBoutiqueAndPeriode(boutiqueid, debut, fin));
    }

    @PostMapping
    public ResponseEntity<Charge> create(@RequestBody ChargeCreateRequest request) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(request.getBoutiqueId());
        Charge charge = new Charge();
        charge.setMontant(request.getMontant());
        charge.setDateCharge(request.getDateCharge());
        charge.setCommentaire(request.getCommentaire());
        Charge saved = service.create(request.getBoutiqueId(), request.getTypeDepenseId(), charge);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charge> update(@PathVariable Long id, @RequestBody Charge charge) {
        return ResponseEntity.ok(service.update(id, charge));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
