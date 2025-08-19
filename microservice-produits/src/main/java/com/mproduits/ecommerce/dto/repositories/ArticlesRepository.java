/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.ecommerce.dto.repositories;

import com.mproduits.ecommerce.dto.entites.Articles;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 *
 * @author USER01
 */
@Repository
public interface ArticlesRepository extends JpaRepository<Articles, Long> {

    @Query("SELECT a FROM Articles a WHERE a.reference like %:kw%")
    public List<Articles> searchArticles(@Param(value = "kw") String keyword);
   public List<Articles> findByReferenceContains(String keyword);
    @Query("SELECT a FROM Articles a WHERE a.categorie= :idCategorie")
    public List<Articles> searchArticlesBycategories(@Param(value = "idCategorie") Long id);
     @Query("SELECT a FROM Articles a WHERE a.categorie= :idCategorie and a.reference like %:kw%")
    public List<Articles> searchArticlesBycategories(@Param(value = "idCategorie") Long id,@Param(value = "kw") String keyword);
    public Articles findByProduit(long produit);
   // @Query("SELECT a FROM Articles a WHERE a.quantite")
    
   

}
