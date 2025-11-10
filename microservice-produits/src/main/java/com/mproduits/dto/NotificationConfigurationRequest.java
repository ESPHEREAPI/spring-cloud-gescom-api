/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.TypeNotification;
import jakarta.validation.constraints.Min;
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
public class NotificationConfigurationRequest {
    
    /**
     * Activer les notifications automatiques
     */
    private Boolean activer;
    
    /**
     * Jours avant échéance pour les relances
     * Exemple: [7, 3, 1]
     */
    private java.util.List<Integer> joursRelances;
    
    /**
     * Canaux d'envoi par défaut
     */
    private java.util.List<TypeNotification> canauxDefaut;
    
    /**
     * Envoyer notification à la création de facture
     */
    private Boolean notifierCreationFacture;
    
    /**
     * Envoyer notification à la réception de paiement
     */
    private Boolean notifierPaiementRecu;
    
    /**
     * Envoyer notification quand facture soldée
     */
    private Boolean notifierFactureSoldee;
    
    /**
     * Nombre de jours de retard avant notification
     */
    @Min(value = 0)
    private Integer joursRetardNotification;
    
}
