/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureUpdateRequest {
    
    @NotNull(message = "L'ID de la facture est obligatoire")
    private Long id;
    
    private Date dateFacture;
    private Date dateEcheance;
    private ModePaiement modePaiementDefaut;
    private Integer delaiPaiementJours;
    
    @Valid
    private List<FactureItemRequest> items;
    
    @Size(max = 5000)
    private String remarques;
    
    @Size(max = 5000)
    private String conditionsPaiement;
    
    @Size(max = 100)
    private String referenceExterne;
     private String username;
    
}
