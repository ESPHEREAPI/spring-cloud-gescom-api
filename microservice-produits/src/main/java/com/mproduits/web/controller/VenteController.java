/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.VenteDto;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.security.TenantContext;
import com.mproduits.services.VenteService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits")
@RequiredArgsConstructor

public class VenteController {

    private final VenteService venteService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;
    private final TenantContext tenantContext;

    @GetMapping
    public Page<VenteDto> getVentes(
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false) String statut,
        //@RequestParam(required = false) String dateDebut,
        //@RequestParam(required = false) String dateFin,
          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dateDebut,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dateFin,

         @RequestParam(required = false) String search, @RequestParam(required = true) Long boutiqueid
    ) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        return venteService.getVentes(page, size, statut, dateDebut, dateFin,search,boutiqueid);
    }

    @PatchMapping("/{venteId}/statut/{boutiqueid}")
    public VenteDto updateStatut(@PathVariable Long venteId,@PathVariable Long boutiqueid, @RequestBody Map<String, String> body) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        // Identite verifiee par le JWT - jamais celle envoyee par le client,
        // qui servait auparavant a maquiller l'auteur d'une annulation de vente.
        return venteService.updateStatut(venteId, tenantContext.currentUsername(), body.get("statut"),boutiqueid);
    }

    @GetMapping("/{id}")
    public VenteDto getVente(@PathVariable Long id) {
        return venteService.getVente(id);
    }

    @GetMapping("/vente/{numeTicket}/{boutiqueid}")
    public ResponseEntity<VenteDto> getVente(@PathVariable String numeTicket,@PathVariable Long boutiqueid){
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        VenteDto venteDto=  venteService.getVente(numeTicket,boutiqueid);
        return venteDto==null ? ResponseEntity.ok(new VenteDto()):ResponseEntity.ok(venteDto);
    }
     @GetMapping("/vente/e-com/{numeTicket}/{boutiqueid}")
    public ResponseEntity<VenteDto> getVentes(@PathVariable long numeTicket,@PathVariable Long boutiqueid){
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        VenteDto venteDto=  venteService.getVenteForECom(numeTicket,boutiqueid);
        return venteDto==null ? ResponseEntity.ok(new VenteDto()):ResponseEntity.ok(venteDto);
    }
    
   
    
}
