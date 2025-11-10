package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO pour l'entité Devis
 * Utilisé pour les échanges avec le frontend
 * 
 * VALIDATIONS:
 * - clientId obligatoire
 * - Au moins 1 article requis
 * - Montants >= 0
 * - Taux TVA entre 0 et 100
 * 
 * @author USER01
 * @version 2.0
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DevisDTO {

    // ========== IDENTIFIANT ==========
    
    /**
     * ID du devis (null lors de la création)
     */
    private Long id;

    /**
     * Numéro unique du devis (généré automatiquement)
     * Format: DEV-2025-0001
     */
    private String numeroDevis;

    /**
     * Version du devis (pour gestion optimiste)
     */
    private Integer version;

    // ========== INFORMATIONS CLIENT ==========
    
    /**
     * ID du client (obligatoire)
     */
//    @NotNull(message = "Le client est obligatoire")
//    @Positive(message = "L'ID du client doit être positif")
//    private Long clientId;
//
//    /**
//     * Nom du client (pour affichage)
//     */
//    private String clientNom;
//
//    /**
//     * Email du client
//     */
//    private String clientEmail;
//
//    /**
//     * Téléphone du client
//     */
//    private String clientTelephone;
    private ClientDto client;

    // ========== DATES ==========
    
    /**
     * Date du devis
     */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateDevis;

    /**
     * Date d'expiration
     */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateExpiration;

    /**
     * Validité en jours (défaut: 30)
     */
    @Min(value = 1, message = "La validité doit être au moins de 1 jour")
    @Max(value = 365, message = "La validité ne peut pas dépasser 365 jours")
    @Builder.Default
    private Integer validiteJours = 30;

    /**
     * Date de création
     */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateCreation;

    /**
     * Date de dernière modification
     */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateModification;

    /**
     * Date d'acceptation
     */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateAcceptation;

    /**
     * Date de conversion en facture
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateConversion;

    // ========== MONTANTS ==========
    
    /**
     * Montant HT
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant HT doit être >= 0")
    @Builder.Default
    private BigDecimal montantHT = BigDecimal.ZERO;

    /**
     * Total des remises
     */
    @DecimalMin(value = "0.0", inclusive = true)
    @Builder.Default
    private BigDecimal totalRemise = BigDecimal.ZERO;

    /**
     * Taux de TVA (en pourcentage)
     */
    @DecimalMin(value = "0.0", message = "Le taux de TVA doit être >= 0")
    @DecimalMax(value = "100.0", message = "Le taux de TVA doit être <= 100")
    @Builder.Default
    private BigDecimal tauxTVA = new BigDecimal("19.25");

    /**
     * Montant total TVA
     */
    @DecimalMin(value = "0.0", inclusive = true)
    @Builder.Default
    private BigDecimal totalTVA = BigDecimal.ZERO;

    /**
     * Montant total TTC
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant total doit être >= 0")
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Indique si TVA doit être appliquée
     */
    @Builder.Default
    private boolean appliquerTVA = true;

    // ========== STATUT ==========
    
    /**
     * Statut du devis
     * Valeurs: EN_ATTENTE, ACCEPTE, REFUSE, CONVERTI, EXPIRE, ANNULE
     */
    private String statut;

    /**
     * Libellé du statut (pour affichage)
     */
    private String statutLibelle;

    /**
     * Indique si le devis peut être modifié
     */
    private boolean modifiable;

    /**
     * Indique si le devis peut être accepté
     */
    private boolean peutEtreAccepte;

    /**
     * Indique si le devis peut être converti en facture
     */
    private boolean peutEtreConverti;

    /**
     * Indique si le devis est expiré
     */
    private boolean expire;

    /**
     * Indique si le devis est proche de l'expiration
     */
    private boolean procheExpiration;

    /**
     * Nombre de jours avant expiration
     */
    private Long joursAvantExpiration;

    // ========== ARTICLES ==========
    
    /**
     * Liste des articles du devis
     */
    @NotEmpty(message = "Au moins un article est requis")
    @Valid
    @Builder.Default
    private List<DevisItemDTO> items = new ArrayList<>();

    /**
     * Nombre total d'articles
     */
    private Integer nombreArticles;

    // ========== INFORMATIONS COMPLÉMENTAIRES ==========
    
    /**
     * Remarques ou notes
     */
    @Size(max = 5000, message = "Les remarques ne peuvent pas dépasser 5000 caractères")
    private String remarques;

    /**
     * Conditions commerciales
     */
    @Size(max = 5000, message = "Les conditions ne peuvent pas dépasser 5000 caractères")
    private String conditions;

    /**
     * Motif de refus (si statut = REFUSE)
     */
    private String motifRefus;

    /**
     * Motif d'annulation (si statut = ANNULE)
     */
    private String motifAnnulation;

    // ========== FACTURE LIÉE ==========
    
    /**
     * ID de la facture générée (si converti)
     */
    private Long factureId;

    /**
     * Numéro de la facture générée
     */
    private String factureNumero;

    // ========== TRAÇABILITÉ ==========
    
    /**
     * Utilisateur ayant créé le devis
     */
    private String usernameCreate;

    /**
     * Utilisateur ayant modifié le devis
     */
    private String usernameUpdate;

    /**
     * Utilisateur ayant validé le devis
     */
    private String usernameValidation;

    // ========== OPTIONS ==========
    
    /**
     * Indique si le stock doit être vérifié lors de la création
     */
    @Builder.Default
    private boolean verifierStock = true;

    /**
     * Indique si le devis a été envoyé au client
     */
    private Boolean envoyeClient;

    /**
     * Date d'envoi au client
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Douala")
    private Date dateEnvoi;

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Retourne le montant total formaté
     */
    public String getTotalFormate() {
        return String.format("%.2f XAF", total);
    }

    /**
     * Retourne le montant HT formaté
     */
    public String getMontantHTFormate() {
        return String.format("%.2f XAF", montantHT);
    }

    /**
     * Retourne le pourcentage de remise globale
     */
    public BigDecimal getPourcentageRemise() {
        if (montantHT == null || montantHT.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal montantBrut = montantHT.add(totalRemise != null ? totalRemise : BigDecimal.ZERO);
        if (montantBrut.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalRemise.multiply(new BigDecimal("100"))
                .divide(montantBrut, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Ajoute un article au devis
     */
    public void addItem(DevisItemDTO item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    /**
     * Retire un article du devis
     */
    public void removeItem(DevisItemDTO item) {
        if (items != null) {
            items.remove(item);
        }
    }

    /**
     * Recalcule le nombre d'articles
     */
    public void updateNombreArticles() {
        this.nombreArticles = (items != null) ? items.size() : 0;
    }
}