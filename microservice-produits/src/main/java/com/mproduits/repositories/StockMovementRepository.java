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

    // Variantes scopees par compagnie (voir StockController /mouvements) -
    // passe par pointVente.depotId (toujours renseigne, boutique ou pas)
    // plutot que pointVente.boutique (nullable pour un depot de stock, ce
    // qui exclurait silencieusement ces lignes d'un join direct).
    @Query("SELECT sm FROM StockMovement sm WHERE sm.produit.id = :produitId " +
           "AND sm.pointVente.depotId.compagnie.id = :compagnieId ORDER BY sm.dateCreation DESC")
    List<StockMovement> findByProduitIdAndCompagnieId(@Param("produitId") Long produitId, @Param("compagnieId") Long compagnieId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.pointVente.id = :pointVenteId " +
           "AND sm.pointVente.depotId.compagnie.id = :compagnieId ORDER BY sm.dateCreation DESC")
    List<StockMovement> findByPointVenteIdAndCompagnieId(@Param("pointVenteId") Long pointVenteId, @Param("compagnieId") Long compagnieId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.pointVente.depotId.compagnie.id = :compagnieId ORDER BY sm.dateCreation DESC")
    List<StockMovement> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);

}
