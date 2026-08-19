/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.dto.ProduitDto;
import com.mproduits.model.Annee;
import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import com.mproduits.model.Magasin;
import com.mproduits.model.PointVente;
import com.mproduits.model.Produit;
import feign.Param;
import java.math.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // Variante scalaire (stockFinalTheorie seul, aucune entite PointVente ni
    // Produit hydratee) de findLatestActiveByProduitBoutiqueAndEntreprise -
    // voir StockRestaurationService.resoudreLignes : PointVente.produit est
    // en fetch EAGER (par defaut, pas de FetchType.LAZY dans le mapping) -
    // charger l'entite PointVente charge donc AUSSI son Produit associe a
    // chaque fois, ce qui, sur un gros fichier reel (~500 references
    // distinctes), accumulait des centaines de Produit dans la session
    // Hibernate partagee de la requete (Open Session In View) et
    // declenchait "Found shared references to a collection" - une
    // projection scalaire (SELECT pv.stockFinalTheorie) n'hydrate ni
    // PointVente ni Produit.
    @Query("SELECT pv.stockFinalTheorie FROM PointVente pv WHERE pv.id = (SELECT MAX(pv2.id) FROM PointVente pv2 JOIN pv2.prixarticlesCollection pa WHERE pv2.boutique = :b AND pv2.produit.id = :produitId AND pa.entreprise = :e AND pa.actif = true)")
    Optional<BigDecimal> findStockFinalTheorieActifByProduitIdBoutiqueAndEntreprise(
            @Param("produitId") Long produitId, @Param("b") Boutique b, @Param("e") Entreprise e);

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
    
    @Query("SELECT SUM(pv.stockFinalTheorie) FROM PointVente pv WHERE pv.produit.id = :produitId AND pv.boutique.id = :boutique AND pv.entreprise.annee.id = :annee  ")
    Optional<BigDecimal> sumStockByProduit(@Param("produitId") Long produitId,
              @Param("boutique") Long boutique,
            @Param("annee") int annee);
    
     @Query("SELECT pv.produit FROM PointVente pv WHERE  pv.boutique.id = :boutique "
            + "AND pv.entreprise.annee.id = :annee  and pv.stockFinalTheorie > :qte ")
    List<Produit> listeProduitHaveStockByBoutiqueAndAnnee(
            @Param("boutique") Long boutique,
            @Param("annee") int annee,
            @Param("qte") BigDecimal qte);
     Optional<PointVente>findByProduitId(Long ProduitId);
     @Query("SELECT pv FROM PointVente pv WHERE  pv.boutique.id = :boutiqueid and pv.produit.id= :ProduitId "
            + "AND pv.entreprise.annee.id = :annee ")
    Optional<PointVente> getProduitHaveStockByBoutiqueAndAnnee(
            @Param("boutiqueid") Long boutiqueid,
            @Param("annee") int annee,
            @Param("produitid") Long ProduitId);

     
     List<PointVente> findByDepotId(Magasin depot);
    List<PointVente> findByBoutique(Boutique boutique);
    List<PointVente> findByProduit(Produit produit);
    
    /**
     * Récupère le dernier enregistrement point de vente pour un produit
     */
    @Query("SELECT p FROM PointVente p WHERE p.depotId.id = :magasinId AND p.produit.id = :produitId ORDER BY p.dateReception DESC LIMIT 1")
    Optional<PointVente> findLatestPointVenteByMagasinAndProduit(
        @Param("magasinId") Long magasinId, 
        @Param("produitId") Long produitId);
    
     // ... méthodes existantes ...
    
    /**
     * Met à jour le stock de manière atomique (thread-safe)
     * Utilise une condition WHERE pour éviter les conflits
     * 
     * @param id ID du point de vente
     * @param quantite Quantité à décrémenter
     * @return Nombre de lignes mises à jour (0 si stock insuffisant, 1 si OK)
     */
    @Modifying
    @Query("UPDATE PointVente pv " +
           "SET pv.stockFinalTheorie = pv.stockFinalTheorie - :quantite, " +
           "    pv.sortiProduit = pv.sortiProduit + :quantite " +
           "WHERE pv.id = :id " +
           "AND pv.stockFinalTheorie >= :quantite")
    int updateStockAtomique(@Param("id") Long id, @Param("quantite") BigDecimal quantite);
    
  
    /**
     * Récupère les produits avec leur stock disponible dans un point de vente (boutique != null)
     * Optimisé avec une seule requête JPA incluant le prix de vente
     * 
     * @param depotId ID du dépôt/point de vente
     * @param anneeid ID de l'année
     * @param boutiqueid ID de la boutique
     * @return Liste [produit.id, produit.code, produit.libelle, produit.reference, stock_final_theorie, prix_vente]
     */
    @Query("SELECT " +
           "p.id, " +
           "p.code, " +
           "p.libelle, " +
           "p.reference, " +
           "pv.stockFinalTheorie, " +
           "COALESCE(pa.prixVenteNet, 0) " +
           "FROM PointVente pv " +
           "JOIN pv.produit p " +
           "LEFT JOIN pv.prixarticlesCollection pa " +
           "WHERE pv.depotId.id = :depotId " +
           "AND pv.entreprise.entreprisePK.anneeId = :anneeid " +
           "AND pv.boutique.id = :boutiqueid " +
           "AND pv.stockFinalTheorie IS NOT NULL " +
           "AND pv.stockFinalTheorie > 0 " +
           "ORDER BY p.libelle ASC")
    List<Object[]> findProduitsWithStockByDepot(
        @Param("depotId") Long depotId,
        @Param("anneeid") Long anneeid,
        @Param("boutiqueid") Long boutiqueid
    );

    /**
     * Récupère le stock d'un produit spécifique dans un point de vente
     * 
     * @param depotId ID du dépôt
     * @param produitId ID du produit
     * @return Stock final théorique
     */
    @Query("SELECT pv.stockFinalTheorie " +
           "FROM PointVente pv " +
           "WHERE pv.depotId.id = :depotId " +
           "AND pv.produit.id = :produitId " +
           "ORDER BY pv.dateReception DESC")
    Optional<BigDecimal> getStockByDepotAndProduit(
        @Param("depotId") Long depotId,
        @Param("produitId") Long produitId
    );

    /**
     * Récupère les détails complets du stock depuis POINTVENTE
     * 
     * @param depotId ID du dépôt
     * @param produitId ID du produit
     * @return [stockInitial, entreeProduit, sortiProduit, stockFinalTheorie, perte, dateReception]
     */
    @Query("SELECT " +
           "pv.stockInitial, " +
           "pv.entreeProduit, " +
           "pv.sortiProduit, " +
           "pv.stockFinalTheorie, " +
           "pv.perte, " +
           "pv.dateReception " +
           "FROM PointVente pv " +
           "WHERE pv.depotId.id = :depotId " +
           "AND pv.produit.id = :produitId " +
           "ORDER BY pv.dateReception DESC")
    Object[] getStockDetailsFromPointVente(
        @Param("depotId") Long depotId,
        @Param("produitId") Long produitId
    );

    /**
     * Trouve le dernier point de vente pour un produit dans un dépôt
     */
    @Query("SELECT pv FROM PointVente pv " +
           "WHERE pv.depotId.id = :depotId " +
           "AND pv.produit.id = :produitId " +
           "ORDER BY pv.dateReception DESC")
    Optional<PointVente> findLatestByDepotAndProduit(
        @Param("depotId") Long depotId,
        @Param("produitId") Long produitId
    );

    /**
     * Récupère le stock total par point de vente
     */
    @Query("SELECT pv.depotId.id, SUM(pv.stockFinalTheorie) " +
           "FROM PointVente pv " +
           "WHERE pv.entreprise.entreprisePK.anneeId = :anneeid " +
           "AND pv.boutique.id = :boutiqueid " +
           "GROUP BY pv.id")
    List<Object[]> getStockTotalParDepot(
        @Param("anneeid") Long anneeid,
        @Param("boutiqueid") Long boutiqueid
    );

    /**
     * Récupère les produits avec stock faible (< seuil) dans un point de vente
     */
    @Query("SELECT " +
           "p.id, " +
           "p.libelle, " +
           "pv.stockFinalTheorie, " +
           "pv.depotId.libelle " +
           "FROM PointVente pv " +
           "JOIN pv.produit p " +
           "WHERE pv.depotId.id = :depotId " +
           "AND pv.stockFinalTheorie > 0 " +
           "AND pv.stockFinalTheorie < :seuil " +
           "ORDER BY pv.stockFinalTheorie ASC")
    List<Object[]> findProduitsStockFaible(
        @Param("depotId") Long depotId,
        @Param("seuil") BigDecimal seuil
    );
    
    @Query("SELECT new  com.mproduits.dto.ProduitDto(" +
       "pa.pointVente.produit.id, " +
       "pa.pointVente.produit.reference, " +
       "pa.pointVente.produit.libelle, " +
       "pa.pointVente.produit.categories, " +
       "pa.prixVenteNet, " +
       "pa.pointVente.stockFinalTheorie) " +
       "FROM PrixArticles pa  " +
       //"JOIN pointVente pdv " +
      //  "JOIN  pdv.produit p " +
       //"LEFT JOIN PrixArticles pa ON pa.pointVente.id = pdv.id AND pa.entreprise.annee.id = :anneeId " +
       "WHERE pa.entreprise.annee.id = :anneeId " +
       "AND pa.pointVente.boutique.id = :boutiqueId and pa.actif= true and pa.pointVente.stockFinalTheorie > 0")
