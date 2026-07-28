/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour la requête d'exécution d'une reconduction annuelle.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionReconductionRequest {
    
    @NotNull(message = "L'année source est obligatoire")
    @Positive(message = "L'année source doit être positive")
    private Integer anneeSourceId;

    @NotNull(message = "L'année cible est obligatoire")
    @Positive(message = "L'année cible doit être positive")
    private Integer anneeCibleId;

    @NotNull(message = "L'employeur est obligatoire")
    @Positive(message = "L'employeur doit être positif")
    private Long employeurId;

    @NotNull(message = "Les options sont obligatoires")
    private OptionsReconduction options;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionsReconduction {

        @Builder.Default
        private Boolean copierStocks = true;

        @Builder.Default
        private Boolean copierPrix = true;

        @Builder.Default
        private Boolean desactiverSource = true;

        @DecimalMin(value = "0.1", message = "Le coefficient doit être au minimum 0.1")
        @Builder.Default
        private BigDecimal coefficientPrix = BigDecimal.ONE;

        @Builder.Default
        private Boolean ignorerProduitsSupprimes = true;

        @Positive(message = "La taille de batch doit être positive")
        @Builder.Default
        private Integer batchSize = 100;
    }
}
