/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Client;
import feign.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByNomContainingIgnoreCase(String nom);
    // ✅ PAR STATUT
    Page<Client> findByStatut(String statut, Pageable pageable);
    List<Client> findAllByStatut(String statut);
    long countByStatut(String statut);
    
    // ✅ FIDÉLITÉ
    List<Client> findByFideliteTrue();
    long countByFideliteTrue();
    
    // ✅ PAR VILLE/RÉGION
    List<Client> findByVille(String ville);
    List<Client> findByRegion(String region);
    
    // ✅ RECHERCHE
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.nom) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Client> search(@Param("term") String term);
    /**
 * Recherche optimisée avec LIMIT dans la requête
 */
@Query("SELECT c FROM Client c WHERE " +
       "(LOWER(c.nom) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
       "LOWER(c.code) LIKE LOWER(CONCAT('%', :term, '%'))) " +
       "AND c.statut = 'ACTIF' " +
       "ORDER BY c.nom ASC")
Page<Client> searchActiveClients(
    @Param("term") String term, 
    Pageable pageable
);
}
