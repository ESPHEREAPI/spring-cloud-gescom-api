package com.mproduits.web.controller;


import com.mproduits.model.Boutique;
import com.mproduits.services.BoutiqueService;
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
@RequestMapping("/microservice-produits")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class BoutiqueController {
    
    private final BoutiqueService boutiqueService;
    
    @GetMapping("/all-boutique")
    public ResponseEntity<Page<Boutique>> allBoutiques(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Boutique> boutiques = boutiqueService.findAll(pageable, search);
        return ResponseEntity.ok(boutiques);
    }
    
    
    
    @GetMapping("/all")
    public ResponseEntity<List<Boutique>> getAllBoutiquesSimple() {
        List<Boutique> boutiques = boutiqueService.findAll();
        return ResponseEntity.ok(boutiques);
    }
    
    @GetMapping("/boutiques/{id}")
    public ResponseEntity<Boutique> getBoutiqueById(@PathVariable Long id) {
        return boutiqueService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Boutique> createBoutique(@RequestBody Boutique boutique) {
        Boutique savedBoutique = boutiqueService.save(boutique);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBoutique);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Boutique> updateBoutique(@PathVariable Long id, @RequestBody Boutique boutique) {
        try {
            Boutique updatedBoutique = boutiqueService.update(id, boutique);
            return ResponseEntity.ok(updatedBoutique);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoutique(@PathVariable Long id) {
        try {
            boutiqueService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}