/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureItemResponse {
    
    private Long id;
    private Long produitId;
    private String produitCode;
    private String produitLibelle;
    private String description;
    private Integer quantite;
    private BigDecimal prixUnitaireHT;
    private BigDecimal tauxRemise;
    private BigDecimal montantRemise;
    private BigDecimal tauxTVA;
    private BigDecimal montantTVA;
    private BigDecimal montantHT;
    private BigDecimal montantTTC;
    private BigDecimal prixUnitaireTTC;
    private String commentaire;
    private Integer ordre; 
    private ProduitDto produit;
}
