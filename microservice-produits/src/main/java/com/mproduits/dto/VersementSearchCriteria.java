/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VersementSearchCriteria {
    private String numeroVersement;
    private Long factureId;
    private Long clientId;
    private ModePaiement modePaiement;
    private String statut;
    private Date dateVersementDebut;
    private Date dateVersementFin;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private String referencePaiement;
    
    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
  
    private java.time.LocalDate dateDebut;
    private java.time.LocalDate dateFin;
    private Long boutiqueid;
    
}
