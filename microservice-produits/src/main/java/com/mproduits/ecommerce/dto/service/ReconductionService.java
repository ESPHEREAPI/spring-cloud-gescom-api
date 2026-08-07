/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

/**
 *
 * @author USER01
 */
import com.mproduits.dto.*;

import com.mproduits.model.*;
import com.mproduits.repositories.AnneeRepository;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.security.TenantContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service principal pour la gestion de la reconduction annuelle.
 * Orchestre toutes les étapes du processus de reconduction.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconductionService {

    private final EntrepriseRepositories entrepriseRepository;
    private final AnneeRepository anneeRepository;
    private final PointVenteRepositories pointVenteRepository;
    private final PrixArticlesRepositories prixArticlesRepository;
    private final ProduitRepositories produitRepository;
    private final TenantContext tenantContext;

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<String, ProgressionReconductionResponse> progressions = new ConcurrentHashMap<>();

    /**
     * Valide les pré-requis avant de lancer une reconduction. La compagnie
     * n'est jamais fournie par le client - toujours derivee du token JWT de
     * l'appelant (TenantContext), pour qu'une compagnie ne puisse jamais
     * reconduire l'exercice d'une autre.
     */
    public ValidationReconductionResponse validerPreRequis(ValidationReconductionRequest request) {
        Long compagnieId = tenantContext.currentCompagnieId();
        log.info("Validation des pré-requis: année {} -> {}, compagnie {}",
                request.getAnneeSourceId(), request.getAnneeCibleId(), compagnieId);

        ValidationReconductionResponse response = ValidationReconductionResponse.builder()
                .valide(true)
                .nombrePointsVente(0)
                .nombrePrixArticles(0)
                .build();

        // 1. Vérifier l'entreprise source
        EntreprisePK pkSource = new EntreprisePK(request.getAnneeSourceId(), compagnieId);
        Optional<Entreprise> entrepriseSourceOpt = entrepriseRepository.findById(pkSource);

        if (entrepriseSourceOpt.isEmpty() || !Boolean.TRUE.equals(entrepriseSourceOpt.get().getActif())) {
            response.setEntrepriseSourceExiste(false);
            response.ajouterErreur("L'entreprise source n'existe pas ou n'est pas active");
        } else {
            response.setEntrepriseSourceExiste(true);
        }

        // 2. Vérifier l'année cible
        Optional<Annee> anneeCibleOpt = anneeRepository.findById(request.getAnneeCibleId());
        if (anneeCibleOpt.isEmpty()) {
            response.setAnneeCibleValide(false);
            response.ajouterErreur("L'année cible n'existe pas");
        } else {
            response.setAnneeCibleValide(true);
        }

        // 3. Vérifier l'entreprise cible
        EntreprisePK pkCible = new EntreprisePK(request.getAnneeCibleId(), compagnieId);
        boolean cibleExiste = entrepriseRepository.existsById(pkCible);
        response.setEntrepriseCibleExiste(cibleExiste);

        if (cibleExiste) {
            response.ajouterErreur("L'entreprise cible existe déjà");
        }

        // 4. Compter les données
        if (response.getEntrepriseSourceExiste()) {
            Integer nbPointsVente = pointVenteRepository.countByEntreprise(pkSource);
            Integer nbPrixArticles = prixArticlesRepository.countByEntrepriseAndActif(pkSource, true);

            response.setNombrePointsVente(nbPointsVente);
            response.setNombrePrixArticles(nbPrixArticles);

            if (nbPointsVente == 0) {
                response.ajouterAvertissement("Aucun point de vente à reconduire");
            }

            // Estimer la durée
            long duree = (long) (nbPointsVente * 0.05 + nbPrixArticles * 0.02);
            response.setDureeEstimeeSecondes(Math.max(duree, 5L));
        }

        return response;
    }

    /**
     * Execute la reconduction complète.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 600)
    public ExecutionReconductionResponse executerReconduction(ExecutionReconductionRequest request) {
        String reconductionId = UUID.randomUUID().toString();
        LocalDateTime heureDebut = LocalDateTime.now();

        log.info("[{}] Démarrage reconduction: {} -> {}",
                reconductionId, request.getAnneeSourceId(), request.getAnneeCibleId());

        ProgressionReconductionResponse progression = ProgressionReconductionResponse.builder()
                .reconductionId(reconductionId)
                .statut(ProgressionReconductionResponse.StatutReconduction.INITIALISATION)
                .etapeActuelle(ProgressionReconductionResponse.EtapeReconduction.VALIDATION_PREREQ)
                .pourcentage(0)
                .heureDebut(heureDebut)
                .build();
        progressions.put(reconductionId, progression);

        try {
            // Validation
            progression.setStatut(ProgressionReconductionResponse.StatutReconduction.VALIDATION);
            validerOuLeverException(request);
            progression.calculerPourcentageGlobal(100);

            // Création entreprise
            progression.setEtapeActuelle(ProgressionReconductionResponse.EtapeReconduction.CREATION_ENTREPRISE);
            Entreprise nouvelleEntreprise = creerEntrepriseCible(request, progression);
            progression.calculerPourcentageGlobal(100);

            // Reconduction stocks
            int nbPointsVente = 0;
            if (request.getOptions().getCopierStocks()) {
                progression.setEtapeActuelle(ProgressionReconductionResponse.EtapeReconduction.RECONDUCTION_STOCKS);
                nbPointsVente = reconduirePointsVente(request, nouvelleEntreprise, progression);
            }

            // Reconduction prix
            int nbPrixArticles = 0;
            if (request.getOptions().getCopierPrix()) {
                progression.setEtapeActuelle(ProgressionReconductionResponse.EtapeReconduction.RECONDUCTION_PRIX);
                nbPrixArticles = reconduirePrixArticles(request, nouvelleEntreprise, progression);
            }

            // Désactivation source
            if (request.getOptions().getDesactiverSource()) {
                desactiverEntrepriseSource(request);
            }

            // Finalisation
            progression.setEtapeActuelle(ProgressionReconductionResponse.EtapeReconduction.FINALISATION);
            entityManager.flush();
            progression.calculerPourcentageGlobal(100);

            LocalDateTime heureFin = LocalDateTime.now();
            long duree = java.time.Duration.between(heureDebut, heureFin).getSeconds();

            progression.setStatut(ProgressionReconductionResponse.StatutReconduction.TERMINE);
            progression.setHeureFin(heureFin);
            progression.setPourcentage(100);

            ExecutionReconductionResponse.StatistiquesReconduction stats
                    = ExecutionReconductionResponse.StatistiquesReconduction.builder()
                            .pointsVenteReconduits(nbPointsVente)
                            .prixArticlesReconduits(nbPrixArticles)
                            .dureeExecutionSecondes(duree)
                            .batchSize(request.getOptions().getBatchSize())
                            .coefficientPrix(request.getOptions().getCoefficientPrix().toString())
                            .build();
            stats.calculerDureeFormatee();

            return ExecutionReconductionResponse.builder()
                    .success(true)
                    .reconductionId(reconductionId)
                    .entrepriseCree(ExecutionReconductionResponse.EntrepriseInfo.builder()
                            .anneeId(request.getAnneeCibleId())
                            .compagnieId(tenantContext.currentCompagnieId())
                            .build())
                    .statistiques(stats)
                    .heureDebut(heureDebut)
                    .heureFin(heureFin)
                    .build();

        } catch (Exception e) {
            log.error("[{}] Erreur reconduction", reconductionId, e);
            progression.setStatut(ProgressionReconductionResponse.StatutReconduction.ERREUR);
            progression.setMessageErreur(e.getMessage());
            throw new RuntimeException("Erreur reconduction: " + e.getMessage(), e);
        }
    }

    private void validerOuLeverException(ExecutionReconductionRequest request) {
        ValidationReconductionRequest validationRequest = ValidationReconductionRequest.builder()
                .anneeSourceId(request.getAnneeSourceId())
                .anneeCibleId(request.getAnneeCibleId())
                .build();

        ValidationReconductionResponse validation = validerPreRequis(validationRequest);
        if (!validation.getValide()) {
            throw new RuntimeException(String.join("; ", validation.getErreurs()));
        }
    }

    private Entreprise creerEntrepriseCible(ExecutionReconductionRequest request,
            ProgressionReconductionResponse progression) {
        Long compagnieId = tenantContext.currentCompagnieId();
        EntreprisePK pkSource = new EntreprisePK(request.getAnneeSourceId(), compagnieId);
        Entreprise source = entrepriseRepository.findById(pkSource)
                .orElseThrow(() -> new RuntimeException("Entreprise source introuvable"));

        EntreprisePK pkCible = new EntreprisePK(request.getAnneeCibleId(), compagnieId);
        Entreprise cible = new Entreprise();
        cible.setEntreprisePK(pkCible);
        cible.setActif(true);
        cible.setActivite(source.getActivite());
        cible.setConventionCollective(source.getConventionCollective());
        cible.setDirecteur(source.getDirecteur());
        cible.setSiteWeb(source.getSiteWeb());
        cible.setTypeResponsable(source.getTypeResponsable());
        cible.setCapital(source.getCapital());
        cible.setDateCreation(new Date());

        Annee annee = anneeRepository.findById(request.getAnneeCibleId())
                .orElseThrow(() -> new RuntimeException("Année cible introuvable"));
        cible.setAnnee(annee);
        cible.setCompagnie(source.getCompagnie());

        return entrepriseRepository.save(cible);
    }

    private int reconduirePointsVente(ExecutionReconductionRequest request,
            Entreprise nouvelleEntreprise,
            ProgressionReconductionResponse progression) {
        EntreprisePK pkSource = new EntreprisePK(request.getAnneeSourceId(), tenantContext.currentCompagnieId());
        List<PointVente> pointsSource = pointVenteRepository.findByEntreprise(pkSource);

        if (pointsSource.isEmpty()) {
            return 0;
        }

        int batchSize = request.getOptions().getBatchSize();
        List<PointVente> nouveaux = new ArrayList<>();
        int traites = 0;

        for (int i = 0; i < pointsSource.size(); i++) {
            PointVente source = pointsSource.get(i);

            if (request.getOptions().getIgnorerProduitsSupprimes()
                    && Boolean.TRUE.equals(source.getProduit().getDeletes())) {
                continue;
            }

            PointVente cible = new PointVente();
            cible.setEntreprise(nouvelleEntreprise);
            cible.setProduit(source.getProduit());
            cible.setBoutique(source.getBoutique());
            cible.setDepotId(source.getDepotId());

            BigDecimal stock = source.getStockFinalTheorie() != null
                    ? source.getStockFinalTheorie()
                    : (source.getStockInitial() != null ? source.getStockInitial() : BigDecimal.ZERO);

            if (stock.compareTo(BigDecimal.ZERO) < 0) {
                stock = BigDecimal.ZERO;
            }

            cible.setStockInitial(stock);
            cible.setStockFinalTheorie(stock);
            cible.setEntreeProduit(BigDecimal.ZERO);
            cible.setSortiProduit(BigDecimal.ZERO);
            cible.setPerte(BigDecimal.ZERO);
            cible.setDateReception(new Date());

            nouveaux.add(cible);

            if ((i + 1) % batchSize == 0 || i == pointsSource.size() - 1) {
                pointVenteRepository.saveAll(nouveaux);
                entityManager.flush();
                entityManager.clear();
                nouveaux.clear();

                traites = i + 1;
                int pourcent = (traites * 100) / pointsSource.size();
                progression.calculerPourcentageGlobal(pourcent);
            }
        }

        return traites;
    }

    private int reconduirePrixArticles(ExecutionReconductionRequest request,
            Entreprise nouvelleEntreprise,
            ProgressionReconductionResponse progression) {
        EntreprisePK pkSource = new EntreprisePK(request.getAnneeSourceId(), tenantContext.currentCompagnieId());
        List<PrixArticles> prixSource = prixArticlesRepository.findByEntrepriseAndActif(pkSource, true);

        if (prixSource.isEmpty()) {
            return 0;
        }

        int batchSize = request.getOptions().getBatchSize();
        List<PrixArticles> nouveaux = new ArrayList<>();
        BigDecimal coefficient = request.getOptions().getCoefficientPrix();

        EntreprisePK pkCible = nouvelleEntreprise.getEntreprisePK();
        List<PointVente> nouveauxPV = pointVenteRepository.findByEntreprise(pkCible);
        Map<String, PointVente> mapPV = new HashMap<>();
        for (PointVente pv : nouveauxPV) {
            String key = pv.getProduit().getId() + "_"
                    + (pv.getBoutique() != null ? pv.getBoutique().getId() : "null");
            mapPV.put(key, pv);
        }

        int traites = 0;
        for (int i = 0; i < prixSource.size(); i++) {
            PrixArticles source = prixSource.get(i);

            String key = source.getPointVente().getProduit().getId() + "_"
                    + (source.getPointVente().getBoutique() != null
                    ? source.getPointVente().getBoutique().getId() : "null");
            PointVente nouveauPV = mapPV.get(key);

            if (nouveauPV == null) {
                continue;
            }

            PrixArticles cible = new PrixArticles();
            cible.setEntreprise(nouvelleEntreprise);
            cible.setPointVente(nouveauPV);
            cible.setActif(true);
            cible.setDateCreation(new Date());

            BigDecimal prixNet = source.getPrixVenteNet()
                    .multiply(coefficient)
                    .setScale(2, RoundingMode.HALF_UP);
            cible.setPrixVenteNet(prixNet);
            cible.setTva(source.getTva());
            cible.setRemise(source.getRemise());

            BigDecimal tva = cible.getTva() != null ? cible.getTva() : BigDecimal.ZERO;
            BigDecimal prixTTC = prixNet.multiply(
                    BigDecimal.ONE.add(tva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
            ).setScale(2, RoundingMode.HALF_UP);
            cible.setPrixVenteTTC(prixTTC);

            nouveaux.add(cible);

            if ((i + 1) % batchSize == 0 || i == prixSource.size() - 1) {
                prixArticlesRepository.saveAll(nouveaux);
                entityManager.flush();
                entityManager.clear();
                nouveaux.clear();

                traites = i + 1;
                int pourcent = (traites * 100) / prixSource.size();
                progression.calculerPourcentageGlobal(pourcent);
            }
        }

        return traites;
    }

    private void desactiverEntrepriseSource(ExecutionReconductionRequest request) {
        EntreprisePK pkSource = new EntreprisePK(request.getAnneeSourceId(), tenantContext.currentCompagnieId());
        Entreprise source = entrepriseRepository.findById(pkSource)
                .orElseThrow(() -> new RuntimeException("Entreprise source introuvable"));
        source.setActif(false);
        entrepriseRepository.save(source);
    }

    public ProgressionReconductionResponse getProgression(String reconductionId) {
        ProgressionReconductionResponse progression = progressions.get(reconductionId);
        if (progression == null) {
            throw new RuntimeException("Reconduction introuvable: " + reconductionId);
        }
        return progression;
    }
}
