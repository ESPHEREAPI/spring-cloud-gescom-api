package com.mproduits.web.controller;


import com.mproduits.model.Typeclient;
import com.mproduits.services.TypeClientService;
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
 * CRUD des types de client de la compagnie de l'appelant (jamais fournie par
 * le client, toujours derivee du token JWT - voir TypeClientService).
 */
@RestController
@RequestMapping("/microservice-produits/types-client")
@RequiredArgsConstructor
public class TypeClientController {

    private final TypeClientService typeClientService;

    @GetMapping
    public ResponseEntity<Page<Typeclient>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Typeclient> typesClient = typeClientService.findAll(pageable, search);
        return ResponseEntity.ok(typesClient);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Typeclient>> getAllSimple() {
        return ResponseEntity.ok(typeClientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Typeclient> getById(@PathVariable Long id) {
        return typeClientService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Typeclient> create(@RequestBody Typeclient typeClient) {
        Typeclient saved = typeClientService.save(typeClient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Typeclient> update(@PathVariable Long id, @RequestBody Typeclient typeClient) {
        try {
            Typeclient updated = typeClientService.update(id, typeClient);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            typeClientService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
