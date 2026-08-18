/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.model.Entreprise;
import com.mproduits.model.EntreprisePK;
import com.mproduits.model.Produit;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface ProduitRepositories extends JpaRepository<Produit, Long>{
     //Page<Produit> findByEntreprise(Entreprise entreprise, Pageable pageable);

//    List<Produit> findByLibelleContainingIgnoreCaseOrReferenceContainingIgnoreCaseAndEntreprise(
//            String libelle, String reference, Entreprise entreprise);

    Optional<Produit> findByReference(String reference);

    Optional<Produit> findByLibelle(String libelle);
    
      
    // Recherche optimisée avec LIKE et index
    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND " +
           "(LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY " +
           "CASE WHEN LOWER(a.libelle) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 1 " +
           "     WHEN LOWER(a.reference) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2 " +
           "     WHEN LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) THEN 3 " +
           "     ELSE 4 END, a.libelle ASC")
    Page<Produit> searchArticles(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Recherche rapide pour l'autocomplétion (limité à 100 résultats)
    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND " +
           "(LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY " +
           "CASE WHEN LOWER(a.libelle) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 1 " +
           "     WHEN LOWER(a.reference) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2 " +
           "     ELSE 3 END, a.libelle ASC")
    List<Produit> findForAutocomplete(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Recherche par catégorie avec pagination
    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND " +
           "(:category IS NULL OR a.categories.libelle = :category) " +
           "ORDER BY a.libelle ASC")
    Page<Produit> findByCategory(@Param("category") String category, Pageable pageable);
    
    // Recherche combinée catégorie + terme
    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND " +
           "(:category IS NULL OR a.categories.libelle = :category) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.libelle ASC")
    Page<Produit> findByCategoryAndSearch(@Param("category") String category, 
                                        @Param("searchTerm") String searchTerm, 
                                        Pageable pageable);
    
    // Requête native pour recherche full-text (si supportée par votre DB)
    @Query(value = "SELECT * FROM Produit a WHERE a.deletes = false AND " +
                   "MATCH(a.libelle, a.description) AGAINST(:searchTerm IN NATURAL LANGUAGE MODE) " +
                   "ORDER BY MATCH(a.libelle, a.description) AGAINST(:searchTerm IN NATURAL LANGUAGE MODE) DESC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Produit> fullTextSearch(@Param("searchTerm") String searchTerm, @Param("limit") int limit);

    @Query(value = "SELECT * FROM Produit a WHERE a.deletes = false AND a.compagnie_id = :compagnieId AND " +
                   "MATCH(a.libelle, a.description) AGAINST(:searchTerm IN NATURAL LANGUAGE MODE) " +
                   "ORDER BY MATCH(a.libelle, a.description) AGAINST(:searchTerm IN NATURAL LANGUAGE MODE) DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<Produit> fullTextSearchByCompagnie(@Param("searchTerm") String searchTerm, @Param("compagnieId") Long compagnieId, @Param("limit") int limit);
    
    // Méthodes de base optimisées
    Page<Produit> findByDeletesFalse(Pageable pageable);

    List<Produit> findTop10000ByDeletesFalseOrderByLibelleAsc();

    // ===== Isolation multi-tenant : variantes scopees par compagnie =====
    // Utilisees par ProduitService a la place des methodes ci-dessus des que
    // l'appelant est un utilisateur de compagnie (voir TenantContext).
    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND a.compagnie.id = :compagnieId AND " +
           "(LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Produit> searchArticlesByCompagnie(@Param("searchTerm") String searchTerm, @Param("compagnieId") Long compagnieId, Pageable pageable);

    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND a.compagnie.id = :compagnieId AND " +
           "(:category IS NULL OR a.categories.libelle = :category)")
    Page<Produit> findByCategoryAndCompagnie(@Param("category") String category, @Param("compagnieId") Long compagnieId, Pageable pageable);

    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND a.compagnie.id = :compagnieId AND " +
           "(:category IS NULL OR a.categories.libelle = :category) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Produit> findByCategoryAndSearchAndCompagnie(@Param("category") String category,
                                        @Param("searchTerm") String searchTerm,
                                        @Param("compagnieId") Long compagnieId,
                                        Pageable pageable);

    Page<Produit> findByDeletesFalseAndCompagnie_Id(Long compagnieId, Pageable pageable);

    List<Produit> findTop10000ByDeletesFalseAndCompagnie_IdOrderByLibelleAsc(Long compagnieId);

    boolean existsByReferenceAndDeletesFalseAndCompagnie_Id(String reference, Long compagnieId);

    // Lookup par id scope compagnie - a utiliser a la place de findById() partout
    // ou l'id vient d'une requete client (evite qu'un utilisateur d'une
    // compagnie A manipule un id de produit appartenant a une compagnie B).
    Optional<Produit> findByIdAndCompagnie_Id(Long id, Long compagnieId);
    Optional<Produit> findByReferenceAndCompagnie_Id(String reference, Long compagnieId);

    // Variante tolerante aux doublons (voir StockRestaurationService) : deux
    // Produit peuvent partager la meme reference si un import anterieur en a
    // cree en double - findByReferenceAndCompagnie_Id planterait
    // (IncorrectResultSizeDataAccessException) au lieu de planter la
    // restauration entiere, on prend simplement le premier. Le "First" (LIMIT 1)
    // est essentiel : charger TOUS les doublons dans le contexte de
    // persistance (findAllBy...) faisait planter Hibernate au flush avec
    // "Found shared references to a collection" des qu'un des doublons
    // trainait dans la session sans etre jamais modifie.
    Optional<Produit> findFirstByReferenceAndCompagnie_IdOrderByIdAsc(String reference, Long compagnieId);

    long countByReferenceAndCompagnie_Id(String reference, Long compagnieId);

    // Produits sans plus aucun PointVente nulle part (toutes boutiques
    // confondues) pour cette compagnie - utilise par
    // StockRestaurationController.reinitialiserBoutique une fois le stock
    // d'une boutique deja vide : a ce moment-la, on ne peut plus retrouver
    // "les produits de cette boutique" via PointVente (il n'y en a plus), le
    // seul signal restant est "plus aucun stock du tout, dans aucune boutique".
    @Query("SELECT p.id FROM Produit p WHERE p.compagnie.id = :compagnieId "
            + "AND p.id NOT IN (SELECT DISTINCT pv.produit.id FROM PointVente pv)")
    List<Long> findIdsWithoutAnyPointVente(@Param("compagnieId") Long compagnieId);

    // Suppression en masse (DML direct, aucune entite chargee) - voir
    // StockRestaurationService.reinitialiserBoutique : deleteById() de
    // Spring Data charge puis supprime l'entite (declenche le cascade=ALL
    // sur facturecommandefournisseurCollection), ce qui replante avec
    // "Found shared references to a collection" des qu'un doublon de
    // reference est implique. Le DELETE direct l'evite completement -
    // echoue proprement (contrainte FK) si le produit a encore des lignes
    // liees ailleurs (ex. Facturecommandefournisseur) plutot que de les
    // orpheliner silencieusement.
    @Modifying
    @Query("DELETE FROM Produit p WHERE p.id = :produitId")
    int deleteByIdBulk(@Param("produitId") Long produitId);

    // Diagnostic ponctuel (voir StockRestaurationController) : liste exacte
    // des contraintes FK reelles en base pointant vers produit(id), plutot
    // que de deviner via les mappings JPA (@OneToMany) qui peuvent ne pas
    // toutes correspondre a une contrainte reellement creee par ddl-auto=update.
    @Query(value = "SELECT CONCAT(TABLE_NAME, '.', COLUMN_NAME) FROM information_schema.KEY_COLUMN_USAGE "
            + "WHERE REFERENCED_TABLE_NAME = 'produit' AND REFERENCED_COLUMN_NAME = 'id' AND TABLE_SCHEMA = DATABASE()",
            nativeQuery = true)
    List<String> findForeignKeysReferencingProduitDiagnostic();

    /**
     * true si AUCUNE des 18 tables ayant une contrainte FK vers produit(id)
     * (liste obtenue via findForeignKeysReferencingProduitDiagnostic, cf.
     * ProduitCleanupService) n'a de ligne pour ce produit. Pre-check
     * deliberement utilise a la place d'un try/catch autour du DELETE : en
     * JPA, des qu'une exception survient pendant l'execution d'une requete,
     * la transaction est marquee "rollback-only" par le fournisseur JPA lui
     * meme (pas seulement par Spring) - l'attraper cote Java ne l'empeche
     * pas, le commit echoue quand meme ensuite avec
     * UnexpectedRollbackException. Verifier avant plutot qu'essayer/attraper
     * evite de declencher l'exception du tout. prixachat est volontairement
     * exclu de cette liste : ProduitCleanupService le supprime lui-meme
     * inconditionnellement avant de supprimer le produit (ce n'est pas un
     * blocage externe, juste un enfant nettoye dans la meme operation).
     */
    @Query(value = "SELECT NOT EXISTS ("
            + "SELECT 1 FROM commande_easy WHERE produitid = :produitId "
            + "UNION ALL SELECT 1 FROM devis_item WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM facture_item_customer WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM facture_item_snapshot WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM historique_correction_stock WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM historique_restauration_stock WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM inventaire WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM lignevente WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM pointvente_easy WHERE produitid = :produitId "
            + "UNION ALL SELECT 1 FROM prix_historique WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM prixclient WHERE produitid = :produitId "
            + "UNION ALL SELECT 1 FROM prixvente WHERE produitid = :produitId "
            + "UNION ALL SELECT 1 FROM produitachat WHERE produitid = :produitId "
            + "UNION ALL SELECT 1 FROM specificationarticles WHERE artticleid = :produitId "
            + "UNION ALL SELECT 1 FROM stock_movement WHERE produit_id = :produitId "
            + "UNION ALL SELECT 1 FROM taxeproduits WHERE produitid = :produitId "
            + "UNION ALL SELECT 1 FROM transfert_stock WHERE produit_id = :produitId"
            + ")", nativeQuery = true)
    boolean estSupprimableSansReferenceRestante(@Param("produitId") Long produitId);
    Optional<Produit> findByLibelleAndCompagnie_Id(String libelle, Long compagnieId);
    Page<Produit> findAllByCompagnie_Id(Long compagnieId, Pageable pageable);
    List<Produit> findAllByCompagnie_Id(Long compagnieId);

    @Query("SELECT a FROM Produit a WHERE a.deletes = false AND a.compagnie.id = :compagnieId AND " +
           "(LOWER(a.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Produit> findForAutocompleteByCompagnie(@Param("searchTerm") String searchTerm, @Param("compagnieId") Long compagnieId, Pageable pageable);
    
    @Query("SELECT DISTINCT a.categories.libelle FROM Produit a WHERE a.deletes = false AND a.categories.libelle IS NOT NULL ORDER BY a.categories.libelle")
    List<String> findAllCategories();
    
    // Recherche par code exact (pour validation)
//    @Query("SELECT p FROM Produit p WHERE p.reference= :code")
//    boolean existsByCodeAndActiveTrue(@Param("searchTerm")String code);
      @Query("SELECT p FROM Produit p WHERE p.reference= :code")
    Produit findByCodeAndActiveTrue(@Param("searchTerm")String code);
    //✅ CORRECTION : Vérifier seulement les produits actifs (deletes = false)
    Optional<Produit> findByReferenceAndDeletesFalse(String reference);
    
    // ✅ Plus performant pour vérifier l'existence
    boolean existsByReferenceAndDeletesFalse(String reference);
    Optional<Produit> findByCode(String code);

    List<Produit> findByDeletes(Boolean deletes);
    
    // ========================================
    // MÉTHODE POUR RÉCUPÉRER PLUSIEURS PRODUITS PAR IDS
    // ========================================
    
    // ✅ OPTION 1: Méthode Spring Data JPA (RECOMMANDÉ - Simple et efficace)
    List<Produit> findByIdIn(List<Long> ids);
    
    // ✅ OPTION 2: Avec @Query personnalisée
    @Query("SELECT p FROM Produit p WHERE p.id IN :ids")
    List<Produit> findProductsByIds(@Param("ids") List<Long> ids);
    
    // ✅ OPTION 3: Avec @Query et boutique
    @Query("SELECT p FROM Produit p  join p.pointventeCollection pv WHERE p.id IN :ids AND pv.boutique.id = :boutiqueId")
    List<Produit> findProductsByIdsAndBoutique(
            @Param("ids") List<Long> ids, 
            @Param("boutiqueId") Long boutiqueId
    );
   
     @Query("SELECT COUNT(DISTINCT pv.produit) FROM PointVente pv " +
            "WHERE pv.entreprise.entreprisePK = :pk " +
            "AND pv.produit.deletes = true")
    Long countProduitsSupprimesDansPointsVente(@Param("pk") EntreprisePK entreprisePK);

   // Optional<Produit> findByCode(String code);

    @Query("SELECT p FROM Produit p WHERE p.deletes = false OR p.deletes IS NULL")
    List<Produit> findAllNonSupprimes();
}
