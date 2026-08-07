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
import com.mproduits.model.Compagnie;
import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import com.mproduits.repositories.AnneeRepository;

import com.mproduits.repositories.EntrepriseRepository;
import com.mproduits.security.TenantContext;
import com.mproduits.exceptions.BadRequestException;
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
    private TenantContext tenantContext;

    /** La compagnie n'est jamais fournie par le client - toujours derivee du token JWT de l'appelant. */
    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    /**
     * Exercice actif d'une compagnie, cree automatiquement (annee civile
     * courante) si la compagnie n'en a encore aucun - ex: toute nouvelle
     * compagnie, jamais explicitement provisionnee (pas d'appel synchrone
     * entre microservice-administration et microservice-produits a la
     * creation d'une compagnie). Auto-guerison plutot que blocage : une
     * compagnie doit toujours pouvoir vendre sans etape de configuration
     * manuelle prealable.
     */
    @Transactional
    public Entreprise obtenirOuCreerExerciceActif(Long compagnieId) {
        return entrepriseRepository.findFirstByEntreprisePK_CompagnieIdAndActifTrue(compagnieId)
                .orElseGet(() -> creerExercicePourAnneeCourante(compagnieId));
    }

    private Entreprise creerExercicePourAnneeCourante(Long compagnieId) {
        int anneeCourante = java.time.Year.now().getValue();
        Annee annee = anneeRepository.findByCode(String.valueOf(anneeCourante))
                .orElseGet(() -> {
                    Annee nouvelle = new Annee(anneeCourante, String.valueOf(anneeCourante));
                    nouvelle.setLibelle("Annee " + anneeCourante);
                    return anneeRepository.save(nouvelle);
                });

        Entreprise entreprise = new Entreprise();
        entreprise.setEntreprisePK(new EntreprisePK(annee.getId(), compagnieId));
        entreprise.setActif(true);
        entreprise.setDateCreation(new Date());
        Entreprise saved = entrepriseRepository.save(entreprise);
        log.info("✅ Exercice {} auto-provisionne pour la compagnie {}", annee.getCode(), compagnieId);
        return saved;
    }

    /**
     * Crée une nouvelle entreprise (exercice) pour la compagnie courante.
     * Si actif = true, désactive les autres exercices de cette compagnie pour cette année.
     */
    public EntrepriseResponse create(EntrepriseCreateRequest request) {
        Long compagnieId = compagnieCourante();
        log.info("📝 Création d'entreprise - Année: {}, Compagnie: {}",
                 request.getAnneeId(), compagnieId);

        // Vérifier si l'année existe
        Annee annee = anneeRepository.findById(request.getAnneeId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Année non trouvée avec l'ID: " + request.getAnneeId()));

        // Vérifier si l'entreprise existe déjà
        EntreprisePK pk = new EntreprisePK(request.getAnneeId(), compagnieId);
        if (entrepriseRepository.existsById(pk)) {
            throw new IllegalArgumentException(
                "Une entreprise existe déjà pour cette compagnie et cette année");
        }

        // Si actif = true, désactiver les autres entreprises
        if (Boolean.TRUE.equals(request.getActif())) {
            desactiverAutresEntreprises(request.getAnneeId(), compagnieId);
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
        entreprise.setDateFinLicense(request.getDateFinLicense() != null ? Date.from(request.getDateFinLicense().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);

        entreprise = entrepriseRepository.save(entreprise);

        log.info("✅ Entreprise créée avec succès - PK: {}", pk);

        return toResponse(entreprise);
    }

    /**
     * Met à jour l'exercice d'une année pour la compagnie courante.
     */
    public EntrepriseResponse update(Integer anneeId, EntrepriseCreateRequest request) {
        Long compagnieId = compagnieCourante();
        log.info("📝 Mise à jour d'entreprise - Année: {}, Compagnie: {}", anneeId, compagnieId);

        EntreprisePK pk = new EntreprisePK(anneeId, compagnieId);
        Entreprise entreprise = entrepriseRepository.findById(pk)
            .orElseThrow(() -> new IllegalArgumentException(
                "Entreprise non trouvée pour cette année"));

        // Si on active cette entreprise, désactiver les autres
        if (Boolean.TRUE.equals(request.getActif()) && !Boolean.TRUE.equals(entreprise.getActif())) {
            desactiverAutresEntreprises(anneeId, compagnieId);
        }

        // Mettre à jour les champs
        entreprise.setDirecteur(request.getDirecteur());
        entreprise.setActivite(request.getActivite());
        entreprise.setConventionCollective(request.getConventionCollective());
        entreprise.setSiteWeb(request.getSiteWeb());
        entreprise.setActif(request.getActif());
        entreprise.setTypeResponsable(request.getTypeResponsable());
        entreprise.setDateCreation(request.getDateCreation() != null ? Date.from(request.getDateCreation().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
        entreprise.setDateFinLicense(request.getDateFinLicense() != null ? Date.from(request.getDateFinLicense().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);

        entreprise = entrepriseRepository.save(entreprise);

        log.info("✅ Entreprise mise à jour avec succès");

        return toResponse(entreprise);
    }

    /**
     * Supprime l'exercice d'une année pour la compagnie courante.
     */
    public void delete(Integer anneeId) {
        Long compagnieId = compagnieCourante();
        log.info("🗑️ Suppression d'entreprise - Année: {}, Compagnie: {}", anneeId, compagnieId);

        EntreprisePK pk = new EntreprisePK(anneeId, compagnieId);
        if (!entrepriseRepository.existsById(pk)) {
            throw new IllegalArgumentException("Entreprise non trouvée");
        }

        entrepriseRepository.deleteById(pk);

        log.info("✅ Entreprise supprimée avec succès");
    }

    /**
     * Récupère l'exercice d'une année pour la compagnie courante.
     */
    @Transactional(readOnly = true)
    public Optional<EntrepriseResponse> findById(Integer anneeId) {
        EntreprisePK pk = new EntreprisePK(anneeId, compagnieCourante());
        return entrepriseRepository.findById(pk)
            .map(this::toResponse);
    }

    /**
     * Récupère tous les exercices de la compagnie courante.
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> findAll() {
        return entrepriseRepository.findByCompagnieId(compagnieCourante()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Récupère l'exercice de la compagnie courante pour une année (au plus un resultat).
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> findByAnnee(Integer anneeId) {
        Long compagnieId = compagnieCourante();
        return entrepriseRepository.findByAnneeId(anneeId).stream()
            .filter(e -> e.getEntreprisePK().getCompagnieId() == compagnieId)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Récupère l'exercice actif de la compagnie courante.
     * @return
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> findAllActive() {
        Long compagnieId = compagnieCourante();
        return entrepriseRepository.findAllActive().stream()
            .filter(e -> e.getEntreprisePK().getCompagnieId() == compagnieId)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Recherche d'exercices avec filtres, toujours restreinte a la compagnie courante.
     */
    @Transactional(readOnly = true)
    public List<EntrepriseResponse> search(Integer anneeId, Boolean actif, String searchTerm) {
        Long compagnieId = compagnieCourante();
        return entrepriseRepository.search(anneeId, compagnieId, actif, searchTerm).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Active un exercice par défaut pour la compagnie courante (désactive les autres).
     */
    public void activerParDefaut(Integer anneeId) {
        Long compagnieId = compagnieCourante();
        log.info("🔄 Activation par défaut - Année: {}, Compagnie: {}", anneeId, compagnieId);

        // Désactiver tous les exercices de cette compagnie pour cette année
        desactiverAutresEntreprises(anneeId, compagnieId);

        // Activer l'entreprise cible
        EntreprisePK pk = new EntreprisePK(anneeId, compagnieId);
        Entreprise entreprise = entrepriseRepository.findById(pk)
            .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée"));

        entreprise.setActif(true);
        entrepriseRepository.save(entreprise);

        log.info("✅ Entreprise activée par défaut");
    }

    /**
     * Désactive tous les autres exercices d'une compagnie pour une année.
     */
    private void desactiverAutresEntreprises(Integer anneeId, Long compagnieId) {
        log.info("🔄 Désactivation des autres entreprises - Année: {}, Compagnie: {}",
                 anneeId, compagnieId);

        List<Entreprise> autresEntreprises = entrepriseRepository.findByCompagnieId(compagnieId)
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
        response.setCompagnieId(entreprise.getEntreprisePK().getCompagnieId());
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

        // Mapper la compagnie
        if (entreprise.getCompagnie() != null) {
            EntrepriseResponse.CompagnieInfo compagnieInfo = new EntrepriseResponse.CompagnieInfo();
            compagnieInfo.setId(entreprise.getCompagnie().getId());
            compagnieInfo.setNom(entreprise.getCompagnie().getNom());
            compagnieInfo.setTelephone(entreprise.getCompagnie().getTel());
            compagnieInfo.setEmail(entreprise.getCompagnie().getEmail());
            compagnieInfo.setVille(entreprise.getCompagnie().getVille());
            response.setCompagnie(compagnieInfo);
        }

        return response;
    }

}
