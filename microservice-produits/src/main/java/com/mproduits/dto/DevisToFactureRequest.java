/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DevisToFactureRequest {
    
    @NotNull(message = "L'ID du devis est obligatoire")
    private Long devisId;
    
    /**
     * Date de la facture (par défaut: date du jour)
     */
    private Date dateFacture;
    
    /**
     * Mode de paiement
     */
    private ModePaiement modePaiementDefaut;
    
    /**
     * Permet de modifier les articles avant conversion (optionnel)
     */
    @Valid
    private List<FactureItemRequest> items;
     private String username;
}
