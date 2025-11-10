/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.dto.VersementSummary;
import com.mproduits.model.Client;

import com.mproduits.model.VersementClient;
import feign.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;

/**
 *
 * @author USER01
 */
public interface VersementClientRepository extends JpaRepository<VersementClient, Long> , JpaSpecificationExecutor<VersementClient>{

//    @Query("SELECT v FROM VersementClient v WHERE v.facture.id= :factureId")
//    List<VersementClient> findByFactureId(@Param("factureId")Long factureId);

    @Query("SELECT SUM(v.montant) FROM VersementClient v WHERE v.facture.id = :factureId")
    BigDecimal sumMontantByFactureId(@Param("factureId") Long factureId);
    
      @Query("SELECT vc FROM VersementClient vc WHERE vc.facture.id = :factureId " +
           "ORDER BY vc.dateVersement DESC")
    List<VersementClient> findByFactureId(@Param("factureId") Long factureId);
    
    Optional<VersementClient> findByNumeroVersement(String numeroVersement);
    @Query("SELECT vc FROM VersementClient vc WHERE vc.client.id = :clientId ORDER BY vc.dateVersement DESC")
   List<VersementClient> findByClientIdOrderByDateVersementDesc(@Param("clientId") Long clientId);
    
  // ========================================================================
    // RECHERCHES PAR FACTURE
    // ========================================================================
    
    /**
     * Récupérer tous les versements d'une facture
     */
    //@Query("SELECT v FROM VersementClient v WHERE v.facture.id = :factureId ORDER BY v.date DESC")
   //List<VersementClient> findByFactureId(@Param("factureId") Long factureId);
    
    /**
     * Calculer le total des versements pour une facture
     */
    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM VersementClient v WHERE v.facture.id = :factureId")
    BigDecimal sumVersementsByFacture(@Param("factureId") Long factureId);
    
    /**
     * Vérifier si une facture a des versements
     */
    boolean existsByFactureId(Long factureId);
    
    /**
     * Compter le nombre de versements pour une facture
     */
    @Query("SELECT COUNT(v) FROM VersementClient v WHERE v.facture.id = :factureId")
    Long countByFactureId(@Param("factureId") Long factureId);

    // ========================================================================
    // RECHERCHES PAR CLIENT
    // ========================================================================
    
    /**
     * Récupérer tous les versements d'un client
     */
    @Query("SELECT v FROM VersementClient v WHERE v.client.id = :clientId ORDER BY v.dateVersement DESC")
    List<VersementClient> findByClientId(@Param("clientId") Long clientId);
    
    /**
     * Calculer le total des versements pour un client
     */
    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM VersementClient v WHERE v.client.id = :clientId")
    BigDecimal sumVersementsByClient(@Param("clientId") Long clientId);
    
    /**
     * Récupérer les versements d'un client entre deux dates
     */
    @Query("SELECT v FROM VersementClient v WHERE v.client.id = :clientId " +
           "AND v.dateVersement BETWEEN :dateDebut AND :dateFin ORDER BY v.dateVersement DESC")
    List<VersementClient> findByClientIdAndDateBetween(
        @Param("clientId") Long clientId,
        @Param("dateDebut") Date dateDebut,
        @Param("dateFin") Date dateFin
    );

    // ========================================================================
    // RECHERCHES PAR MODE DE PAIEMENT
    // ========================================================================
    
    /**
     * Récupérer les versements par mode de paiement
     */
    @Query("SELECT v FROM VersementClient v WHERE v.modePaiement = :modePaiement ORDER BY v.dateVersement DESC")
    List<VersementClient> findByModePaiement(@Param("modePaiement") String modePaiement);
    
    /**
     * Calculer le total des versements par mode de paiement
     */
    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM VersementClient v WHERE v.modePaiement = :modePaiement")
    BigDecimal sumByModePaiement(@Param("modePaiement") String modePaiement);
    
    /**
     * Compter les versements par mode de paiement pour une période
     */
    @Query("SELECT COUNT(v) FROM VersementClient v WHERE v.modePaiement = :modePaiement " +
           "AND v.dateVersement BETWEEN :dateDebut AND :dateFin")
    Long countByModePaiementAndDateBetween(
        @Param("modePaiement") String modePaiement,
        @Param("dateDebut") Date dateDebut,
        @Param("dateFin") Date dateFin
    );

    // ========================================================================
    // RECHERCHES PAR RÉFÉRENCE
    // ========================================================================
    
