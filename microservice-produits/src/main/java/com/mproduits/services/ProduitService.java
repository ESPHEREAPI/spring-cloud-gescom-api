/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.ApiResponse;
import com.mproduits.dto.ArticleSearchResponse;
import com.mproduits.dto.ProduitDto;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Categories;
import com.mproduits.model.Produit;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.exceptions.GlobalException;
import com.mproduits.model.Boutique;
import com.mproduits.model.Compagnie;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.security.TenantContext;
import java.math.BigDecimal;
import java.util.Date;


import java.util.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 *
 * @author USER01
 */
@Service
@Transactional(readOnly = true)

@Slf4j


public class ProduitService {

    @Autowired
    private ProduitRepositories articleRepository;
    @Autowired
    CategorieRepositories categorieRepositories;
    @Autowired
    MapperDtoImpl mapperdto;
    private static final int DEFAULT_AUTOCOMPLETE_LIMIT = 100;
    private static final int MAX_SEARCH_LIMIT = 500;
      @Autowired
      BoutiqueRepositories boutiqueRepository;
      @Autowired
      TenantContext tenantContext;

    /**
     * Recherche optimisée pour l'autocomplétion Cache les résultats fréquents
     * pendant 5 minutes
     *
     * @param searchTerm
     * @param limit
     * @return
     */
    @Cacheable(value = "articleAutocomplete",
            key = "#searchTerm + '_' + #limit + '_' + @tenantContext.currentCompagnieId()",
            unless = "#searchTerm.length() < 2")
    public List<ProduitDto> searchForAutocomplete(String searchTerm, Integer limit,Long boutiqueid) {
        if (!StringUtils.hasText(searchTerm) || searchTerm.trim().length() < 2) {
            return getTopArticles(boutiqueid);
        }

        int searchLimit = (limit != null && limit > 0)
                ? Math.min(limit, DEFAULT_AUTOCOMPLETE_LIMIT)
                : DEFAULT_AUTOCOMPLETE_LIMIT;

        Pageable pageable = PageRequest.of(0, searchLimit);
        List<Produit> articles = articleRepository.findForAutocompleteByCompagnie(
                searchTerm.trim(), tenantContext.currentCompagnieId(), pageable);
        Optional<Boutique>boutique=boutiqueRepository.findById(boutiqueid);

        return (List<ProduitDto>) articles.stream()
                .map(a -> mapperdto.mapperProduitDto(a,boutique.isPresent() ? boutique.get():new Boutique()))
                .collect(Collectors.toList());
    }
    
     // ==================== VALIDATION ======g==============

    
    public boolean existsByReference(String reference) {
        if (!StringUtils.hasText(reference)) {
            return false;
        }
        return articleRepository.existsByReferenceAndDeletesFalseAndCompagnie_Id(reference.trim(), tenantContext.currentCompagnieId());
    }

    /**
     * Recherche paginée complète
     */
    public ArticleSearchResponse searchArticles(String searchTerm, String category,
            Integer page, Integer size) {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0)
                ? Math.min(size, MAX_SEARCH_LIMIT) : 20;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Produit> articlePage;

        Long compagnieId = tenantContext.currentCompagnieId();
        if (StringUtils.hasText(searchTerm) && StringUtils.hasText(category)) {
            articlePage = articleRepository.findByCategoryAndSearchAndCompagnie(
                    category, searchTerm.trim(), compagnieId, pageable);
        } else if (StringUtils.hasText(searchTerm)) {
            articlePage = articleRepository.searchArticlesByCompagnie(searchTerm.trim(), compagnieId, pageable);
        } else if (StringUtils.hasText(category)) {
            articlePage = articleRepository.findByCategoryAndCompagnie(category, compagnieId, pageable);
        } else {
            articlePage = articleRepository.findByDeletesFalseAndCompagnie_Id(compagnieId, pageable);
        }
 //Optional<Boutique>boutique=boutiqueRepository.findById(boutiqueid);
        List<ProduitDto> articles = articlePage.getContent().stream()
                .map(a -> mapperdto.mapperProduitDto(a))
                .collect(Collectors.toList());

