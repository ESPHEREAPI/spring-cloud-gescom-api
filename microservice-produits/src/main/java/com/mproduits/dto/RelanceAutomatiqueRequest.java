/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.TypeNotification;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelanceAutomatiqueRequest {
    
    /**
     * Nombre de jours avant échéance pour envoyer la relance
     * Valeurs courantes: 7, 3, 1
     */
    @Min(value = 0, message = "Le nombre de jours doit être positif")
    private Integer joursAvantEcheance;
    
    /**
     * Canaux d'envoi
     */
    @NotEmpty (message = "Au moins un canal d'envoi est requis")
    private java.util.List<TypeNotification> canaux;
    
    /**
     * Filtrer par client (optionnel)
     */
    private Long clientId;   
}
