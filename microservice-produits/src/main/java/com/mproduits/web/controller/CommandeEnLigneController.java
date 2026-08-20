package com.mproduits.web.controller;

import com.mproduits.dto.DevisDTO;
import com.mproduits.dto.DevisToFactureRequest;
import com.mproduits.dto.FactureResponse;
import com.mproduits.dto.FactureValidationRequest;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.security.TenantContext;
import com.mproduits.services.DevisService;
import com.mproduits.services.EntrepriseService;
import com.mproduits.services.FactureService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ecran admin "Commandes en ligne" - devis EN_LIGNE (voir CanalOrigine)
 * uniquement, jamais les devis internes crees par le personnel. Le
 * one-click "valider-et-facturer" enchaine dans UNE transaction les 3
 * etapes normalement separees pour un devis interne (accepter -> convertir
 * en facture -> valider la facture, cette derniere etape etant celle qui
 * deduit reellement le stock - voir FactureService.validerFacture) : une
 * commande en ligne suit toujours exactement cette meme sequence sans
 * variation possible (contrairement a un devis negocie manuellement), donc
 * autant epargner au personnel 3 ecrans separes par commande.
 */
@RestController
@RequestMapping("/microservice-produits/commandes-en-ligne")
@RequiredArgsConstructor
@Slf4j
public class CommandeEnLigneController {

    private final DevisService devisService;
    private final FactureService factureService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;
    private final TenantContext tenantContext;
    private final EntrepriseService entrepriseService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> lister(
            @RequestParam(defaultValue = "EN_ATTENTE") String statut,
            @RequestParam Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        List<DevisDTO> commandes = devisService.findCommandesEnLigne(statut, boutiqueid);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", commandes);
        response.put("total", commandes.size());
        response.put("statut", statut);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('PERM_FACTURE_VALIDER') or hasRole('COMPANY_ADMIN')")
    @PostMapping("/{devisId}/valider-et-facturer")
    @Transactional
    public ResponseEntity<FactureResponse> validerEtFacturer(@PathVariable Long devisId) {
        String username = tenantContext.currentUsername();
        Long compagnieId = tenantContext.currentCompagnieId();
        int anneeid = entrepriseService.obtenirOuCreerExerciceActif(compagnieId).getEntreprisePK().getAnneeId();

        DevisDTO devisAccepte = devisService.accepterDevis(devisId, anneeid);

        DevisToFactureRequest conversionRequest = DevisToFactureRequest.builder()
                .devisId(devisId)
                .username(username)
                .boutiqueid(devisAccepte.getBoutiqueid())
                .build();
        FactureResponse facture = factureService.convertirDevisEnFacture(conversionRequest, username);

        FactureValidationRequest validationRequest = FactureValidationRequest.builder()
                .factureId(facture.getId())
                .username(username)
                .commentaire("Commande en ligne validee")
                .build();
        FactureResponse factureValidee = factureService.validerFacture(validationRequest, username);

        log.info("Commande en ligne {} validee et facturee (facture {}) par {}",
                devisId, factureValidee.getNumeroFacture(), username);
        return ResponseEntity.ok(factureValidee);
    }
}
