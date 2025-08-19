/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.model.Devis;
import com.mproduits.services.DevisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits/devis")

public class DevisController {

    private final DevisService devisService;
@Autowired
    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    @PostMapping
    public ResponseEntity<Devis> create(@RequestBody Devis devis) {
        return ResponseEntity.ok(devisService.create(devis));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Devis> update(@PathVariable Long id, @RequestBody Devis devis) {
        return ResponseEntity.ok(devisService.update(id, devis));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devis> findById(@PathVariable Long id) {
        return devisService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Devis>> findByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(devisService.findByClientId(clientId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        devisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

