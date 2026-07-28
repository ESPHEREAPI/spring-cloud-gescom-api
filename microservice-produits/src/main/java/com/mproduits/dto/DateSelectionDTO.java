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

/**
 * DTO pour la sélection de date.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateSelectionDTO {

    /**
     * Date.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;

    /**
     * Nombre d'opérations pour cette date.
     */
    private Long nombreOperations;

    /**
     * Total pour cette date.
     */
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;
}
