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
 * Entité FactureItem - Article d'une facture
 * 
 * RESPONSABILITÉS:
 * - Stocke les informations d'un article dans une facture
 * - Effectue le snapshot des données produit au moment de la facturation
 * - Calcule automatiquement les montants (HT, remise, TVA, TTC)
 * - Liaison avec le produit pour mise à jour des stocks
 * 
 * CALCULS AUTOMATIQUES:
 * - Montant brut = Prix unitaire × Quantité
 * - Montant remise = Montant brut × (Taux remise / 100)
 * - Montant HT = Montant brut - Montant remise
 * - Montant TVA = Montant HT × (Taux TVA / 100)
 * - Montant TTC = Montant HT + Montant TVA
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 3.0
 */
@Entity
@Table(name = "facture_item", indexes = {
    @Index(name = "idx_facture_item_facture", columnList = "facture_id"),
    @Index(name = "idx_facture_item_produit", columnList = "produit_id")
})

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FactureItem implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== IDENTIFIANT ==========
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELATIONS ==========
    
    /**
     * Facture parente
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    /**
     * Produit associé (référence active pour gestion de stock)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    // ========== SNAPSHOT PRODUIT (TRAÇABILITÉ) ==========
    
    /**
     * Code du produit au moment de la facturation
     * Conservé pour traçabilité même si le produit change
     */
    @Column(name = "produit_code_snapshot", length = 50)
    private String produitCodeSnapshot;

    /**
     * Libellé du produit au moment de la facturation
     */
    @Column(name = "produit_libelle_snapshot", length = 255, nullable = false)
    private String produitLibelleSnapshot;

    /**
     * Description du produit
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Référence du produit
     */
    @Column(name = "produit_reference_snapshot", length = 100)
    private String produitReferenceSnapshot;

    // ========== QUANTITÉS ET PRIX ==========
    
    /**
     * Quantité facturée
     * Utilisée pour la sortie de stock
     */
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    /**
     * Prix unitaire HT au moment de la facturation
     */
    @Column(name = "prix_unitaire_ht", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixUnitaireHT;

    // ========== REMISES ==========
    
    /**
     * Taux de remise en pourcentage (0-100)
     */
    @Column(name = "taux_remise", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    /**
     * Montant de la remise en valeur absolue
     * Calculé automatiquement
     */
    @Column(name = "montant_remise", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantRemise = BigDecimal.ZERO;

    /**
     * Type de remise (pourcentage ou montant fixe)
     */
    @Column(name = "type_remise", length = 20)
    private String typeRemise; // POURCENTAGE, MONTANT_FIXE

    // ========== TVA ==========
    
    /**
     * Taux de TVA applicable (en pourcentage)
     */
    @Column(name = "taux_tva", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal tauxTVA = BigDecimal.ZERO;

    /**
     * Montant de la TVA
     * Calculé automatiquement
     */
    @Column(name = "montant_tva", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTVA = BigDecimal.ZERO;

    // ========== MONTANTS CALCULÉS ==========
    
    /**
     * Montant HT de la ligne (après remise)
     * Calculé automatiquement
     */
    @Column(name = "montant_ht", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal montantHT = BigDecimal.ZERO;

    /**
     * Montant TTC de la ligne
     * Calculé automatiquement
     */
    @Column(name = "montant_ttc", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal montantTTC = BigDecimal.ZERO;

    /**
     * Prix unitaire TTC (pour affichage)
     * Calculé automatiquement
     */
    @Column(name = "prix_unitaire_ttc", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal prixUnitaireTTC = BigDecimal.ZERO;

    // ========== ORDRE D'AFFICHAGE ==========
    
    /**
     * Ordre d'affichage de l'article dans la facture
     */
    @Column(name = "ordre")
    private Integer ordre;

    // ========== INFORMATIONS COMPLÉMENTAIRES ==========
    
    /**
     * Commentaire spécifique à cette ligne
     */
    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    // ========== MÉTHODES DE CALCUL ==========
    
    /**
     * Calcule tous les montants de la ligne
     * Appelé automatiquement avant persist et update
     */
    @PrePersist
    @PreUpdate
    public void calculerMontants() {
        if (prixUnitaireHT == null || quantite == null) {
            return;
        }

        // 1. Montant brut (avant remise)
        BigDecimal montantBrut = prixUnitaireHT
                .multiply(new BigDecimal(quantite))
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Calcul de la remise
        if (tauxRemise != null && tauxRemise.compareTo(BigDecimal.ZERO) > 0) {
            if ("MONTANT_FIXE".equals(typeRemise)) {
                // Remise fixe
                montantRemise = tauxRemise.setScale(2, RoundingMode.HALF_UP);
            } else {
                // Remise en pourcentage (par défaut)
                montantRemise = montantBrut
                        .multiply(tauxRemise)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        } else {
            montantRemise = BigDecimal.ZERO;
        }

        // 3. Montant HT (après remise)
        montantHT = montantBrut.subtract(montantRemise)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Calcul de la TVA
        if (tauxTVA != null && tauxTVA.compareTo(BigDecimal.ZERO) > 0) {
            montantTVA = montantHT
                    .multiply(tauxTVA)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            montantTVA = BigDecimal.ZERO;
        }

        // 5. Montant TTC
        montantTTC = montantHT.add(montantTVA)
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Prix unitaire TTC (pour affichage)
        if (quantite > 0) {
            prixUnitaireTTC = montantTTC
                    .divide(new BigDecimal(quantite), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Copie les informations du produit (snapshot)
     * À appeler lors de l'ajout d'un produit à la facture
     */
    public void creerSnapshot() {
        if (produit != null) {
            this.produitCodeSnapshot = produit.getCode();
            this.produitLibelleSnapshot = produit.getLibelle();
            this.description = produit.getDescription();
            this.produitReferenceSnapshot = produit.getReference();
        }
    }

    /**
     * Initialise la ligne avec un produit et copie ses informations
     * 
     * @param produit Produit à facturer
     * @param quantite Quantité à facturer
     * @param prixUnitaireHT Prix unitaire HT
     * @param tauxTVA Taux de TVA
     */
    public void initialiserAvecProduit(Produit produit, Integer quantite, 
                                       BigDecimal prixUnitaireHT, BigDecimal tauxTVA) {
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaireHT = prixUnitaireHT;
        this.tauxTVA = tauxTVA;
        creerSnapshot();
        calculerMontants();
    }

    /**
     * Applique une remise en pourcentage
     */
    public void appliquerRemisePourcentage(BigDecimal pourcentage) {
        this.tauxRemise = pourcentage;
        this.typeRemise = "POURCENTAGE";
        calculerMontants();
    }

    /**
     * Applique une remise en montant fixe
     */
    public void appliquerRemiseMontant(BigDecimal montant) {
        this.tauxRemise = montant;
        this.typeRemise = "MONTANT_FIXE";
        calculerMontants();
    }

    /**
     * Calcule le prix unitaire HT après remise
     */
    public BigDecimal getPrixUnitaireHTApresRemise() {
        if (prixUnitaireHT == null || quantite == null || quantite == 0) {
            return prixUnitaireHT;
        }
        return montantHT.divide(new BigDecimal(quantite), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le taux de remise effectif en pourcentage
     */
    public BigDecimal getTauxRemiseEffectif() {
        if (prixUnitaireHT == null || quantite == null || quantite == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal montantBrut = prixUnitaireHT.multiply(new BigDecimal(quantite));
        if (montantBrut.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return montantRemise.multiply(new BigDecimal("100"))
                .divide(montantBrut, 2, RoundingMode.HALF_UP);
    }

    // ========== MÉTHODES DE VALIDATION ==========
    
    /**
     * Valide que la ligne est cohérente
     */
    public boolean isValide() {
        return produit != null
                && quantite != null && quantite > 0
                && prixUnitaireHT != null && prixUnitaireHT.compareTo(BigDecimal.ZERO) >= 0
                && tauxTVA != null && tauxTVA.compareTo(BigDecimal.ZERO) >= 0
                && (tauxRemise == null || tauxRemise.compareTo(BigDecimal.ZERO) >= 0);
    }

    /**
     * Retourne le montant total de la ligne formaté
     */
    public String getMontantTTCFormate() {
        return String.format("%.2f XAF", montantTTC);
    }

    /**
     * Retourne le prix unitaire formaté
     */
    public String getPrixUnitaireFormate() {
        return String.format("%.2f XAF", prixUnitaireTTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FactureItem)) return false;
        FactureItem that = (FactureItem) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("FactureItem[id=%d, produit=%s, quantite=%d, montantTTC=%s]",
                id,
                produitLibelleSnapshot != null ? produitLibelleSnapshot : "N/A",
                quantite,
                montantTTC);
    }
}