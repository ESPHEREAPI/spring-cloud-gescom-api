/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

/**
 *
 * @author USER01
 */

import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Entreprise
 * 
 * @author Système de Gestion
 */
@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, EntreprisePK> {

    /**
     * Trouve toutes les entreprises d'une année donnée
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "ORDER BY e.employeur.societe ASC")
  Optional<Entreprise> findByAnneeId(@Param("anneeId") Integer anneeId);

    /**
     * Trouve toutes les entreprises d'un employeur
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.employeurId = :employeurId " +
           "ORDER BY e.annee.code DESC")
    List<Entreprise> findByEmployeurId(@Param("employeurId") Long employeurId);

    /**
     * Trouve l'entreprise active d'un employeur pour une année
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.entreprisePK.employeurId = :employeurId " +
           "AND e.actif = true")
    Optional<Entreprise> findActiveEntreprise(
        @Param("anneeId") Integer anneeId,
        @Param("employeurId") Long employeurId
    );

    /**
     * Trouve toutes les entreprises actives
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.actif = true " +
           "ORDER BY e.employeur.societe ASC")
    List<Entreprise> findAllActive();

    /**
     * Trouve toutes les entreprises actives d'une année
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.actif = true " +
           "ORDER BY e.employeur.societe ASC")
    List<Entreprise> findActiveByAnneeId(@Param("anneeId") Integer anneeId);

    /**
     * Vérifie si une entreprise existe pour une année et un employeur
     */
    @Query("SELECT COUNT(e) > 0 FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.entreprisePK.employeurId = :employeurId")
    boolean existsByAnneeAndEmployeur(
        @Param("anneeId") Integer anneeId,
        @Param("employeurId") Long employeurId
    );

    /**
     * Désactive toutes les entreprises d'un employeur pour une année
     */
    @Query("UPDATE Entreprise e SET e.actif = false " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.entreprisePK.employeurId = :employeurId")
    void deactivateAllForEmployeur(
        @Param("anneeId") Integer anneeId,
        @Param("employeurId") Long employeurId
    );

    /**
     * Compte le nombre d'entreprises actives
     */
    @Query("SELECT COUNT(e) FROM Entreprise e WHERE e.actif = true")
    long countActive();

    /**
     * Recherche d'entreprises avec filtres
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE (:anneeId IS NULL OR e.entreprisePK.anneeId = :anneeId) " +
           "AND (:employeurId IS NULL OR e.entreprisePK.employeurId = :employeurId) " +
           "AND (:actif IS NULL OR e.actif = :actif) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(e.employeur.societe) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(e.employeur.personne.userName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(e.directeur) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY e.employeur.societe ASC")
    List<Entreprise> search(
        @Param("anneeId") Integer anneeId,
        @Param("employeurId") Long employeurId,
        @Param("actif") Boolean actif,
        @Param("searchTerm") String searchTerm
    );
    
     boolean existsById(EntreprisePK pk);

    @Query("SELECT e FROM Entreprise e WHERE e.employeur.id = :employeurId AND e.actif = true")
    List<Entreprise> findActiveByEmployeurId(@Param("employeurId") Long employeurId);

    @Query("SELECT e FROM Entreprise e WHERE e.employeur.id = :employeurId ORDER BY e.annee.code DESC")
    List<Entreprise> findAllByEmployeurId(@Param("employeurId") Long employeurId);
     Entreprise findByActif(Boolean actif);
}
