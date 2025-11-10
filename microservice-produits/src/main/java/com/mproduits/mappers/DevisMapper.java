package com.mproduits.mappers;

import com.mproduits.dto.ClientDto;
import com.mproduits.dto.DevisDTO;
import com.mproduits.dto.DevisItemDTO;
import com.mproduits.model.Client;
import com.mproduits.model.Devis;
import com.mproduits.model.DevisItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper pour conversion Entité ↔ DTO
 * 
 * RESPONSABILITÉS:
 * - Convertir Devis → DevisDTO
 * - Convertir DevisDTO → Devis
 * - Convertir DevisItem → DevisItemDTO
 * - Gérer les relations
 * - Enrichir les DTOs avec informations calculées
 * 
 * @author USER01
 * @version 2.0
 */
@Component
@RequiredArgsConstructor
public class DevisMapper {

    // ================================================================
    // CONVERSION DEVIS → DTO
    // ================================================================

    /**
     * Convertit une entité Devis en DevisDTO
     * Enrichit le DTO avec toutes les informations calculées
     * 
     * @param devis Entité à convertir
     * @return DTO avec toutes les informations
     */
    @Autowired
    MapperDtoImpl mapper;
    public DevisDTO toDto(Devis devis) {
        if (devis == null) {
            return null;
        }

        Client cl=devis.getClient();
        ClientDto cldto=mapper.mapperClientByClientDto(cl);
        DevisDTO dto = DevisDTO.builder()
                // Identifiant
                .id(devis.getId())
                .numeroDevis(devis.getNumeroDevis())
                .version(devis.getVersion())
                
                // Client
//                .clientId(devis.getClient() != null ? devis.getClient().getId() : null)
//                .clientNom(devis.getClient() != null ? devis.getClient().getNom() : null)
//                .clientEmail(devis.getClient() != null ? devis.getClient().getEmail() : null)
//                .clientTelephone(devis.getClient() != null ? devis.getClient().getTelephone() : null)
                  .client(cldto)
                
                // Dates
                .dateDevis(devis.getDateDevis())
                .dateExpiration(devis.getDateExpiration())
                .validiteJours(devis.getValiditeJours())
                .dateCreation(devis.getDateCreation())
                .dateModification(devis.getDateModification())
                .dateAcceptation(devis.getDateAcceptation())
                .dateConversion(devis.getDateConversion())
                
                // Montants
                .montantHT(devis.getMontantHT())
                .totalRemise(devis.getTotalRemise())
                .tauxTVA(devis.getTauxTVA())
                .totalTVA(devis.getTotalTVA())
                .total(devis.getTotal())
                .appliquerTVA(devis.getAppliquerTVA())
                
                // Statut
                .statut(devis.getStatut().name())
                .statutLibelle(devis.getStatut().getLibelle())
                
                // États calculés
                .modifiable(devis.isModifiable())
                .peutEtreAccepte(devis.isPeutEtreAccepte())
                .peutEtreConverti(devis.isPeutEtreConverti())
                .expire(devis.isExpire())
                .procheExpiration(devis.isProcheExpiration())
                .joursAvantExpiration(devis.getJoursAvantExpiration())
                
                // Informations complémentaires
                .remarques(devis.getRemarques())
                .conditions(devis.getConditions())
                .motifRefus(devis.getMotifRefus())
                .motifAnnulation(devis.getMotifAnnulation())
                
                // Facture liée
                .factureId(devis.getFacture() != null ? devis.getFacture().getId() : null)
                .factureNumero(devis.getFacture() != null ? devis.getFacture().getNumeroFacture() : null)
                
                // Traçabilité
                .usernameCreate(devis.getUsernameCreate())
                .usernameUpdate(devis.getUsernameUpdate())
                .usernameValidation(devis.getUsernameValidation())
                
                // Options
                .envoyeClient(devis.getEnvoyeClient())
                .dateEnvoi(devis.getDateEnvoi())
                
                .build();

        // Convertir les articles
        if (devis.getItems() != null && !devis.getItems().isEmpty()) {
            List<DevisItemDTO> itemsDto = devis.getItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemsDto);
            dto.setNombreArticles(itemsDto.size());
        } else {
            dto.setItems(new ArrayList<>());
            dto.setNombreArticles(0);
        }

