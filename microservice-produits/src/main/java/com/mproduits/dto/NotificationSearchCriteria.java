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

public class NotificationSearchCriteria {
    
    private Long clientId;
    private Long factureId;
    private TypeNotification typeNotification;
    private String categorie;
    private String statut;
    private Date dateCreationDebut;
    private Date dateCreationFin;
    private Date dateEnvoiDebut;
    private Date dateEnvoiFin;
    private String destinataire;
    private Integer priorite;
    
    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
    
}
