/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Magasin;
import com.mproduits.model.PrixVente;
import com.mproduits.model.Produit;
import feign.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface PrixVenteRepositories extends JpaRepository<PrixVente, Long>{
    @Query("SELECT pv FROM PrixVente pv WHERE pv.id=(SELECT MAX(prv.id)  FROM PrixVente prv  where prv.produit= :produit)")
    Optional<PrixVente> findLastPrixventeByProduit(@Param("produit") Produit produit);
}
