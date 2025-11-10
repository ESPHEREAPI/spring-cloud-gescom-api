/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Builder
@Data

@NoArgsConstructor
@AllArgsConstructor
public class VersementValidationRequest {
    
    @NotNull(message = "L'ID du versement est obligatoire")
    private Long versementId;
    
    /**
     * Date de validation (par défaut: maintenant)
     */
    private Date dateValidation;
    
    /**
     * Commentaire de validation
     */
    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    private String commentaire;
    private String username;
}
