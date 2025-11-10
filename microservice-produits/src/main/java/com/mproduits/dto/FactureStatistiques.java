package com.mproduits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour les statistiques des factures
 * Contient les métriques calculées sur une période donnée
 * 
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FactureStatistiques {
    
    // ========== COMPTEURS PAR STATUT ==========
    
    private Long totalFactures;
    private Long facturesBrouillon;
    private Long facturesNonPayees;
    private Long facturesPartielles;
    private Long facturesPayees;
    private Long facturesEnRetard;
    private Long facturesAnnulees;
    
    // ========== MONTANTS FINANCIERS ==========
    
    private BigDecimal chiffreAffairesTotalHT;
    private BigDecimal chiffreAffairesTotalTTC;
    private BigDecimal montantTotalPaye;
    private BigDecimal montantTotalImpaye;
    private BigDecimal montantEnRetard;
    
    // ========== INDICATEURS ==========
    
    /**
     * Taux de recouvrement : Montant payé / Total TTC (en pourcentage)
     */
    private BigDecimal tauxRecouvrement;
    
    /**
     * Délai moyen de paiement en jours
     */
    private BigDecimal delaiMoyenPaiement;
    
    /**
     * Constructeur utilisé par la requête JPQL du repository
     * L'ordre des paramètres doit correspondre exactement à celui de la requête SELECT
     * 
     * @param totalFactures nombre total de factures
     * @param facturesBrouillon nombre de factures en brouillon
     * @param facturesNonPayees nombre de factures non payées
     * @param facturesPartielles nombre de factures partiellement payées
     * @param facturesPayees nombre de factures payées
     * @param facturesEnRetard nombre de factures en retard
     * @param facturesAnnulees nombre de factures annulées
     * @param chiffreAffairesTotalHT CA total HT
     * @param chiffreAffairesTotalTTC CA total TTC
     * @param montantTotalPaye montant total payé
     * @param montantTotalImpaye montant total impayé
     * @param montantEnRetard montant en retard
     * @param tauxRecouvrement taux de recouvrement (%)
     * @param delaiMoyenPaiement délai moyen de paiement (jours)
     */
    public FactureStatistiques(
            Long totalFactures,
            Long facturesBrouillon,
            Long facturesNonPayees,
            Long facturesPartielles,
            Long facturesPayees,
            Long facturesEnRetard,
            Long facturesAnnulees,
            BigDecimal chiffreAffairesTotalHT,
            BigDecimal chiffreAffairesTotalTTC,
            BigDecimal montantTotalPaye,
            BigDecimal montantTotalImpaye,
            BigDecimal montantEnRetard,
            Double tauxRecouvrement,
            Double delaiMoyenPaiement) {
        
        // Initialisation des compteurs (protection contre null)
        this.totalFactures = totalFactures != null ? totalFactures : 0L;
        this.facturesBrouillon = facturesBrouillon != null ? facturesBrouillon : 0L;
        this.facturesNonPayees = facturesNonPayees != null ? facturesNonPayees : 0L;
        this.facturesPartielles = facturesPartielles != null ? facturesPartielles : 0L;
        this.facturesPayees = facturesPayees != null ? facturesPayees : 0L;
        this.facturesEnRetard = facturesEnRetard != null ? facturesEnRetard : 0L;
        this.facturesAnnulees = facturesAnnulees != null ? facturesAnnulees : 0L;
        
        // Initialisation des montants (protection contre null)
        this.chiffreAffairesTotalHT = chiffreAffairesTotalHT != null ? chiffreAffairesTotalHT : BigDecimal.ZERO;
        this.chiffreAffairesTotalTTC = chiffreAffairesTotalTTC != null ? chiffreAffairesTotalTTC : BigDecimal.ZERO;
        this.montantTotalPaye = montantTotalPaye != null ? montantTotalPaye : BigDecimal.ZERO;
        this.montantTotalImpaye = montantTotalImpaye != null ? montantTotalImpaye : BigDecimal.ZERO;
        this.montantEnRetard = montantEnRetard != null ? montantEnRetard : BigDecimal.ZERO;
        
        // Conversion des indicateurs de Double vers BigDecimal
        this.tauxRecouvrement = tauxRecouvrement != null ? 
            BigDecimal.valueOf(tauxRecouvrement).setScale(2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
            
        this.delaiMoyenPaiement = delaiMoyenPaiement != null ? 
            BigDecimal.valueOf(delaiMoyenPaiement).setScale(2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
    }
    
    /**
     * Calcule le pourcentage de factures payées par rapport au total
     */
    public BigDecimal getPourcentageFacturesPayees() {
        if (totalFactures == null || totalFactures == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(facturesPayees)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalFactures), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calcule le pourcentage de factures en retard par rapport au total
     */
    public BigDecimal getPourcentageFacturesEnRetard() {
        if (totalFactures == null || totalFactures == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(facturesEnRetard)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalFactures), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Vérifie si les statistiques sont vides (aucune facture)
     */
    public boolean isEmpty() {
        return totalFactures == null || totalFactures == 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "FactureStatistiques[" +
            "total=%d, payées=%d, impayées=%d, en retard=%d, " +
            "CA TTC=%s, montant payé=%s, taux recouvrement=%.2f%%, " +
            "délai moyen=%.1f jours]",
            totalFactures, facturesPayees, facturesNonPayees, facturesEnRetard,
            chiffreAffairesTotalTTC, montantTotalPaye, tauxRecouvrement, delaiMoyenPaiement
        );
    }
}