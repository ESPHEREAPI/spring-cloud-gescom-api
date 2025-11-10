/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.TypeNotification;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.*;
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
public class NotificationSendRequest {
    
    /**
     * ID du client destinataire (obligatoire)
     */
    @NotNull(message = "Le client est obligatoire")
    private Long clientId;
    
    /**
     * Type de notification (obligatoire)
     */
    @NotNull(message = "Le type de notification est obligatoire")
    private TypeNotification typeNotification;
    
    /**
     * Catégorie de notification (obligatoire)
     */
    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String categorie;
    
    /**
     * Titre/Objet de la notification
     */
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String titre;
    
    /**
     * Message de la notification (obligatoire)
     */
    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 5000, message = "Le message ne peut pas dépasser 5000 caractères")
    private String message;
    
    /**
     * Message HTML (pour emails)
     */
    @Size(max = 10000, message = "Le message HTML ne peut pas dépasser 10000 caractères")
    private String messageHtml;
    
    /**
     * ID de la facture concernée (optionnel)
     */
    private Long factureId;
    
    /**
     * ID du versement concerné (optionnel)
     */
    private Long versementId;
    
    /**
     * Priorité (1=haute, 2=normale, 3=basse)
     */
    @Min(value = 1, message = "La priorité doit être entre 1 et 3")
    @Max(value = 3, message = "La priorité doit être entre 1 et 3")
    private Integer priorite;
    
    /**
     * Date prévue d'envoi (pour notification programmée)
     */
    private Date datePrevueEnvoi;
    
    /**
     * Données supplémentaires (optionnel)
     */
    private Map<String, Object> donneesSupplementaires;
}
