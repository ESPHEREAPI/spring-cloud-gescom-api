/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.model.VersementClient;
import com.mproduits.services.VersementClientService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits/versement")

public class VersementClientController {

    private final VersementClientService versementService;
@Autowired
    public VersementClientController(VersementClientService versementService) {
        this.versementService = versementService;
    }

    @PostMapping
    public ResponseEntity<VersementClient> save(@RequestBody VersementClient versement) {
        return ResponseEntity.ok(versementService.save(versement));
    }

    @GetMapping("/facture/{factureId}")
    public ResponseEntity<List<VersementClient>> findByFacture(@PathVariable Long factureId) {
        return ResponseEntity.ok(versementService.findByFactureId(factureId));
    }

    @GetMapping("/facture/{factureId}/total")
    public ResponseEntity<BigDecimal> totalVersements(@PathVariable Long factureId) {
        return ResponseEntity.ok(versementService.getTotalVersementsByFacture(factureId));
    }
}

