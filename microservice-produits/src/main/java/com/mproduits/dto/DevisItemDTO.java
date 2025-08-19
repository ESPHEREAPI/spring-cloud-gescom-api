/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class DevisItemDTO {
     private Long produitId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal remise;
}
