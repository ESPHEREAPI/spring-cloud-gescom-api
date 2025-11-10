/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.VersementStatistiques;
import com.mproduits.enums.ModePaiement;
import com.mproduits.model.VersementClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author USER01
 */
@Repository
public class VersementRepositoryImpl implements VersementRepositoryCustom{
    
    
    @PersistenceContext
    private EntityManager em;

    @Override
    public VersementStatistiques calculerStatistiques(Date dateDebut, Date dateFin) {
        VersementStatistiques stats = new VersementStatistiques();
        stats.setDateDebut(dateDebut);
        stats.setDateFin(dateFin);

        // 🔹 1️⃣ Récupérer tous les versements dans la période
        String jpql = "SELECT v FROM VersementClient v " +
                      "WHERE v.dateVersement BETWEEN :debut AND :fin";
        TypedQuery<VersementClient> query = em.createQuery(jpql, VersementClient.class);
        query.setParameter("debut", dateDebut);
        query.setParameter("fin", dateFin);

        List<VersementClient> versements = query.getResultList();

        // 🔹 2️⃣ Compteurs par statut
        long total = versements.size();
        long enAttente = versements.stream().filter(v -> "EN_ATTENTE".equals(v.getStatut())).count();
        long valides = versements.stream().filter(v -> "VALIDE".equals(v.getStatut())).count();
        long annules = versements.stream().filter(v -> "ANNULE".equals(v.getStatut())).count();

        // 🔹 3️⃣ Montants
        BigDecimal montantTotal = somme(versements);
        BigDecimal montantValide = somme(versements.stream()
                .filter(v -> "VALIDE".equals(v.getStatut()))
                .toList());
        BigDecimal montantEnAttente = somme(versements.stream()
                .filter(v -> "EN_ATTENTE".equals(v.getStatut()))
                .toList());

        // 🔹 4️⃣ Par mode de paiement
        Map<ModePaiement, BigDecimal> montantsParMode = versements.stream()
                .collect(Collectors.groupingBy(
                        VersementClient::getModePaiement,
                        Collectors.reducing(BigDecimal.ZERO,
                                VersementClient::getMontant,
                                BigDecimal::add)
                ));

        Map<ModePaiement, Long> nombreParMode = versements.stream()
                .collect(Collectors.groupingBy(
                        VersementClient::getModePaiement,
                        Collectors.counting()
                ));

        // 🔹 5️⃣ Remplir l’objet VersementStatistiques
        stats.setTotalVersements(total);
        stats.setVersementsEnAttente(enAttente);
        stats.setVersementsValides(valides);
        stats.setVersementsAnnules(annules);

        stats.setMontantTotalVersements(montantTotal);
        stats.setMontantValide(montantValide);
        stats.setMontantEnAttente(montantEnAttente);

        stats.setMontantsParMode(montantsParMode);
        stats.setNombreParMode(nombreParMode);

        return stats;
    }

    // Méthode utilitaire
    private BigDecimal somme(List<VersementClient> liste) {
        return liste.stream()
                .map(VersementClient::getMontant)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
