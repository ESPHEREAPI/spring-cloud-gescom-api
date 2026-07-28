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
 * DTO pour le résumé/statistiques de l'historique.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoriquePhotocopieSummaryDTO {

    /**
     * Total des montants.
     */
    @Builder.Default
    private BigDecimal totalMontant = BigDecimal.ZERO;

    /**
     * Nombre total d'opérations.
     */
    private Long nombreOperations;

    /**
     * Montant moyen par opération.
     */
    @Builder.Default
    private BigDecimal montantMoyen = BigDecimal.ZERO;

    /**
     * Date de début de la période.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateDebut;

    /**
     * Date de fin de la période.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateFin;

    /**
     * Mois sélectionné.
     */
    private String moisLibelle;

    /**
     * Année sélectionnée.
     */
    private Integer annee;

    /**
     * Caissier sélectionné (si filtre actif).
     */
    private String caissier;
}
