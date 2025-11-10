/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import com.mproduits.enums.StatutVersement;
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
public class VersementSummary {
    private Long id;
    private String numeroVersement;
    private Date dateVersement;
    private BigDecimal montant;
    private ModePaiement modePaiement;
    private String referencePaiement;
    private StatutVersement statut;
    private String factureNumero;
    private String clientNom;
}
