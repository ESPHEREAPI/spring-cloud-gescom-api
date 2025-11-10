/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

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
public class NotificationEnvoiResult {
    
    private Long notificationId;
    private Boolean succes;
    private String message;
    private String codeReponse;
    private String idExterne;
    private Date dateEnvoi;   
}
