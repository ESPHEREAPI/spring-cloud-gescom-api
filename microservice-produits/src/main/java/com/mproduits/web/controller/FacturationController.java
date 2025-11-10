///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.mproduits.web.controller;
//
///**
// *
// * @author USER01
// */
//import com.mproduits.dto.*;
//import com.mproduits.model.*;
//import com.mproduits.services.*;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.math.BigDecimal;
//import java.util.*;
//
///**
// * API REST - Gestion Facturation
// */
//@RestController
//@RequestMapping("/microservice-produits/facturation")
//@Slf4j
//@RequiredArgsConstructor
//public class FacturationController {
//    
//    private final FacturationService facturationService;
//    private final FactureService factureService;
//    private final PdfService pdfService;
//    
//   /**
//     * Créer une facture à partir d'un devis
//     * POST /api/v1/factures/from-devis/{devisId}
//     */
//    @PostMapping("/from-devis/{devisId}")
//    public ResponseEntity<Map<String, Object>> creerFactureFromDevis(
//        @PathVariable Long devisId,
//        @RequestHeader("X-Username") String username) {
//        
//        try {
//            Facture facture = facturationService.creerFactureFromDevis(devisId, username);
//            
//            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
//                "success", true,
//                "message", "Facture créée avec succès",
//                "factureId", facture.getId(),
//                "numeroFacture", facture.getNumeroFacture()
//            ));
//        } catch (Exception e) {
//            log.error("Erreur création facture", e);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                "success", false,
//                "message", e.getMessage()
//            ));
//        }
//    }
//    
//    /**
//     * Récupérer une facture par ID
//     * GET /api/v1/factures/{id}
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<Facture> getFacture(@PathVariable Long id) {
//        return factureService.findById(id)
//            .map(ResponseEntity::ok)
//            .orElse(ResponseEntity.notFound().build());
//    }
//    
//    /**
//     * Lister les factures avec pagination et filtres
//     * GET /api/v1/factures?statut=&clientId=&page=0&size=20
//     */
//    @GetMapping
//    public ResponseEntity<Page<Facture>> listFactures(
//        @RequestParam(required = false) String statut,
//        @RequestParam(required = false) Long clientId,
//        Pageable pageable) {
//        
//        Page<Facture> page = factureService.findByFilters(statut, clientId, pageable);
//        return ResponseEntity.ok(page);
//    }
//    
//    /**
//     * Enregistrer un versement
//     * POST /api/v1/factures/{id}/versement
//     */
//    @PostMapping("/{id}/versement")
//    public ResponseEntity<Map<String, Object>> enregistrerVersement(
//        @PathVariable Long id,
//        @Valid @RequestBody VersementClientDTO dto,
//        @RequestHeader("X-Username") String username) {
//        
//        try {
//            dto.setFactureId(id);
//            VersementClient versement = facturationService.enregistrerVersement(dto, username);
//            
//            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
//                "success", true,
//                "message", "Versement enregistré",
//                "versementId", versement.getId()
//            ));
//        } catch (Exception e) {
//            log.error("Erreur versement", e);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                "success", false,
//                "message", e.getMessage()
//            ));
//        }
//    }
//    
//    /**
//     * Récupérer historique des versements d'une facture
//     * GET /api/v1/factures/{id}/versements
//     */
//    @GetMapping("/{id}/versements")
//    public ResponseEntity<List<VersementClient>> getVersements(@PathVariable Long id) {
//        List<VersementClient> versements = factureService.getVersements(id);
//        return ResponseEntity.ok(versements);
//    }
//    
//    /**
//     * Générer PDF facture
//     * GET /api/v1/factures/{id}/pdf
//     */
//    @GetMapping("/{id}/pdf")
//    public ResponseEntity<byte[]> genererPdfFacture(@PathVariable Long id) {
//        try {
//            Facture facture = factureService.findById(id)
//                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
//            
//            byte[] pdf = pdfService.genererFacturePdf(facture);
//            
//            return ResponseEntity.ok()
//                .header("Content-Disposition", 
//                    "attachment; filename=\"FAC_" + facture.getNumeroFacture() + ".pdf\"")
//                .header("Content-Type", "application/pdf")
//                .body(pdf);
//        } catch (Exception e) {
//            log.error("Erreur génération PDF", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//    
//    /**
//     * Rapport facturation par période
//     * GET /api/v1/factures/rapport?from=2024-01-01&to=2024-12-31
//     */
//    @GetMapping("/rapport/periode")
//    public ResponseEntity<RapportFacturationDTO> genererRapport(
//        @RequestParam String from,
//        @RequestParam String to) {
//        
//        RapportFacturationDTO rapport = factureService.genererRapport(from, to);
//        return ResponseEntity.ok(rapport);
//    }
//}