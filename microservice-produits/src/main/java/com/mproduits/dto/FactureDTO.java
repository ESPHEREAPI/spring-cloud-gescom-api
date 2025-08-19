/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class FactureDTO {
     private Long id;
    private Date dateFacture;
    private BigDecimal totalHt;
    private BigDecimal totalTtc;
    private String statut;
    private String modePaiement;
    private Long clientId;
    private Long devisId;
    private List<FactureItemDTO> items;
}
