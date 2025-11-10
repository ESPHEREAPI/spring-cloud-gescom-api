/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.ArticleSearchResponse;
import com.mproduits.dto.ProduitDto;
import com.mproduits.model.Specifique;
import com.mproduits.repositories.SpecifiqueRepositories;
import com.mproduits.services.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.mproduits.dto.ApiResponse;
import org.springframework.http.HttpStatus;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits")
//@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
//@Tag(name = "Articles", description = "API pour la gestion des articles")
public class ArticleController {

    private final ProduitService articleService;
    private final SpecifiqueRepositories specifiqueRepositories;

    @Autowired
    public ArticleController(ProduitService articleService, SpecifiqueRepositories specifiqueRepositories) {
        this.articleService = articleService;
        this.specifiqueRepositories = specifiqueRepositories;
    }

    /**
     * Endpoint optimisé pour l'autocomplétion GET
     * /api/articles/autocomplete?q=terme&limit=100
     *
     * @return
     */
    @GetMapping("/produits/autocomplete")
    @Operation(summary = "Recherche rapide pour autocomplétion",
            description = "Retourne une liste limitée d'articles pour l'autocomplétion. Optimisé pour la performance.")
   // @ApiResponse(responseCode = "200", description = "Liste des articles trouvés")
    public ResponseEntity<List<ProduitDto>> autocomplete(
            @Parameter(description = "Terme de recherche (minimum 2 caractères)")
            @RequestParam(value = "q", required = false)
            @Size(min = 2, max = 100, message = "Le terme de recherche doit contenir entre 2 et 100 caractères") String query,
            @Parameter(description = "Nombre maximum de résultats (max 10000)")
            @RequestParam(value = "limit", defaultValue = "1000")
            @Min(value = 1, message = "La limite doit être au moins 1")
            @Max(value = 10000, message = "La limite ne peut pas dépasser 10000") Integer limit) {

        List<ProduitDto> articles = articleService.searchForAutocomplete(query, limit);

        // Cache côté client pendant 2 minutes pour les requêtes fréquentes
        CacheControl cacheControl = CacheControl.maxAge(2, TimeUnit.MINUTES)
                .cachePublic();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .header("X-Total-Count", String.valueOf(articles.size()))
                .body(articles);
    }

    /**
     * Endpoint principal de recherche avec pagination GET
     * /api/articles/search?q=terme&category=cat&page=0&size=20
     *
     * @param query
     * @param category
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/articles/search")
    @Operation(summary = "Recherche complète avec pagination",
            description = "Recherche avancée avec filtres et pagination")
  //  @ApiResponse(responseCode = "200", description = "Résultats de recherche paginés")
    public ResponseEntity<ArticleSearchResponse> search(
//            @Parameter(description = "Terme de recherche")
//            @RequestParam(value = "q", required = false)
//            @Size(max = 100, message = "Le terme de recherche ne peut pas dépasser 100 caractères") String query,
//            @Parameter(description = "Catégorie à filtrer")
//            @RequestParam(value = "category", required = false) String category,
//            @Parameter(description = "Numéro de page (commence à 0)")
//            @RequestParam(value = "page", defaultValue = "0")
//            @Min(value = 0, message = "Le numéro de page doit être positif") Integer page,
//            @Parameter(description = "Taille de la page (max 5000)")
//            @RequestParam(value = "size", defaultValue = "20")
//            @Min(value = 1, message = "La taille de page doit être au moins 1")
//            @Max(value = 5000, message = "La taille de page ne peut pas dépasser 5000") Integer size
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
             @RequestParam(required = false) String category) 
    {

        ArticleSearchResponse response = articleService.searchArticles(query, "", page, size);

        // Headers pour pagination
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(response.getTotalPages()))
                .header("X-Current-Page", String.valueOf(response.getPage()))
                .body(response);
    }

    /**
     * Endpoint pour récupérer les articles populaires/récents GET
     * /api/articles/top
     *
     * @return
     */
    @GetMapping("/top")
    @Operation(summary = "Articles populaires",
            description = "Retourne les 50 articles les plus populaires pour l'affichage initial")
   // @ApiResponse(responseCode = "200", description = "Liste des articles populaires")
    public ResponseEntity<List<ProduitDto>> getTopArticles() {
        List<ProduitDto> articles = articleService.getTopArticles();

        // Cache plus long pour les articles populaires
        CacheControl cacheControl = CacheControl.maxAge(10, TimeUnit.MINUTES)
                .cachePublic();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(articles);
    }

