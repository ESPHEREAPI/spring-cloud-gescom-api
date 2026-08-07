package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mproduits.FlexibleLocalDateTimeDeserializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.*;

/**
 * Entité JPA représentant une recette de photocopie. Cette classe modélise les
 * opérations de recette (photocopie, impression, scanner, etc.) avec toutes les
 * informations nécessaires pour le suivi et l'audit.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-27
 */
@Entity
@Table(name = "photocopie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photocopie {

    /**
     * Identifiant unique de la photocopie. Généré automatiquement par la base
     * de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Libellé descriptif de l'opération. Ne peut pas être vide et limité à 500
     * caractères.
     */
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 500, message = "Le libellé ne peut pas dépasser 500 caractères")
    @Column(name = "libelle", nullable = false, length = 500)
    private String libelle;

    /**
     * Montant de la recette en FCFA. Doit être positif et ne peut pas dépasser
     * 999 999 999.
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1", message = "Le montant doit être supérieur à 0")
    @DecimalMax(value = "999999999", message = "Le montant ne peut pas dépasser 999 999 999")
    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    /**
     * Date de réception/enregistrement de l'opération. Doit être comprise dans
     * les limites du mois sélectionné. Format accepté: ISO 8601
     * (2026-01-29T12:11:12 ou 2026-01-29 12:11:12)
     */
    @NotNull(message = "La date de réception est obligatoire")
    @PastOrPresent(message = "La date de réception ne peut pas être dans le futur")
    @Column(name = "date_reception", nullable = false)
    //@JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
   // @JsonSerialize(using = LocalDateTimeSerializer.class)
    //@JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate dateReception;

    /**
     * Relation Many-to-One avec l'entreprise. Une entreprise peut avoir
     * plusieurs photocopies.
     */
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "compagnie_id", referencedColumnName = "compagnie_id")
    })
    @ManyToOne(optional = false)
    private Entreprise entreprise;

    /**
     * Identifiant de l'utilisateur qui a créé l'enregistrement. Utilisé pour
     * l'audit et la traçabilité.
     */
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    /**
     * Date et heure de création de l'enregistrement. Généré automatiquement par
     * Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;

    /**
     * Identifiant de l'utilisateur qui a modifié l'enregistrement en dernier.
     * Utilisé pour l'audit et la traçabilité.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Date et heure de la dernière modification. Mise à jour automatiquement
     * par Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedAt;

    /**
     * Indicateur de suppression logique. Permet de marquer un enregistrement
     * comme supprimé sans l'effacer physiquement.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Date de suppression logique. Renseignée uniquement si l'enregistrement
     * est marqué comme supprimé.
     */
    @Column(name = "deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime deletedAt;

    /**
     * Commentaire optionnel sur l'opération. Permet d'ajouter des informations
     * complémentaires.
     */
    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @JoinColumn(name = "Boutiqueid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Boutique boutique;
    /**
     * Relation Many-to-One avec la personne (caissier/opérateur).
     */
    @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "Personneid", referencedColumnName = "Id")
    @OneToOne(optional = false)
    private Personne personne;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Moisid", nullable = false, referencedColumnName = "Id")
    @NotNull
    private Mois mois;

    /**
     * Callback JPA exécuté avant la persistance. Initialise les valeurs par
     * défaut.
     */
    @PrePersist
    protected void onCreate() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    /**
     * Méthode utilitaire pour vérifier si l'enregistrement est actif.
     *
     * @return true si l'enregistrement n'est pas supprimé logiquement
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.isDeleted);
    }

    /**
     * Méthode utilitaire pour marquer l'enregistrement comme supprimé.
     *
     * @param deletedBy Identifiant de l'utilisateur effectuant la suppression
     */
    public void markAsDeleted(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedBy = deletedBy;
    }

    /**
     * Méthode utilitaire pour restaurer un enregistrement supprimé.
     *
     * @param restoredBy Identifiant de l'utilisateur effectuant la restauration
     */
    public void restore(String restoredBy) {
        this.isDeleted = false;
        this.deletedAt = null;
        this.updatedBy = restoredBy;
    }
}
