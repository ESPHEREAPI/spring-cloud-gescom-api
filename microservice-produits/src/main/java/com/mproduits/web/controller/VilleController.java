package com.mproduits.web.controller;


import com.mproduits.model.Ville;
import com.mproduits.services.VilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/microservice-produits/villes")
@RequiredArgsConstructor

public class VilleController {
    
    private final VilleService villeService;
    
    @GetMapping
    public ResponseEntity<Page<Ville>> getAllVilles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Ville> villes = villeService.findAll(pageable, search);
        return ResponseEntity.ok(villes);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Ville>> getAllVillesSimple() {
        List<Ville> villes = villeService.findAll();
        return ResponseEntity.ok(villes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ville> getVilleById(@PathVariable Long id) {
        return villeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Ville> createVille(@RequestBody Ville ville) {
        Ville savedVille = villeService.save(ville);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVille);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Ville> updateVille(@PathVariable Long id, @RequestBody Ville ville) {
        try {
            Ville updatedVille = villeService.update(id, ville);
            return ResponseEntity.ok(updatedVille);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVille(@PathVariable Long id) {
        try {
            villeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}