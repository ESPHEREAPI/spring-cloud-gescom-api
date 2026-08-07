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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.mproduits.model.MagasinFournisseur;
import com.mproduits.model.MagasinFournisseurPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité MagasinFournisseur
 */

/**
 *
 * @author USER01
 */
@Repository
public interface MagasinFournisseurRepositories extends JpaRepository<MagasinFournisseur, MagasinFournisseurPK>{
      @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.boutique IS NULL")
    Collection<MagasinFournisseur> findDepotByFournisseurInAnnee();

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.boutique IS NULL AND df.magasin.compagnie.id = :compagnieId")
    Collection<MagasinFournisseur> findDepotByFournisseurInAnneeAndCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT df FROM MagasinFournisseur df GROUP BY df.magasin")
    Collection<MagasinFournisseur> findDepotByFournisseurByGroupDepot();

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.compagnie.id = :compagnieId GROUP BY df.magasin")
    Collection<MagasinFournisseur> findDepotByFournisseurByGroupDepotAndCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.boutique IS NOT NULL")
    Collection<MagasinFournisseur> findDepotByFournisseurHaveBoutique();

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.boutique IS NOT NULL AND df.magasin.compagnie.id = :compagnieId")
    Collection<MagasinFournisseur> findDepotByFournisseurHaveBoutiqueAndCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin.compagnie.id = :compagnieId")
    List<MagasinFournisseur> findAllByCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT df FROM MagasinFournisseur df WHERE df.magasin = :d AND df.fournisseur = :f")
    Optional<MagasinFournisseur> findByFournisseurAndDepot(@Param("f") Fournisseur f, @Param("d") Magasin d);

//    @Query("SELECT d FROM MagasinFournisseur d JOIN d.commandeCollection c WHERE c.mois = :mois AND c.numeroRepartition IS NOT NULL GROUP BY c.magasin")
//    Collection<MagasinFournisseur> findDepotInCommande(@Param("mois") Mois mois);
   //List<MagasinFournisseur> findByDisnctincMagasin
    
     /**
     * Rechercher toutes les associations d'un magasin
     */
    List<MagasinFournisseur> findByMagasinFournisseurPK_DepotId(Long depotId);

    /**
     * Rechercher toutes les associations d'un fournisseur
     */
    List<MagasinFournisseur> findByMagasinFournisseurPK_FournisseurId(Long fournisseurId);

    /**
     * Rechercher des associations avec critères multiples
     */
    @Query("SELECT mf FROM MagasinFournisseur mf " +
           "WHERE LOWER(mf.magasin.libelle) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(mf.magasin.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(mf.fournisseur.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(mf.fournisseur.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MagasinFournisseur> searchAssociations(@Param("search") String search, Pageable pageable);

    /**
     * Compter les associations d'un magasin
     */
    long countByMagasinFournisseurPK_DepotId(Long depotId);

    /**
     * Compter les associations d'un fournisseur
     */
    long countByMagasinFournisseurPK_FournisseurId(Long fournisseurId);

    /**
     * Vérifier si une association existe
     */
    boolean existsByMagasinFournisseurPK_DepotIdAndMagasinFournisseurPK_FournisseurId(Long depotId, Long fournisseurId);
}
