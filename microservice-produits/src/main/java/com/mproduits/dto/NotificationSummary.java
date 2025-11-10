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
import java.util.Date;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummary {
    
    private Long id;
    private TypeNotification typeNotification;
    private String categorie;
    private String destinataire;
    private Date dateEnvoi;
    private String statut;
    private String clientNom;
    private String factureNumero;
    
}
