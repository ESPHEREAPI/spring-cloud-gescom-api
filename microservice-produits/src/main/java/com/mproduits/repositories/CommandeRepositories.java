/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Annee;
import com.mproduits.model.Boutique;
import com.mproduits.model.Commande;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Fournisseur;
import com.mproduits.model.Magasin;
import com.mproduits.model.Mois;
import com.mproduits.model.Produit;
import feign.Param;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author USER01
 */
@Repository
public interface CommandeRepositories extends JpaRepository<Commande, Long>{
     // Équivalent de getCodeCommande
    @Query("SELECT c FROM Commande c WHERE c.numeroRepartition = :repartition")
    Optional<Commande> findByNumeroRepartition(@Param("repartition") String repartition);

    // Équivalent de finKey
    @Query("SELECT c FROM Commande c WHERE c.produit = :produit  AND c.magasinid = :depot AND c.numeroRepartition = :repartition")
    Optional<Commande> findByProduitAndMoisAndDepotAndNumeroRepartition(
            @Param("produit") Produit produit,
            
            @Param("depot") Magasin depot,
            @Param("repartition") String numeroRepartition
    );

    // Équivalent de getDernierCommandeByProduitByDepotByMois
    @Query("SELECT c FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE c2.produit = :produit  AND c2.magasinid = :depot)")
    Optional<Commande> findLastCommandeByProduitAndMoisAndDepot(
            @Param("produit") Produit produit,
            
            @Param("depot") Magasin depot
    );

    // Équivalent de getDernierCommandeByProduitByDepot
    @Query("SELECT c FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE c2.produit = :produit AND c2.magasinid = :depot)")
    Optional<Commande> findLastCommandeByProduitAndDepot(
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot
    );

    // Équivalent de getDernierCommandeByProduitByDepotByPrixVente
    @Query("SELECT c FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE c2.produit = :produit AND c2.magasinid = :depot AND c2.prixVente > :valeur AND c2.prixVente IS NOT NULL)")
    Optional<Commande> findLastCommandeByProduitAndDepotWithPrixVente(
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot,
            @Param("valeur") BigDecimal valeur
    );

    // Équivalent de getCommandeByEntreprise
    @Query("SELECT c FROM Commande c WHERE c.entreprise.annee = :annee ")
    List<Commande> findByEntreprise(@Param("annee") Annee annee);

    // Équivalent de getCommandeActifByEntrepriseByLimiteStock
    @Query("SELECT c FROM Commande c WHERE c.produit.deletes = :actif AND c.entreprise.annee = :annee AND c.stockFinalTheorie <= :qte AND c.magasinid.boutique IS NULL ORDER BY c.produit.reference")
    List<Commande> findActiveCommandesByEntrepriseAndStockLimit(
            @Param("annee") Annee annee,
            @Param("qte") BigInteger limite,
            @Param("actif") Boolean actif
    );

    // Équivalent de getCommandeByEntrepriseWithProduit
    @Query("SELECT c FROM Commande c WHERE c.produit = :produit GROUP BY c.magasinid.id")
    List<Commande> findByEntrepriseAndProduit(@Param("produit") Produit produit);

    // Équivalent de getCommandeByEntrepriseWithProduitByGroup
    @Query("SELECT c FROM Commande c WHERE c.entreprise.annee = :annee AND c.produit = :produit ")
    List<Commande> findByEntrepriseAndProduitGrouped(
            @Param("annee") Annee annee,
            @Param("produit") Produit produit
    );

    // Équivalent de getCommandeByProduitWithDepot
    @Query("SELECT c FROM Commande c WHERE c.produit = :produit AND c.magasinid = :depot")
    List<Commande> findByProduitAndDepot(
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot
    );


//    // Équivalent de getNumeroRepartionByEntrepriseWithMonthIsNotFacture
//    @Query("SELECT c.numeroRepartition FROM Commande c WHERE c.mois = :mois AND c.magasinid = :depot AND c.numeroRepartition NOT IN (SELECT f.numeroRepartition FROM Facture f WHERE f.entreprise = :entreprise) GROUP BY c.numeroRepartition")
//    List<String> findNumeroRepartitionNotFactured(
//            @Param("mois") Mois mois,
//            @Param("depot") Magasin depot,
//            @Param("entreprise") Entreprise entreprise
//    );

