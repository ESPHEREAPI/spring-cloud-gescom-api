/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

/**
 *
 * @author USER01
 */

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * DTO pour créer/modifier une facture
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {
    private Long id;
    
    @NotNull(message = "Client requis")
    private Long clientId;
    
    private Long devisId;
    
    private List<FactureItemDTO> items;
    
    private BigDecimal totalHt;
    private BigDecimal totalTva;
    private BigDecimal totalTtc;
    private BigDecimal montantDejaPaye;
    
    @NotBlank(message = "Statut requis")
    private String statut;
    
    private String modePaiement;
    
    private Date dateFacture;
    
    private String numeroFacture;
    private List<VersementClientDTO> versements;
    
}
