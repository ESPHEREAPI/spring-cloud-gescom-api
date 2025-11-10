/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */

    
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FactureAnnulationRequest {
    
    @NotNull(message = "L'ID de la facture est obligatoire")
    private Long factureId;
    
    /**
     * Motif d'annulation (obligatoire)
     */
    @NotBlank(message = "Le motif d'annulation est obligatoire")
    @Size(max = 1000, message = "Le motif ne peut pas dépasser 1000 caractères")
    private String motifAnnulation;
    private String username;
}