    // Équivalent de getCommandeByDepotWithMois
    @Query("SELECT c FROM Commande c WHERE  c.magasinid = :depot GROUP BY c.produit")
    List<Commande> findByDepotAndMois(
           
            @Param("depot") Magasin depot
    );

//    // Équivalent de getCommandeByEntrepriseInLivraison
//    @Query("SELECT c FROM Commande c JOIN c.livraisonCollection l WHERE l.commande.mois.annee = :annee ORDER BY c.mois.numero")
//    List<Commande> findByEntrepriseInLivraison(@Param("annee") Annee annee);

//    // Équivalent de getCommandeByEntrepriseInLivraison avec mois
//    @Query("SELECT c FROM Commande c JOIN c.livraisonCollection l WHERE l.commande.mois.annee = :annee AND l.commande.mois = :mois ORDER BY c.mois.numero")
//    List<Commande> findByEntrepriseInLivraisonAndMois(
//            @Param("annee") Annee annee,
//            @Param("mois") Mois mois
//    );

//    // Équivalent de getCommandeInLivraisonByDate
//    @Query("SELECT l.commande FROM Livraison l WHERE l.commande.mois = :mois AND l.commande.produit = :produit AND l.commande.magasinid = :depot AND l.dateLivraison = :date")
//    List<Commande> findCommandeInLivraisonByDate(
//            @Param("mois") Mois mois,
//            @Param("produit") Produit produit,
//            @Param("depot") Magasin depot,
//            @Param("date") Date date
//    );

    // Équivalent de getCommandeByProduitWithDateCreation
    @Query("SELECT c FROM Commande c WHERE  c.produit = :produit AND c.magasinid = :depot AND c.dateReception = :date")
    Optional<Commande> findByProduitAndDateCreation(
            
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot,
            @Param("date") Date date
    );

    // Équivalent de getCommandeByProduitByMoisByDepot
    @Query("SELECT c FROM Commande c WHERE  c.produit = :produit AND c.magasinid.ville = :ville ORDER BY c.dateReception")
    List<Commande> findByProduitAndMoisAndDepotVille(
            
            @Param("produit") Produit produit,
            @Param("ville") String ville
    );

    // Équivalent de getTotalEntreeCommandeByProduitByMoisByDepot
    @Query("SELECT COALESCE(SUM(c.entreeProduit), 0) FROM Commande c WHERE c.entreprise.annee = :annee AND c.produit = :produit AND c.magasinid.ville = :ville")
    BigInteger getTotalEntreeByProduitAndEntrepriseAndDepot(
            @Param("annee") Annee annee,
            @Param("produit") Produit produit,
            @Param("ville") String ville
    );

    // Équivalent de getTotalSortieCommandeByProduitByMoisByDepot
    @Query("SELECT COALESCE(SUM(c.sortiProduit), 0) FROM Commande c WHERE c.entreprise.annee = :annee AND c.produit = :produit AND c.magasinid.ville = :ville")
    BigInteger getTotalSortieByProduitAndEntrepriseAndDepot(
            @Param("annee") Annee annee,
            @Param("produit") Produit produit,
            @Param("ville") String ville
    );

    // Équivalent de getTotalEntreeCommandeByProduitByQuizaineByDepot
    @Query("SELECT COALESCE(SUM(c.entreeProduit), 0) FROM Commande c WHERE c.dateReception BETWEEN :debut AND :fin AND c.produit = :produit AND c.magasinid = :depot ")
    BigInteger getTotalEntreeByProduitAndQuinzaineAndDepot(
            @Param("debut") Date debut,
            @Param("fin") Date fin,
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot
    );

    // Équivalent de getTotalEntreeCommandeByProduitByQuizaineByFournisseur
    @Query("SELECT COALESCE(SUM(c.entreeProduit), 0) FROM Commande c WHERE c.dateReception BETWEEN :debut AND :fin AND c.produit = :produit  ")
    BigInteger getTotalEntreeByProduitAndQuinzaineAndFournisseur(
            @Param("debut") Date debut,
            @Param("fin") Date fin,
            @Param("produit") Produit produit,
            @Param("fournisseur") Fournisseur fournisseur
    );

