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
public class VersementClientDTO {
     private Long id;
    private Date date;
    private BigDecimal montant;
    private String modePaiement;
    private String referencePaiement;
    private Long clientId;
    private Long factureId;
}
