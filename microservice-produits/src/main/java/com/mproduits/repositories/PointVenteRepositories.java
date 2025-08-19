/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Annee;
import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Magasin;
import com.mproduits.model.Mois;
import com.mproduits.model.PointVente;
import com.mproduits.model.Produit;
import feign.Param;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface PointVenteRepositories extends JpaRepository<PointVente, Long> {

    @Query("SELECT pv FROM PointVente pv JOIN pv.prixarticlesCollection pa WHERE pv.boutique = :b AND pv.produit = :cde AND pa.entreprise = :e")
    Optional<PointVente> findByProduitAndBoutiqueAndEntreprise(Produit cde, Boutique b, Entreprise e);

    @Query("SELECT pv FROM PointVente pv WHERE pv.id = (SELECT MAX(p.id) FROM PointVente p JOIN p.prixarticlesCollection pa WHERE p.produit = :cde AND pa.entreprise = :e)")
    Optional<PointVente> findLastByProduitAndEntreprise(Produit cde, Entreprise e);

    @Query("SELECT MAX(pv.id) FROM PointVente pv JOIN pv.prixarticlesCollection pa WHERE pv.boutique = :b AND pv.produit = :cde AND pa.entreprise = :e AND pa.actif = TRUE")
    Optional<Long> findMaxIdByProduitAndBoutiqueAndEntrepriseActif(Produit cde, Boutique b, Entreprise e);

    @Query("SELECT MAX(pv.id) FROM PointVente pv JOIN pv.prixarticlesCollection pa WHERE pv.boutique = :b AND pv.produit = :cde AND pa.entreprise = :e")
    Optional<Long> findMaxIdByProduitAndBoutiqueAndEntreprise(Produit cde, Boutique b, Entreprise e);

    @Query("SELECT p FROM PointVente p JOIN p.prixarticlesCollection pa WHERE pa.entreprise = :e AND pa.actif = TRUE AND p.boutique = :b AND p.produit.deletes = FALSE")
    List<PointVente> findAllByBoutiqueAndEntreprise(Boutique b, Entreprise e);

    // @Query("SELECT DISTINCT pa.dateLivraison FROM PointVente p JOIN p.livraisonCollection pa WHERE pa.mois :e AND pa.actif = TRUE AND p.produit.deletes = FALSE ORDER BY pa.dateEnregistrement")
    // List<Date> findAllDatesByEntreprise(Entreprise e);
    @Query("SELECT p FROM PointVente p JOIN p.prixarticlesCollection pa WHERE pa.entreprise = :e AND pa.actif = TRUE AND p.boutique = :b AND p.produit.deletes = FALSE AND pa.dateEnregistrement BETWEEN :debut AND :fin")
    List<PointVente> findAllByBoutiqueAndEntrepriseAndDateRange(Boutique b, Entreprise e, Date debut, Date fin);

    @Query("SELECT p FROM PointVente p JOIN p.prixarticlesCollection pa WHERE pa.entreprise = :e AND pa.actif = TRUE AND p.boutique = :b AND p.produit.deletes = FALSE AND p.produit.categories = :cat")
    List<PointVente> findAllByBoutiqueAndEntrepriseAndCategorie(Boutique b, Entreprise e, Categories cat);

    // Dernier PointVente pour un produit et entreprise
    @Query("SELECT pv FROM PointVente pv WHERE pv.id = (SELECT MAX(p.id) FROM PointVente p JOIN p.prixarticlesCollection pa WHERE p.produit = :cde AND pa.entreprise = :e)")
    Optional<PointVente> findLatestByProduitAndEntreprise(@Param("cde") Produit cde, @Param("e") Entreprise e);

    // Dernier PointVente actif par mois
    @Query("SELECT pv FROM PointVente pv WHERE pv.id = (SELECT MAX(pv2.id) FROM PointVente pv2 JOIN pv2.prixarticlesCollection pa WHERE pv2.boutique = :b AND pv2.produit = :cde AND pa.entreprise = :e AND pa.actif = true)")
    Optional<PointVente> findLatestActiveByProduitBoutiqueAndEntreprise(@Param("cde") Produit cde, @Param("b") Boutique b, @Param("e") Entreprise e);

    // Dernier PointVente (sans filtre sur actif)
    @Query("SELECT pv FROM PointVente pv WHERE pv.id = (SELECT MAX(pv2.id) FROM PointVente pv2 JOIN pv2.prixarticlesCollection pa WHERE pv2.boutique = :b AND pv2.produit = :cde AND pa.entreprise = :e)")
    Optional<PointVente> findLatestByProduitBoutiqueAndEntreprise(@Param("cde") Produit cde, @Param("b") Boutique b, @Param("e") Entreprise e);

    // Toutes les dates d’enregistrement pour une entreprise
    @Query("SELECT DISTINCT pa.dateEnregistrement FROM PointVente p JOIN p.prixarticlesCollection pa WHERE pa.entreprise = :e AND pa.actif = true AND p.produit.deletes = false ORDER BY pa.dateEnregistrement")
    Set<Date> findAllDateEnregistrementByEntreprise(@Param("e") Entreprise e);

    // Tous les points de vente pour une période donnée
    @Query("SELECT p FROM PointVente p JOIN p.prixarticlesCollection pa WHERE pa.entreprise = :e AND pa.actif = true AND p.boutique = :b AND p.produit.deletes = false AND pa.dateEnregistrement BETWEEN :debut AND :fin")
    List<PointVente> findAllByBoutiqueEntrepriseAndPeriode(@Param("b") Boutique b, @Param("e") Entreprise e, @Param("debut") Date debut, @Param("fin") Date fin);

    // Tous les points de vente par boutique, entreprise et catégorie
    @Query("SELECT p FROM PointVente p JOIN p.prixarticlesCollection pa WHERE pa.entreprise = :e AND pa.actif = true AND p.boutique = :b AND p.produit.deletes = false AND p.produit.categories = :cat")
    List<PointVente> findAllByBoutiqueEntrepriseAndCategorie(@Param("b") Boutique b, @Param("e") Entreprise e, @Param("cat") Categories cat);

    @Query("SELECT pv FROM PointVente pv WHERE pv.produit = :produit AND pv.boutique = :boutique  AND pv.entreprise.annee = :annee  ORDER BY pv.id DESC")
    Optional<PointVente> findLastByProduitAndBoutiqueAndEntrepriseAndMois(@Param("produit") Produit produit,
            @Param("boutique") Boutique boutique,
            @Param("annee") Annee annee
    );

    @Query("SELECT pv FROM PointVente pv WHERE pv.produit = :produit AND pv.boutique = :boutique "
            + "AND pv.entreprise.annee = :annee  ORDER BY pv.id DESC")
    Optional<PointVente> findLastByProduitAndBoutiqueAndEntrepriseAndMoisOrderByIdDesc(@Param("produit") Produit produit,
            @Param("boutique") Boutique boutique,
            @Param("annee") Annee annee);

    @Query("SELECT pv.stockFinalTheorie FROM PointVente pv WHERE pv.produit = :produit AND pv.boutique = :boutique "
            + "AND pv.entreprise = :entreprise  ")
    Optional<BigDecimal> stockFinalProduit(@Param("produit") Produit produit,
            @Param("boutique") Boutique boutique,
            @Param("entreprise") Entreprise entreprise);
}
