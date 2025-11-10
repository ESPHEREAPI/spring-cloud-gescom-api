/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.dto.ProduitDto;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import feign.Param;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface PrixArticlesRepositories extends JpaRepository<PrixArticles, Long> {

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente = :pv AND pa.actif = TRUE")
    Optional<PrixArticles> findActiveByEntrepriseAndPointVente(@Param("e") Entreprise e, @Param("pv") PointVente pv);

    List<PrixArticles> findByPointVente_Produit(Produit produit);

    @Query("SELECT MAX(pa.id) FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente = :pv AND pa.actif = TRUE ")
    Optional<Long> findMaxIdByEntreprisePointVenteMois(@Param("e") Entreprise e, @Param("pv") PointVente pv, @Param("mois") Mois mois);

    @Query("SELECT MAX(pa.id) FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente = :pv AND pa.actif = TRUE")
    Optional<Long> findMaxIdByEntreprisePointVente(@Param("e") Entreprise e, @Param("pv") PointVente pv);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.actif = TRUE AND pa.pointVente.produit.deletes = FALSE AND pa.pointVente.stockFinalTheorie > 0 ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findActiveWithStock(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.dateEnregistrement IS NULL")
    List<PrixArticles> findWithNullDateEnregistrement();

//    @Query("SELECT p FROM PrixArticles p WHERE NOT EXISTS (SELECT b FROM BarcodeProduit b WHERE b.prixArticles = p) AND p.entreprise = :e")
//    List<PrixArticles> findWithoutBarcode(@Param("e") Entreprise e);
    @Query("SELECT pa.pointVente.produit.libelle FROM PrixArticles pa WHERE pa.entreprise = :e AND (:filter IS NULL OR pa.pointVente.produit.libelle LIKE %:filter%)")
    List<String> searchProduitLibelle(@Param("e") Entreprise e, @Param("filter") String filter);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.pointVente.produit = :p")
    List<PrixArticles> findAllByProduit(@Param("p") Produit p);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.id = (SELECT max(pa2.id) FROM PrixArticles pa2 WHERE pa2.entreprise = :e AND pa2.pointVente = :pv AND pa2.actif = true )")
    Optional<PrixArticles> findLastActiveByEntrepriseAndPointVenteAndMois(@Param("e") Entreprise e, @Param("pv") PointVente pv, @Param("m") Mois m);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.id = (SELECT max(pa2.id) FROM PrixArticles pa2 WHERE pa2.entreprise = :e AND pa2.pointVente = :pv AND pa2.actif = true)")
    Optional<PrixArticles> findLastActiveByEntrepriseAndPointVente(@Param("e") Entreprise e, @Param("pv") PointVente pv);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.id = (SELECT max(pa2.id) FROM PrixArticles pa2 WHERE pa2.entreprise = :e AND pa2.pointVente.produit = :p AND pa2.actif = true)")
    Optional<PrixArticles> findLastActiveByEntrepriseAndProduit(@Param("e") Entreprise e, @Param("p") Produit p);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente.produit.deletes = false AND pa.actif = true AND pa.pointVente.stockFinalTheorie > 0 ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findActifByEntrepriseWithStock(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente.produit.deletes = false AND pa.actif = true AND pa.pointVente.stockFinalTheorie = 0 ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findActifByEntrepriseWithZeroStock(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente.produit.deletes = false AND pa.actif = true ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findAllActifByEntreprise(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente.produit.deletes = false AND pa.actif = true ORDER BY pa.pointVente.sortiProduit DESC")
    List<PrixArticles> findAllActifByEntrepriseOrderedBySortirDesc(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente.produit.deletes = false AND pa.actif = true ORDER BY pa.pointVente.sortiProduit ASC")
    List<PrixArticles> findAllActifByEntrepriseOrderedBySortirAsc(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.pointVente.produit.deletes = false AND pa.actif = true AND pa.pointVente.produit.pacquets = true AND pa.pointVente.stockFinalTheorie > 0 ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findActifByEntrepriseAndPacquetWithStock(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.actif = true AND pa.pointVente.produit.deletes = false AND pa.pointVente.produit.pacquets = false ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findActifByEntrepriseWithoutPacquet(@Param("e") Entreprise e);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND pa.actif = true AND pa.pointVente.stockFinalTheorie <= :qte ORDER BY pa.pointVente.produit.reference")
    List<PrixArticles> findActifByEntrepriseWithStockLessThan(@Param("e") Entreprise e, @Param("qte") BigInteger qte);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.dateEnregistrement is null")
    List<PrixArticles> findAllWithoutDateEnregistrement();

    ////    @Query("SELECT p FROM PrixArticles p WHERE NOT EXISTS (SELECT b FROM BarcodeProduit b WHERE b.prixArticles = p ) AND p.entreprise = :e")
////    List<PrixArticles> findAllWithoutBarcode(@Param("e") Entreprise e)
    /// @param e;
    /// @param search
    /// @param pageable
    /// @return 

    @Query("SELECT pa  FROM PrixArticles pa WHERE pa.entreprise = :e AND  pa.pointVente.produit.reference LIKE %:search%")
    Page<PrixArticles> findProduitLibelleByEntrepriseWithFilter(@Param("e") Entreprise e, @Param("search") String search, Pageable pageable);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e   AND pa.actif = :active")
    Page<PrixArticles> findAllByEntrepriseProduitActif(@Param("e") Entreprise e, @Param("active") Boolean active, Pageable pageable);

    @Query("SELECT DISTINCT pa FROM PrixArticles pa WHERE pa.entreprise = :e AND  pa.pointVente.produit.libelle LIKE %:search%")
    List<PrixArticles> findProduitLibelleByEntrepriseWithFilter(@Param("e") Entreprise e, @Param("search") String search);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e   AND pa.actif = :active")
    List<PrixArticles> findAllByEntrepriseProduitActif(@Param("e") Entreprise e, @Param("active") Boolean active);

    List<PrixArticles> findTop10000ByActifTrueOrderByDateCreationDesc();

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.pointVente.produit.reference= :ref")
    Optional<PrixArticles> getPrixAticlesByReferenceProduit(@Param("reference") String ref);

    @Query(" SELECT pa.pointVente FROM PrixArticles pa  WHERE pa.entreprise.annee.id= :anneeid and pa.pointVente.boutique.id= :boutiqueid and pa.pointVente.produit.categories.id= :categorieid and pa.actif= :active")
    public List<PointVente> chargeStockPointVente(@Param("anneeid") int anneeid, @Param("active") Boolean active, @Param("boutiqueid") Long boutiqueid, @Param("categorieid") Long categorieid);

    @Query("SELECT pa FROM PrixArticles pa WHERE pa.entreprise = :e   AND pa.actif = :active and pa.pointVente.stockFinalTheorie <= :stock")
    List<PrixArticles> findAllByEntrepriseProduitActifWithSeuilStock(@Param("e") Entreprise e, @Param("active") Boolean active, @Param("stock") BigDecimal stock);

    @Query("""
    SELECT p 
    FROM PrixArticles p 
    WHERE p.actif = true 
      AND p.pointVente.stockFinalTheorie > 0 
    ORDER BY p.dateCreation DESC
""")
    List<PrixArticles> findTopActifWithStockFinalPositive(Pageable pageable);
        @Query("SELECT p FROM PrixArticles p WHERE p.pointVente.produit.reference  like :kw and p.actif= true and p.entreprise.annee.id= :anneeid")
     public List<PrixArticles> searchProduit(@Param(value ="kw")String keyword,@Param("anneeid") int anneeid);
     
      @Query("SELECT p FROM PrixArticles p WHERE p.pointVente.produit.categories.id= :idCategorie and p.actif= true and p.entreprise.annee.id= :anneeid")
     public List<PrixArticles> searchProduitBycategories(@Param(value ="idCategorie") Long id,@Param("anneeid") int anneeid);
     
      @Query("SELECT pa FROM PrixArticles pa WHERE pa.actif = true ORDER BY pa.dateCreation DESC LIMIT 1")
    Optional<PrixArticles> findCurrentPrice(Long produitId);
}
