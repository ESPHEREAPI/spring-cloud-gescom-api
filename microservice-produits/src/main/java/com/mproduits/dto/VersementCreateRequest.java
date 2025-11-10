/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Builder
@Data

@NoArgsConstructor
@AllArgsConstructor
public class VersementCreateRequest {
     
    /**
     * ID de la facture concernée (obligatoire)
     */
    @NotNull(message = "La facture est obligatoire")
    private Long factureId;
    
    /**
     * Montant du versement (obligatoire)
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;
    
    /**
     * Date du versement (par défaut: maintenant)
     */
    private Date dateVersement;
    
    /**
     * Mode de paiement (obligatoire)
     */
    @NotNull(message = "Le mode de paiement est obligatoire")
    private ModePaiement modePaiement;
    
    /**
     * Référence du paiement
     * Obligatoire pour certains modes (chèque, virement, mobile money)
     */
    @Size(max = 100, message = "La référence ne peut pas dépasser 100 caractères")
    private String referencePaiement;
    
    /**
     * Banque émettrice (pour chèque ou virement)
     */
    @Size(max = 100, message = "Le nom de la banque ne peut pas dépasser 100 caractères")
    private String banque;
    
    /**
     * Numéro de compte (si applicable)
     */
    @Size(max = 50, message = "Le numéro de compte ne peut pas dépasser 50 caractères")
    private String numeroCompte;
    
    /**
     * Remarques
     */
    @Size(max = 1000, message = "Les remarques ne peuvent pas dépasser 1000 caractères")
    private String remarques;
    private String username;
}
