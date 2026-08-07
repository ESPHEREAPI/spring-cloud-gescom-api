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
     * Trouve l'entreprise (exercice) d'une année donnée, toutes compagnies confondues -
     * a n'utiliser que dans des contextes deja scopes (jamais directement depuis un
     * endpoint metier, voir EntrepriseService#compagnieCourante).
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "ORDER BY e.compagnie.nom ASC")
  Optional<Entreprise> findByAnneeId(@Param("anneeId") Integer anneeId);

    /**
     * Variante scopee par compagnie de findByAnneeId - a utiliser des qu'un
     * appelant metier a besoin de l'exercice d'une annee donnee pour SA
     * compagnie (evite de recuperer l'exercice d'une autre compagnie qui
     * partagerait la meme annee).
     */
    Optional<Entreprise> findByEntreprisePK_AnneeIdAndEntreprisePK_CompagnieId(Integer anneeId, Long compagnieId);

    /**
     * Trouve tous les exercices d'une compagnie
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.compagnieId = :compagnieId " +
           "ORDER BY e.annee.code DESC")
    List<Entreprise> findByCompagnieId(@Param("compagnieId") Long compagnieId);

    /**
     * Trouve l'exercice actif d'une compagnie pour une année
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.entreprisePK.compagnieId = :compagnieId " +
           "AND e.actif = true")
    Optional<Entreprise> findActiveEntreprise(
        @Param("anneeId") Integer anneeId,
        @Param("compagnieId") Long compagnieId
    );

    /**
     * Trouve toutes les entreprises actives, toutes compagnies confondues - a filtrer
     * par compagnie cote appelant (voir EntrepriseService#findAllActive).
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.actif = true " +
           "ORDER BY e.compagnie.nom ASC")
    List<Entreprise> findAllActive();

    /**
     * Trouve toutes les entreprises actives d'une année, toutes compagnies confondues.
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.actif = true " +
           "ORDER BY e.compagnie.nom ASC")
    List<Entreprise> findActiveByAnneeId(@Param("anneeId") Integer anneeId);

    /**
     * Vérifie si une entreprise existe pour une année et une compagnie
     */
    @Query("SELECT COUNT(e) > 0 FROM Entreprise e " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.entreprisePK.compagnieId = :compagnieId")
    boolean existsByAnneeAndCompagnie(
        @Param("anneeId") Integer anneeId,
        @Param("compagnieId") Long compagnieId
    );

    /**
     * Désactive toutes les entreprises d'une compagnie pour une année
     */
    @Query("UPDATE Entreprise e SET e.actif = false " +
           "WHERE e.entreprisePK.anneeId = :anneeId " +
           "AND e.entreprisePK.compagnieId = :compagnieId")
    void deactivateAllForCompagnie(
        @Param("anneeId") Integer anneeId,
        @Param("compagnieId") Long compagnieId
    );

    /**
     * Compte le nombre d'entreprises actives
     */
    @Query("SELECT COUNT(e) FROM Entreprise e WHERE e.actif = true")
    long countActive();

    /**
     * Recherche d'exercices avec filtres, toujours restreinte a une compagnie
     * (voir EntrepriseService#search - compagnieId n'est jamais fourni par le client).
     */
    @Query("SELECT e FROM Entreprise e " +
           "WHERE (:anneeId IS NULL OR e.entreprisePK.anneeId = :anneeId) " +
           "AND e.entreprisePK.compagnieId = :compagnieId " +
           "AND (:actif IS NULL OR e.actif = :actif) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(e.compagnie.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(e.directeur) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY e.compagnie.nom ASC")
    List<Entreprise> search(
        @Param("anneeId") Integer anneeId,
        @Param("compagnieId") Long compagnieId,
        @Param("actif") Boolean actif,
        @Param("searchTerm") String searchTerm
    );

     boolean existsById(EntreprisePK pk);

    /**
     * @deprecated recherche globale non scopee par compagnie - conservee uniquement
     * pour compatibilite avec les appelants existants non encore migres vers
     * findActiveEntreprise(anneeId, compagnieId). Ne pas utiliser dans du code neuf.
     */
    @Deprecated
    Entreprise findByActif(Boolean actif);

    /** Exercice actif d'une compagnie, quelle que soit l'annee. */
    Optional<Entreprise> findFirstByEntreprisePK_CompagnieIdAndActifTrue(Long compagnieId);
}
