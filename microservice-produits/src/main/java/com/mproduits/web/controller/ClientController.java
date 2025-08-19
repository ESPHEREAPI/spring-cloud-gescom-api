/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.ClientDto;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Client;
import com.mproduits.services.ClientService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Client> create(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.save(client));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable Long id, @RequestBody Client client) {
        return ResponseEntity.ok(clientService.update(id, client));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Client>> search(@RequestParam String nom) {
        return ResponseEntity.ok(clientService.searchByNom(nom));
    }
    
    @GetMapping("/{id}")
public ResponseEntity<ClientDto> findByIdDto(@PathVariable Long id) {
    return clientService.findById(id)
        .map(mapper::mapperClientByClientDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

@GetMapping
public ResponseEntity<List<ClientDto>> findAllDto() {
    return ResponseEntity.ok(
        clientService.findAll()
            .stream()
            .map(mapper::mapperClientByClientDto)
            .toList()
    );
}
    
    
}
