package com.mproduits.services;

import com.mproduits.dto.MargeDetailDTO;
import com.mproduits.dto.RessourceConsolideeDTO;
import com.mproduits.model.Charge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Marge reelle d'une boutique = Ressources - Charges, avec detail complet
 * (pas seulement le solde net). Distinct de MargeCibleService (config de
 * taux cible par categorie) et de la "marge" par article calculee dans
 * HistoriqueCaisseService/MargeVenteComponent ("Marge Caisse") - trois
 * concepts differents qui partagent le mot "marge".
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MargeReelleService {

    private final RessourceService ressourceService;
    private final ChargeService chargeService;

    public MargeDetailDTO getDetail(Long boutiqueId, LocalDate debut, LocalDate fin) {
        RessourceConsolideeDTO ressources = ressourceService.getConsolide(boutiqueId, debut, fin);

        List<Charge> charges = chargeService.findByBoutiqueAndPeriode(boutiqueId, debut, fin);
        BigDecimal totalCharges = charges.stream()
                .map(Charge::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal marge = ressources.getTotal().subtract(totalCharges);

        return MargeDetailDTO.builder()
                .ressources(ressources)
                .charges(charges)
                .totalCharges(totalCharges)
                .marge(marge)
                .build();
    }
}
