package com.mproduits.web.controller;

import com.mproduits.model.Verrouillage;
import com.mproduits.services.VerrouillageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD des verrouillages de stock de la compagnie de l'appelant (jamais
 * fournie par le client, toujours derivee du token JWT - voir VerrouillageService).
 */
@RestController
@RequestMapping("/microservice-produits/verrouillages")
@RequiredArgsConstructor
public class VerrouillageController {

    private final VerrouillageService verrouillageService;

    @GetMapping
    public ResponseEntity<List<Verrouillage>> allVerrouillages() {
        return ResponseEntity.ok(verrouillageService.findAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Verrouillage>> getAllVerrouillagesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(verrouillageService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Verrouillage> getVerrouillageById(@PathVariable Long id) {
        return verrouillageService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @PostMapping
    public ResponseEntity<Verrouillage> createVerrouillage(@RequestBody Verrouillage verrouillage) {
        Verrouillage saved = verrouillageService.save(verrouillage);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Verrouillage> updateVerrouillage(@PathVariable Long id, @RequestBody Verrouillage verrouillage) {
        try {
            Verrouillage updated = verrouillageService.update(id, verrouillage);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerrouillage(@PathVariable Long id) {
        try {
            verrouillageService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
