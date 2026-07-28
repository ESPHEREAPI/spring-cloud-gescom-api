/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.LigneVente;
import com.mproduits.model.Produit;
import com.mproduits.model.Vente;
import feign.Param;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface LigneVenteRepositories extends JpaRepository<LigneVente, Long> {

    List<LigneVente> findByVente(Vente vente);

    @Query("SELECT l FROM LigneVente l WHERE  DATE(l.vente.dateVente)= :dateVente and l.vente.entreprise.annee.id= :anneeid and l.vente.boutique.id= :boutiqueid")
    List<LigneVente> listeProduitVendueJournalier(@Param("dateVente") Date dateVente, @Param("anneeid") int anneeid , @Param("boutiqueid") Long boutiqueid);

    @Query("SELECT l FROM LigneVente l WHERE  DATE(l.vente.dateVente) BETWEEN  :debut and :fin and  l.vente.entreprise.annee.id= :anneeid and l.vente.boutique.id= :boutiqueid ")
    List<LigneVente> listeProduitVendueMensuelle(@Param("debut") Date debut, @Param("fin") Date fin, @Param("anneeid") int anneeid, @Param("boutiqueid") Long boutiqueid);
     Optional<LigneVente> findByVenteAndProduit(Vente vente, Produit produit);
}
