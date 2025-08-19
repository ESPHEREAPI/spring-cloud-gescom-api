/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class PrixarticlesDTO implements Serializable{

    private Long id;
    private boolean actif;
    private BigDecimal prixVenteTTC;
    private BigDecimal remise;
    private BigDecimal tva;
    private PointVenteDTO pointVenteDTO;
    private BigDecimal prixVenteNet;

}
