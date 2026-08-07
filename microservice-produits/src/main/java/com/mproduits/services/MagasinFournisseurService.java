/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;


import com.mproduits.dto.MagasinFournisseurDTO;
import com.mproduits.exceptions.ResourceNotFoundException;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Fournisseur;
import com.mproduits.model.Magasin;
import com.mproduits.model.MagasinFournisseur;
import com.mproduits.model.MagasinFournisseurPK;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.EntrepriseRepository;
import com.mproduits.repositories.FournisseurRepositories;
import com.mproduits.repositories.MagasinFournisseurRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des associations Magasin-Fournisseur
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MagasinFournisseurService {

    private final MagasinFournisseurRepositories magasinFournisseurRepository;
    private final MagasinRepositories magasinRepository;
    private final FournisseurRepositories fournisseurRepository;
    private final EntrepriseRepositories entrepriseRepository;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    /**
     * Récupérer toutes les associations avec pagination
     */
    public Page<MagasinFournisseurDTO> getAllAssociations(Pageable pageable) {
        log.debug("Récupération de toutes les associations - page: {}", pageable.getPageNumber());
        return magasinFournisseurRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Rechercher des associations
     */
    public Page<MagasinFournisseurDTO> searchAssociations(String search, Pageable pageable) {
        log.debug("Recherche d'associations avec le terme: {}", search);
        return magasinFournisseurRepository.searchAssociations(search, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Récupérer toutes les associations sans pagination
     */
    public List<MagasinFournisseurDTO> getAllAssociationsNoPagination() {
        log.debug("Récupération de toutes les associations sans pagination");
        return magasinFournisseurRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une association par ID composite
     */
    public MagasinFournisseurDTO getAssociationById(Long depotId, Long fournisseurId) {
        log.debug("Récupération de l'association Magasin {} - Fournisseur {}", depotId, fournisseurId);
        
        MagasinFournisseurPK pk = new MagasinFournisseurPK(depotId, fournisseurId);
        MagasinFournisseur association = magasinFournisseurRepository.findById(pk)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Association non trouvée pour Magasin ID: " + depotId + " et Fournisseur ID: " + fournisseurId));
        
        return convertToDTO(association);
    }

    /**
     * Créer une nouvelle association
     */
    public MagasinFournisseurDTO createAssociation(MagasinFournisseurDTO dto) {
        log.info("Création d'une association Magasin {} - Fournisseur {}", 
                 dto.getDepotId(), dto.getFournisseurId());

        // Vérifier si l'association existe déjà
        if (associationExists(dto.getDepotId(), dto.getFournisseurId())) {
            throw new ResourceNotFoundException("Cette association existe déjà");
        }

        // Vérifier que le magasin existe
        Magasin magasin = magasinRepository.findById(dto.getDepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé avec l'ID: " + dto.getDepotId()));

        // Vérifier que le fournisseur existe
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + dto.getFournisseurId()));

        
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        // Créer l'association
        MagasinFournisseur association = new MagasinFournisseur();
        association.setMagasinFournisseurPK(new MagasinFournisseurPK(dto.getDepotId(), dto.getFournisseurId()));
        association.setMagasin(magasin);
        association.setFournisseur(fournisseur);
        association.setEntreprise(e);

        // Sauvegarder
        MagasinFournisseur saved = magasinFournisseurRepository.save(association);
        
        log.info("Association créée avec succès");
        return convertToDTO(saved);
    }

    /**
     * Mettre à jour une association
     */
    public MagasinFournisseurDTO updateAssociation(Long depotId, Long fournisseurId, MagasinFournisseurDTO dto) {
        log.info("Mise à jour de l'association Magasin {} - Fournisseur {}", depotId, fournisseurId);

        MagasinFournisseurPK pk = new MagasinFournisseurPK(depotId, fournisseurId);
        MagasinFournisseur association = magasinFournisseurRepository.findById(pk)
                .orElseThrow(() -> new ResourceNotFoundException("Association non trouvée"));

        // Si les IDs changent, vérifier que la nouvelle association n'existe pas
        if (!depotId.equals(dto.getDepotId()) || !fournisseurId.equals(dto.getFournisseurId())) {
            if (associationExists(dto.getDepotId(), dto.getFournisseurId())) {
                throw new ResourceNotFoundException("Une association existe déjà avec ces identifiants");
            }
            
            // Supprimer l'ancienne et créer la nouvelle
            magasinFournisseurRepository.delete(association);
            return createAssociation(dto);
        }

        // Mettre à jour les champs si nécessaire (entreprise, etc.)
        // association.setEntreprise(...);

        MagasinFournisseur updated = magasinFournisseurRepository.save(association);
        
        log.info("Association mise à jour avec succès");
        return convertToDTO(updated);
    }

    /**
     * Supprimer une association
     */
    public void deleteAssociation(Long depotId, Long fournisseurId) {
        log.info("Suppression de l'association Magasin {} - Fournisseur {}", depotId, fournisseurId);

        MagasinFournisseurPK pk = new MagasinFournisseurPK(depotId, fournisseurId);
        
        if (!magasinFournisseurRepository.existsById(pk)) {
            throw new ResourceNotFoundException("Association non trouvée");
        }

        magasinFournisseurRepository.deleteById(pk);
        log.info("Association supprimée avec succès");
    }

    /**
     * Récupérer les associations d'un magasin
     */
    public List<MagasinFournisseurDTO> getAssociationsByMagasin(Long depotId) {
        log.debug("Récupération des associations pour le magasin ID: {}", depotId);
        
        // Vérifier que le magasin existe
        if (!magasinRepository.existsById(depotId)) {
            throw new ResourceNotFoundException("Magasin non trouvé avec l'ID: " + depotId);
        }

        return magasinFournisseurRepository.findByMagasinFournisseurPK_DepotId(depotId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les associations d'un fournisseur
     */
    public List<MagasinFournisseurDTO> getAssociationsByFournisseur(Long fournisseurId) {
        log.debug("Récupération des associations pour le fournisseur ID: {}", fournisseurId);
        
        // Vérifier que le fournisseur existe
        if (!fournisseurRepository.existsById(fournisseurId)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + fournisseurId);
        }

        return magasinFournisseurRepository.findByMagasinFournisseurPK_FournisseurId(fournisseurId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Vérifier si une association existe
     */
    public boolean associationExists(Long depotId, Long fournisseurId) {
        MagasinFournisseurPK pk = new MagasinFournisseurPK(depotId, fournisseurId);
        return magasinFournisseurRepository.existsById(pk);
    }

    /**
     * Créer plusieurs associations en batch
     */
    public List<MagasinFournisseurDTO> createAssociationsBatch(List<MagasinFournisseurDTO> dtos) {
        log.info("Création de {} associations en batch", dtos.size());

        List<MagasinFournisseur> associations = new ArrayList<>();

        for (MagasinFournisseurDTO dto : dtos) {
            // Vérifier les doublons
            if (associationExists(dto.getDepotId(), dto.getFournisseurId())) {
                log.warn("Association déjà existante ignorée: Magasin {} - Fournisseur {}", 
                         dto.getDepotId(), dto.getFournisseurId());
                continue;
            }

            Magasin magasin = magasinRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Magasin non trouvé: " + dto.getDepotId()));

            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé: " + dto.getFournisseurId()));

            MagasinFournisseur association = new MagasinFournisseur();
            association.setMagasinFournisseurPK(new MagasinFournisseurPK(dto.getDepotId(), dto.getFournisseurId()));
            association.setMagasin(magasin);
            association.setFournisseur(fournisseur);
            
            associations.add(association);
        }

        List<MagasinFournisseur> saved = magasinFournisseurRepository.saveAll(associations);
        
        log.info("{} associations créées avec succès", saved.size());
        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer toutes les associations d'un magasin
     */
    public int deleteAssociationsByMagasin(Long depotId) {
        log.info("Suppression de toutes les associations du magasin ID: {}", depotId);
        
        List<MagasinFournisseur> associations = magasinFournisseurRepository.findByMagasinFournisseurPK_DepotId(depotId);
        int count = associations.size();
        
        magasinFournisseurRepository.deleteAll(associations);
        
        log.info("{} association(s) supprimée(s)", count);
        return count;
    }

    /**
     * Supprimer toutes les associations d'un fournisseur
     */
    public int deleteAssociationsByFournisseur(Long fournisseurId) {
        log.info("Suppression de toutes les associations du fournisseur ID: {}", fournisseurId);
        
        List<MagasinFournisseur> associations = magasinFournisseurRepository.findByMagasinFournisseurPK_FournisseurId(fournisseurId);
        int count = associations.size();
        
        magasinFournisseurRepository.deleteAll(associations);
        
        log.info("{} association(s) supprimée(s)", count);
        return count;
    }

    /**
     * Statistiques d'une association
     */
    public Map<String, Object> getAssociationStatistics(Long depotId, Long fournisseurId) {
        log.debug("Récupération des statistiques pour Magasin {} - Fournisseur {}", depotId, fournisseurId);

        MagasinFournisseurDTO association = getAssociationById(depotId, fournisseurId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("depotId", depotId);
        stats.put("fournisseurId", fournisseurId);
        stats.put("magasinNom", association.getMagasinLibelle());
        stats.put("fournisseurNom", association.getFournisseurNom());
        stats.put("nombreCommandes", association.getNombreCommandes() != null ? association.getNombreCommandes() : 0);
        stats.put("montantTotal", association.getMontantTotal() != null ? association.getMontantTotal() : 0.0);
        stats.put("statut", association.getStatut() != null ? association.getStatut() : "ACTIF");

        return stats;
    }

    /**
     * Statistiques globales du dashboard
     */
    public Map<String, Object> getDashboardStatistics() {
        log.debug("Récupération des statistiques globales");

        Map<String, Object> stats = new HashMap<>();
        
        long totalAssociations = magasinFournisseurRepository.count();
        long totalMagasins = magasinRepository.count();
        long totalFournisseurs = fournisseurRepository.count();

        stats.put("totalAssociations", totalAssociations);
        stats.put("totalMagasins", totalMagasins);
        stats.put("totalFournisseurs", totalFournisseurs);
        stats.put("moyenneAssociationsParMagasin", 
                  totalMagasins > 0 ? (double) totalAssociations / totalMagasins : 0.0);
        stats.put("moyenneAssociationsParFournisseur", 
                  totalFournisseurs > 0 ? (double) totalAssociations / totalFournisseurs : 0.0);

        return stats;
    }

    /**
     * Convertir entité vers DTO
     */
    private MagasinFournisseurDTO convertToDTO(MagasinFournisseur entity) {
        MagasinFournisseurDTO dto = new MagasinFournisseurDTO();
        
        dto.setDepotId(entity.getMagasinFournisseurPK().getDepotId());
        dto.setFournisseurId(entity.getMagasinFournisseurPK().getFournisseurId());

        // Informations du magasin
        if (entity.getMagasin() != null) {
            dto.setMagasinCode(entity.getMagasin().getCode());
            dto.setMagasinLibelle(entity.getMagasin().getLibelle());
            if (entity.getMagasin().getBoutiqueId() != null) {
                dto.setBoutiqueNom(entity.getMagasin().getBoutiqueId().getNom());
            }
        }

        // Informations du fournisseur
        if (entity.getFournisseur() != null) {
            dto.setFournisseurCode(entity.getFournisseur().getCode());
            dto.setFournisseurNom(entity.getFournisseur().getNom());
            dto.setFournisseurEmail(entity.getFournisseur().getEmail());
            dto.setFournisseurTel(entity.getFournisseur().getTel());
            dto.setFournisseurVille(entity.getFournisseur().getVille());
        }

        // Informations entreprise
        if (entity.getEntreprise() != null) {
            dto.setAnneeId(entity.getEntreprise().getEntreprisePK().getAnneeId());
            dto.setEmployeurId(entity.getEntreprise().getEntreprisePK().getCompagnieId());
        }

        // Métadonnées
        dto.setDateCreation(LocalDateTime.now());
        dto.setStatut("ACTIF");

        return dto;
    }
    
}
