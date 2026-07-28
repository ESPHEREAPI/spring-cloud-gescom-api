package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mproduits.FlexibleLocalDateTimeDeserializer;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) pour les requêtes et réponses de l'API Photocopie.
 * Sépare la couche de présentation de la couche domaine.
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
public class PhotocopieDTO {

    /**
     * Identifiant unique de la photocopie. Non renseigné lors de la création.
     */
    private Long id;

    /**
     * Libellé descriptif de l'opération.
     */
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 500, message = "Le libellé ne peut pas dépasser 500 caractères")
    private String libelle;

    /**
     * Montant de la recette en FCFA.
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1", message = "Le montant doit être supérieur à 0")
    @DecimalMax(value = "999999999", message = "Le montant ne peut pas dépasser 999 999 999")
    private BigDecimal montant;

    /**
     * Date de réception/enregistrement de l'opération.
     */
 @NotNull(message = "La date de réception est obligatoire")
   // @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
   // @JsonSerialize(using = LocalDateTimeSerializer.class)
   // @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate dateReception;

    /**
     * Identifiant de l'entreprise.
     */
    @NotNull(message = "L'entreprise est obligatoire")
    private Long entrepriseId;

    /**
     * Nom de l'entreprise (lecture seule).
     */
    private String entrepriseNom;

    /**
     * Identifiant du mois comptable.
     */
//    @NotNull(message = "Le mois est obligatoire")
//    private Long moisId;
    private String username;
    private Long boutiqueid;

    /**
     * Libellé du mois (lecture seule).
     */
    private String moisLibelle;

    /**
     * Commentaire optionnel.
     */
    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;

    /**
     * Identifiant de l'utilisateur créateur (lecture seule).
     */
    private String createdBy;

    /**
     * Date de création (lecture seule).
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Identifiant de l'utilisateur modificateur (lecture seule).
     */
    private String updatedBy;

    /**
     * Date de dernière modification (lecture seule).
     */
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Indicateur de suppression logique (lecture seule).
     */
    private Boolean isDeleted;
}
