/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.ClientDto;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Client;
import com.mproduits.services.ClientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits/client")

public class ClientController {

    private final ClientService clientService;
    @Autowired
    MapperDtoImpl mapper;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

//    @PostMapping
//    public ResponseEntity<Client> create(@RequestBody Client client) {
//        return ResponseEntity.ok(clientService.create(client));
//    }
//    @PutMapping("/{id}")
//    public ResponseEntity<Client> update(@PathVariable Long id, @RequestBody Client client) {
//        return ResponseEntity.ok(clientService.update(id, client));
//    }
//    @GetMapping
//    public ResponseEntity<List<Client>> findAll() {
//        return ResponseEntity.ok(clientService.findAll());
//    }
//    @GetMapping("/{id}")
//    public ResponseEntity<Client> findById(@PathVariable Long id) {
//        return clientService.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        clientService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//    @GetMapping("/search")
//    public ResponseEntity<List<Client>> search(@RequestParam String nom) {
//        return ResponseEntity.ok(clientService.search(nom));
//    }
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> findByIdDto(@PathVariable Long id) {
        return clientService.findById(id)
                .map(mapper::mapperClientByClientDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/allClient")
    public ResponseEntity<List<ClientDto>> findAllDto() {
        return ResponseEntity.ok(
                clientService.allClient()
                        .stream()
                        .map(cl -> mapper.mapperClientByClientDto(cl))
                        .toList()
        );
    }
    // ========== CRUD ==========

    /**
     * GET /api/v1/clients?statut=ACTIF&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<Client>> getClients(
            @RequestParam(required = false) String statut,
            Pageable pageable) {

        Page<Client> clients;
        if (statut != null && !statut.isEmpty()) {
            clients = clientService.findByStatut(statut, pageable);
        } else {
            clients = clientService.findAll(pageable);
        }

        return ResponseEntity.ok(clients);
    }

    /**
     * GET /api/v1/clients/1
     */
//    @GetMapping("/{id}")
//    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
//        return clientService.findById(id)
//            .map(ResponseEntity::ok)
//            .orElse(ResponseEntity.notFound().build());
//    }
    /**
     * POST /api/v1/clients
     */
    @PostMapping
    public ResponseEntity<Client> createClient(@Valid @RequestBody Client client) {
        Client created = clientService.create(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/clients/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody Client client) {

        Client updated = clientService.update(id, client);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/v1/clients/1
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ========== RECHERCHE & FILTRES ==========
    /**
     * GET /api/v1/clients/search?q=CLI001
     */
    @GetMapping("/search")
    public ResponseEntity<List<Client>> searchClients(@RequestParam String q) {
        List<Client> results = clientService.search(q);
        return ResponseEntity.ok(results);
    }

    /**
     * ✅ GET /api/v1/clients/by-statut/ACTIF Récupère tous les clients avec un
     * statut spécifique
     */
    @GetMapping("/by-statut/{statut}")
    public ResponseEntity<List<Client>> findByStatut(@PathVariable String statut) {
        List<Client> clients = clientService.findAllByStatut(statut);
        return ResponseEntity.ok(clients);
    }

    /**
     * ✅ GET /api/v1/clients/fidelite Récupère uniquement les clients fidélité
     */
    @GetMapping("/fidelite")
    public ResponseEntity<List<Client>> getFidelites() {
        List<Client> clients = clientService.findFidelites();
        return ResponseEntity.ok(clients);
    }

    /**
     * ✅ GET /api/v1/clients/by-ville?ville=Yaoundé
     */
    @GetMapping("/by-ville")
    public ResponseEntity<List<Client>> findByVille(@RequestParam String ville) {
        List<Client> clients = clientService.findByVille(ville);
        return ResponseEntity.ok(clients);
    }

    /**
     * ✅ GET /api/v1/clients/by-region?region=Centre
     */
    @GetMapping("/by-region")
    public ResponseEntity<List<Client>> findByRegion(@RequestParam String region) {
        List<Client> clients = clientService.findByRegion(region);
        return ResponseEntity.ok(clients);
    }

    /**
     * ✅ GET /api/v1/clients/statistics Obtenir statistiques clients
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(clientService.getStatistics());
    }
    
    
    /**
 * ✅ Recherche autocomplete limitée à 20 résultats
 * GET /api/v1/clients/search-autocomplete?q=dupont&limit=20
 */
@GetMapping("/search-autocomplete")
public ResponseEntity<List<ClientDto>> searchAutocomplete(
        @RequestParam String q,
        @RequestParam(defaultValue = "20") int limit) {
    
    List<Client> results = clientService.search(q);
    
    return ResponseEntity.ok(
        results.stream()
            .limit(limit)
            .map(mapper::mapperClientByClientDto)
            .toList()
    );
}

/**
 * ✅ Top 50 clients actifs pour liste initiale
 * GET /api/v1/clients/actifs-top?limit=50
 */
@GetMapping("/actifs-top")
public ResponseEntity<List<ClientDto>> getTopActifs(
        @RequestParam(defaultValue = "50") int limit) {
    
    List<Client> clients = clientService.findAllByStatut("ACTIF");
    
    return ResponseEntity.ok(
        clients.stream()
            .limit(limit)
            .map(mapper::mapperClientByClientDto)
            .toList()
    );
}
    
    
}
