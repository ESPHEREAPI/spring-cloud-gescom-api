/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
public class PrixDto {
     private Long produitId;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private BigDecimal prixVenteTTC;
    private String devise;
    
    public PrixDto(Long produitId, BigDecimal prixAchat, BigDecimal prixVente, BigDecimal prixVenteTTC) {
        this.produitId = produitId;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.prixVenteTTC = prixVenteTTC;
    }
}
