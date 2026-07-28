package com.entreprise.recette.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour les résumés et statistiques des photocopies.
 * Utilisé pour fournir des informations agrégées à l'interface utilisateur.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhotocopieSummaryDTO {

    /**
     * Montant total des recettes pour la période.
     */
    private BigDecimal totalMontant;

    /**
     * Nombre total d'entrées pour la période.
     */
    private Long nombreEntrees;

    /**
     * Date de début de la période considérée.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate periodeDebut;

    /**
     * Date de fin de la période considérée.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate periodeFin;

    /**
     * Montant moyen par entrée.
     */
    private BigDecimal montantMoyen;

    /**
     * Montant minimum enregistré.
     */
    private BigDecimal montantMin;

    /**
     * Montant maximum enregistré.
     */
    private BigDecimal montantMax;

    /**
     * Identifiant du mois concerné.
     */
    private Long moisId;

    /**
     * Libellé du mois concerné.
     */
    private String moisLibelle;

    /**
     * Identifiant de l'entreprise concernée.
     */
    private Long entrepriseId;

    /**
     * Nom de l'entreprise concernée.
     */
    private String entrepriseNom;
}
