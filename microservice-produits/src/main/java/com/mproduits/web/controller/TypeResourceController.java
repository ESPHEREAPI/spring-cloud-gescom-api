package com.mproduits.web.controller;

import com.mproduits.model.TypeResource;
import com.mproduits.services.TypeResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/microservice-produits/types-resource")
@RequiredArgsConstructor
public class TypeResourceController {

    private final TypeResourceService service;

    @GetMapping
    public ResponseEntity<List<TypeResource>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<TypeResource> create(@RequestBody TypeResource typeResource) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(typeResource));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeResource> update(@PathVariable Long id, @RequestBody TypeResource typeResource) {
        return ResponseEntity.ok(service.update(id, typeResource));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
