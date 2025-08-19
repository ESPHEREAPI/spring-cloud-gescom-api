/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.Produit;
import com.mproduits.model.Taxeproduit;
import feign.Param;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface TaxeproduitRepositories extends JpaRepository<Taxeproduit, Long> {

    @Query("SELECT t FROM Taxeproduit t WHERE t.produit= :produit and t.mois= :mois and t.entreprise= :entreprise")
    Optional<Taxeproduit> findByProduitAndMoisAndEntreprise(
            @Param("produit") Produit produit,
            @Param("mois") Mois mois,
            @Param("entreprise") Entreprise entreprise
    );

 @Query("SELECT t FROM Taxeproduit t WHERE t.entreprise= :entreprise and t.mois= :mois")
Collection<Taxeproduit> findByEntrepriseAndMois(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois
);
}
