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
public class CommandeDto {
    
    private BigDecimal prixAchat;

    private BigDecimal prixVente;
    private BigDecimal quantite;
    private Long produitid;
    private Long depotid;
    private Long fournisseurid;
    private String codeBarre;

    private String reference;
    private String libelle;
}
