/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class NotificationFactureRequest {
    
    @NotNull(message = "La facture est obligatoire")
    private Long factureId;
    
    /**
     * Type de notification
     * Valeurs: FACTURE_CREEE, RAPPEL_PAIEMENT, FACTURE_RETARD, etc.
     */
    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;
    
    /**
     * Canaux d'envoi (EMAIL, SMS, WHATSAPP, SYSTEME)
     */
    @NotEmpty(message = "Au moins un canal d'envoi est requis")
    private java.util.List<TypeNotification> canaux;   
}
