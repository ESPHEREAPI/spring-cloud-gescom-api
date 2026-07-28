/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;


/**
 *
 * @author USER01
 */
@Data
public class CommandeRequest {

    private Long produitid;

    private Long depotid;
    private Long fournisseurid;

    private BigDecimal quantite;

    private BigDecimal prixAchat;

    private BigDecimal prixVente;

    private String barcode;
    private String reference;
    private String libelle;
    private Date dateReception;
    private String numeroRepartition;
    private String usercreate;
  

}