        return dto;
    }

    /**
     * Convertit une liste de Devis en liste de DevisDTO
     */
    public List<DevisDTO> toDtoList(List<Devis> devisList) {
        if (devisList == null) {
            return new ArrayList<>();
        }
        
        return devisList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ================================================================
    // CONVERSION DTO → DEVIS (pour création/mise à jour)
    // ================================================================

    /**
     * Convertit un DevisDTO en entité Devis
     * Utilisé pour la création d'un nouveau devis
     * 
     * NOTE: Ne pas utiliser pour mise à jour, privilégier la méthode updateEntity()
     * 
     * @param dto DTO source
     * @return Entité Devis (non persistée)
     */
    public Devis toEntity(DevisDTO dto) {
        if (dto == null) {
            return null;
        }

        Devis devis = Devis.builder()
                .numeroDevis(dto.getNumeroDevis())
                .dateDevis(dto.getDateDevis())
                .dateExpiration(dto.getDateExpiration())
                .validiteJours(dto.getValiditeJours())
                .montantHT(dto.getMontantHT())
                .totalRemise(dto.getTotalRemise())
                .tauxTVA(dto.getTauxTVA())
                .totalTVA(dto.getTotalTVA())
                .total(dto.getTotal())
                .appliquerTVA(dto.isAppliquerTVA())
                .remarques(dto.getRemarques())
                .conditions(dto.getConditions())
                .usernameCreate(dto.getUsernameCreate())
                .dateCreation(dto.getDateCreation())
                .build();

        // Note: Les relations (client, items, facture) doivent être gérées 
        // par le service qui appelle ce mapper

        return devis;
    }

    /**
     * Met à jour une entité Devis existante à partir d'un DTO
     * Utilisé pour les modifications
     * 
     * @param entity Entité à mettre à jour
     * @param dto DTO source des modifications
     */
    public void updateEntity(Devis entity, DevisDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        // Mise à jour des champs modifiables uniquement
        if (dto.getRemarques() != null) {
            entity.setRemarques(dto.getRemarques());
        }
        
        if (dto.getConditions() != null) {
            entity.setConditions(dto.getConditions());
        }
        
        if (dto.getTauxTVA() != null) {
            entity.setTauxTVA(dto.getTauxTVA());
        }
        
        entity.setAppliquerTVA(dto.isAppliquerTVA());
        
        if (dto.getUsernameUpdate() != null) {
            entity.setUsernameUpdate(dto.getUsernameUpdate());
        }

        // Les montants, items, statut, etc. sont gérés par le service
    }

    // ================================================================
    // CONVERSION DEVISITEM → DTO
    // ================================================================

    /**
     * Convertit un DevisItem en DevisItemDTO
     * 
     * @param item Entité à convertir
     * @return DTO avec toutes les informations
     */
    public DevisItemDTO toItemDto(DevisItem item) {
        if (item == null) {
            return null;
        }

        DevisItemDTO dto = DevisItemDTO.builder()
                // Identifiant
                .id(item.getId())
                
                // Produit
                .produitId(item.getProduit() != null ? item.getProduit().getId() : null)
                .produitCode(item.getProduitCode())
                .produitLibelle(item.getProduitLibelle())
                .description(item.getDescription())
                
                // Quantités et prix
                .quantite(item.getQuantite())
                .prixUnitaire(item.getPrixUnitaire())
                .tauxRemise(item.getTauxRemise())
                .montantRemise(item.getMontantRemise())
                
                // Montants calculés
                .montantHT(item.getMontantHT())
                .tauxTVA(item.getTauxTVA())
                .montantTVA(item.getMontantTVA())
                .montantTTC(item.getMontantTTC())
                
                // Ordre
                .ordre(item.getOrdre())
                
                .build();

        return dto;
    }

    /**
     * Convertit une liste de DevisItem en liste de DevisItemDTO
     */
    public List<DevisItemDTO> toItemDtoList(List<DevisItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        
        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    // ================================================================
    // CONVERSION DTO → DEVISITEM
    // ================================================================

    /**
     * Convertit un DevisItemDTO en entité DevisItem
     * 
     * @param dto DTO source
     * @return Entité DevisItem (non persistée)
     */
    public DevisItem toItemEntity(DevisItemDTO dto) {
        if (dto == null) {
            return null;
        }

        DevisItem item = DevisItem.builder()
                .produitCode(dto.getProduitCode())
                .produitLibelle(dto.getProduitLibelle())
                .description(dto.getDescription())
                .quantite(dto.getQuantite())
                .prixUnitaire(dto.getPrixUnitaire())
                .tauxRemise(dto.getTauxRemise())
                .montantRemise(dto.getMontantRemise())
                .montantHT(dto.getMontantHT())
                .tauxTVA(dto.getTauxTVA())
                .montantTVA(dto.getMontantTVA())
                .montantTTC(dto.getMontantTTC())
                .ordre(dto.getOrdre())
                .build();

        // Note: Les relations (devis, produit) doivent être gérées 
        // par le service qui appelle ce mapper

        return item;
    }

    /**
     * Convertit une liste de DevisItemDTO en liste de DevisItem
     */
    public List<DevisItem> toItemEntityList(List<DevisItemDTO> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        
        return dtos.stream()
                .map(this::toItemEntity)
                .collect(Collectors.toList());
    }

    // ================================================================
    // MÉTHODES UTILITAIRES
    // ================================================================

    /**
     * Copie les informations essentielles d'un Devis vers un autre
     * Utilisé pour la duplication
     * 
     * @param source Devis source
     * @param target Devis cible
     */
    public void copyEssentialFields(Devis source, Devis target) {
        if (source == null || target == null) {
            return;
        }

        target.setClient(source.getClient());
        target.setAppliquerTVA(source.getAppliquerTVA());
        target.setTauxTVA(source.getTauxTVA());
        target.setRemarques(source.getRemarques());
        target.setConditions(source.getConditions());
        target.setValiditeJours(source.getValiditeJours());
    }

    /**
     * Copie les informations d'un DevisItem vers un autre
     * Utilisé pour la duplication
     * 
     * @param source Item source
     * @param target Item cible
     */
    public void copyItemFields(DevisItem source, DevisItem target) {
        if (source == null || target == null) {
            return;
        }

        target.setProduit(source.getProduit());
        target.setProduitCode(source.getProduitCode());
        target.setProduitLibelle(source.getProduitLibelle());
        target.setDescription(source.getDescription());
        target.setQuantite(source.getQuantite());
        target.setPrixUnitaire(source.getPrixUnitaire());
        target.setTauxRemise(source.getTauxRemise());
        target.setTauxTVA(source.getTauxTVA());
        target.setOrdre(source.getOrdre());
    }

    /**
     * Enrichit un DevisDTO avec des informations de calcul
     * Utilisé après la récupération depuis la base
     * 
     * @param dto DTO à enrichir
     */
    public void enrichDto(DevisDTO dto) {
        if (dto == null) {
            return;
        }

        // Calculer le nombre d'articles si non défini
        if (dto.getNombreArticles() == null && dto.getItems() != null) {
            dto.setNombreArticles(dto.getItems().size());
        }

        // Enrichir chaque article
        if (dto.getItems() != null) {
            dto.getItems().forEach(this::enrichItemDto);
        }
    }

    /**
     * Enrichit un DevisItemDTO avec des informations calculées
     * 
     * @param itemDto DTO à enrichir
     */
    public void enrichItemDto(DevisItemDTO itemDto) {
        if (itemDto == null) {
            return;
        }

        // Les calculs sont déjà faits côté entité
        // Mais on peut ajouter des informations supplémentaires si nécessaire
    }

    /**
     * Valide la cohérence entre un Devis et son DTO
     * Utilisé pour les tests
     * 
     * @param entity Entité
     * @param dto DTO
     * @return true si cohérent
     */
    public boolean validateConsistency(Devis entity, DevisDTO dto) {
        if (entity == null || dto == null) {
            return false;
        }

        // Vérifier les champs principaux
        boolean consistent = 
                entity.getId().equals(dto.getId()) &&
                entity.getNumeroDevis().equals(dto.getNumeroDevis()) &&
                entity.getTotal().compareTo(dto.getTotal()) == 0;

        return consistent;
    }
}