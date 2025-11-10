/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.model.Facture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RapportClientData {
    private Long clientId;
    private String clientCode;
    private String clientNom;
    private String clientEmail;
    private String clientTelephone;
    
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateGeneration;
    
    private List<VersementResponse> versements;
    private List<Facture> factures;
    
    private BigDecimal totalVersements;
    private BigDecimal totalValides;
    private BigDecimal totalEnAttente;
    private BigDecimal totalAnnules;
    private BigDecimal soldeTotal;
    
    private Integer nombreVersements;
    private BigDecimal montantMoyenVersement;
}
