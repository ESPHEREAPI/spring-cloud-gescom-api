/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.model.Categories;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProduitDto {
     private Long id;
    private String reference;
    private String libelle;
    private Categories  categories;
    private BigDecimal prixVente;
    private BigDecimal prixAchat;
    private BigInteger stock;
    private Boolean prixVenteModifiable;
    private Boolean pacquets;
    private BigDecimal quantiteByPacquet;
    private String description;
    private BigDecimal prixVenteModifiableAccepter;
     private Boolean deletes;
     private String username;
     private BigDecimal prixVenteNet;
     private BigDecimal prixVenteTTC;
     private BigDecimal stockFinal,quantitePrete,quantiteLivree,remise,tva;
     private  String  barcode;
     
     
        


  
  


//
//    // Constructeur depuis entité
   
}
