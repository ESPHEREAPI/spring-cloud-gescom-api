/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le résumé global d'un mois.
 * Contient les totaux de toutes les recettes du mois.
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
public class ControleVenteSummaryDTO {

    /**
     * Total de la caisse pour le mois.
     */
    @Builder.Default
    private BigDecimal totalCaisse = BigDecimal.ZERO;

    /**
     * Total des clients pour le mois.
     */
    @Builder.Default
    private BigDecimal totalClient = BigDecimal.ZERO;

    /**
     * Total des photocopies pour le mois.
     */
    @Builder.Default
    private BigDecimal totalPhotocopies = BigDecimal.ZERO;

    /**
     * Total des ressources pour le mois.
     */
    @Builder.Default
    private BigDecimal totalResources = BigDecimal.ZERO;

    /**
     * Total des remises pour le mois.
     */
    @Builder.Default
    private BigDecimal totalRemises = BigDecimal.ZERO;

    /**
     * Total général (avant remise).
     */
    @Builder.Default
    private BigDecimal totalGeneral = BigDecimal.ZERO;

    /**
     * Total net (après remise).
     */
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    /**
     * Nombre de jours avec des ventes.
     */
    private Integer nombreJours;

    /**
     * Moyenne journalière.
     */
    @Builder.Default
    private BigDecimal moyenneJournaliere = BigDecimal.ZERO;

    /**
     * Période de début.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate periodeDebut;

    /**
     * Période de fin.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate periodeFin;

    /**
     * ID du mois.
     */
    private Long moisId;

    /**
     * Libellé du mois.
     */
    private String moisLibelle;
}