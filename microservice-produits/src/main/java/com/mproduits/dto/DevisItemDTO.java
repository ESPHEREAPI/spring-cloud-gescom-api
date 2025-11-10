package com.mproduits.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour DevisItem - Article d'un devis
 * 
 * VALIDATIONS:
 * - produitId obligatoire
 * - quantite > 0
 * - prixUnitaire >= 0
 * - tauxRemise entre 0 et 100
 * 
 * @author USER01
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevisItemDTO {

    // ========== IDENTIFIANT ==========
    
    /**
     * ID de l'article (null lors de la création)
     */
    private Long id;

    // ========== PRODUIT ==========
    
    /**
     * ID du produit (obligatoire)
     */
    @NotNull(message = "Le produit est obligatoire")
    @Positive(message = "L'ID du produit doit être positif")
    private Long produitId;

    /**
     * Code du produit (pour affichage)
     */
    private String produitCode;

    /**
     * Libellé du produit
     */
    private String produitLibelle;

    /**
     * Description du produit
     */
    private String description;
    private String typeRemise;

    // ========== QUANTITÉS ET PRIX ==========
    
    /**
     * Quantité commandée (obligatoire)
     */
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins de 1")
    @Max(value = 999999, message = "La quantité ne peut pas dépasser 999999")
    private Integer quantite;

    /**
     * Prix unitaire HT (obligatoire)
     */
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix unitaire doit être >= 0")
    private BigDecimal prixUnitaire;

    /**
     * Taux de remise en pourcentage (0-100)
     */
    @DecimalMin(value = "0.0", message = "Le taux de remise doit être >= 0")
    @DecimalMax(value = "100.0", message = "Le taux de remise doit être <= 100")
    @Builder.Default
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    /**
     * Montant de la remise (calculé)
     */
    @Builder.Default
    private BigDecimal montantRemise = BigDecimal.ZERO;

    // ========== MONTANTS CALCULÉS ==========
    
    /**
     * Montant HT de la ligne (après remise)
     */
    @Builder.Default
    private BigDecimal montantHT = BigDecimal.ZERO;

    /**
     * Taux de TVA applicable
     */
    @Builder.Default
    private BigDecimal tauxTVA = BigDecimal.ZERO;

    /**
     * Montant de la TVA
     */
    @Builder.Default
    private BigDecimal montantTVA = BigDecimal.ZERO;

    /**
     * Montant TTC de la ligne
     */
    @Builder.Default
    private BigDecimal montantTTC = BigDecimal.ZERO;

    // ========== ORDRE ==========
    
    /**
     * Ordre d'affichage
     */
    private Integer ordre;

    // ========== INFORMATIONS STOCK ==========
    
    /**
     * Stock disponible (pour vérification frontend)
     */
    private BigDecimal stockDisponible;

    /**
     * Indique si le stock est suffisant
     */
    private Boolean stockSuffisant;

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Calcule le prix unitaire après remise
     */
    public BigDecimal getPrixUnitaireApresRemise() {
        if (prixUnitaire == null || tauxRemise == null) {
            return prixUnitaire;
        }

        BigDecimal remiseUnitaire = prixUnitaire
                .multiply(tauxRemise)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

        return prixUnitaire.subtract(remiseUnitaire);
    }

    /**
     * Calcule le montant brut (avant remise)
     */
    public BigDecimal getMontantBrut() {
        if (prixUnitaire == null || quantite == null) {
            return BigDecimal.ZERO;
        }

        return prixUnitaire
                .multiply(new BigDecimal(quantite))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Retourne le montant TTC formaté
     */
    public String getMontantTTCFormate() {
        return String.format("%.2f XAF", montantTTC);
    }

    /**
     * Retourne le montant HT formaté
     */
    public String getMontantHTFormate() {
        return String.format("%.2f XAF", montantHT);
    }

    /**
     * Retourne le prix unitaire formaté
     */
    public String getPrixUnitaireFormate() {
        return String.format("%.2f XAF", prixUnitaire);
    }

    /**
     * Valide la cohérence des données
     */
    public boolean isValide() {
        return produitId != null 
                && quantite != null && quantite > 0
                && prixUnitaire != null && prixUnitaire.compareTo(BigDecimal.ZERO) >= 0
                && (tauxRemise == null || (tauxRemise.compareTo(BigDecimal.ZERO) >= 0 
                    && tauxRemise.compareTo(new BigDecimal("100")) <= 0));
    }

    /**
     * Calcule tous les montants
     * Appelé avant l'envoi au backend
     */
    public void calculerMontants() {
        if (prixUnitaire == null || quantite == null) {
            return;
        }

        // Montant brut
        BigDecimal montantBrut = prixUnitaire
                .multiply(new BigDecimal(quantite))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Remise
        if (tauxRemise != null && tauxRemise.compareTo(BigDecimal.ZERO) > 0) {
            montantRemise = montantBrut
                    .multiply(tauxRemise)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        } else {
            montantRemise = BigDecimal.ZERO;
        }

        // Montant HT
        montantHT = montantBrut.subtract(montantRemise)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // TVA
        if (tauxTVA != null && tauxTVA.compareTo(BigDecimal.ZERO) > 0) {
            montantTVA = montantHT
                    .multiply(tauxTVA)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        } else {
            montantTVA = BigDecimal.ZERO;
        }

        // Montant TTC
        montantTTC = montantHT.add(montantTVA)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Retourne une description complète de l'article
     */
    public String getDescriptionComplete() {
        return String.format("%s - %s (Qté: %d × %.2f XAF = %.2f XAF)",
                produitCode != null ? produitCode : "N/A",
                produitLibelle != null ? produitLibelle : "Produit",
                quantite,
                prixUnitaire,
                montantTTC);
    }
}