    // Équivalent de getTotalEntreeCommandeByProduitByQuizaineByFournisseur avec depot
    @Query("SELECT COALESCE(SUM(c.entreeProduit), 0) FROM Commande c WHERE c.dateReception BETWEEN :debut AND :fin AND c.produit = :produit   AND c.magasinid = :depot")
    BigInteger getTotalEntreeByProduitAndQuinzaineAndFournisseurAndDepot(
            @Param("debut") Date debut,
            @Param("fin") Date fin,
            @Param("produit") Produit produit,
      
            @Param("depot") Magasin depot
    );

    // Équivalent de getTotalFoncuveCommandeByProduitByMoisByDepot
//    @Query("SELECT COALESCE(SUM(c.fondCuve), 0) FROM Commande c WHERE c.mois.annee = :annee AND c.produit = :produit AND c.magasinid.villeid = :ville")
//    BigInteger getTotalFondCuveByProduitAndEntrepriseAndDepot(
//            @Param("annee") Annee annee,
//            @Param("produit") Produit produit,
//            @Param("ville") String ville
//    );

    // Équivalent de getDateByCommande par année
    @Query("SELECT DISTINCT c.dateReception FROM Commande c WHERE c.entreprise.annee = :annee")
    List<Date> findDistinctDatesByAnnee(@Param("annee") Annee annee);

    // Équivalent de getDateByCommande par mois
    @Query("SELECT DISTINCT c.dateReception FROM Commande c ")
    List<Date> findDistinctDatesByMois();

    // Équivalent de getCommandeByMois
    @Query("SELECT c FROM Commande c")
    List<Commande> findByMois();

//    // Équivalent de getNumeroRepartitionByMois
//    @Query("SELECT DISTINCT c.numeroRepartition FROM Commande c WHERE c.mois = :mois")
//    List<String> findDistinctNumeroRepartitionByMois(@Param("mois") Mois mois);

    // Équivalent de getDernierCommandeByDate
    @Query("SELECT c FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE   c2.produit = :produit AND c2.magasinid = :depot AND c2.dateReception < :date)")
    Optional<Commande> findLastCommandeByDate(
            
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot,
            @Param("date") Date date
    );

    // Équivalent de getDernierCommandeByDateForNumeroBordereauOrRepartition
    @Query("SELECT c FROM Commande c WHERE c.id = (SELECT MIN(c2.id) FROM Commande c2 WHERE  c2.produit = :produit AND c2.magasinid = :depot AND c2.dateReception >= :date AND c2.numeroRepartition = :numeroRepartition)")
    Optional<Commande> findLastCommandeByDateAndNumeroRepartition(
            
            @Param("produit") Produit produit,
            @Param("depot") Magasin depot,
            @Param("date") Date date,
            @Param("numeroRepartition") String numeroRepartition
    );

    // Équivalent de testeNumerorepartitionExiste
    @Query("SELECT COUNT(c) > 0 FROM Commande c WHERE c.entreprise.annee = :annee AND c.numeroRepartition = :numero")
    boolean existsByAnneeAndNumeroRepartition(
            @Param("annee") Annee annee,
            @Param("numero") String numeroRepartition
    );

    // Équivalent de getNumeroRepartitionByAnnee
    @Query("SELECT DISTINCT c.numeroRepartition FROM Commande c WHERE c.entreprise.annee = :annee")
    List<String> findDistinctNumeroRepartitionByAnnee(@Param("annee") Annee annee);

    // Équivalent de getCommandeByNumeroRepartion
    @Query("SELECT c FROM Commande c WHERE c.numeroRepartition = :numero")
    List<Commande> findByNumeroRepartitionAndMois(
            @Param("numero") String numeroRepartition
            
    );

    // Équivalent de getCommandeByNumeroRepartionInDepot
    @Query("SELECT c FROM Commande c WHERE c.numeroRepartition = :numero AND  c.magasinid = :depot")
    List<Commande> findByNumeroRepartitionAndMoisAndDepot(
            @Param("numero") String numeroRepartition,
         
            @Param("depot") Magasin depot
    );

