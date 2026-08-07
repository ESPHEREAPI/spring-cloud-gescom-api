package com.mproduits.web.controller;

import com.mproduits.model.Barcodeproduit;
import com.mproduits.services.BarcodeGestionService;
import lombok.Data;
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
 * CRUD des associations code-barres / article de la compagnie de l'appelant
 * (jamais fournie par le client, toujours derivee du token JWT - voir
 * BarcodeGestionService). Distinct de /code-bare (scan en caisse).
 */
@RestController
@RequestMapping("/microservice-produits/barcodes")
@RequiredArgsConstructor
public class BarcodeGestionController {

    private final BarcodeGestionService barcodeGestionService;

    @Data
    public static class BarcodeCreateRequest {
        private String codeBard;
        private Long produitId;
        private Long boutiqueId;
    }

    @Data
    public static class BarcodeUpdateRequest {
        private String codeBard;
    }

    @GetMapping
    public ResponseEntity<List<Barcodeproduit>> allBarcodes() {
        return ResponseEntity.ok(barcodeGestionService.findAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Barcodeproduit>> getAllBarcodesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(barcodeGestionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barcodeproduit> getBarcodeById(@PathVariable Long id) {
        return barcodeGestionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Barcodeproduit> createBarcode(@RequestBody BarcodeCreateRequest request) {
        Barcodeproduit saved = barcodeGestionService.create(
                request.getCodeBard(), request.getProduitId(), request.getBoutiqueId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Barcodeproduit> updateBarcode(@PathVariable Long id, @RequestBody BarcodeUpdateRequest request) {
        try {
            Barcodeproduit updated = barcodeGestionService.updateCode(id, request.getCodeBard());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBarcode(@PathVariable Long id) {
        try {
            barcodeGestionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
