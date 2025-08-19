/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class PointVenteDTO implements Serializable{

    private Long id;
    private BigDecimal stockFinalTheorie;
    ProduitDTO produitDTO;
    private List<PrixarticlesDTO> prixarticlesListDTO;
    private Boolean promotion;

}
