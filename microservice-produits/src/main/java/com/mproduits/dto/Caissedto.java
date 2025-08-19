/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.ecommerce.dto.entites.Articles;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class Caissedto {

    private Long id;
    private ProduitDto article;
    private BigDecimal quantite;
    BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    private BigDecimal remise;
    private String numeroTicket;
    private String typePaiement;

    private Articles product;

}
