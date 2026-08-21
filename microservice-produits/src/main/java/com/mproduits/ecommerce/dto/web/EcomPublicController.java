package com.mproduits.ecommerce.dto.web;

import com.mproduits.dto.ArticlePublicDTO;
import com.mproduits.dto.BoutiquePubliqueDTO;
import com.mproduits.dto.CategoriePubliqueDTO;
import com.mproduits.dto.CompagniePubliqueDTO;
import com.mproduits.dto.SpecificationPubliqueDTO;
import com.mproduits.model.Boutique;
import com.mproduits.model.Compagnie;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.model.ProduitPhoto;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.CompagnieRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitPhotoRepository;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.SpecificationarticlesRepositories;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Page de vente publique (visiteur non authentifie) : resolution explicite
 * de la compagnie via son code d'URL puis de la boutique choisie. Ne doit
 * JAMAIS s'appuyer sur TenantContext - il n'y a pas de JWT pour un visiteur
 * anonyme, contrairement au reste de l'application. Endpoints distincts de
 * /e-com/articles/** (legacy, non scope, laisse inchange pour l'instant).
 */
@RestController
@AllArgsConstructor
@RequestMapping("/microservice-produits/e-com/compagnie")
public class EcomPublicController {

    private final CompagnieRepositories compagnieRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final ProduitRepositories produitRepositories;
    private final ProduitPhotoRepository produitPhotoRepository;
    private final SpecificationarticlesRepositories specificationarticlesRepository;

    @GetMapping("/{code}")
    public ResponseEntity<CompagniePubliqueDTO> getCompagnieParCode(@PathVariable String code) {
        return compagnieRepositories.findByCode(code)
                .map(this::toCompagnieDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{code}/boutiques/{boutiqueId}/produits")
    public ResponseEntity<Map<String, Object>> getProduitsParBoutique(@PathVariable String code,
            @PathVariable Long boutiqueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) String search) {
        Compagnie compagnie = compagnieRepositories.findByCode(code).orElse(null);
        if (compagnie == null) {
            return ResponseEntity.notFound().build();
        }
        // La boutique doit appartenir a la compagnie resolue par le code de
        // l'URL - empeche un visiteur de forger un boutiqueId d'une autre
        // compagnie tout en restant sur cette page publique.
        boolean boutiqueValide = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnie.getId()).isPresent();
        if (!boutiqueValide) {
            return ResponseEntity.notFound().build();
        }

        String searchNettoye = (search != null && !search.isBlank()) ? search.trim() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PrixArticles> prixArticles = prixArticlesRepositories
                .findActifsByBoutiqueIdAndFilters(boutiqueId, categorieId, searchNettoye, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", prixArticles.getContent().stream().map(this::toArticleDTO).collect(Collectors.toList()));
        response.put("totalElements", prixArticles.getTotalElements());
        response.put("totalPages", prixArticles.getTotalPages());
        response.put("page", prixArticles.getNumber());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}/boutiques/{boutiqueId}/categories")
    public ResponseEntity<List<CategoriePubliqueDTO>> getCategoriesParBoutique(@PathVariable String code,
            @PathVariable Long boutiqueId) {
        Compagnie compagnie = compagnieRepositories.findByCode(code).orElse(null);
        if (compagnie == null) {
            return ResponseEntity.notFound().build();
        }
        boolean boutiqueValide = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnie.getId()).isPresent();
        if (!boutiqueValide) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(prixArticlesRepositories.findDistinctCategoriesByBoutiqueId(boutiqueId));
    }

    // Fiche produit detaillee (voir "voir les details" cote storefront) -
    // meme DTO que le catalogue, avec en plus les specifications (si le
    // produit en a - beaucoup n'en auront aucune, la liste sera vide).
    @GetMapping("/{code}/boutiques/{boutiqueId}/produits/{produitId}")
    public ResponseEntity<ArticlePublicDTO> getProduitDetail(@PathVariable String code,
            @PathVariable Long boutiqueId, @PathVariable Long produitId) {
        Compagnie compagnie = compagnieRepositories.findByCode(code).orElse(null);
        if (compagnie == null) {
            return ResponseEntity.notFound().build();
        }
        boolean boutiqueValide = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueId, compagnie.getId()).isPresent();
        if (!boutiqueValide) {
            return ResponseEntity.notFound().build();
        }

