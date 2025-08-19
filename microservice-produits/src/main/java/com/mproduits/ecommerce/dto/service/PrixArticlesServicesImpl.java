/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.service;

import com.mproduits.ecommerce.dto.CategoriesDTO;
import com.mproduits.ecommerce.dto.PrixarticlesDTO;
import com.mproduits.ecommerce.dto.mappers.ProduitMapperImpl;
import com.mproduits.ecommerce.dto.repositories.CategoriesRepository;
import com.mproduits.model.Categories;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PrixArticles;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 *
 * @author USER01
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PrixArticlesServicesImpl implements PrixArticlesServices {

    private ProduitMapperImpl dtoMapper;
    private PrixArticlesRepositories prixArticleRepository;
    private CategoriesRepository categoriesRepository;
    private EntrepriseRepositories entrepriseRepositories;
  
    @Override
    public List<PrixarticlesDTO> listeProduits() {
        log.info("lister des produits");
        List<PrixArticles> listeProduits = prixArticleRepository.findAll();
        List<PrixarticlesDTO> colletcDTO = listeProduits.stream()
                .map(prod -> dtoMapper.formPrixarticles(prod))
                .collect(Collectors.toList());
        return colletcDTO;
    }

    @Override
    public List<PrixarticlesDTO> searchProduitByLibelle(String keyword) {
              Entreprise e=entrepriseRepositories.findByActif(Boolean.TRUE);
        List<PrixArticles> listeProduits = prixArticleRepository.searchProduit(keyword,e.getAnnee().getId());
        List<PrixarticlesDTO> colletcDTO = listeProduits.stream().map(prod -> dtoMapper.formPrixarticles(prod)).collect(Collectors.toList());
        return colletcDTO;

    }

    @Override
    public List<PrixarticlesDTO> searchProduitByCategorie(long idCategorie) {
        Entreprise e=entrepriseRepositories.findByActif(Boolean.TRUE);
        List<PrixArticles> listeProduits = prixArticleRepository.searchProduitBycategories(idCategorie,e.getAnnee().getId());
        List<PrixarticlesDTO> colletcDTO = listeProduits.stream().map(prod -> dtoMapper.formPrixarticles(prod)).collect(Collectors.toList());
        return colletcDTO;
    
    }

    @Override
    public List<CategoriesDTO> listeCategorieHaveProduit() {
         List<Categories>  listeCategories=this.categoriesRepository.findAll();
         List<CategoriesDTO> collectDTOs=listeCategories.stream().map(cat-> dtoMapper.formCategories(cat)).collect(Collectors.toList());
         return collectDTOs;
    }

 

}
