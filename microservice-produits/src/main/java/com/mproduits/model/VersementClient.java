package com.mproduits.model;

import com.mproduits.enums.ModePaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entité VersementClient - Gestion des paiements clients
 * 
 * RESPONSABILITÉS:
 * - Enregistrer chaque paiement effectué par un client
 * - Lier les paiements aux factures concernées
 * - Tracer les modes et références de paiement
 * - Calculer automatiquement le solde restant des factures
 * 
 * RÈGLES MÉTIER:
 * - Un versement est toujours lié à une facture
 * - Le montant d'un versement ne peut pas dépasser le solde de la facture
 * - Un versement validé ne peut plus être modifié (sauf annulation)
 * - La somme des versements d'une facture = montant payé
 * - Mise à jour automatique du statut de la facture après chaque versement
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 2.0
 */
@Entity
@Table(name = "versement_client", indexes = {
    @Index(name = "idx_versement_client", columnList = "client_id"),
    @Index(name = "idx_versement_facture", columnList = "facture_id"),
    @Index(name = "idx_versement_date", columnList = "date_versement DESC"),
    @Index(name = "idx_versement_reference", columnList = "reference_paiement")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersementClient implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== IDENTIFIANT ==========
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Numéro unique du versement (Format: VERS-YYYY-NNNN)
     * Exemple: VERS-2025-0001
     */
    @Column(name = "numero_versement", unique = true, length = 20)
    private String numeroVersement;

    // ========== DATES ==========
    
    /**
     * Date et heure du versement
     */
    @Column(name = "date_versement", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateVersement;

    @CreationTimestamp
    @Column(name = "date_enregistrement", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnregistrement;

    // ========== MONTANT ==========
    
    /**
     * Montant du versement
     */
    @Column(name = "montant", precision = 12, scale = 2, nullable = false)
    private BigDecimal montant;

    // ========== MODE DE PAIEMENT ==========
    
    /**
     * Mode de paiement utilisé
     */
    @Column(name = "mode_paiement", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement;

    /**
     * Référence du paiement
     * Obligatoire pour certains modes (chèque, virement, mobile money)
     * Exemples:
     * - Numéro de chèque
     * - Référence de virement
     * - Transaction ID Mobile Money
     */
    @Column(name = "reference_paiement", length = 100)
    private String referencePaiement;

    /**
     * Banque émettrice (pour chèque ou virement)
     */
    @Column(name = "banque", length = 100)
    private String banque;

    /**
     * Numéro de compte (si applicable)
     */
    @Column(name = "numero_compte", length = 50)
    private String numeroCompte;

    // ========== RELATIONS ==========
    
    /**
     * Client ayant effectué le versement
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Facture concernée par le versement
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    // ========== STATUT ==========
    
    /**
     * Statut du versement
     * Valeurs: EN_ATTENTE, VALIDE, ANNULE, REJETE
     */
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private String statut = "EN_ATTENTE";

    /**
     * Date de validation du versement
     */
    @Column(name = "date_validation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValidation;

    /**
     * Date d'annulation du versement
     */
    @Column(name = "date_annulation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAnnulation;

    // ========== INFORMATIONS COMPLÉMENTAIRES ==========
    
    /**
     * Remarques ou notes sur le versement
     */
    @Column(name = "remarques", columnDefinition = "TEXT")
    private String remarques;

    /**
     * Motif d'annulation (si statut = ANNULE)
     */
    @Column(name = "motif_annulation", columnDefinition = "TEXT")
    private String motifAnnulation;

    /**
     * Reçu de paiement généré (nom du fichier)
     */
    @Column(name = "recu_paiement", length = 255)
    private String recuPaiement;

    // ========== TRAÇABILITÉ ==========
    
    /**
     * Utilisateur ayant enregistré le versement
     */
    @Column(name = "username_create", nullable = false, length = 100)
    private String usernameCreate;

    /**
     * Utilisateur ayant validé le versement
     */
    @Column(name = "username_validation", length = 100)
    private String usernameValidation;

    /**
     * Utilisateur ayant annulé le versement
     */
    @Column(name = "username_annulation", length = 100)
    private String usernameAnnulation;

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Vérifie si le versement peut être modifié
     */
    public boolean isModifiable() {
        return "EN_ATTENTE".equals(statut);
    }

    /**
     * Vérifie si le versement peut être validé
     */
    public boolean isPeutEtreValide() {
        return "EN_ATTENTE".equals(statut) && montant != null && montant.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Vérifie si le versement peut être annulé
     */
    public boolean isPeutEtreAnnule() {
        return "VALIDE".equals(statut) || "EN_ATTENTE".equals(statut);
    }

    /**
     * Vérifie si le versement est validé
     */
    public boolean isValide() {
        return "VALIDE".equals(statut);
    }

    /**
     * Vérifie si la référence de paiement est obligatoire
     */
    public boolean isReferenceObligatoire() {
        return modePaiement != null && modePaiement.isReferenceObligatoire();
    }

    /**
     * Valide les données du versement
     */
    public boolean isVersementValide() {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (modePaiement == null) {
            return false;
        }
        if (isReferenceObligatoire() && (referencePaiement == null || referencePaiement.trim().isEmpty())) {
            return false;
        }
        return facture != null && client != null;
    }

    /**
     * Retourne le montant formaté
     */
    public String getMontantFormate() {
        return String.format("%.2f XAF", montant);
    }

    /**
     * Génère une description courte du versement
     */
    public String getDescription() {
        return String.format("%s - %s - %s",
                numeroVersement != null ? numeroVersement : "N/A",
                modePaiement != null ? modePaiement.getLibelle() : "N/A",
                getMontantFormate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersementClient)) return false;
        VersementClient that = (VersementClient) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("VersementClient[id=%d, numero=%s, montant=%s, mode=%s, statut=%s]",
                id, numeroVersement, montant,
                modePaiement != null ? modePaiement.getLibelle() : "N/A",
                statut);
    }
}