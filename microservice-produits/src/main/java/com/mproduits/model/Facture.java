package com.mproduits.model;

import com.mproduits.enums.ModePaiement;
import com.mproduits.enums.StatutFacture;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entité Facture - Gestion complète des factures clients
 * 
 * MODES DE CRÉATION:
 * 1. Conversion d'un devis accepté → Facture avec référence au devis
 * 2. Création directe → Facture sans devis
 * 
 * CYCLE DE VIE:
 * 1. BROUILLON     → Facture en création, modifiable
 * 2. NON_PAYEE     → Facture validée, stock sorti, en attente de paiement
 * 3. PARTIELLE     → Paiement partiel reçu
 * 4. PAYEE         → Totalement payée
 * 5. EN_RETARD     → Échéance dépassée
 * 6. ANNULEE       → Facture annulée, stock réintégré
 * 
 * RÈGLES MÉTIER:
 * - Une facture BROUILLON peut être modifiée
 * - À la validation (BROUILLON → NON_PAYEE):
 *   * Sortie de stock (PointVente.stockFinalTheorie - quantité)
 *   * Incrémentation PointVente.sortiProduit
 *   * Création notification client
 * - À l'annulation:
 *   * Réintégration stock (PointVente.stockFinalTheorie + quantité)
 *   * Incrémentation PointVente.entreeProduit
 *   * Notification client
 * - Calcul automatique des montants
 * - Notifications automatiques selon échéances
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 3.0
 */
