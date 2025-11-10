/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.StatutFacture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class FactureSummary {
    
    private Long id;
    private String numeroFacture;
    private Date dateFacture;
    private Date dateEcheance;
    private BigDecimal totalTtc;
    private BigDecimal montantPaye;
    private BigDecimal soldeRestant,montantRestant;
    private StatutFacture statut;
    private String statutLibelle;
   private ClientDto client;
    private Boolean enRetard;
    private Long joursRetard;
    private Integer nombreArticles;    
   


}