    // Équivalent de getInventaireByAnneeForDepot
    @Query("SELECT DISTINCT c.produit FROM Commande c WHERE c.entreprise.annee = :annee AND c.magasinid = :depot")
    List<Produit> findDistinctProduitsByAnneeAndDepot(
            @Param("annee") Annee annee,
            @Param("depot") Magasin depot
    );

    // Équivalent de getCommandeForInventaireByAnneeForDepot
    @Query("SELECT c FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE c2.entreprise.annee = :annee AND c2.magasinid = :depot AND c2.produit = :produit)")
    Optional<Commande> findCommandeForInventaire(
            @Param("annee") Annee annee,
            @Param("depot") Magasin depot,
            @Param("produit") Produit produit
    );

    // Équivalent de getPrixAchatProduitByDepot
    @Query("SELECT COALESCE(c.prixAchat, 0) FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE c2.entreprise.annee = :annee AND c2.magasinid = :depot AND c2.produit = :produit)")
    BigDecimal getPrixAchatProduitByDepot(
            @Param("annee") Annee annee,
            @Param("depot") Magasin depot,
            @Param("produit") Produit produit
    );

    // Équivalent de getPrixAchatProduitByBoutique
    @Query("SELECT COALESCE(c.prixAchat, 0) FROM Commande c WHERE c.id = (SELECT MAX(c2.id) FROM Commande c2 WHERE c2.magasinid.boutique = :boutique AND c2.produit = :produit)")
    BigDecimal getPrixAchatProduitByBoutique(
            @Param("boutique") Boutique boutique,
            @Param("produit") Produit produit
    );
    
    //Optional<Commande> findTopByProduitAndDepotOrderByDateCreationDesc(Produit produit, Magasin depot);

  //  Optional<Commande> findTopByProduitAndDepotAndPrixVenteIsNotNullOrderByDateCreationDesc(
          // Produit produit, Magasin depot);

    boolean existsByNumeroRepartition(String numeroRepartition);

    @Query("SELECT c FROM Commande c WHERE c.entreprise.entreprisePK.compagnieId = :compagnieId ORDER BY c.dateReception DESC")
    List<Commande> findByCompagnieId(@Param("compagnieId") Long compagnieId);

    @Query("SELECT c FROM Commande c WHERE c.numeroRepartition = :code")
    Commande findByCode(@Param("code") String code);
    
    
    @Query("SELECT c FROM Commande c WHERE c.produit = :produit AND  c.magasinid = :depot " +
           "ORDER BY c.id DESC")
    Optional<Commande> findLastCommandeByProduitAndDepotAndMois(@Param("produit") Produit produit, 
                                                                
                                                                @Param("depot") Magasin depot);
   
    
   
    
    @Query("SELECT c  FROM Commande c WHERE c.magasinid= :depot and c.produit= :produit and c.entreprise= :e")
    Optional<Commande>findLastcaommandeByProduit(@Param("depot") Magasin depot,@Param("produit") Produit produit, @Param("e") Entreprise e);
    
      
    @Query("SELECT c.stockFinalTheorie  FROM Commande c WHERE c.magasinid= :depot and c.produit= :produit and c.entreprise= :e")
    Optional<BigDecimal>stockFinalDepot(@Param("depot") Magasin depot,@Param("produit") Produit produit, @Param("e") Entreprise e);
    
     List<Commande> findByMagasinid(Magasin magasin);
    List<Commande> findByProduit(Produit produit);
    
    /**
     * Récupère la commande la plus récente pour un produit dans un magasin
     */
    @Query("SELECT c FROM Commande c WHERE c.magasinid.id = :magasinId AND c.produit.id = :produitId ORDER BY c.dateReception DESC LIMIT 1")
    Optional<Commande> findLatestCommandeByMagasinAndProduit(
        @Param("magasinId") Long magasinId, 
        @Param("produitId") Long produitId);
    
