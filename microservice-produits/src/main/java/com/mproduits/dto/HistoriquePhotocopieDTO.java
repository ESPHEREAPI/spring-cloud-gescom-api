package com.entreprise.historique.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour l'affichage de l'historique des photocopies.
 * Représente une ligne dans le tableau d'historique.
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
public class HistoriquePhotocopieDTO {

    /**
     * ID de l'opération.
     */
    private Long id;

    /**
     * Libellé de l'opération.
     */
    private String libelle;

    /**
     * Montant de l'opération.
     */
    @Builder.Default
    private BigDecimal montant = BigDecimal.ZERO;

    /**
     * Date de l'opération.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateReception;

    /**
     * Heure de l'opération.
     */
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalDateTime heure;

    /**
     * Nom complet du caissier/opérateur.
     */
    private String nomCaissier;

    /**
     * Référence de l'opération.
     */
    private String reference;

    /**
     * Quantité (nombre de copies/pages).
     */
    private Integer quantite;

    /**
     * Type d'opération.
     */
    private String typeOperation;

    /**
     * Observations.
     */
    private String observations;

    /**
     * Mois de l'opération (libellé).
     */
    private String moisLibelle;

    /**
     * Année de l'opération.
     */
    private Integer annee;
}


/**
 * DTO pour les filtres de recherche.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class HistoriquePhotocopieFilterDTO {

    /**
     * ID de l'année.
     */
    private Long anneeId;

    /**
     * ID du mois.
     */
    private Long moisId;

    /**
     * Date sélectionnée.
     */
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;

    /**
     * ID du caissier (optionnel).
     */
    private Long personneId;

    /**
     * Mode multi-caisse activé.
     */
    @Builder.Default
    private Boolean multiCaisse = false;

    /**
     * ID de l'entreprise.
     */
    private Long entrepriseId;
}

/**
 * DTO pour les statistiques par caissier.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-30
 */
