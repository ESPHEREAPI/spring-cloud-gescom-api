package com.mproduits.web.controller;

import com.mproduits.dto.MargeDetailDTO;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.MargeReelleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/microservice-produits/marge-reelle")
@RequiredArgsConstructor
public class MargeReelleController {

    private final MargeReelleService service;
    private final BoutiqueAccessGuard boutiqueAccessGuard;

    @GetMapping
    public ResponseEntity<MargeDetailDTO> getDetail(
            @RequestParam Long boutiqueid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        return ResponseEntity.ok(service.getDetail(boutiqueid, debut, fin));
    }
}
