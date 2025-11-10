/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

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
public class HistoriqueNotificationClient {
    
    private Long clientId;
    private String clientNom;
    
    private Long totalNotifications;
    private Long notificationsEnvoyees;
    private Long notificationsEchec;
    private Long notificationsLues;
    
    private Double tauxSucces;
    private Double tauxLecture;
    
    private java.util.List<NotificationSummary> dernieresNotifications;    
}
