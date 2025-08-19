/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Fournisseur;
import com.mproduits.model.Magasin;
import com.mproduits.model.MagasinFournisseur;
import com.mproduits.model.MagasinFournisseurPK;
import com.mproduits.model.Mois;
import feign.Param;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface MagasinFournisseurRepositories extends JpaRepository<MagasinFournisseur, MagasinFournisseurPK>{
      @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.boutique IS NULL")
    Collection<MagasinFournisseur> findDepotByFournisseurInAnnee();

    @Query("SELECT df FROM MagasinFournisseur df GROUP BY df.magasin")
    Collection<MagasinFournisseur> findDepotByFournisseurByGroupDepot();

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.boutique IS NOT NULL")
    Collection<MagasinFournisseur> findDepotByFournisseurHaveBoutique();

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin = :d AND df.fournisseur = :f")
    Optional<MagasinFournisseur> findByFournisseurAndDepot(@Param("f") Fournisseur f, @Param("d") Magasin d);

//    @Query("SELECT d FROM MagasinFournisseur d JOIN d.commandeCollection c WHERE c.mois = :mois AND c.numeroRepartition IS NOT NULL GROUP BY c.magasin")
//    Collection<MagasinFournisseur> findDepotInCommande(@Param("mois") Mois mois);
   //List<MagasinFournisseur> findByDisnctincMagasin
}
