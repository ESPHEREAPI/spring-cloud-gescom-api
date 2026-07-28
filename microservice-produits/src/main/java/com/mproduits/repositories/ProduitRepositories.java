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
    
    // Méthodes de base optimisées
    Page<Produit> findByDeletesFalse(Pageable pageable);

    List<Produit> findTop10000ByDeletesFalseOrderByLibelleAsc();
    
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
