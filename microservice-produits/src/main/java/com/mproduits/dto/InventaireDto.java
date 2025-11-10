/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import java.math.BigDecimal;
import java.util.Date;
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
public class InventaireDto {

    Long id;
    ProduitDto produit;
    BigDecimal quantite;
    BigDecimal prix;
    BigDecimal total;
    Date dateInventaire;
    Boutique boutique;
    DepoteDto depot;
    Categories categorie;
}
