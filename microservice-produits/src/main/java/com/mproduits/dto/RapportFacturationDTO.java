/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.util.List;
import lombok.*;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
/**
 * DTO pour rapport/statistique
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RapportFacturationDTO {
    private int totalFactures;
    private BigDecimal totalMontantTTC;
    private BigDecimal montantPaye;
    private BigDecimal montantRestant;
    private String periode;
    private List<FactureDetailsDTO> factures;
    
}
