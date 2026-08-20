/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.ArticleSearchResponse;
import com.mproduits.dto.ProduitDto;
import com.mproduits.model.Produit;
import com.mproduits.model.ProduitPhoto;
import com.mproduits.model.Specifique;
import com.mproduits.repositories.ProduitPhotoRepository;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.SpecifiqueRepositories;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.security.TenantContext;
import com.mproduits.services.ProduitService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
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
import com.mproduits.dto.BatchValeur;
import com.mproduits.dto.DecrementStockRequest;
import com.mproduits.dto.StockDecrementItem;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.services.StockService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import java.util.*;


/**
 *
 * @author USER01
 */
@RestController
@Slf4j
@RequestMapping("/microservice-produits")
//@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
//@Tag(name = "Articles", description = "API pour la gestion des articles")
public class ArticleController {

    private static final Set<String> CONTENT_TYPES_PHOTO_AUTORISES = Set.of(
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp");
    private static final long TAILLE_PHOTO_MAX_OCTETS = 3L * 1024 * 1024;

    private final ProduitService articleService;
    private final SpecifiqueRepositories specifiqueRepositories;
    private final StockService stockService;
    private final BoutiqueAccessGuard boutiqueAccessGuard;
    private final ProduitRepositories produitRepositories;
    private final ProduitPhotoRepository produitPhotoRepository;
    private final TenantContext tenantContext;
    @Autowired
    MapperDtoImpl mapperdto;

    @Autowired
    public ArticleController(ProduitService articleService, SpecifiqueRepositories specifiqueRepositories,
            StockService stockService, BoutiqueAccessGuard boutiqueAccessGuard,
            ProduitRepositories produitRepositories, ProduitPhotoRepository produitPhotoRepository,
            TenantContext tenantContext) {
        this.articleService = articleService;
        this.specifiqueRepositories = specifiqueRepositories;
        this.stockService = stockService;
        this.boutiqueAccessGuard = boutiqueAccessGuard;
        this.produitRepositories = produitRepositories;
        this.produitPhotoRepository = produitPhotoRepository;
        this.tenantContext = tenantContext;
    }

    /**
     * Endpoint optimisé pour l'autocomplétion GET
     * /api/articles/autocomplete?q=terme&limit=100
     *
     * @return
     */
    @GetMapping("/produits/autocomplete/{boutiqueid}")
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
            @Max(value = 10000, message = "La limite ne peut pas dépasser 10000") Integer limit, @PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        List<ProduitDto> articles = articleService.searchForAutocomplete(query, limit, boutiqueid);

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
    @GetMapping("/articles/search/{boutiqueid}")
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
            @RequestParam(required = false) String category) {

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
    @GetMapping("/top/{boutiqueid}")
    @Operation(summary = "Articles populaires",
            description = "Retourne les 50 articles les plus populaires pour l'affichage initial")
    // @ApiResponse(responseCode = "200", description = "Liste des articles populaires")
    public ResponseEntity<List<ProduitDto>> getTopArticles(@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        List<ProduitDto> articles = articleService.getTopArticles(boutiqueid);

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

        ProduitDto article = articleService.findByReference(code, 0l);

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
     * @param produitDto
     * @return
     */
    @PostMapping("/articles")
    public ResponseEntity<ApiResponse<ProduitDto>> createArticles(@RequestBody ProduitDto produitDto) {

        ApiResponse<ProduitDto> response = articleService.createArticles(produitDto);

        return ResponseEntity
                .status(HttpStatus.CREATED) // ✅ 201 au lieu de 200
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

    // ========================================
    // MÉTHODE 1: DÉCRÉMENTATION STOCK UNITAIRE
    // ========================================
    /**
     * Décrémente le stock d'un article spécifique
     *
     * Frontend appelle: PUT /stock/{articleId}/decrement Body: { "quantite": 2
     * }
     *
     * @param articleId ID de l'article
     * @param request Objet contenant la quantité à décrémenter
     * @return Le nouveau stock après décrémentation
     */
    @PutMapping("/stock/{articleId}/{boutiqueid}/decrement")
    public ResponseEntity<Integer> decrementStock(
            @PathVariable Long articleId, @PathVariable Long boutiqueid,
            @RequestBody DecrementStockRequest request) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        try {
            // Validation
            if (request.getQuantite() == null || request.getQuantite() <= 0) {
                return ResponseEntity.badRequest().build();
            }

            // Décrémentation du stock
            Integer newStock = stockService.decrementStock(articleId, request.getQuantite(), boutiqueid);

            return ResponseEntity.ok(newStock);

        } catch (IllegalArgumentException e) {
            // Article non trouvé ou stock insuffisant
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========================================
    // MÉTHODE 2: RAFRAÎCHISSEMENT BATCH PRODUITS
    // ========================================
    /**
     * Récupère les détails complets de plusieurs produits
     *
     * Frontend appelle: POST /produits/batch Body: [1, 2, 3, 45, 67]
     *
     * @param articleIds Liste des IDs d'articles à récupérer
     * @return Liste des produits avec leurs détails complets
     */
    @PostMapping("/produits/batch/{boutiqueid}")
    public ResponseEntity<List<ProduitDto>> getProductsBatch(@PathVariable Long boutiqueid,
            @RequestBody List<Long> articleIds) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        try {
            // Validation
            if (articleIds == null || articleIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Récupération des produits
            List<ProduitDto> produits = stockService.getProductsByIds(articleIds, boutiqueid);

            return ResponseEntity.ok(produits);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Décrémente les stocks de plusieurs articles en une seule transaction
     *
     * POST /api/microservice-produits/stocks/batch/decrement Body: [ {
     * "articleId": 1, "quantite": 2 }, { "articleId": 2, "quantite": 1 }, {
     * "articleId": 3, "quantite": 5 } ]
     *
     * Response: { "1": 48, "2": 22, "3": 15 }
     */
    @PostMapping("/stocks/batch/decrement/{boutiqueid}")
    public ResponseEntity<?> decrementStocksBatch(
            @RequestBody List<StockDecrementItem> items,
            @PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        try {
            List<BatchValeur> listbatchValeur=new ArrayList<>();
            Map<String, Integer> newStocks = new HashMap<>();
            

            for (StockDecrementItem item : items) {
                Long articleId = item.getArticleId();
                Integer quantite = item.getQuantite() == null ? 0 : item.getQuantite();

                // ✅ Si quantite = 0, juste lire le stock
                if (quantite == null || quantite == 0) {
                    Integer stock = stockService.getCurrentStock(articleId, boutiqueid);
                    newStocks.put(String.valueOf(articleId), stock);
                } else {
                    // Décrémenter normalement
                    Integer stock = stockService.decrementStock(articleId, quantite, boutiqueid);
                    newStocks.put(String.valueOf(articleId), stock);
                }
            }

            return ResponseEntity.ok(newStocks);

        } catch (Exception e) {
            log.error("❌ Erreur", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/stocks/batch/decrement/prix/{boutiqueid}")
    public ResponseEntity<?> decrementStocksAndPrixBatch(
            @RequestBody List<StockDecrementItem> items,
            @PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        try {
            List<BatchValeur> listbatchValeur=new ArrayList<>();
            Map<String, Integer> newStocks = new HashMap<>();
            

            for (StockDecrementItem item : items) {
                Long articleId = item.getArticleId();
               // Integer quantite = item.getQuantite() == null ? 0 : item.getQuantite();

                // ✅ Si quantite = 0, juste lire le stock
               // if (quantite == null || quantite == 0) {
                    Integer prix = stockService.getCurrentPrix(articleId, boutiqueid);
                    newStocks.put(String.valueOf(articleId), prix);
//                } else {
//                    // Décrémenter normalement
//                    Integer stock = stockService.decrementStock(articleId, quantite, boutiqueid);
//                    newStocks.put(String.valueOf(articleId), stock);
//                }
            }

            return ResponseEntity.ok(newStocks);

        } catch (Exception e) {
            log.error("❌ Erreur", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * ✅ NOUVEL ENDPOINT : Récupérer les stocks de plusieurs articles
     *
     * GET /stocks/batch/{boutiqueid} Body: [1, 2, 3, 45, 67]
     *
     * Response: { "1": 50, "2": 23, "3": 100 }
     */
    @PostMapping("/stocks/batch/{boutiqueid}")
    public ResponseEntity<Map<String, Integer>> getStocksBatch(
            @PathVariable Long boutiqueid,
            @RequestBody List<Long> articleIds) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        try {
            log.info("📦 Récupération stocks pour {} articles", articleIds.size());

            // Validation
            if (articleIds == null || articleIds.isEmpty()) {
                log.warn("⚠️ Liste vide");
                return ResponseEntity.badRequest().build();
            }

            // Récupérer les stocks (SANS décrémenter)
            Map<String, Integer> stocks = stockService.getStocksBatch(articleIds, boutiqueid);

            log.info("✅ {} stocks récupérés", stocks.size());

            return ResponseEntity.ok(stocks);

        } catch (Exception e) {
            log.error("❌ Erreur récupération stocks batch", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/produits/search/{boutiqueid}")
    public ResponseEntity<List<ProduitDto>> searchProduits(
            @PathVariable Long boutiqueid,
            @RequestParam String search
    ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        List<ProduitDto> results = articleService.searchProduitsByBoutique(boutiqueid, search);
        return ResponseEntity.ok(results);
    }
//recuper le prix

    @GetMapping("/produits/currentPrix/{articleId}/{boutiqueid}")
    public ResponseEntity<Integer> currentPrix(
            @PathVariable Long boutiqueid, @PathVariable Long articleId
    ) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        return ResponseEntity.ok(stockService.getCurrentPrix(articleId, boutiqueid));

    }
    
    @PostMapping("/produits/currentPrix/batch/{boutiqueid}")
public ResponseEntity<Map<String, Integer>> currentPrixBatch(
        @PathVariable Long boutiqueid,
        @RequestBody List<Long> articleIds
) {
    boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
    if (articleIds == null || articleIds.isEmpty()) {
        return ResponseEntity.ok(Collections.emptyMap());
    }

    Map<String, Integer> prixMap = stockService.getCurrentPrixBatch(articleIds, boutiqueid);
    return ResponseEntity.ok(prixMap);
}

    /**
     * Televerse/remplace la photo d'un produit (voir ProduitPhoto - stockage
     * BLOB, aucune association JPA avec Produit pour ne pas alourdir chaque
     * chargement de Produit deja EAGER a de nombreux endroits).
     */
    @PostMapping("/articles/{id}/photo")
    public ResponseEntity<?> uploaderPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Produit produit = produitRepositories.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId())
                .orElse(null);
        if (produit == null) {
            return ResponseEntity.notFound().build();
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide.");
        }
        if (file.getSize() > TAILLE_PHOTO_MAX_OCTETS) {
            return ResponseEntity.badRequest().body("La photo depasse la taille maximale autorisee (3 Mo).");
        }
        if (!CONTENT_TYPES_PHOTO_AUTORISES.contains(file.getContentType())) {
            return ResponseEntity.badRequest().body("Format non supporte (jpeg, png ou webp uniquement).");
        }

        try {
            ProduitPhoto photo = produitPhotoRepository.findById(id).orElseGet(ProduitPhoto::new);
            photo.setProduitId(id);
            photo.setData(file.getBytes());
            photo.setContentType(file.getContentType());
            photo.setDateMaj(new Date());
            produitPhotoRepository.save(photo);
            return ResponseEntity.ok().build();
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la lecture du fichier.");
        }
    }

    @GetMapping("/articles/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        Produit produit = produitRepositories.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId())
                .orElse(null);
        if (produit == null) {
            return ResponseEntity.notFound().build();
        }
        ProduitPhoto photo = produitPhotoRepository.findById(id).orElse(null);
        if (photo == null || photo.getData() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType contentType = MediaType.parseMediaType(
                photo.getContentType() != null ? photo.getContentType() : MediaType.IMAGE_JPEG_VALUE);
        return ResponseEntity.ok().contentType(contentType).body(photo.getData());
    }

    @DeleteMapping("/articles/{id}/photo")
    public ResponseEntity<?> supprimerPhoto(@PathVariable Long id) {
        Produit produit = produitRepositories.findByIdAndCompagnie_Id(id, tenantContext.currentCompagnieId())
                .orElse(null);
        if (produit == null) {
            return ResponseEntity.notFound().build();
        }
        produitPhotoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
