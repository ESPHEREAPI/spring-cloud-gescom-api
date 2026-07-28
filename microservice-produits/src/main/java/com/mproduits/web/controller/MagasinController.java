package com.mproduits.web.controller;

import com.mproduits.model.Magasin;
import com.mproduits.services.MagasinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/microservice-produits/magasins")
@RequiredArgsConstructor

public class MagasinController {
    
    private final MagasinService magasinService;
    
    @GetMapping
    public ResponseEntity<Page<Magasin>> getAllMagasins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Magasin> magasins = magasinService.findAll(pageable, search);
        return ResponseEntity.ok(magasins);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Magasin> getMagasinById(@PathVariable Long id) {
        return magasinService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Magasin> createMagasin(@RequestBody Magasin magasin) {
        Magasin savedMagasin = magasinService.save(magasin);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMagasin);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Magasin> updateMagasin(@PathVariable Long id, @RequestBody Magasin magasin) {
        try {
            Magasin updatedMagasin = magasinService.update(id, magasin);
            return ResponseEntity.ok(updatedMagasin);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMagasin(@PathVariable Long id) {
        try {
            magasinService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}