@Entity
@Table(name = "facture_client", indexes = {
    @Index(name = "idx_facture_numero", columnList = "numero_facture"),
    @Index(name = "idx_facture_client", columnList = "client_id"),
    @Index(name = "idx_facture_statut", columnList = "statut"),
    @Index(name = "idx_facture_date", columnList = "date_facture DESC"),
    @Index(name = "idx_facture_echeance", columnList = "date_echeance"),
    @Index(name = "idx_facture_devis", columnList = "devis_id"),
    @Index(name = "idx_facture_client_statut", columnList = "client_id, statut")
})

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Facture implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== IDENTIFIANT ==========
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Numéro unique de la facture (Format: FAC-YYYY-NNNN)
     * Exemple: FAC-2025-0001
     * Généré automatiquement à la création
     */
    @Column(name = "numero_facture", unique = true, nullable = false, length = 20)
    private String numeroFacture;

    // ========== DATES ==========
    
    /**
     * Date de création de la facture
     */
    @Column(name = "date_facture", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFacture;

    /**
     * Date d'échéance de paiement
     * Par défaut: dateFacture + délai de paiement client
     */
    @Column(name = "date_echeance")
    @Temporal(TemporalType.DATE)
    private Date dateEcheance;

    /**
     * Date de validation de la facture (passage de BROUILLON à NON_PAYEE)
     */
    @Column(name = "date_validation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    /**
     * Date de paiement complet (statut = PAYEE)
     */
    @Column(name = "date_paiement_complet")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePaiementComplet;

    /**
     * Date d'annulation (si statut = ANNULEE)
     */
    @Column(name = "date_annulation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAnnulation;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModification;

    // ========== MONTANTS FINANCIERS ==========
    
    /**
     * Montant total HT (Hors Taxes)
     */
    @Column(name = "total_ht", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalHt = BigDecimal.ZERO;

    /**
     * Montant total des remises
     */
    @Column(name = "total_remise", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRemise = BigDecimal.ZERO;

    /**
     * Montant total de la TVA
     */
    @Column(name = "total_tva", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalTVA = BigDecimal.ZERO;

    /**
     * Montant total TTC (Toutes Taxes Comprises)
     */
    @Column(name = "total_ttc", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalTtc = BigDecimal.ZERO;

    /**
     * Montant déjà payé (somme de tous les versements)
     */
    @Column(name = "montant_paye", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    /**
     * Solde restant à payer
     * Calculé: totalTtc - montantPaye
     */
    @Column(name = "solde_restant", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal soldeRestant = BigDecimal.ZERO;

    // ========== STATUT ==========
    
    /**
     * Statut de la facture
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutFacture statut = StatutFacture.BROUILLON;

    /**
     * Version pour gestion optimiste des conflits
     */
    @Version
    @Column(name = "version")
    private Integer version;

    // ========== PAIEMENT ==========
    
    /**
     * Mode de paiement par défaut/principal
     */
    @Column(name = "mode_paiement_defaut", length = 30)
    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiementDefaut;

    /**
     * Délai de paiement en jours
     * Utilisé pour calculer la date d'échéance
     */
    @Column(name = "delai_paiement_jours")
    @Builder.Default
    private Integer delaiPaiementJours = 30;

    /**
     * Pénalités de retard (en pourcentage par jour)
     */
    @Column(name = "penalite_retard", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal penaliteRetard = BigDecimal.ZERO;

    /**
     * Montant des pénalités calculées (si en retard)
     */
    @Column(name = "montant_penalite", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantPenalite = BigDecimal.ZERO;

    // ========== RELATIONS ==========
    
    /**
     * Client associé à la facture (obligatoire)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Devis source (si la facture provient d'un devis)
     * Null si création directe
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id")
    private Devis devis;

    /**
     * Articles de la facture
     */
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FactureItem> items = new ArrayList<>();

    /**
     * Versements effectués sur cette facture
     */
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    @Builder.Default
    private List<VersementClient> versements = new ArrayList<>();

    /**
     * Mouvements de stock générés par cette facture
     */
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StockMovement> mouvementsStock = new ArrayList<>();

    /**
     * Notifications envoyées pour cette facture
     */
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NotificationClient> notifications = new ArrayList<>();

    // ========== INFORMATIONS COMPLÉMENTAIRES ==========
    
    /**
     * Notes ou remarques sur la facture
     */
    @Column(name = "remarques", columnDefinition = "TEXT")
    private String remarques;

    /**
     * Conditions de paiement spécifiques
     */
    @Column(name = "conditions_paiement", columnDefinition = "TEXT")
    private String conditionsPaiement;

    /**
     * Motif d'annulation (si statut = ANNULEE)
     */
    @Column(name = "motif_annulation", columnDefinition = "TEXT")
    private String motifAnnulation;

    /**
     * Référence externe (commande client, bon de commande, etc.)
     */
    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;

    // ========== TRAÇABILITÉ ==========
    
    /**
     * Utilisateur ayant créé la facture
     */
    @Column(name = "username_create", nullable = false, length = 100)
    private String usernameCreate;

    /**
     * Utilisateur ayant modifié la facture
     */
    @Column(name = "username_update", length = 100)
    private String usernameUpdate;

    /**
     * Utilisateur ayant validé la facture
     */
    @Column(name = "username_validation", length = 100)
    private String usernameValidation;

    /**
     * Utilisateur ayant annulé la facture
     */
    @Column(name = "username_annulation", length = 100)
    private String usernameAnnulation;

    // ========== FLAGS ==========
    
    /**
     * Indique si la facture a été envoyée au client
     */
    @Column(name = "envoyee_client")
    @Builder.Default
    private Boolean envoyeeClient = false;

    /**
     * Date d'envoi au client
     */
    @Column(name = "date_envoi_client")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoiClient;

    /**
     * Nombre de relances effectuées
     */
    @Column(name = "nombre_relances")
    @Builder.Default
    private Integer nombreRelances = 0;

    /**
     * Date de dernière relance
     */
    @Column(name = "date_derniere_relance")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDerniereRelance;

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Ajoute un article à la facture
     */
    public void addItem(FactureItem item) {
        items.add(item);
        item.setFacture(this);
    }

    /**
     * Retire un article de la facture
     */
    public void removeItem(FactureItem item) {
        items.remove(item);
        item.setFacture(null);
    }

    /**
     * Ajoute un versement à la facture
     */
    public void addVersement(VersementClient versement) {
        versements.add(versement);
        versement.setFacture(this);
    }

    /**
     * Vérifie si la facture peut être modifiée
     */
    public boolean isModifiable() {
        return statut == StatutFacture.BROUILLON;
    }

    /**
     * Vérifie si la facture peut être validée
     */
    public boolean isPeutEtreValidee() {
        return statut == StatutFacture.BROUILLON && items != null && !items.isEmpty();
    }

    /**
     * Vérifie si la facture peut être annulée
     */
    public boolean isPeutEtreAnnulee() {
        return statut.isPeutEtreAnnulee();
    }

    /**
     * Vérifie si la facture peut recevoir un paiement
     */
    public boolean isPeutRecevoirPaiement() {
        return statut.isPeutRecevoirPaiement();
    }

    /**
     * Vérifie si la facture est en retard
     */
    public boolean isEnRetard() {
        if (dateEcheance == null || statut == StatutFacture.PAYEE || statut == StatutFacture.ANNULEE) {
            return false;
        }
        return new Date().after(dateEcheance) && soldeRestant.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calcule le nombre de jours de retard
     */
    public long getJoursRetard() {
        if (!isEnRetard()) {
            return 0;
        }
        long diff = new Date().getTime() - dateEcheance.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * Calcule le nombre de jours avant échéance
     */
    public long getJoursAvantEcheance() {
        if (dateEcheance == null) {
            return -1;
        }
        long diff = dateEcheance.getTime() - new Date().getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * Vérifie si l'échéance est proche (< 7 jours)
     */
    public boolean isEcheanceProche() {
        long jours = getJoursAvantEcheance();
        return jours >= 0 && jours <= 7 && statut.isPeutRecevoirPaiement();
    }

    /**
     * Calcule le pourcentage payé
     */
    public BigDecimal getPourcentagePaye() {
        if (totalTtc.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return montantPaye.multiply(new BigDecimal("100"))
                .divide(totalTtc, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Recalcule les montants totaux à partir des items
     */
    public void recalculerMontants() {
        if (items == null || items.isEmpty()) {
            totalHt = BigDecimal.ZERO;
            totalRemise = BigDecimal.ZERO;
            totalTVA = BigDecimal.ZERO;
            totalTtc = BigDecimal.ZERO;
            return;
        }

        totalHt = items.stream()
                .map(FactureItem::getMontantHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalRemise = items.stream()
                .map(FactureItem::getMontantRemise)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalTVA = items.stream()
                .map(FactureItem::getMontantTVA)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalTtc = items.stream()
                .map(FactureItem::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Recalcul du solde restant
        soldeRestant = totalTtc.subtract(montantPaye);
    }

    /**
     * Recalcule le montant payé à partir des versements
     */
    public void recalculerMontantPaye() {
        if (versements == null || versements.isEmpty()) {
            montantPaye = BigDecimal.ZERO;
        } else {
            montantPaye = versements.stream()
                    .map(VersementClient::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        soldeRestant = totalTtc.subtract(montantPaye);
    }

    /**
     * Met à jour automatiquement le statut en fonction des paiements
     */
    public void mettreAJourStatut() {
        if (statut == StatutFacture.BROUILLON || statut == StatutFacture.ANNULEE) {
            return; // Ne pas changer ces statuts automatiquement
        }

        if (soldeRestant.compareTo(BigDecimal.ZERO) <= 0) {
            statut = StatutFacture.PAYEE;
            datePaiementComplet = new Date();
        } else if (montantPaye.compareTo(BigDecimal.ZERO) > 0) {
            statut = StatutFacture.PARTIELLEMENT_PAYEE;
        } else if (isEnRetard()) {
            statut = StatutFacture.EN_RETARD;
        } else {
            statut = StatutFacture.NON_PAYEE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facture)) return false;
        Facture facture = (Facture) o;
        return id != null && id.equals(facture.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Facture[id=%d, numero=%s, client=%s, totalTTC=%s, statut=%s]",
                id, numeroFacture,
                client != null ? client.getNom() : "N/A",
                totalTtc, statut);
    }
}