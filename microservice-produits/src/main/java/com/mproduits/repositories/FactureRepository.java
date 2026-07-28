package com.mproduits.repositories;

import com.mproduits.model.Facture;
import com.mproduits.dto.FactureStatistiques;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long>, JpaSpecificationExecutor<Facture> {
    
    @Query("SELECT f FROM Facture f WHERE f.client.id = :clientId")
    List<Facture> findByClientId(Long clientId);
    
    @Query("SELECT f FROM Facture f WHERE f.statut = :statut")
    Page<Facture> findByStatut(@Param("statut") String statut, Pageable pageable);
    
    @Query("SELECT f FROM Facture f WHERE " +
           "(:statut IS NULL OR f.statut = :statut) AND " +
           "(:clientId IS NULL OR f.client.id = :clientId)")
    Page<Facture> findByFilters(@Param("statut") String statut, 
                                 @Param("clientId") Long clientId, 
                                 Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM Facture f WHERE YEAR(f.dateFacture) = :year")
    int countByYear(@Param("year") int year);
    
    Optional<Facture> findByNumeroFacture(String numeroFacture);
    
    /**
     * Recherche toutes les factures d'un client donné,
     * triées par date de facture décroissante.
     */
    List<Facture> findByClientIdAndBoutiqueIdOrderByDateFactureDesc(Long clientId,Long boutiqueid);
    
    List<Facture> findByNombreRelances(int NombreRelances);
    
    /**
     * Calcule les statistiques des factures sur une période donnée
     * 
     * @param dateDebut date de début de la période
     * @param dateFin date de fin de la période
     * @return statistiques calculées
     */
//    @Query("SELECT new com.mproduits.dto.FactureStatistiques(" +
//           "COUNT(f), " +
//           "SUM(CASE WHEN f.statut = 'BROUILLON' THEN 1 ELSE 0 END), " +
//           "SUM(CASE WHEN f.statut = 'NON_PAYEE' THEN 1 ELSE 0 END), " +
//           "SUM(CASE WHEN f.statut = 'PARTIELLE' THEN 1 ELSE 0 END), " +
//           "SUM(CASE WHEN f.statut = 'PAYEE' THEN 1 ELSE 0 END), " +
//           "SUM(CASE WHEN f.statut = 'EN_RETARD' THEN 1 ELSE 0 END), " +
//           "SUM(CASE WHEN f.statut = 'ANNULEE' THEN 1 ELSE 0 END), " +
//           "COALESCE(SUM(f.totalHt), 0), " +
//           "COALESCE(SUM(f.totalTtc), 0), " +
//           "COALESCE(SUM(f.montantPaye), 0), " +
//           "COALESCE(SUM(f.soldeRestant), 0), " +
//           "COALESCE(SUM(CASE WHEN f.statut = 'EN_RETARD' THEN f.soldeRestant ELSE 0 END), 0), " +
//           "CASE WHEN SUM(f.totalTtc) > 0 THEN (SUM(f.montantPaye) * 100.0 / SUM(f.totalTtc)) ELSE 0 END, " +
//           "COALESCE(AVG(CASE WHEN f.datePaiementComplet IS NOT NULL " +
//           "THEN DATEDIFF(day, f.dateFacture, f.datePaiementComplet) ELSE NULL END), 0)) " +
//           "FROM Facture f " +
//           "WHERE f.dateFacture BETWEEN :dateDebut AND :dateFin")
//    FactureStatistiques calculerStatistiques(@Param("dateDebut") Date dateDebut, 
//                                             @Param("dateFin") Date dateFin);
    
     List<Facture> findByDateFactureBetween(Date dateDebut, Date dateFin);
    /**
     * Compte le nombre de factures par statut sur une période
     */
    @Query("SELECT f.statut, COUNT(f) FROM Facture f " +
           "WHERE f.dateFacture BETWEEN :dateDebut AND :dateFin " +
           "GROUP BY f.statut")
    List<Object[]> countByStatutBetweenDates(@Param("dateDebut") Date dateDebut, 
                                             @Param("dateFin") Date dateFin);
    
    /**
     * Trouve les factures en retard de paiement
     */
    @Query("SELECT f FROM Facture f WHERE " +
           "f.dateEcheance < CURRENT_DATE AND " +
           "f.soldeRestant > 0 AND " +
           "f.statut NOT IN ('PAYEE', 'ANNULEE')")
    List<Facture> findFacturesEnRetard();
    
    /**
     * Trouve les factures avec échéance proche (dans les N jours)
     */
    @Query("SELECT f FROM Facture f WHERE " +
           "f.dateEcheance BETWEEN CURRENT_DATE AND :dateLimit AND " +
           "f.soldeRestant > 0 AND " +
           "f.statut NOT IN ('PAYEE', 'ANNULEE')")
    List<Facture> findFacturesEcheanceProche(@Param("dateLimit") Date dateLimit);
    
    /**
     * Calcule le CA total sur une période
     */
    @Query("SELECT COALESCE(SUM(f.totalTtc), 0) FROM Facture f " +
           "WHERE f.dateFacture BETWEEN :dateDebut AND :dateFin " +
           "AND f.statut != 'ANNULEE'")
    BigDecimal calculerChiffreAffaire(@Param("dateDebut") Date dateDebut, 
                                      @Param("dateFin") Date dateFin);
    
    /**
     * Calcule le montant total impayé
     */
    @Query("SELECT COALESCE(SUM(f.soldeRestant), 0) FROM Facture f " +
           "WHERE f.statut NOT IN ('PAYEE', 'ANNULEE')")
    BigDecimal calculerMontantTotalImpaye();
    
    /**
     * Trouve les factures d'un client avec un solde impayé
     */
    @Query("SELECT f FROM Facture f WHERE " +
           "f.client.id = :clientId AND " +
           "f.soldeRestant > 0 AND " +
           "f.statut NOT IN ('PAYEE', 'ANNULEE') " +
           "ORDER BY f.dateEcheance ASC")
    List<Facture> findFacturesImpayeesByClient(@Param("clientId") Long clientId);
}