/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.model.Boutique;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class PointVenteDto {

    private Long id;
    private ProduitDto produit;
    private BigDecimal entreeProduit;
    private BigDecimal stockFinalTheorie;
    private BigDecimal stockInitial;
    private BigDecimal sortiProduit;
    private Boutique boutique;
    private Long depotid;
    private BigDecimal prix;

}
