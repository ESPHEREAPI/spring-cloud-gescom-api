package com.mproduits.web.controller;


import com.mproduits.model.Specifique;
import com.mproduits.services.SpecifiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD des specificites produit de la compagnie de l'appelant (jamais
 * fournie par le client, toujours derivee du token JWT - voir SpecifiqueService).
 */
@RestController
@RequestMapping("/microservice-produits/specifiques")
@RequiredArgsConstructor
public class SpecifiqueController {

    private final SpecifiqueService specifiqueService;

    @GetMapping
    public ResponseEntity<Page<Specifique>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(specifiqueService.findAll(pageable, search));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Specifique>> getAllSimple(@RequestParam(required = false) Long categorieId) {
        return ResponseEntity.ok(specifiqueService.findAll(categorieId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specifique> getById(@PathVariable Long id) {
        return specifiqueService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Specifique> create(@RequestBody Specifique specifique) {
        Specifique saved = specifiqueService.save(specifique);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Specifique> update(@PathVariable Long id, @RequestBody Specifique specifique) {
        try {
            return ResponseEntity.ok(specifiqueService.update(id, specifique));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            specifiqueService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
