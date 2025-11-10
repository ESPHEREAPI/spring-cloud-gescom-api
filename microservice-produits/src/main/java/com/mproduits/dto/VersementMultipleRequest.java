/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersementMultipleRequest {
    @NotNull(message = "La facture est obligatoire")
    private Long factureId;
    
    @NotEmpty(message = "Au moins un versement est requis")
    private java.util.List<VersementDetail> versements;
    
    private String username;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersementDetail {
        
        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
        private BigDecimal montant;
        
        private Date dateVersement;
        
        @NotNull(message = "Le mode de paiement est obligatoire")
        private ModePaiement modePaiement;
        
        @Size(max = 100)
        private String referencePaiement;
        
        @Size(max = 100)
        private String banque;
    }
}
