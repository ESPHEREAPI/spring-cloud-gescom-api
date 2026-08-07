package com.mproduits.web.controller;

import com.mproduits.model.MargeCible;
import com.mproduits.services.MargeCibleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/microservice-produits/marges-cibles")
@RequiredArgsConstructor
public class MargeCibleController {

    private final MargeCibleService service;

    @Data
    public static class MargeCibleRequest {
        private Long categorieId;
        private BigDecimal tauxCible;
    }

    @GetMapping
    public ResponseEntity<List<MargeCible>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<MargeCible> create(@RequestBody MargeCibleRequest request) {
        MargeCible saved = service.create(request.getCategorieId(), request.getTauxCible());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MargeCible> update(@PathVariable Long id, @RequestBody MargeCibleRequest request) {
        return ResponseEntity.ok(service.update(id, request.getTauxCible()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
