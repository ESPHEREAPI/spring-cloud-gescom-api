/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.TypeNotification;
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
public class NotificationResponse {
    
    private Long id;
    private TypeNotification typeNotification;
    private String typeLibelle;
    private String categorie;
    
    // Contenu
    private String titre;
    private String message;
    private String messageHtml;
    
    // Destinataire
    private String destinataire;
    private String nomDestinataire;
    
    // Dates
    private Date dateCreation;
    private Date dateEnvoi;
    private Date dateLecture;
    private Date datePrevueEnvoi;
    
    // Statut
    private String statut;
    private Integer tentatives;
    private Integer maxTentatives;
    
    // Résultat d'envoi
    private String codeReponse;
    private String messageErreur;
    private String idExterne;
    
    // Relations
    private Long clientId;
    private String clientNom;
    private Long factureId;
    private String factureNumero;
    private Long versementId;
    private String versementNumero;
    
    // Informations complémentaires
    private Integer priorite;
    private Map<String, Object> donneesSupplementaires;
    
    // Traçabilité
    private String usernameCreate;
    
}
