package com.mproduits.web.controller;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.ConflictException;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.model.Annee;
import com.mproduits.repositories.AnneeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD des annees (exercices) - reference globale partagee par toutes les
 * compagnies (comme Ville), l'identifiant est l'annee elle-meme (ex: 2027).
 */
@RestController
@RequestMapping("/microservice-produits/annees")
@RequiredArgsConstructor
public class AnneeController {

    private final AnneeRepository anneeRepository;

    @GetMapping
    public ResponseEntity<List<Annee>> findAll() {
        return ResponseEntity.ok(anneeRepository.findAllOrderByCodeDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Annee> findById(@PathVariable Integer id) {
        Annee annee = anneeRepository.findById(id.intValue());
        return annee != null ? ResponseEntity.ok(annee) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Annee> create(@RequestBody Annee annee) {
        if (annee.getId() == null) {
            throw new BadRequestException("L'annee (id) est obligatoire, ex: 2027");
        }
        if (anneeRepository.findById(annee.getId().intValue()) != null) {
            throw new ConflictException("Cette annee existe deja : " + annee.getId());
        }
        if (annee.getCode() == null || annee.getCode().isBlank()) {
            annee.setCode(String.valueOf(annee.getId()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(anneeRepository.save(annee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Annee> update(@PathVariable Integer id, @RequestBody Annee annee) {
        Annee existante = anneeRepository.findById(id.intValue());
        if (existante == null) {
            throw new ResourceNotFoundException("Annee non trouvee : " + id);
        }
        existante.setLibelle(annee.getLibelle());
        return ResponseEntity.ok(anneeRepository.save(existante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Annee existante = anneeRepository.findById(id.intValue());
        if (existante == null) {
            throw new ResourceNotFoundException("Annee non trouvee : " + id);
        }
        anneeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
