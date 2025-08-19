/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.util.Date;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class NotificationClientDTO {
     private Long id;
    private String type; // EMAIL, SMS
    private String message;
    private Date dateEnvoi;
    private String statut;
    private Long clientId;
    
}
