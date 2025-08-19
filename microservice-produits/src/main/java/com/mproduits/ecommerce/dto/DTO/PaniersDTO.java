/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class PaniersDTO  implements Serializable{

    private Long id;
    private Date date_enregistrement;
    private BigDecimal montant;
    private String num_panier;
    private BigDecimal quantite;
    private Date heure;
    private PrixarticlesDTO prixarticlesDTO;

}
