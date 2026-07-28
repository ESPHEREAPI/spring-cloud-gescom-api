/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesCaissierDTO {

    /**
     * Nom du caissier.
     */
    private String nomCaissier;

    /**
     * Nombre d'opérations.
     */
    private Long nombreOperations;

    /**
     * Total des montants.
     */
    @Builder.Default
    private BigDecimal totalMontant = BigDecimal.ZERO;

    /**
     * Pourcentage du total.
     */
    private Double pourcentage;
}

/**
 * DTO pour les statistiques par date.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class StatistiquesDateDTO {

    /**
     * Date.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;

    /**
     * Nombre d'opérations.
     */
    private Long nombreOperations;

    /**
     * Total des montants.
     */
    @Builder.Default
    private BigDecimal totalMontant = BigDecimal.ZERO;

    /**
     * Montant moyen.
     */
    @Builder.Default
    private BigDecimal montantMoyen = BigDecimal.ZERO;
}

