/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Magasin;
import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface MagasinRepositories  extends JpaRepository<Magasin, Long>{
    @Query("SELECT m FROM Magasin m WHERE m.boutique is not null GROUP BY m.boutique")
    List<Magasin> findByBoutiqueDistinctIsNotNull();

    List<Magasin> findByBoutiqueIsNotNullAndCompagnie_Id(Long compagnieId);
    Optional<Magasin> findByCode(String code);
    List<Magasin> findByLibelle(String libelle);
    
    /**
     * Récupère uniquement les magasins de stock (boutique = null)
     
    @Query("SELECT m FROM Magasin m WHERE m.boutique IS NULL")
    List<Magasin> findMagasinsDeStock();*/
    
    /**
     * Récupère uniquement les points de vente (boutique != null)
     
    @Query("SELECT m FROM Magasin m WHERE m.boutique IS NOT NULL")
    List<Magasin> findPointsDeVente();*/
    
    /**
     * Vérifie si un magasin est un magasin de stock
     */
    @Query("SELECT CASE WHEN m.boutique IS NULL THEN true ELSE false END FROM Magasin m WHERE m.id = :id")
    Boolean isMagasinDeStock(@Param("id") Long magasinId);
    
       /**
     * Trouve un magasin par son ID
     */
    Optional<Magasin> findById(Long id);

    /** Trouve un magasin par son ID, verifie qu'il appartient bien a la compagnie de l'appelant. */
    Optional<Magasin> findByIdAndCompagnie_Id(Long id, Long compagnieId);

    /**
     * Liste tous les magasins actifs
     */
    @Query("SELECT m FROM Magasin m ORDER BY m.libelle ASC")
    List<Magasin> findAllOrderByLibelle();

    /**
     * Récupère uniquement les magasins de stock (boutique = null)
     */
    @Query("SELECT m FROM Magasin m WHERE m.boutique IS NULL ORDER BY m.libelle ASC")
    List<Magasin> findMagasinsDeStock();

    /**
     * Récupère uniquement les points de vente (boutique != null)
     */
    @Query("SELECT m FROM Magasin m WHERE m.boutique IS NOT NULL ORDER BY m.libelle ASC")
    List<Magasin> findPointsDeVente();

    /** Variantes scopees par compagnie (voir DashboardService - dashboard transfert stock). */
    @Query("SELECT m FROM Magasin m WHERE m.boutique IS NULL AND m.compagnie.id = :compagnieId ORDER BY m.libelle ASC")
    List<Magasin> findMagasinsDeStockByCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT m FROM Magasin m WHERE m.boutique IS NOT NULL AND m.compagnie.id = :compagnieId ORDER BY m.libelle ASC")
    List<Magasin> findPointsDeVenteByCompagnieId(@Param("compagnieId") Long compagnieId);

    /** Tous les magasins (depots ET points de vente) d'une compagnie - utilise
     * pour le Transfert de Stock, qui doit pouvoir cibler n'importe quel
     * magasin comme source ou destination (les 4 combinaisons Magasin/Point
     * de vente sont toutes valides), pas seulement les depots purs. */
    @Query("SELECT m FROM Magasin m WHERE m.compagnie.id = :compagnieId ORDER BY m.libelle ASC")
    List<Magasin> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);

    /**
     * Vérifie si un magasin est un point de vente
     */
    @Query("SELECT CASE WHEN m.boutique IS NOT NULL THEN true ELSE false END " +
           "FROM Magasin m WHERE m.id = :magasinId")
    Boolean isPointDeVente(@Param("magasinId") Long magasinId);
    
}
