/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author USER01
 */
/**
 * DTO pour versement client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersementClientDTO {
    private Long id;
    
    @NotNull(message = "Facture requise")
    private Long factureId;
    
    @NotNull(message = "Montant requis")
    @DecimalMin(value = "0.01", message = "Montant > 0")
    private BigDecimal montant;
    
    @NotBlank(message = "Mode de paiement requis")
    private String modePaiement; // ESPECE, CHEQUE, VIREMENT, CARTE
    
    private String referencePaiement;
    
    private Date date;
    
}
