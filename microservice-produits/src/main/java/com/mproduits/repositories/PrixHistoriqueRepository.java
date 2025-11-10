/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.PrixHistorique;
import feign.Param;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author USER01
 */
public interface PrixHistoriqueRepository extends JpaRepository<PrixHistorique, Long> {
    
    @Query("SELECT ph FROM PrixHistorique ph WHERE ph.produit.id = :produitId " +
           "AND ph.actif = true ORDER BY ph.dateCreation DESC LIMIT 1")
    Optional<PrixHistorique> findActiveByProduit(@Param("produitId") Long produitId);
    
    @Query("SELECT ph FROM PrixHistorique ph WHERE ph.produit.id = :produitId " +
           "AND ph.actif = true")
    List<PrixHistorique> findAllActiveByProduit(@Param("produitId") Long produitId);
}
