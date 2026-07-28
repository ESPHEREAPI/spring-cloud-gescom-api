package com.mproduits.mappers;


import com.mproduits.dto.PhotocopieDTO;
import com.mproduits.model.Photocopie;


import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre l'entité Photocopie et son DTO.
 * Utilise le pattern Mapper pour séparer les couches de l'application.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-27
 */
@Component
public class PhotocopieMapper {

    /**
     * Convertit une entité Photocopie en DTO.
     * Toutes les informations nécessaires à la présentation sont incluses.
     *
     * @param photocopie L'entité à convertir
     * @return Le DTO correspondant, ou null si l'entité est null
     */
    public PhotocopieDTO toDTO(Photocopie photocopie) {
        if (photocopie == null) {
            return null;
        }

        return PhotocopieDTO.builder()
                .id(photocopie.getId())
                .libelle(photocopie.getLibelle())
                .montant(photocopie.getMontant())
                .dateReception(photocopie.getDateReception())
                //.entrepriseId(photocopie.getEntreprise() != null ? photocopie.getEntreprise(). : null)
                //.entrepriseNom(photocopie.getEntreprise() != null ? photocopie.getEntreprise().getNom() : null)
               // .moisId(photocopie.getMois() != null ? photocopie.getMois().getId() : null)
               // .moisLibelle(photocopie.getMois() != null ? photocopie.getMois().getLibelle() : null)
                .commentaire(photocopie.getCommentaire())
                .createdBy(photocopie.getCreatedBy())
                .createdAt(photocopie.getCreatedAt())
                .updatedBy(photocopie.getUpdatedBy())
                .updatedAt(photocopie.getUpdatedAt())
                .isDeleted(photocopie.getIsDeleted())
                .build();
    }

    /**
     * Convertit un DTO en entité Photocopie.
     * Note: Les relations (Entreprise, Mois) doivent être définies séparément
     * car seuls les IDs sont fournis dans le DTO.
     *
     * @param dto Le DTO à convertir
     * @return L'entité correspondante, ou null si le DTO est null
     */
    public Photocopie toEntity(PhotocopieDTO dto) {
        if (dto == null) {
            return null;
        }

        return Photocopie.builder()
                .id(dto.getId())
                .libelle(dto.getLibelle())
                .montant(dto.getMontant())
                .dateReception(dto.getDateReception())
                .commentaire(dto.getCommentaire())
                .build();
    }

    /**
     * Met à jour une entité existante avec les données d'un DTO.
     * Ne met à jour que les champs modifiables (pas les champs d'audit).
     *
     * @param entity L'entité à mettre à jour
     * @param dto Le DTO contenant les nouvelles données
     */
    public void updateEntityFromDTO(Photocopie entity, PhotocopieDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setLibelle(dto.getLibelle());
        entity.setMontant(dto.getMontant());
        entity.setDateReception(dto.getDateReception());
        entity.setCommentaire(dto.getCommentaire());
        // Les relations (Entreprise, Mois) et les champs d'audit sont gérés par le service
    }

    /**
     * Crée un DTO minimal à partir d'une entité (sans relations).
     * Utile pour les listes où toutes les informations ne sont pas nécessaires.
     *
     * @param photocopie L'entité à convertir
     * @return Le DTO minimal correspondant
     */
    public PhotocopieDTO toMinimalDTO(Photocopie photocopie) {
        if (photocopie == null) {
            return null;
        }

        return PhotocopieDTO.builder()
                .id(photocopie.getId())
                .libelle(photocopie.getLibelle())
                .montant(photocopie.getMontant())
                .dateReception(photocopie.getDateReception())
                .createdAt(photocopie.getCreatedAt())
                .build();
    }
}
