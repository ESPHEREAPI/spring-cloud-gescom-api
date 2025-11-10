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
import java.util.Date;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
 public class FactureSearchCriteria {
    
    private String numeroFacture;
    private Long clientId;
    private StatutFacture statut;
    private Date dateFactureDebut;
    private Date dateFactureFin;
    private Date dateEcheanceDebut;
    private Date dateEcheanceFin;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private Boolean enRetard;
    private String referenceExterne;
    
    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;    
}
