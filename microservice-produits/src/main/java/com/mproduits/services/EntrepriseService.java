/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

/**
 *
 * @author USER01
 */

import com.mproduits.dto.EntrepriseCreateRequest;
import com.mproduits.dto.EntrepriseResponse;

import com.mproduits.model.Annee;
import com.mproduits.model.Employeur;
import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import com.mproduits.repositories.AnneeRepository;
import com.mproduits.repositories.EmployeurRepository;

import com.mproduits.repositories.EntrepriseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.ZoneId;

/**
 * Service métier pour la gestion des entreprises
 * 
 * @author Système de Gestion
 */
@Slf4j
@Service
@Transactional
public class EntrepriseService {

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    @Autowired
    private AnneeRepository anneeRepository;

    @Autowired
    private EmployeurRepository employeurRepository;

    /**
     * Crée une nouvelle entreprise
     * Si actif = true, désactive les autres entreprises du même employeur pour cette année
     */
    public EntrepriseResponse create(EntrepriseCreateRequest request) {
        log.info("📝 Création d'entreprise - Année: {}, Employeur: {}", 
                 request.getAnneeId(), request.getEmployeurId());

        // Vérifier si l'année existe
        Annee annee = anneeRepository.findById(request.getAnneeId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Année non trouvée avec l'ID: " + request.getAnneeId()));

        // Vérifier si l'employeur existe
        Employeur employeur = employeurRepository.findById(request.getEmployeurId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Employeur non trouvé avec l'ID: " + request.getEmployeurId()));

        // Vérifier si l'entreprise existe déjà
        EntreprisePK pk = new EntreprisePK(request.getAnneeId(), request.getEmployeurId());
        if (entrepriseRepository.existsById(pk)) {
            throw new IllegalArgumentException(
                "Une entreprise existe déjà pour cet employeur et cette année");
        }

        // Si actif = true, désactiver les autres entreprises
        if (Boolean.TRUE.equals(request.getActif())) {
            desactiverAutresEntreprises(request.getAnneeId(), request.getEmployeurId());
        }

        // Créer l'entreprise
        Entreprise entreprise = new Entreprise();
        entreprise.setEntreprisePK(pk);
        entreprise.setDirecteur(request.getDirecteur());
        entreprise.setActivite(request.getActivite());
        entreprise.setConventionCollective(request.getConventionCollective());
        entreprise.setSiteWeb(request.getSiteWeb());
        entreprise.setActif(request.getActif());
        entreprise.setTypeResponsable(request.getTypeResponsable());
        entreprise.setDateCreation(request.getDateCreation() != null ?  Date.from(request.getDateCreation().atStartOfDay(ZoneId.systemDefault()).toInstant()) : new Date());
        entreprise.setDateFinLicense(Date.from(request.getDateFinLicense().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        entreprise = entrepriseRepository.save(entreprise);

        log.info("✅ Entreprise créée avec succès - PK: {}", pk);

        return toResponse(entreprise);
    }

    /**
     * Met à jour une entreprise existante
     */
    public EntrepriseResponse update(Integer anneeId, Long employeurId, 
                                     EntrepriseCreateRequest request) {
        log.info("📝 Mise à jour d'entreprise - Année: {}, Employeur: {}", anneeId, employeurId);

        EntreprisePK pk = new EntreprisePK(anneeId, employeurId);
        Entreprise entreprise = entrepriseRepository.findById(pk)
            .orElseThrow(() -> new IllegalArgumentException(
                "Entreprise non trouvée pour cette année et cet employeur"));

        // Si on active cette entreprise, désactiver les autres
        if (Boolean.TRUE.equals(request.getActif()) && !Boolean.TRUE.equals(entreprise.getActif())) {
            desactiverAutresEntreprises(anneeId, employeurId);
        }

        // Mettre à jour les champs
        entreprise.setDirecteur(request.getDirecteur());
        entreprise.setActivite(request.getActivite());
        entreprise.setConventionCollective(request.getConventionCollective());
        entreprise.setSiteWeb(request.getSiteWeb());
        entreprise.setActif(request.getActif());
        entreprise.setTypeResponsable(request.getTypeResponsable());
        entreprise.setDateCreation(Date.from(request.getDateCreation()!=null ? request.getDateCreation().atStartOfDay(ZoneId.systemDefault()).toInstant():null));
        entreprise.setDateFinLicense(Date.from(request.getDateFinLicense()!=null ?request.getDateFinLicense().atStartOfDay(ZoneId.systemDefault()).toInstant(): null));

        entreprise = entrepriseRepository.save(entreprise);

        log.info("✅ Entreprise mise à jour avec succès");

        return toResponse(entreprise);
    }

    /**
     * Supprime une entreprise
     */
    public void delete(Integer anneeId, Long employeurId) {
        log.info("🗑️ Suppression d'entreprise - Année: {}, Employeur: {}", anneeId, employeurId);

        EntreprisePK pk = new EntreprisePK(anneeId, employeurId);
        if (!entrepriseRepository.existsById(pk)) {
            throw new IllegalArgumentException("Entreprise non trouvée");
        }

        entrepriseRepository.deleteById(pk);

        log.info("✅ Entreprise supprimée avec succès");
    }

    /**
     * Récupère une entreprise par sa clé composite
     */
    @Transactional(readOnly = true)
    public Optional<EntrepriseResponse> findById(Integer anneeId, Long employeurId) {
        EntreprisePK pk = new EntreprisePK(anneeId, employeurId);
        return entrepriseRepository.findById(pk)
            .map(this::toResponse);
    }

    /**
     * Récupère toutes les entreprises
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> findAll() {
        return entrepriseRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Récupère les entreprises par année
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> findByAnnee(Integer anneeId) {
        return entrepriseRepository.findByAnneeId(anneeId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Récupère les entreprises actives
     * @return 
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> findAllActive() {
        return entrepriseRepository.findAllActive().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Recherche d'entreprises avec filtres
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> search(Integer anneeId, Long employeurId, 
                                          Boolean actif, String searchTerm) {
        return entrepriseRepository.search(anneeId, employeurId, actif, searchTerm).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Active une entreprise par défaut (désactive les autres)
     */
    public void activerParDefaut(Integer anneeId, Long employeurId) {
        log.info("🔄 Activation par défaut - Année: {}, Employeur: {}", anneeId, employeurId);

        // Désactiver toutes les entreprises de cet employeur pour cette année
        desactiverAutresEntreprises(anneeId, employeurId);

        // Activer l'entreprise cible
        EntreprisePK pk = new EntreprisePK(anneeId, employeurId);
        Entreprise entreprise = entrepriseRepository.findById(pk)
            .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée"));

        entreprise.setActif(true);
        entrepriseRepository.save(entreprise);

        log.info("✅ Entreprise activée par défaut");
    }

    /**
     * Désactive toutes les autres entreprises d'un employeur pour une année
     */
    private void desactiverAutresEntreprises(Integer anneeId, Long employeurId) {
        log.info("🔄 Désactivation des autres entreprises - Année: {}, Employeur: {}", 
                 anneeId, employeurId);

        List<Entreprise> autresEntreprises = entrepriseRepository.findByEmployeurId(employeurId)
            .stream()
            .filter(e -> e.getEntreprisePK().getAnneeId()==(anneeId))
            .filter(e -> Boolean.TRUE.equals(e.getActif()))
            .collect(Collectors.toList());

        for (Entreprise autre : autresEntreprises) {
            autre.setActif(false);
            entrepriseRepository.save(autre);
        }

        log.info("✅ {} entreprise(s) désactivée(s)", autresEntreprises.size());
    }

    /**
     * Convertit une entité Entreprise en DTO Response
     */
    private EntrepriseResponse toResponse(Entreprise entreprise) {
        EntrepriseResponse response = new EntrepriseResponse();
        
        response.setAnneeId(entreprise.getEntreprisePK().getAnneeId());
        response.setEmployeurId(entreprise.getEntreprisePK().getEmployeurId());
        response.setDirecteur(entreprise.getDirecteur());
        response.setActivite(entreprise.getActivite());
        response.setConventionCollective(entreprise.getConventionCollective());
        response.setSiteWeb(entreprise.getSiteWeb());
        response.setActif(entreprise.getActif());
        response.setTypeResponsable(entreprise.getTypeResponsable());
        response.setDateCreation(entreprise.getDateCreation().toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate());
        response.setDateFinLicense(entreprise.getDateFinLicense()==null ? null:entreprise.getDateFinLicense().toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate());

        // Mapper l'année
        if (entreprise.getAnnee() != null) {
            EntrepriseResponse.AnneeInfo anneeInfo = new EntrepriseResponse.AnneeInfo();
            anneeInfo.setId(entreprise.getAnnee().getId().intValue());
            anneeInfo.setCode(entreprise.getAnnee().getCode());
            anneeInfo.setLibelle(entreprise.getAnnee().getLibelle());
            //anneeInfo.setActif(entreprise.getAnnee().getActif());
            response.setAnnee(anneeInfo);
        }

        // Mapper l'employeur
        if (entreprise.getEmployeur() != null) {
            EntrepriseResponse.EmployeurInfo employeurInfo = new EntrepriseResponse.EmployeurInfo();
            employeurInfo.setId(entreprise.getEmployeur().getId());
            employeurInfo.setMatricule(entreprise.getEmployeur().getPersonne().getUserName());
            employeurInfo.setSociete(entreprise.getEmployeur().getSociete());
            employeurInfo.setAbreviation(entreprise.getEmployeur().getAbreviation());
            employeurInfo.setTelephone(entreprise.getEmployeur().getPersonne().getTel());
            employeurInfo.setEmail(entreprise.getEmployeur().getPersonne().getEmail());
            //employeurInfo.setAdresse(entreprise.getEmployeur().get);
            employeurInfo.setVille(entreprise.getEmployeur().getPersonne().getVille());
            response.setEmployeur(employeurInfo);
        }

        return response;
    }
    
}