    /**
     * Rechercher un versement par référence de paiement
     */
    @Query("SELECT v FROM VersementClient v WHERE v.referencePaiement = :reference")
    Optional<VersementClient> findByReferencePaiement(@Param("reference") String reference);
    
    /**
     * Vérifier si une référence de paiement existe déjà
     */
    boolean existsByReferencePaiement(String referencePaiement);

    // ========================================================================
    // RECHERCHES PAR PÉRIODE
    // ========================================================================
    
    /**
     * Récupérer les versements entre deux dates
     */
    @Query("SELECT v FROM VersementClient v WHERE v.dateVersement BETWEEN :dateDebut AND :dateFin ORDER BY v.dateVersement DESC")
    List<VersementClient> findByDateBetween(
        @Param("dateDebut") Date dateDebut,
        @Param("dateFin") Date dateFin
    );
    
    /**
     * Calculer le total des versements pour une période
     */
    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM VersementClient v " +
           "WHERE v.dateVersement BETWEEN :dateDebut AND :dateFin")
    BigDecimal sumByDateBetween(
        @Param("dateDebut") Date dateDebut,
        @Param("dateFin") Date dateFin
    );
    
    /**
     * Récupérer les versements du jour
     */
    @Query("SELECT v FROM VersementClient v WHERE DATE(v.dateVersement) = CURRENT_DATE ORDER BY v.dateVersement DESC")
    List<VersementClient> findVersementsDuJour();
    
    /**
     * Récupérer les versements de la semaine
     */
    @Query("SELECT v FROM VersementClient v WHERE YEARWEEK(v.dateVersement, 1) = YEARWEEK(CURRENT_DATE, 1) " +
           "ORDER BY v.dateVersement DESC")
    List<VersementClient> findVersementsDeLaSemaine();
    
    /**
     * Récupérer les versements du mois
     */
    @Query("SELECT v FROM VersementClient v WHERE YEAR(v.dateVersement) = YEAR(CURRENT_DATE) " +
           "AND MONTH(v.dateVersement) = MONTH(CURRENT_DATE) ORDER BY v.dateVersement DESC")
    List<VersementClient> findVersementsDuMois();
    
    /**
     * Récupérer les versements de l'année
     */
    @Query("SELECT v FROM VersementClient v WHERE YEAR(v.dateVersement) = YEAR(CURRENT_DATE) ORDER BY v.dateVersement DESC")
    List<VersementClient> findVersementsDeLAnnee();

    // ========================================================================
    // STATISTIQUES ET ANALYSES
    // ========================================================================
    
    /**
     * Récupérer les derniers versements (limité)
     */
    @Query("SELECT v FROM VersementClient v ORDER BY v.dateVersement DESC")
    List<VersementClient> findTop10ByOrderByDateDesc();
    
    /**
     * Récupérer les versements supérieurs à un montant
     */
    @Query("SELECT v FROM VersementClient v WHERE v.montant >= :montantMin ORDER BY v.montant DESC")
    List<VersementClient> findByMontantGreaterThanEqual(@Param("montantMin") BigDecimal montantMin);
    
    /**
     * Calculer le montant moyen des versements
     */
    @Query("SELECT AVG(v.montant) FROM VersementClient v")
    BigDecimal calculateMontantMoyen();
    
    /**
     * Récupérer le montant maximum des versements
     */
    @Query("SELECT MAX(v.montant) FROM VersementClient v")
    BigDecimal findMontantMax();
    
    /**
     * Récupérer le montant minimum des versements
     */
    @Query("SELECT MIN(v.montant) FROM VersementClient v")
    BigDecimal findMontantMin();
    
    /**
     * Compter le nombre total de versements
     */
    @Query("SELECT COUNT(v) FROM VersementClient v")
    Long countAllVersements();
    
    /**
     * Calculer le montant total de tous les versements
     */
    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM VersementClient v")
    BigDecimal sumTotalVersements();

    // ========================================================================
    // REQUÊTES COMPLEXES
    // ========================================================================
    
    /**
     * Récupérer les versements avec facture et client (jointure)
     */
    @Query("SELECT v FROM VersementClient v " +
           "JOIN FETCH v.facture f " +
           "JOIN FETCH v.client c " +
           "WHERE v.id = :id")
    Optional<VersementClient> findByIdWithFactureAndClient(@Param("id") Long id);
    
    /**
     * Récupérer les versements non validés (si vous avez un statut)
     */
    @Query("SELECT v FROM VersementClient v WHERE v.statut = 'EN_ATTENTE' ORDER BY v.dateVersement DESC")
    List<VersementClient> findVersementsEnAttente();
    
    /**
     * Récupérer les versements par client et mode de paiement
     */
    @Query("SELECT v FROM VersementClient v WHERE v.client.id = :clientId " +
           "AND v.modePaiement = :modePaiement ORDER BY v.dateVersement DESC")
    List<VersementClient> findByClientIdAndModePaiement(
        @Param("clientId") Long clientId,
        @Param("modePaiement") String modePaiement
    );
    
    /**
     * Recherche multicritères (recherche globale)
     */
    @Query("SELECT v FROM VersementClient v " +
           "WHERE (:clientId IS NULL OR v.client.id = :clientId) " +
           "AND (:factureId IS NULL OR v.facture.id = :factureId) " +
           "AND (:modePaiement IS NULL OR v.modePaiement = :modePaiement) " +
           "AND (:dateDebut IS NULL OR v.dateVersement >= :dateDebut) " +
           "AND (:dateFin IS NULL OR v.dateVersement <= :dateFin) " +
           "ORDER BY v.dateVersement DESC")
    List<VersementClient> rechercheMulticriteres(
        @Param("clientId") Long clientId,
        @Param("factureId") Long factureId,
        @Param("modePaiement") String modePaiement,
        @Param("dateDebut") Date dateDebut,
        @Param("dateFin") Date dateFin
    );
    
    /**
     * Récupérer les statistiques par mode de paiement
     */
    @Query("SELECT v.modePaiement, COUNT(v), SUM(v.montant) " +
           "FROM VersementClient v " +
           "GROUP BY v.modePaiement")
    List<Object[]> getStatistiquesParModePaiement();
    
    /**
     * Récupérer les statistiques par client
     */
    @Query("SELECT v.client.id, v.client.nom, COUNT(v), SUM(v.montant) " +
           "FROM VersementClient v " +
           "GROUP BY v.client.id, v.client.nom " +
           "ORDER BY SUM(v.montant) DESC")
    List<Object[]> getStatistiquesParClient();
    
    // Récupérer les versements résumés d'un client
