/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.PrixAchat;
import com.mproduits.model.Produit;
import feign.Param;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface PrixAchatRepositories extends JpaRepository<PrixAchat, Long> {

    @Query("SELECT pv FROM PrixAchat pv WHERE pv.id=(SELECT MAX(prv.id)  FROM PrixAchat prv  where prv.produit= :produit)")
    Optional<PrixAchat> findLastPrixAchatByProduit(@Param("produit") Produit produit);

    @Query("SELECT pv FROM PrixAchat pv WHERE pv.produit= :produit and pv.datedebut<= :endOfDay and pv.datefin is null")
    Optional<PrixAchat> findLastPrixAchatByProduitLastPrice(@Param("produit") Produit produit,  @Param("endOfDay") Date endOfDay);

    @Query("SELECT pv FROM PrixAchat pv WHERE pv.produit= :produit and pv.datedebut Between :debut and :fin  and pv.datefin is null")
    Optional<PrixAchat> findLastPrixAchatByProduitLastPrice(@Param("produit") Produit produit, @Param("debut") Date debut, @Param("fin") Date fin);
    
    Optional<PrixAchat> findTopByProduitAndDatedebutLessThanEqualAndDatefinIsNullOrderByDatedebutDesc(
    Produit produit,
    Date date
);
    
    Optional<PrixAchat> findTopByProduitAndDatedebutBetweenAndDatefinIsNullOrderByDatedebutDesc(
    Produit produit,
    Date debut,
    Date fin
);
    
   
}
