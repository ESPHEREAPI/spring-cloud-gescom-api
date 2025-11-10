/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureDetailsDTO {
    private Long id;
    private String numeroFacture;
    private String clientNom;
    private BigDecimal montantTTC;
    private BigDecimal montantPaye;
    private String statut;
    private Date dateFacture;
    
}
