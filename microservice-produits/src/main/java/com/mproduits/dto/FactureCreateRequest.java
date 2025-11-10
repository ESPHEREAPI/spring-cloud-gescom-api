/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

/**
 *
 * @author USER01
 */
import com.mproduits.enums.ModePaiement;
import com.mproduits.enums.StatutFacture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTOs pour la gestion des factures
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */

// ========== REQUEST DTOs ==========

/**
 * DTO pour la création d'une facture
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FactureCreateRequest {
    
    /**
     * ID du client (obligatoire)
     */
    @NotNull(message = "Le client est obligatoire")
    private Long clientId;
    
    /**
     * ID du devis source (optionnel, null si création directe)
     */
    private Long devisId;
    
    /**
     * Date de la facture (par défaut: date du jour)
     */
    private Date dateFacture;
    
    /**
     * Date d'échéance (par défaut: dateFacture + délai client)
     */
    private Date dateEcheance;
    
    /**
     * Mode de paiement par défaut
     */
    private ModePaiement modePaiementDefaut;
    
    /**
     * Délai de paiement en jours
     */
    @Min(value = 0, message = "Le délai de paiement doit être positif")
    private Integer delaiPaiementJours;
    
    /**
     * Articles de la facture
     */
    @Valid
    @NotEmpty(message = "La facture doit contenir au moins un article")
    private List<FactureItemRequest> items = new ArrayList<>();
    
    /**
     * Remarques
     */
    @Size(max = 5000, message = "Les remarques ne peuvent pas dépasser 5000 caractères")
    private String remarques;
    
    /**
     * Conditions de paiement
     */
    @Size(max = 5000, message = "Les conditions ne peuvent pas dépasser 5000 caractères")
    private String conditionsPaiement;
    
    /**
     * Référence externe
     */
    @Size(max = 100, message = "La référence externe ne peut pas dépasser 100 caractères")
    private String referenceExterne;
     private String username;
}

