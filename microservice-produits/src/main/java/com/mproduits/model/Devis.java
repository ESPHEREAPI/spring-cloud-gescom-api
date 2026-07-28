package com.mproduits.model;

import com.mproduits.enums.StatutDevis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entité Devis - Gestion complète des devis clients
 * 
 * CYCLE DE VIE:
 * 1. EN_ATTENTE   → Devis créé, en attente de validation client
 * 2. ACCEPTE      → Client accepte, prêt pour conversion en facture
 * 3. REFUSE       → Client refuse le devis
 * 4. CONVERTI     → Devis converti en facture
 * 5. EXPIRE       → Dépassement de la date de validité
 * 6. ANNULE       → Annulation par le vendeur
 * 
 * RÈGLES MÉTIER:
 * - Un devis EN_ATTENTE peut être modifié
 * - Un devis ACCEPTE peut être converti en facture
 * - Un devis REFUSE/EXPIRE/ANNULE ne peut plus être modifié
 * - Un devis CONVERTI est lié à une facture
 * - Notification automatique 3 jours avant expiration
 * 
 * @author USER01
 * @version 2.0
 */
@Entity
@Table(name = "devis", indexes = {
    @Index(name = "idx_devis_client", columnList = "client_id"),
    @Index(name = "idx_devis_statut", columnList = "statut"),
    @Index(name = "idx_devis_date", columnList = "date_devis DESC"),
    @Index(name = "idx_devis_validite", columnList = "date_expiration")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Devis implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== IDENTIFIANT ==========
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Numéro unique du devis (Format: DEV-YYYY-NNNN)
     * Exemple: DEV-2025-0001
     */
    @Column(name = "numero_devis", unique = true, length = 20)
    private String numeroDevis;

    // ========== INFORMATIONS TEMPORELLES ==========
    
    /**
     * Date de création du devis
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_devis", nullable = false)
    private Date dateDevis;

    /**
     * Date d'expiration du devis (par défaut: dateDevis + 30 jours)
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_expiration")
    private Date dateExpiration;

    /**
     * Validité en jours (pour calcul dynamique)
     */
    @Column(name = "validite_jours")
    private Integer validiteJours = 30;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Date dateCreation;
    
      @JoinColumn(name = "Boutiqueid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Boutique boutique;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_modification")
    private Date dateModification;

    /**
     * Date d'acceptation du devis
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_acceptation")
    private Date dateAcceptation;

    /**
     * Date de conversion en facture
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_conversion")
    private Date dateConversion;

    // ========== MONTANTS FINANCIERS ==========
    
    /**
     * Montant total HT (Hors Taxes)
     */
    @Column(name = "montant_ht", precision = 12, scale = 2)
    private BigDecimal montantHT = BigDecimal.ZERO;

    /**
     * Montant total des remises
     */
    @Column(name = "total_remise", precision = 12, scale = 2)
    private BigDecimal totalRemise = BigDecimal.ZERO;

    /**
     * Taux de TVA appliqué (en pourcentage)
     */
    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tauxTVA = BigDecimal.ZERO;

    /**
     * Montant total de la TVA
     */
    @Column(name = "total_tva", precision = 12, scale = 2)
    private BigDecimal totalTVA = BigDecimal.ZERO;

    /**
     * Montant total TTC (Toutes Taxes Comprises)
     */
    @Column(name = "total_ttc", precision = 12, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Indique si la TVA doit être appliquée
     */
    @Column(name = "appliquer_tva")
    private Boolean appliquerTVA = true;

    // ========== STATUT ET ÉTAT ==========
    
    /**
     * Statut du devis
     * Valeurs possibles: EN_ATTENTE, ACCEPTE, REFUSE, CONVERTI, EXPIRE, ANNULE
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatutDevis statut = StatutDevis.EN_ATTENTE;

    /**
     * Version du devis (pour gestion des modifications)
     */
    @Version
    @Column(name = "version")
    private Integer version = 0;

    /**
     * Indique si le devis a été envoyé au client
     */
    @Column(name = "envoye_client")
    private Boolean envoyeClient = false;

    /**
     * Date d'envoi au client
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_envoi")
    private Date dateEnvoi;

    // ========== RELATIONS ==========
    
    /**
     * Client associé au devis
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Articles du devis
     */
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DevisItem> items = new ArrayList<>();

    /**
     * Facture générée à partir de ce devis (si converti)
     */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "devis")
    private Facture facture;

    // ========== INFORMATIONS COMPLÉMENTAIRES ==========
    
    /**
     * Remarques ou notes sur le devis
     */
    @Column(name = "remarques", columnDefinition = "TEXT")
    private String remarques;

    /**
     * Conditions commerciales spécifiques
     */
    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    /**
     * Motif de refus (si statut = REFUSE)
     */
    @Column(name = "motif_refus", columnDefinition = "TEXT")
    private String motifRefus;

    /**
     * Motif d'annulation (si statut = ANNULE)
     */
    @Column(name = "motif_annulation", columnDefinition = "TEXT")
    private String motifAnnulation;

    // ========== TRAÇABILITÉ ==========
    
    /**
     * Utilisateur ayant créé le devis
     */
    @Column(name = "username_create", nullable = false, length = 100)
    private String usernameCreate;

    /**
     * Utilisateur ayant modifié le devis
     */
    @Column(name = "username_update", length = 100)
    private String usernameUpdate;

    /**
     * Utilisateur ayant validé le devis
     */
    @Column(name = "username_validation", length = 100)
    private String usernameValidation;

   

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Ajoute un article au devis
     */
    public void addItem(DevisItem item) {
        items.add(item);
        item.setDevis(this);
    }
//     public void updateItem(DevisItem item) {
//        items.get(0)
//        item.setDevis(this);
//    }

    /**
     * Retire un article du devis
     */
    public void removeItem(DevisItem item) {
        items.remove(item);
        item.setDevis(null);
    }

    /**
     * Vérifie si le devis peut être modifié
     */
    public boolean isModifiable() {
        return statut == StatutDevis.EN_ATTENTE;
    }

    /**
     * Vérifie si le devis peut être accepté
     */
    public boolean isPeutEtreAccepte() {
        return statut == StatutDevis.EN_ATTENTE && !isExpire();
    }

    /**
     * Vérifie si le devis est expiré
     */
    public boolean isExpire() {
        if (dateExpiration == null) {
            return false;
        }
        return new Date().after(dateExpiration);
    }

    /**
     * Vérifie si le devis peut être converti en facture
     */
    public boolean isPeutEtreConverti() {
        return statut == StatutDevis.ACCEPTE && facture == null;
    }

    /**
     * Calcule le nombre de jours avant expiration
     */
    public long getJoursAvantExpiration() {
        if (dateExpiration == null) {
            return -1;
        }
        long diff = dateExpiration.getTime() - new Date().getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * Vérifie si le devis est proche de l'expiration (< 3 jours)
     */
    public boolean isProcheExpiration() {
        long jours = getJoursAvantExpiration();
        return jours >= 0 && jours <= 3 && statut == StatutDevis.EN_ATTENTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Devis)) return false;
        Devis devis = (Devis) o;
        return id != null && id.equals(devis.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Devis[id=%d, numero=%s, client=%s, total=%s, statut=%s]",
                id, numeroDevis, 
                client != null ? client.getNom() : "N/A",
                total, statut);
    }
}