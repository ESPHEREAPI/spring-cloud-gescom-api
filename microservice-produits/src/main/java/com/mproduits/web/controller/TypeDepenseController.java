package com.mproduits.web.controller;

import com.mproduits.model.TypeDepense;
import com.mproduits.services.TypeDepenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/microservice-produits/types-depense")
@RequiredArgsConstructor
public class TypeDepenseController {

    private final TypeDepenseService service;

    @GetMapping
    public ResponseEntity<List<TypeDepense>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<TypeDepense> create(@RequestBody TypeDepense typeDepense) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(typeDepense));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeDepense> update(@PathVariable Long id, @RequestBody TypeDepense typeDepense) {
        return ResponseEntity.ok(service.update(id, typeDepense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
