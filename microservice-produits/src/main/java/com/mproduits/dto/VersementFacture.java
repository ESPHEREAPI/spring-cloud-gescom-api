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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersementFacture {

    private Long id;
    private String numeroFacture;
    private BigDecimal montantTTC;
    private BigDecimal montantRestant;
}
