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

    /**
     * Vérifie si un magasin est un point de vente
     */
    @Query("SELECT CASE WHEN m.boutique IS NOT NULL THEN true ELSE false END " +
           "FROM Magasin m WHERE m.id = :magasinId")
    Boolean isPointDeVente(@Param("magasinId") Long magasinId);
    
}