        return new ArticleSearchResponse(
                articles,
                articlePage.getTotalElements(),
                articlePage.getTotalPages(),
                articlePage.getNumber(),
                articlePage.getSize(),
                articlePage.isFirst(),
                articlePage.isLast()
        );
    }

    /**
     * Récupère les articles les plus populaires pour l'affichage initial Cache
     * pendant 10 minutes
     */
    @Cacheable(value = "topArticles",
            key = "#boutiqueid + '_' + @tenantContext.currentCompagnieId()",
            unless = "#result.isEmpty()")
    public List<ProduitDto> getTopArticles(Long boutiqueid) {
        List<Produit> articles = articleRepository.findTop10000ByDeletesFalseAndCompagnie_IdOrderByLibelleAsc(tenantContext.currentCompagnieId());
        Optional<Boutique>boutique=boutiqueRepository.findById(boutiqueid);
        
        return articles.stream()
                .map(p -> mapperdto.mapperProduitDto(p,boutique.isPresent() ? boutique.get():new Boutique()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les catégories disponibles Cache pendant 30 minutes
     */
    @Cacheable(value = "categories", unless = "#result.isEmpty()")
    public List<String> getAllCategories() {
        return articleRepository.findAllCategories();
    }

    /**
     * Recherche par code exact
     */
    public ProduitDto findByReference(String reference, Long boutiqueid) {
        if (!StringUtils.hasText(reference)) {
            return null;
        }

        Produit article = articleRepository.findByReferenceAndCompagnie_Id(reference.trim(), tenantContext.currentCompagnieId()).orElse(null);
        Optional<Boutique> boutique =boutiqueRepository.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        return article != null ? mapperdto.mapperProduitDto(article,boutique.isPresent() ? boutique.get(): new Boutique()) : null;
    }

    /**
     * Vérifie si un code existe déjà
     */
//    public boolean existsByReference(String code) {
//        if (!StringUtils.hasText(code)) {
//            return false;
//        }
//        return articleRepository.findByCodeAndActiveTrue(code.trim()) != null;
//    }

    /**
     * Recherche full-text (si supportée par la base de données)
     */
    public List<ProduitDto> fullTextSearch(String searchTerm, Integer limit) {
//        if (!StringUtils.hasText(searchTerm)) {
//            return getTopArticles(boutiqueid);
//        }

        int searchLimit = (limit != null && limit > 0)
                ? Math.min(limit, DEFAULT_AUTOCOMPLETE_LIMIT)
                : DEFAULT_AUTOCOMPLETE_LIMIT;

        Long compagnieId = tenantContext.currentCompagnieId();
        try {
            List<Produit> articles = articleRepository.fullTextSearchByCompagnie(
                    searchTerm.trim(), compagnieId, searchLimit);
            return articles.stream()
                    .map(p -> mapperdto.mapperProduitDto(p))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback vers recherche LIKE si full-text non supporté
           // return searchForAutocomplete(searchTerm, limit//);
        }
        List<Produit> fallback = this.articleRepository.findTop10000ByDeletesFalseAndCompagnie_IdOrderByLibelleAsc(compagnieId);
        return fallback.subList(0, Math.min(50, fallback.size())).stream().map(p -> mapperdto.mapperProduitDto(p)).toList();
    }

    /**
     * Conversion Article -> ArticleDTO
     *
     * @param produitDto
     * @return
     */

    
    /**
     * Crée un nouveau produit
     * @param produitDto Données du produit
     * @return Produit créé
     */
    @Transactional
    public ApiResponse<ProduitDto> createArticles(ProduitDto produitDto) {
        log.info("Création du produit avec référence: {}", produitDto.getReference());

        Long compagnieId = tenantContext.currentCompagnieId();
        // ✅ Vérifier si la référence existe (seulement produits actifs, dans cette compagnie)
        if (articleRepository.existsByReferenceAndDeletesFalseAndCompagnie_Id(produitDto.getReference(), compagnieId)) {
            log.error("La référence {} existe déjà", produitDto.getReference());
            throw new GlobalException("Erreur de création",
                "Un produit avec la référence '" + produitDto.getReference() + "' existe déjà");
        }

        // ✅ Vérifier la catégorie
        Categories categorie = categorieRepositories.findById(produitDto.getCategories().getId())
            .orElseThrow(() -> new GlobalException("Erreur sur la catégorie", "Catégorie non existante"));

        // ✅ Créer et configurer le produit
        Produit produit = buildProduitFromDto(produitDto, categorie);
        produit.setCompagnie(new Compagnie(compagnieId));

        // ✅ Sauvegarder
        Produit produitSave = articleRepository.save(produit);
        log.info("✅ Produit créé avec succès - ID: {}, Référence: {}", 
            produitSave.getId(), produitSave.getReference());
        
        // ✅ Retourner le DTO
        ProduitDto produitDtoSaved = mapperdto.mapperProduitDto(produitSave);
         // ✅ NOUVELLE LIGNE
    return ApiResponse.created(produitDtoSaved, "Produit créé avec succès");
    }
    
    /**
     * ✅ Méthode privée pour construire le produit (réutilisable)
     */
    private Produit buildProduitFromDto(ProduitDto dto, Categories categorie) {
        Produit produit = new Produit();
        produit.setCategorie(categorie);
        produit.setReference(dto.getReference());
        produit.setDescription(dto.getDescription());
        produit.setLibelle(dto.getLibelle());
        produit.setPacquets(dto.getPacquets());
        produit.setPrixVenteModifiable(dto.getPrixVenteModifiable());
        produit.setPrixVenteModifiableAccepter(dto.getPrixVenteModifiableAccepter());
        // Identite verifiee par le JWT - jamais celle envoyee par le client
        // dans le corps de la requete (falsifiable).
        produit.setUsername(tenantContext.currentUsername());
        produit.setDeletes(Boolean.FALSE);
        
        // ✅ UNE SEULE FOIS (corrigé la duplication)
        if (dto.getQuantiteByPacquet() != null) {
            produit.setQuantiteByPacquet(BigDecimal.valueOf(dto.getQuantiteByPacquet().doubleValue()));
        }
        
        return produit;
    }

    @Transactional(readOnly = false)
    public ProduitDto updateProduit(Long id, ProduitDto produitDto) {
        Optional<Produit> p = articleRepository.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId());
        if (p.isEmpty()) {
            throw new GlobalException("Erreur ..... ", "Articles non existant  ");
        }
        Optional<Categories> cat = categorieRepositories.findById(produitDto.getCategories().getId());
        if (cat.isEmpty()) {
            throw new GlobalException("Erreur sur la categorie ", "categorie non existant");

        }
        Produit prd = p.get();
        prd.setCategorie(cat.get());
        prd.setReference(produitDto.getReference());
        prd.setDescription(produitDto.getDescription());
        prd.setLibelle(produitDto.getLibelle());
        prd.setPacquets(produitDto.getPacquets());
        prd.setQuantiteByPacquet(produitDto.getQuantiteByPacquet()!=null ? BigDecimal.valueOf(produitDto.getQuantiteByPacquet().doubleValue()):BigDecimal.ZERO);
        prd.setPrixVenteModifiable(produitDto.getPrixVenteModifiable());
        prd.setPrixVenteModifiableAccepter(produitDto.getPrixVenteModifiableAccepter());
        prd.setDeletes(produitDto.getDeletes());
       // prd.setQuantiteByPacquet(BigDecimal.valueOf(produitDto.getQuantiteByPacquet().doubleValue()));
        prd.setCategorie(cat.get());

        if (Objects.equals(produitDto.getDeletes(), Boolean.TRUE)) {
            prd.setDateDelete(new Date());
            prd.setDeleteUser(tenantContext.currentUsername());

        }
        prd.setUsernameupdate(tenantContext.currentUsername());
        Produit prodSave = articleRepository.save(prd);
        System.out.println("qunatite pacquet " + prodSave.getQuantiteByPacquet());
        // Mise a jour du catalogue, pas scopee a une boutique : la variante
        // sans Boutique (pas de calcul de stockFinal) evite de passer une
        // Boutique() transitoire/non sauvegardee a une requete JPA
        // (TransientObjectException - stock d'une boutique factice n'aurait
        // de toute facon aucun sens ici).
        ProduitDto pdto = mapperdto.mapperProduitDto(prodSave);
        return pdto;

    }

    @Transactional(readOnly = false)
    public void delesteArticles(Long id) {
        Optional<Produit> p = articleRepository.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId());
        if (p.isEmpty()) {
            throw new GlobalException("Erreur ..... ", "Articles non existant  ");
        }
        articleRepository.delete(p.get());
    }
    
    public List<ProduitDto> searchProduitsByBoutique(Long boutiqueid, String searchTerm) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
        return new ArrayList<>();
    }
    
    String term = searchTerm.trim().toLowerCase();

    return articleRepository.findAllByCompagnie_Id(tenantContext.currentCompagnieId()).stream()
        .filter(p -> !p.getDeletes())
        .filter(p ->
            p.getLibelle().toLowerCase().contains(term) ||
            (p.getReference() != null && p.getReference().toLowerCase().contains(term))
        )
          .map(pr -> mapperdto.mapperProduitDto(pr))
        .limit(100)
        .collect(Collectors.toList());
}
    
}