     /**
     * Récupère les produits avec leur stock disponible dans un magasin (boutique = null)
     * Optimisé avec une seule requête JPA
     * 
     * @param depotId ID du dépôt/magasin
     * @param anneeid ID de l'année
     * @param boutiqueid ID de la boutique
     * @return Liste [produit.id, produit.code, produit.libelle, produit.reference, stock_final_theorie, prix_achat]
     */
    @Query("SELECT " +
           "p.id, " +
           "p.code, " +
           "p.libelle, " +
           "p.reference, " +
           "pa.pointVente.stockFinalTheorie, " +
           "pa.prixVenteNet " +
           "FROM PrixArticles pa " +
           "JOIN pa.pointVente.produit p " +
           "WHERE pa.pointVente.depotId.id = :depotId " +
           "AND pa.entreprise.entreprisePK.anneeId = :anneeid " +
           "AND pa.pointVente.boutique.id = :boutiqueid " +
           "AND pa.pointVente.stockFinalTheorie IS NOT NULL " +
           "AND pa.pointVente.stockFinalTheorie > 0 " +
           "ORDER BY p.libelle ASC")
    List<Object[]> findProduitsWithStockByDepot(
        @Param("depotId") Long depotId,
        @Param("anneeid") Long anneeid,
        @Param("boutiqueid") Long boutiqueid
    );
    
        @Query("SELECT " +
           "p.id, " +
           "p.code, " +
           "p.libelle, " +
           "p.reference, " +
           "c.stockFinalTheorie, " +
           "c.prixAchat " +
           "FROM Commande c " +
           "JOIN c.produit p " +
           "WHERE c.magasinid.id = :depotId " +
           "AND c.entreprise.entreprisePK.anneeId = :anneeid " +
           "AND c.magasinid.boutique IS  NULL " +
           "AND c.stockFinalTheorie IS NOT NULL " +
           "AND c.stockFinalTheorie > 0 " +
           "ORDER BY p.libelle ASC")
    List<Object[]> findProduitsWithStockByDepot(
        @Param("depotId") Long depotId,
        @Param("anneeid") Long anneeid
    );

    /**
     * Récupère le stock d'un produit spécifique dans un dépôt
     * 
     * @param depotId ID du dépôt
     * @param produitId ID du produit
     * @return Stock final théorique
     */
    @Query("SELECT c.stockFinalTheorie " +
           "FROM Commande c " +
           "WHERE c.magasinid.id = :depotId " +
           "AND c.produit.id = :produitId " +
           "ORDER BY c.dateReception DESC")
    Optional<BigDecimal> getStockByDepotAndProduit(
        @Param("depotId") Long depotId,
        @Param("produitId") Long produitId
    );

    /**
     * Récupère les détails complets du stock depuis COMMANDE
     * 
     * @param depotId ID du dépôt
     * @param produitId ID du produit
     * @return [stockInitial, entreeProduit, sortiProduit, stockFinalTheorie, prixAchat, dateReception]
     */
    @Query("SELECT " +
           "c.stockInitial, " +
           "c.entreeProduit, " +
           "c.sortiProduit, " +
           "c.stockFinalTheorie, " +
           "c.prixAchat, " +
           "c.dateReception " +
           "FROM Commande c " +
           "WHERE c.magasinid.id = :depotId " +
           "AND c.produit.id = :produitId " +
           "ORDER BY c.dateReception DESC")
    Object[] getStockDetailsFromCommande(
        @Param("depotId") Long depotId,
        @Param("produitId") Long produitId
    );

    /**
     * Trouve la dernière commande pour un produit dans un dépôt
     */
    @Query("SELECT c FROM Commande c " +
           "WHERE c.magasinid.id = :depotId " +
           "AND c.produit.id = :produitId " +
           "ORDER BY c.dateReception DESC")
    Optional<Commande> findLatestByDepotAndProduit(
        @Param("depotId") Long depotId,
        @Param("produitId") Long produitId
    );

    /**
     * Récupère le stock total par dépôt
     */
    @Query("SELECT c.magasinid.id, SUM(c.stockFinalTheorie) " +
           "FROM Commande c " +
           "WHERE c.entreprise.entreprisePK.anneeId = :anneeid " +
           "AND c.magasinid.boutique.id = :boutiqueid " +
           "GROUP BY c.magasinid.id")
    List<Object[]> getStockTotalParDepot(
        @Param("anneeid") Long anneeid,
        @Param("boutiqueid") Long boutiqueid
    );
}
