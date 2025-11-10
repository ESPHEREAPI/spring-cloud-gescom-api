/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;


import com.mproduits.dto.HistoriquePaiementClient;
import com.mproduits.dto.VersementSummary;
import com.mproduits.enums.StatutVersement;
import com.mproduits.model.Client;
import com.mproduits.model.Facture;
import com.mproduits.model.VersementClient;
import com.mproduits.repositories.ClientRepositories;
import com.mproduits.repositories.FactureRepository;
import com.mproduits.repositories.VersementClientRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoriquePaiementService {

    private final ClientRepositories clientRepository;
    private final FactureRepository factureRepository;
    private final VersementClientRepository versementRepository;

    /**
     * Construit l'historique complet avec les 10 derniers versements
     */
    public HistoriquePaiementClient getHistoriquePaiementClient(Long clientId) {
        // 1️⃣ Vérifier que le client existe
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable avec ID: " + clientId));

        // 2️⃣ Récupérer les factures et versements du client
        List<Facture> factures = factureRepository.findByClientId(clientId);
        List<VersementClient> versements = versementRepository.findByClientIdOrderByDateVersementDesc(clientId);

        // 3️⃣ Calculer les montants
        BigDecimal montantTotalFactures = factures.stream()
                .map(Facture::getTotalTtc)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTotalPaye = versements.stream()
                .filter(VersementClient::isValide)
                .map(VersementClient::getMontant)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTotalImpaye = montantTotalFactures.subtract(montantTotalPaye)
                .max(BigDecimal.ZERO);

        // 4️⃣ Calculer le taux de recouvrement
        BigDecimal tauxRecouvrement = montantTotalFactures.compareTo(BigDecimal.ZERO) > 0
                ? montantTotalPaye.multiply(BigDecimal.valueOf(100))
                    .divide(montantTotalFactures, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 5️⃣ Calculer le délai moyen de paiement (entre date facture et date versement)
        BigDecimal delaiMoyenPaiement = calculerDelaiMoyenPaiement(factures, versements);

        // 6️⃣ Récupérer les 10 derniers versements
        List<VersementSummary> derniersVersements = versements.stream()
                .sorted(Comparator.comparing(VersementClient::getDateVersement).reversed())
                .limit(10)
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        // 7️⃣ Construire et retourner le DTO
        return HistoriquePaiementClientBuilder(client, factures, versements,
                montantTotalFactures, montantTotalPaye, montantTotalImpaye,
                tauxRecouvrement, delaiMoyenPaiement, derniersVersements);
    }

    /**
     * Version allégée sans les détails des versements
     */
    public HistoriquePaiementClient getHistoriqueResume(Long clientId) {
        HistoriquePaiementClient complet = getHistoriquePaiementClient(clientId);
        complet.setDerniersVersements(null); // on retire les détails
        return complet;
    }

    /**
     * Calcule le délai moyen de paiement entre les factures et les versements
     */
    private BigDecimal calculerDelaiMoyenPaiement(List<Facture> factures, List<VersementClient> versements) {
        List<Long> delais = versements.stream()
                .filter(v -> v.getFacture() != null && v.getFacture().getDateFacture() != null)
                .map(v -> {
                    long jours = Duration.between(
                            v.getFacture().getDateFacture().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(),
                            v.getDateVersement().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay()
                    ).toDays();
                    return Math.max(jours, 0);
                })
                .collect(Collectors.toList());

        if (delais.isEmpty()) return BigDecimal.ZERO;

        double moyenne = delais.stream().mapToLong(Long::longValue).average().orElse(0);
        return BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Mappe un VersementClient vers son résumé (DTO)
     */
    private VersementSummary mapToSummary(VersementClient v) {
        return VersementSummary.builder()
                .id(v.getId())
                .numeroVersement(v.getNumeroVersement())
                .dateVersement(v.getDateVersement())
                .montant(v.getMontant())
                .modePaiement(v.getModePaiement())
                .referencePaiement(v.getReferencePaiement())
                .statut(StatutVersement.valueOf(v.getStatut()))
                .factureNumero(v.getFacture() != null ? v.getFacture().getNumeroFacture() : null)
                .build();
    }

    /**
     * Construit l'objet HistoriquePaiementClient
     */
    private HistoriquePaiementClient HistoriquePaiementClientBuilder(
            Client client,
            List<Facture> factures,
            List<VersementClient> versements,
            BigDecimal totalFactures,
            BigDecimal totalPaye,
            BigDecimal totalImpaye,
            BigDecimal tauxRecouvrement,
            BigDecimal delaiMoyen,
            List<VersementSummary> derniersVersements
    ) {
        HistoriquePaiementClient dto = new HistoriquePaiementClient();
        dto.setClientId(client.getId());
        dto.setClientNom(client.getNom());
        dto.setNombreFactures((long) factures.size());
        dto.setNombreVersements((long) versements.size());
        dto.setMontantTotalFactures(totalFactures);
        dto.setMontantTotalPaye(totalPaye);
        dto.setMontantTotalImpaye(totalImpaye);
        dto.setTauxRecouvrement(tauxRecouvrement);
        dto.setDelaiMoyenPaiement(delaiMoyen);
        dto.setDerniersVersements(derniersVersements);
        return dto;
    }
}
