package com.mproduits.web.controller;

import com.mproduits.dto.ClientSoldeDTO;
import com.mproduits.services.CompteClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/microservice-produits/compte-client")
@RequiredArgsConstructor
public class CompteClientController {

    private final CompteClientService service;

    // Classement "clients a haute redevance" (plus gros reste-a-payer en tete).
    @GetMapping("/soldes")
    public ResponseEntity<List<ClientSoldeDTO>> getSoldesClients() {
        return ResponseEntity.ok(service.getSoldesClients());
    }

    @GetMapping("/{clientId}/solde")
    public ResponseEntity<ClientSoldeDTO> getSoldeClient(@PathVariable Long clientId) {
        ClientSoldeDTO solde = service.getSoldeClient(clientId);
        return solde != null ? ResponseEntity.ok(solde) : ResponseEntity.noContent().build();
    }

    // Combien la compagnie attend au total, tous clients confondus.
    @GetMapping("/total-attendu")
    public ResponseEntity<BigDecimal> getTotalAttendu() {
        return ResponseEntity.ok(service.getTotalAttendu());
    }
}