List<ProduitDto> findProduitsDtoByAnneeAndBoutique(
    @Param("anneeId") int anneeId, 
    @Param("boutiqueId") Long boutiqueId
);

  @Query("SELECT pv FROM PointVente pv WHERE pv.entreprise.entreprisePK = :pk")
    List<PointVente> findByEntreprise(@org.springframework.data.repository.query.Param("pk") EntreprisePK entreprisePK);

    @Query("SELECT COUNT(pv) FROM PointVente pv WHERE pv.entreprise.entreprisePK = :pk")
    Integer countByEntreprise(@org.springframework.data.repository.query.Param("pk") EntreprisePK pk);

    @Query("SELECT pv FROM PointVente pv WHERE pv.entreprise.entreprisePK = :pk " +
            "AND pv.produit.id = :produitId")
    List<PointVente> findByEntrepriseAndProduit(
            @org.springframework.data.repository.query.Param("pk") EntreprisePK pk,
            @org.springframework.data.repository.query.Param("produitId") Long produitId
    );

    // Reinitialisation d'une boutique (voir StockRestaurationService.reinitialiserBoutique) :
    // projection/comptage/suppression en masse, jamais de chargement d'entite
    // Produit - charger deux Produit qui partagent la meme reference (doublon
    // en base) dans la meme session, meme juste pour les supprimer, declenche
    // le meme "Found shared references to a collection" que la restauration.
    @Query("SELECT DISTINCT pv.produit.id FROM PointVente pv WHERE pv.boutique = :boutique")
    List<Long> findDistinctProduitIdsByBoutique(@Param("boutique") Boutique boutique);

    @Query("SELECT COUNT(pv) FROM PointVente pv WHERE pv.produit.id = :produitId")
    long countByProduitId(@Param("produitId") Long produitId);

    @Modifying
    @Query("DELETE FROM PointVente pv WHERE pv.boutique = :boutique")
    int deleteByBoutiqueBulk(@Param("boutique") Boutique boutique);
}
