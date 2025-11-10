/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.VersementStatistiques;
import com.mproduits.enums.ModePaiement;

import com.mproduits.repositories.VersementClientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author USER01
 */
@Service
@RequiredArgsConstructor
public class VersementStatistiqueService {
     private final VersementClientRepository versementRepository;

    @Transactional(readOnly = true)
    public VersementStatistiques getStatistiques(Date dateDebut, Date dateFin) {
        VersementStatistiques stats = new VersementStatistiques();
        stats.setDateDebut(dateDebut);
        stats.setDateFin(dateFin);

        // 1️⃣ Statistiques globales
        Map<String, Object> global = versementRepository.getStatistiquesGlobale(dateDebut, dateFin);

        stats.setTotalVersements(((Number) global.get("totalVersements")).longValue());
        stats.setVersementsEnAttente(((Number) global.get("versementsEnAttente")).longValue());
        stats.setVersementsValides(((Number) global.get("versementsValides")).longValue());
        stats.setVersementsAnnules(((Number) global.get("versementsAnnules")).longValue());
        stats.setMontantTotalVersements(toBigDecimal(global.get("montantTotalVersements")));
        stats.setMontantEnAttente(toBigDecimal(global.get("montantEnAttente")));
        stats.setMontantValide(toBigDecimal(global.get("montantValide")));

        // 2️⃣ Par mode de paiement
        List<Map<String, Object>> modes = versementRepository.getStatistiquesParMode(dateDebut, dateFin);

        Map<ModePaiement, BigDecimal> montantsParMode = new HashMap<>();
        Map<ModePaiement, Long> nombreParMode = new HashMap<>();

        for (Map<String, Object> row : modes) {
            ModePaiement mode = ModePaiement.valueOf(row.get("mode").toString());
            nombreParMode.put(mode, ((Number) row.get("nombre")).longValue());
            montantsParMode.put(mode, toBigDecimal(row.get("total")));
        }

        stats.setMontantsParMode(montantsParMode);
        stats.setNombreParMode(nombreParMode);

        return stats;
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
