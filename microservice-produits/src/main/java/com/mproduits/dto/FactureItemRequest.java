/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureItemRequest {
    
    /**
     * ID du produit
     */
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;
    
    /**
     * Quantité
     */
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
    
    /**
     * Prix unitaire HT
     */
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaireHT;
    
    /**
     * Taux de remise en pourcentage
     */
    @DecimalMin(value = "0.0", message = "Le taux de remise doit être positif")
    @DecimalMax(value = "100.0", message = "Le taux de remise ne peut pas dépasser 100%")
    private BigDecimal tauxRemise;
    
    /**
     * Type de remise (POURCENTAGE ou MONTANT_FIXE)
     */
    private String typeRemise;
    
    /**
     * Taux de TVA
     */
    @NotNull(message = "Le taux de TVA est obligatoire")
    @DecimalMin(value = "0.0", message = "Le taux de TVA doit être positif")
    private BigDecimal tauxTVA;
    
    /**
     * Description personnalisée
     */
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;
    
    /**
     * Commentaire
     */
    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    private String commentaire;
    
    /**
     * Ordre d'affichage
     */
    private Integer ordre;
    
}
