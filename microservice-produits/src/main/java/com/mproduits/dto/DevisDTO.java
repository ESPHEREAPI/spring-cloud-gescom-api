/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class DevisDTO {
      private Long id;
    private Date dateDevis;
    private BigDecimal total;
    private String statut;
    private Date validite;
    private Long clientId;
    private List<DevisItemDTO> items; 
}
