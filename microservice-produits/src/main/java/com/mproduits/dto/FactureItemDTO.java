/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
/**
 *
 * @author USER01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class FactureItemDTO {
    private Long id;
    
    @NotNull(message = "Produit requis")
    private Long produitId;
    
    @NotNull(message = "Quantité requise")
    @Min(value = 1, message = "Quantité min: 1")
    private Integer quantite;
    
    @NotNull(message = "Prix requis")
    @DecimalMin(value = "0.01", message = "Prix > 0")
    private BigDecimal prixUnitaire;
    
    @DecimalMin(value = "0.0", message = "Remise min: 0%")
    @DecimalMax(value = "50.0", message = "Remise max: 50%")
    private BigDecimal remisePercent;
    
}