        PrixArticles prixArticles = prixArticlesRepositories
                .findActifByProduitIdAndBoutiqueId(produitId, boutiqueId)
                .orElse(null);
        if (prixArticles == null) {
            return ResponseEntity.notFound().build();
        }

        ArticlePublicDTO dto = toArticleDTO(prixArticles);
        List<SpecificationPubliqueDTO> specifications = specificationarticlesRepository
                .findByArtticleId_Id(produitId).stream()
                .map(s -> new SpecificationPubliqueDTO(s.getSpecifiqueId().getLibelle(), s.getLibelle()))
                .collect(Collectors.toList());
        dto.setSpecifications(specifications);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{code}/produits/{produitId}/photo")
    public ResponseEntity<byte[]> getPhotoProduit(@PathVariable String code, @PathVariable Long produitId) {
        Compagnie compagnie = compagnieRepositories.findByCode(code).orElse(null);
        if (compagnie == null) {
            return ResponseEntity.notFound().build();
        }
        // Le produit doit appartenir a la compagnie resolue par le code de
        // l'URL - meme garde anti-usurpation que boutiqueValide ci-dessus.
        boolean produitValide = produitRepositories.findByIdAndCompagnie_Id(produitId, compagnie.getId()).isPresent();
        if (!produitValide) {
            return ResponseEntity.notFound().build();
        }

        Optional<ProduitPhoto> photo = produitPhotoRepository.findById(produitId);
        if (photo.isEmpty() || photo.get().getData() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = MediaType.parseMediaType(
                photo.get().getContentType() != null ? photo.get().getContentType() : MediaType.IMAGE_JPEG_VALUE);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header("Cache-Control", "public, max-age=86400")
                .body(photo.get().getData());
    }

    private CompagniePubliqueDTO toCompagnieDTO(Compagnie compagnie) {
        List<Boutique> boutiques = boutiqueRepositories.findByCompagnie_Id(compagnie.getId());

        CompagniePubliqueDTO dto = new CompagniePubliqueDTO();
        dto.setId(compagnie.getId());
        dto.setNom(compagnie.getNom());
        dto.setCode(compagnie.getCode());
        dto.setLogoChemin(compagnie.getLogoChemin());
        dto.setBoutiques(boutiques.stream().map(this::toBoutiqueDTO).collect(Collectors.toList()));
        return dto;
    }

    private BoutiquePubliqueDTO toBoutiqueDTO(Boutique boutique) {
        BoutiquePubliqueDTO dto = new BoutiquePubliqueDTO();
        dto.setId(boutique.getId());
        dto.setNom(boutique.getNom());
        dto.setQuartier(boutique.getQuartier());
        dto.setPrincipale(Boolean.TRUE.equals(boutique.getPrincipale()));
        return dto;
    }

    private ArticlePublicDTO toArticleDTO(PrixArticles prixArticles) {
        ArticlePublicDTO dto = new ArticlePublicDTO();
        dto.setPrixArticlesId(prixArticles.getId());
        dto.setPrixVenteNet(prixArticles.getPrixVenteNet());
        dto.setPrixVenteTTC(prixArticles.getPrixVenteTTC());
        dto.setPrixEffectif(prixArticles.getPrixEffectif());
        dto.setRemise(prixArticles.getRemise());
        dto.setTypePromotion(prixArticles.getTypePromotion());
        dto.setDateDebutPromo(prixArticles.getDateDebutPromo());
        dto.setDateFinPromo(prixArticles.getDateFinPromo());
        dto.setPromotionActive(prixArticles.isPromotionActive());

        if (prixArticles.getPointVente() != null) {
            dto.setQuantiteDisponible(prixArticles.getPointVente().getStockFinalTheorie());

            Produit produit = prixArticles.getPointVente().getProduit();
            if (produit != null) {
                dto.setProduitId(produit.getId());
                dto.setReference(produit.getReference());
                dto.setLibelle(produit.getLibelle());
                dto.setDescription(produit.getDescription());
                dto.setPhotoName(produit.getPhotoName());
                if (produit.getCategorie() != null) {
                    dto.setCategorieId(produit.getCategorie().getId());
                    dto.setCategorieLibelle(produit.getCategorie().getLibelle());
                }
            }
        }
        return dto;
    }
}
