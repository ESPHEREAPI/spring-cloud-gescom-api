package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour le contrôle des ventes journalier.
 * Consolide toutes les recettes d'une journée.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ControleVenteDTO {

    /**
     * Date du contrôle.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;

    /**
     * Montant de la caisse.
     */
    @Builder.Default
    private BigDecimal caisse = BigDecimal.ZERO;

    /**
     * Montant des versements clients.
     */
    @Builder.Default
    private BigDecimal client = BigDecimal.ZERO;

    /**
     * Montant des photocopies.
     */
    @Builder.Default
    private BigDecimal photocopies = BigDecimal.ZERO;

    /**
     * Montant des ressources.
     */
    @Builder.Default
    private BigDecimal resources = BigDecimal.ZERO;

    /**
     * Montant des remises.
     */
    @Builder.Default
    private BigDecimal remise = BigDecimal.ZERO;

    /**
     * Montant total (avant remise).
     */
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Montant total net (après remise).
     */
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    /**
     * ID du mois.
     */
    private Long moisId;

    /**
     * Libellé du mois.
     */
    private String moisLibelle;

    /**
     * ID de l'année.
     */
    private int anneeId;

    /**
     * Valeur de l'année.
     */
    private Integer anneeValeur;
}


