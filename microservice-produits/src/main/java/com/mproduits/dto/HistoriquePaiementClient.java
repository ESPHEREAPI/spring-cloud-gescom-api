/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HistoriquePaiementClient {
     private Long clientId;
    private String clientNom;
    
    private Long nombreFactures;
    private Long nombreVersements;
    
    private BigDecimal montantTotalFactures;
    private BigDecimal montantTotalPaye;
    private BigDecimal montantTotalImpaye;
    
    private BigDecimal tauxRecouvrement;
    private BigDecimal delaiMoyenPaiement; // En jours
    
    private java.util.List<VersementSummary> derniersVersements;
}
