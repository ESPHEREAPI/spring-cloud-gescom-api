/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.StockMovement;
import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 *
 * @author USER01
 */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.produit.id = :produitId " +
           "ORDER BY sm.dateCreation DESC")
    List<StockMovement> findByProduitId(@Param("produitId") Long produitId);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.pointVente.id = :pointVenteId " +
           "ORDER BY sm.dateCreation DESC")
    List<StockMovement> findByPointVenteId(@Param("pointVenteId") Long pointVenteId);
    
}
