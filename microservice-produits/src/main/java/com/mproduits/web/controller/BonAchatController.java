package com.mproduits.web.controller;

import com.mproduits.model.BonAchat;
import com.mproduits.services.BonAchatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * CRUD des bons d'achat de la compagnie de l'appelant (jamais fournie par le
 * client, toujours derivee du token JWT - voir BonAchatService).
 */
@RestController
@RequestMapping("/microservice-produits/bons-achat")
@RequiredArgsConstructor
public class BonAchatController {

    private final BonAchatService bonAchatService;

    @GetMapping
    public ResponseEntity<List<BonAchat>> allBonsAchat() {
        return ResponseEntity.ok(bonAchatService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonAchat> getBonAchatById(@PathVariable Long id) {
        return bonAchatService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BonAchat> createBonAchat(@RequestBody BonAchat bonAchat) {
        BonAchat saved = bonAchatService.create(bonAchat);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonAchat> updateBonAchat(@PathVariable Long id, @RequestBody BonAchat bonAchat) {
        try {
            return ResponseEntity.ok(bonAchatService.update(id, bonAchat));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBonAchat(@PathVariable Long id) {
        try {
            bonAchatService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Emission rapide depuis la caisse (conversion d'un reliquat de monnaie
    // en bon d'achat) - genere le code et applique la periode de validite
    // par defaut, contrairement au POST generique qui exige tous les champs.
    @PostMapping("/emettre-reliquat")
    public ResponseEntity<BonAchat> emettreDepuisRendu(@RequestBody EmettreReliquatRequest request) {
        BonAchat bon = bonAchatService.emettreDepuisRendu(request.getNomClient(), request.getTelephoneClient(), request.getMontant());
        return ResponseEntity.status(HttpStatus.CREATED).body(bon);
    }

    @Data
    public static class EmettreReliquatRequest {
        private String nomClient;
        private String telephoneClient;
        private BigDecimal montant;
    }
}
