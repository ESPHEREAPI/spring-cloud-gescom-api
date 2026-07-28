/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDecrementItem {
    
    /**
     * ID de l'article à décrémenter
     */
    @NotNull(message = "L'ID de l'article est obligatoire")
    @JsonProperty("articleId")
    private Long articleId;
    
    /**
     * Quantité à décrémenter (doit être > 0)
     */
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @JsonProperty("stockValue")
    private Integer quantite;
//      @JsonProperty("prixVenteTTC")
//    private Integer prixVenteTTC;
    
    @Override
    public String toString() {
        return String.format("StockDecrementItem{articleId=%d, quantite=%d}", 
                articleId, quantite);
    }
    
    
}