    /**
     * Endpoint pour récupérer toutes les catégories GET
     * /api/articles/categories
     */
    @GetMapping("/categories-auto")
    @Operation(summary = "Liste des catégories",
            description = "Retourne toutes les catégories disponibles")
    //@ApiResponse(responseCode = "200", description = "Liste des catégories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = articleService.getAllCategories();

        // Cache long pour les catégories (changent peu souvent)
        CacheControl cacheControl = CacheControl.maxAge(30, TimeUnit.MINUTES)
                .cachePublic();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(categories);
    }

    /**
     * Endpoint pour recherche par code exact GET /api/articles/by-code/{code}
     *
     * @param code
     * @return
     */
    @GetMapping("/by-code/{code}")
    @Operation(summary = "Recherche par code",
            description = "Trouve un article par son code exact")
  //  @ApiResponse(responseCode = "200", description = "Article trouvé")
    //@ApiResponse(responseCode = "404", description = "Article non trouvé")
    public ResponseEntity<ProduitDto> findByCode(
            @Parameter(description = "Code de l'article")
            @PathVariable
            @Size(min = 1, max = 50, message = "Le code doit contenir entre 1 et 50 caractères") String code) {

        ProduitDto article = articleService.findByReference(code);

        if (article != null) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                    .body(article);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint pour vérifier l'existence d'un code GET
     * /api/articles/exists/{code}
     */
    @GetMapping("/exists/{code}")
    @Operation(summary = "Vérifier l'existence d'un code",
            description = "Vérifie si un code article existe déjà")
   // @ApiResponse(responseCode = "200", description = "Résultat de la vérification")
    public ResponseEntity<Boolean> existsByCode(
            @Parameter(description = "Code à vérifier")
            @PathVariable
            @Size(min = 1, max = 50, message = "Le code doit contenir entre 1 et 50 caractères") String code) {

        boolean exists = articleService.existsByReference(code);
        return ResponseEntity.ok(exists);
    }

    /**
     * Endpoint pour recherche full-text (si supportée) GET
     * /api/articles/fulltext?q=terme&limit=100
     *
     * @param query
     * @param limit
     * @return
     */
    @GetMapping("/fulltext")
    @Operation(summary = "Recherche full-text",
            description = "Recherche full-text avancée (si supportée par la base de données)")
   // @ApiResponse(responseCode = "200", description = "Résultats de la recherche full-text")
    public ResponseEntity<List<ProduitDto>> fullTextSearch(
            @Parameter(description = "Terme de recherche pour full-text")
            @RequestParam(value = "q", required = true)
            @Size(min = 2, max = 100, message = "Le terme de recherche doit contenir entre 2 et 100 caractères") String query,
            @Parameter(description = "Nombre maximum de résultats")
            @RequestParam(value = "limit", defaultValue = "100")
            @Min(value = 1, message = "La limite doit être au moins 1")
            @Max(value = 100, message = "La limite ne peut pas dépasser 100") Integer limit) {

        List<ProduitDto> articles = articleService.fullTextSearch(query, limit);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(articles.size()))
                .body(articles);
    }

    /**
     *
     * @return
     */
    @GetMapping("/specifiques")
    public ResponseEntity<List<Specifique>> produits() {

        List<Specifique> allSpecifique = specifiqueRepositories.findAll();
        return ResponseEntity.ok(allSpecifique);
    }

    /**
     *
     * @param produitDto
     * @return
     */
    @PostMapping("/articles")
   public ResponseEntity<ApiResponse<ProduitDto>> createArticles( @RequestBody ProduitDto produitDto) {
    
    ApiResponse<ProduitDto> response = articleService.createArticles(produitDto);
    
    return ResponseEntity
        .status(HttpStatus.CREATED)  // ✅ 201 au lieu de 200
        .body(response);
}

    @PutMapping("/articles/{id}")
    public ResponseEntity<ProduitDto> updateArticles(@PathVariable Long id, @RequestBody ProduitDto produitDto) {
        ProduitDto produitDtoUpdate = articleService.updateProduit(id, produitDto);

        return ResponseEntity.ok(produitDtoUpdate);
    }

    @DeleteMapping("/articles/{id}")
    public void selectArticles(@PathVariable Long id) {
       articleService.delesteArticles(id);

        //return ResponseEntity.ok();
    }
}
