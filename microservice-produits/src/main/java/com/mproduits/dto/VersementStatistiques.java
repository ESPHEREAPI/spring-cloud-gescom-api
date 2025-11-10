/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VersementStatistiques {
    
    // Compteurs
    private Long totalVersements;
    private Long versementsEnAttente;
    private Long versementsValides;
    private Long versementsAnnules;
    
    // Montants
    private BigDecimal montantTotalVersements;
    private BigDecimal montantEnAttente;
    private BigDecimal montantValide;
    
    // Par mode de paiement
    private java.util.Map<ModePaiement, BigDecimal> montantsParMode;
    private java.util.Map<ModePaiement, Long> nombreParMode;
    
    // Période
    private Date dateDebut;
    private Date dateFin;
    
}