//List<VersementSummary> findVersementsSummaryByClient(Long clientId);

// Compter les versements d'un client
//Long countVersementsByClient(Long clientId);

// Compter les factures distinctes payées par un client
//Long countDistinctFacturesByClientId(Long clientId);

// Calculer le montant total des factures d'un client
//BigDecimal sumTotalFacturesByClient(Long clientId);
@Query("SELECT SUM(v.montant) FROM VersementClient v WHERE v.client.id = :clientId")
    BigDecimal sumTotalFacturesByClient(@Param("clientId") Long clientId);

// Calculer le délai moyen de paiement d'un client
//Double calculateDelaiMoyenPaiementClient(Long clientId);

    @Query(value = """
        SELECT 
            COUNT(*) AS totalVersements,
            SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS versementsEnAttente,
            SUM(CASE WHEN statut = 'VALIDE' THEN 1 ELSE 0 END) AS versementsValides,
            SUM(CASE WHEN statut = 'ANNULE' THEN 1 ELSE 0 END) AS versementsAnnules,
            COALESCE(SUM(montant), 0) AS montantTotalVersements,
            COALESCE(SUM(CASE WHEN statut = 'EN_ATTENTE' THEN montant ELSE 0 END), 0) AS montantEnAttente,
            COALESCE(SUM(CASE WHEN statut = 'VALIDE' THEN montant ELSE 0 END), 0) AS montantValide
        FROM VersementClient
        WHERE dateVersement BETWEEN :dateDebut AND :dateFin
        """, nativeQuery = true)
    Map<String, Object> getStatistiquesGlobale(@Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin);

    @Query(value = """
        SELECT 
            mode_paiement AS mode,
            COUNT(*) AS nombre,
            COALESCE(SUM(montant), 0) AS total
        FROM VersementClient
        WHERE dateVersement BETWEEN :dateDebut AND :dateFin
        GROUP BY mode_paiement
        """, nativeQuery = true)
    List<Map<String, Object>> getStatistiquesParMode(@Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin);
    
@Query("SELECT v  FROM VersementClient v  WHERE v.client.id= :clientId and dateVersement BETWEEN :dateDebut AND :dateFin")
List<VersementClient> findByClientIdAndDateVersementBetweenOrderByDateVersementDesc(@Param("clientId")Long clientId, @Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin);
//lister les clients qui ont deja effectuer un versement  
@Query("SELECT DISTINCT v.client FROM VersementClient v")
List<Client> listeClientVersement();
}
