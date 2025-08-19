/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.math.BigInteger;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class StockDto {
    private Long produitId;
    private String produitLibelle;
    private String produitReference;
    private Long depotId;
    private String depotNom;
    private Long boutiqueId;
    private String boutiqueNom;
    private BigInteger stockActuel;
    private BigInteger stockInitial;
    private BigInteger entree;
    private BigInteger sortie;
    
    public StockDto(Long produitId, String produitLibelle, String produitReference,
                   Long depotId, String depotNom, BigInteger stockActuel) {
        this.produitId = produitId;
        this.produitLibelle = produitLibelle;
        this.produitReference = produitReference;
        this.depotId = depotId;
        this.depotNom = depotNom;
        this.stockActuel = stockActuel;
    }
}
