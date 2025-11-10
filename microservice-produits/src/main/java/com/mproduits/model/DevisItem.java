package com.mproduits.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entité DevisItem - Article d'un devis
 * 
 * RESPONSABILITÉS:
 * - Stocke les informations d'un article dans un devis
 * - Calcule automatiquement les montants (HT, remise, TVA, TTC)
 * - Référence le produit concerné
 * 
 * CALCULS:
 * - Montant HT ligne = (Prix unitaire × Quantité) - Remise
 * - Remise = (Prix unitaire × Quantité × % remise) / 100
 * - TVA ligne = Montant HT × Taux TVA
 * - Montant TTC = Montant HT + TVA
 * 
 * @author USER01
 * @version 2.0
 */
@Entity
@Table(name = "devis_item", indexes = {
    @Index(name = "idx_devis_item_devis", columnList = "devis_id"),
    @Index(name = "idx_devis_item_produit", columnList = "produit_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevisItem implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== IDENTIFIANT ==========
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // ========== RELATIONS ==========
    
    /**
     * Devis parent
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;

    /**
     * Produit associé (relation vers l'entité Produit)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    // ========== INFORMATIONS PRODUIT (SNAPSHOT) ==========
    
    /**
     * Code du produit au moment du devis (pour traçabilité)
     */
    @Column(name = "produit_code", length = 50)
    private String produitCode;

    /**
     * Libellé du produit au moment du devis
     */
    @Column(name = "produit_libelle", length = 255)
    private String produitLibelle;

    /**
     * Description du produit
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ========== QUANTITÉS ET PRIX ==========
    
    /**
     * Quantité commandée
     */
    @Column(nullable = false)
    private Integer quantite;

    /**
     * Prix unitaire HT
     */
    @Column(name = "prix_unitaire", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    /**
     * Taux de remise en pourcentage (0-100)
     */
    @Column(name = "taux_remise", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    /**
     * Montant de la remise en valeur absolue
     */
    @Column(name = "montant_remise", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantRemise = BigDecimal.ZERO;

    // ========== MONTANTS CALCULÉS ==========
    
    /**
     * Montant HT de la ligne (après remise)
     */
    @Column(name = "montant_ht", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantHT = BigDecimal.ZERO;

    /**
     * Taux de TVA applicable
     */
    @Column(name = "taux_tva", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxTVA = BigDecimal.ZERO;

    /**
     * Montant de la TVA
     */
    @Column(name = "montant_tva", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTVA = BigDecimal.ZERO;

    /**
     * Montant TTC de la ligne
     */
    @Column(name = "montant_ttc", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTTC = BigDecimal.ZERO;

    // ========== ORDRE D'AFFICHAGE ==========
    
    /**
     * Ordre d'affichage de l'article dans le devis
     */
    @Column(name = "ordre")
    private Integer ordre;

    // ========== MÉTHODES DE CALCUL ==========
    
    /**
     * Calcule tous les montants de la ligne
     * Doit être appelé avant la sauvegarde
     */
//    @PrePersist
//    @PreUpdate
//    public void calculerMontants() {
//        if (prixUnitaire == null || quantite == null) {
//            return;
//        }
//
//        // Montant brut (avant remise)
//        BigDecimal montantBrut = prixUnitaire
//                .multiply(new BigDecimal(quantite))
//                .setScale(2, RoundingMode.HALF_UP);
//
//        // Calcul de la remise
//        if (tauxRemise != null && tauxRemise.compareTo(BigDecimal.ZERO) > 0) {
//            montantRemise = montantBrut
//                    .multiply(tauxRemise)
//                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//        } else {
//            montantRemise = BigDecimal.ZERO;
//        }
//
//        // Montant HT (après remise)
//        montantHT = montantBrut.subtract(montantRemise)
//                .setScale(2, RoundingMode.HALF_UP);
//
//        // Calcul de la TVA
//        if (tauxTVA != null && tauxTVA.compareTo(BigDecimal.ZERO) > 0) {
//            montantTVA = montantHT
//                    .multiply(tauxTVA)
//                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//        } else {
//            montantTVA = BigDecimal.ZERO;
//        }
//
//        // Montant TTC
//        montantTTC = montantHT.add(montantTVA)
//                .setScale(2, RoundingMode.HALF_UP);
//    }

    /**
     * Calcule le montant unitaire HT après remise
     */
    public BigDecimal getPrixUnitaireApresRemise() {
        if (prixUnitaire == null || tauxRemise == null) {
            return prixUnitaire;
        }

        BigDecimal remiseUnitaire = prixUnitaire
                .multiply(tauxRemise)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        return prixUnitaire.subtract(remiseUnitaire);
    }

    /**
     * Copie les informations du produit (snapshot)
     */
    public void copierInfosProduit() {
        if (produit != null) {
            this.produitCode = produit.getCode();
            this.produitLibelle = produit.getLibelle();
            this.description = produit.getDescription();
        }
    }

    /**
     * Initialise avec un produit et copie ses informations
     */
    public void initialiserAvecProduit(Produit produit, Integer quantite, BigDecimal prixUnitaire) {
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        copierInfosProduit();
    }

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Valide que la ligne est cohérente
     */
    public boolean isValide() {
        return produit != null 
                && quantite != null && quantite > 0
                && prixUnitaire != null && prixUnitaire.compareTo(BigDecimal.ZERO) >= 0
                && (tauxRemise == null || (tauxRemise.compareTo(BigDecimal.ZERO) >= 0 
                    && tauxRemise.compareTo(new BigDecimal("100")) <= 100));
    }

    /**
     * Retourne le montant total de la ligne en format texte
     */
    public String getMontantTotalFormate() {
        return String.format("%.2f XAF", montantTTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DevisItem)) return false;
        DevisItem that = (DevisItem) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("DevisItem[id=%d, produit=%s, quantite=%d, montantTTC=%s]",
                id, 
                produitLibelle != null ? produitLibelle : "N/A",
                quantite, 
                montantTTC);
    }
}