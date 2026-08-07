package com.mproduits.web.controller;

import com.mproduits.dto.CompagnieDashboardDTO;
import com.mproduits.services.CompagnieDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/microservice-produits/dashboard-compagnie")
@RequiredArgsConstructor
public class CompagnieDashboardController {

    private final CompagnieDashboardService service;

    @GetMapping
    public ResponseEntity<CompagnieDashboardDTO> obtenirDashboard() {
        return ResponseEntity.ok(service.genererDashboard());
    }
}
