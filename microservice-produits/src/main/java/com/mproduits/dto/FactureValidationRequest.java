/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FactureValidationRequest {
    
    @NotNull(message = "L'ID de la facture est obligatoire")
    private Long factureId;
    
    /**
     * Date de validation (par défaut: maintenant)
     */
    private Date dateValidation;
    
    /**
     * Commentaire de validation
     */
    @Size(max = 500)
    private String commentaire;   
    private String username;
}
