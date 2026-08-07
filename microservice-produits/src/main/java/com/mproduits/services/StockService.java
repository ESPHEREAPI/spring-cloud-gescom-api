package com.mproduits.services;

import com.mproduits.dto.ProduitDto;
import com.mproduits.dto.StockDecrementItem;
import com.mproduits.dto.StockUpdateResponse;
import com.mproduits.enums.MovementType;
import com.mproduits.exceptions.MetierException;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.*;
import com.mproduits.repositories.*;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;

/**
 * Service de gestion des stocks avec optimisations Java 17/21
 *
 * @author USER01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    // Constants
    private static final BigDecimal MAX_STOCK = new BigDecimal("999999");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    // Dependencies (final grâce à @RequiredArgsConstructor)
    private final PrixArticlesRepositories prixArticlesRepositories;
    private final EntrepriseRepositories entrepriseRepositories;
    private final PointVenteRepositories pointVenteRepo;
    private final StockMovementRepository movementRepo;
    private final ProduitRepositories produitRepo;
    private final BoutiqueRepositories boutiqueRepositories;
    private final TenantContext tenantContext;
    private final EntrepriseService entrepriseService;
    @Autowired
    MapperDtoImpl mapperdto;

    private static final int SEUIL_ALERTE = 5;

    /**
     * Met à jour le stock après une vente
     */
    @Transactional(readOnly = true)
    public StockUpdateResponse updateStockAfterSale(Long produitId, Long boutiqueid, Integer quantite) {
        var produit = findProduitOrThrow(produitId);
        var entreprise = findActiveEntreprise();

        return prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(entreprise, produit, boutiqueid)
                .map(pa -> buildStockResponse(pa.getPointVente(), produitId))
                .orElseGet(() -> StockUpdateResponse.builder()
                .success(false)
                .message("Prix article non trouvé")
                .productId(produitId)
                .build());
    }

    /**
     * Vérifie la disponibilité du stock
     */
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long produitId, Long boutiqueid, Integer quantite) {
        var stockActuel = getCurrentStock(produitId, boutiqueid);
        return stockActuel >= quantite;
    }

    /**
     * Récupère le stock actuel d'un produit
     */
    @Transactional(readOnly = true)
    public Integer getCurrentStock(Long produitId, Long boutiqueid) {
        var produit = findProduitOrThrow(produitId);
        var entreprise = findActiveEntreprise();

        return prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(entreprise, produit, boutiqueid)
                .map(pa -> pa.getPointVente().getStockFinalTheorie())
                .map(BigDecimal::intValue)
                .orElse(0);
    }

    /**
     * recuper le dernier prix
     */
    @Transactional(readOnly = true)
    public Integer getCurrentPrix(Long produitId, Long boutiqueid) {
        var produit = findProduitOrThrow(produitId);
        var entreprise = findActiveEntreprise();

        return prixArticlesRepositories
                .findLastActiveByEntrepriseAndProduit(entreprise, produit, boutiqueid)
                .map(pa -> pa.getPrixVenteTTC())
                .map(BigDecimal::intValue)
                .orElse(0);
    }
    
    /**
 * ✅ VERSION BATCH de getCurrentPrix
 * Résout les prix de plusieurs articles en une seule requête DB (IN clause)
 * au lieu de N requêtes individuelles.
 */
@Transactional(readOnly = true)
public Map<String, Integer> getCurrentPrixBatch(List<Long> produitIds, Long boutiqueid) {
    Map<String, Integer> result = new HashMap<>();
 
    if (produitIds == null || produitIds.isEmpty()) {
        return result;
    }
 
    var entreprise = findActiveEntreprise();
 
    // ✅ Initialiser tous les IDs à 0 (fallback si pas de prix trouvé)
    produitIds.forEach(id -> result.put(String.valueOf(id), 0));
 
    // ✅ Une seule requête DB avec IN clause pour tous les articles
    List<PrixArticles> prixList = prixArticlesRepositories
            .findLastActiveByEntrepriseAndProduitsAndBoutique(entreprise, produitIds, boutiqueid);
 
    // ✅ Remplir la map avec les prix trouvés
    prixList.forEach(pa -> {
        String key = String.valueOf(pa.getPointVente().getProduit().getId());
        Integer prix = Optional.ofNullable(pa.getPrixVenteTTC())
                .map(BigDecimal::intValue)
                .orElse(0);
        result.put(key, prix);
    });
 
    return result;
}

    /**
     * Augmente le stock (entrée)
     */
    @Transactional
    public void augmenterStock(Long produitId, Long pointVenteId, BigDecimal quantite,
            MovementType type, String motif, String username) {

        log.info("Augmentation stock: produit={}, quantite={}, type={}",
                produitId, quantite, type);

        validateQuantite(quantite);

        var pointVente = findPointVenteOrThrow(pointVenteId);
        var produit = findProduitOrThrow(produitId);

        var stockActuel = Optional.ofNullable(pointVente.getStockInitial()).orElse(ZERO);
        var nouveauStock = stockActuel.add(quantite);

        validateMaxStock(nouveauStock);

        pointVente.setStockInitial(nouveauStock);
        pointVenteRepo.save(pointVente);

        creerMouvement(produit, pointVente, quantite, type, motif, username);
    }

    /**
     * Diminue le stock (sortie)
     */
    @Transactional
    public void diminuerStock(Long produitId, Long pointVenteId, BigDecimal quantite,
            MovementType type, String motif, String username) {

        log.info("Diminution stock: produit={}, quantite={}, type={}",
                produitId, quantite, type);

        validateQuantite(quantite);

        var pointVente = findPointVenteOrThrow(pointVenteId);
        var produit = findProduitOrThrow(produitId);

        var stockActuel = Optional.ofNullable(pointVente.getStockFinalTheorie()).orElse(ZERO);

        validateStockSuffisant(stockActuel, quantite);

        var nouveauStock = stockActuel.subtract(quantite);
        pointVente.setStockInitial(nouveauStock);
        pointVenteRepo.save(pointVente);

        creerMouvement(produit, pointVente, quantite.negate(), type, motif, username);
    }

    /**
     * Récupère le stock total d'un produit (tous points de vente)
     */
    @Transactional(readOnly = true)
    public BigDecimal getStockTotal(Long produitId, Long boutiqueid, int anneeid) {
        //Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        return pointVenteRepo.sumStockByProduit(produitId, boutiqueid, anneeid).orElse(ZERO);
    }

    /**
     * Récupère l'historique des mouvements de stock avec filtres
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMouvementsHistorique(Long produitId, Long pointVenteId) {

        log.debug("Récupération historique: produit={}, pointVente={}", produitId, pointVenteId);

        var mouvements = switch (determineFiltreType(produitId, pointVenteId)) {
            case PRODUIT_ONLY ->
                movementRepo.findByProduitId(produitId);
            case POINT_VENTE_ONLY ->
                movementRepo.findByPointVenteId(pointVenteId);
            case BOTH ->
                movementRepo.findByProduitId(produitId).stream()
                .filter(m -> m.getPointVente().getId().equals(pointVenteId))
                .toList();
            case NONE ->
                movementRepo.findAll();
        };

        return mouvements.stream()
                .map(this::convertToMap)
                .toList();
    }

    /**
     * Récupère le stock par point de vente
     */
    @Transactional(readOnly = true)
    public BigDecimal getStockParPointVente(Long produitId, Long pointVenteId) {
        log.debug("Stock: produit={}, pointVente={}", produitId, pointVenteId);

        return pointVenteRepo.findById(pointVenteId)
                .map(PointVente::getStockInitial)
                .orElse(ZERO);
    }

    /**
     * Récupère tous les mouvements pour un produit
     */
    @Transactional(readOnly = true)
    public List<StockMovement> getMouvementsProduit(Long produitId) {
        log.debug("Mouvements produit: {}", produitId);
        return movementRepo.findByProduitId(produitId);
    }

    /**
     * Récupère tous les mouvements pour un point de vente
     */
    @Transactional(readOnly = true)
    public List<StockMovement> getMouvementsPointVente(Long pointVenteId) {
        log.debug("Mouvements point de vente: {}", pointVenteId);
        return movementRepo.findByPointVenteId(pointVenteId);
    }

    /**
     * Récupère les derniers mouvements de stock (limite configurable)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDerniersMouvements(int limite) {
        log.debug("Derniers {} mouvements", limite);

        return movementRepo.findAll().stream()
                .sorted(Comparator.comparing(StockMovement::getDateCreation).reversed())
                .limit(limite)
                .map(this::convertToMap)
                .toList();
    }

    /**
     * Récupère les mouvements d'une facture
     */
    @Transactional(readOnly = true)
    public List<StockMovement> getMouvementsFacture(Long factureId) {
        log.debug("Mouvements facture: {}", factureId);

        return movementRepo.findAll().stream()
                .filter(m -> m.getFacture() != null && m.getFacture().getId().equals(factureId))
                .toList();
    }

    // ==================== Méthodes privées ====================
    /**
     * Crée un mouvement de stock
     */
    private void creerMouvement(Produit produit, PointVente pointVente, BigDecimal quantite,
            MovementType type, String motif, String username) {

        var movement = StockMovement.builder()
                .produit(produit)
                .pointVente(pointVente)
                .quantite(quantite)
                .typeMouvement(type)
                .motif(motif)
                .usernameCreate(username)
                .dateCreation(LocalDateTime.now())
                .build();

        movementRepo.save(movement);
        log.debug("Mouvement créé: id={}, type={}", movement.getId(), type);
    }

    /**
     * Convertit un StockMovement en Map pour le retour API
     */
    private Map<String, Object> convertToMap(StockMovement movement) {
        return Map.of(
                "id", movement.getId(),
                "type", movement.getTypeMouvement().getLabel(),
                "quantite", movement.getQuantite(),
                "produitId", movement.getProduit().getId(),
                "produitCode", movement.getProduit().getCode(),
                "pointVenteId", movement.getPointVente().getId(),
                "motif", Optional.ofNullable(movement.getMotif()).orElse(""),
                "username", movement.getUsernameCreate(),
                "dateCreation", movement.getDateCreation()
        );
    }

    /**
     * Construit une réponse de stock
     */
    private StockUpdateResponse buildStockResponse(PointVente pointVente, Long produitId) {
        return StockUpdateResponse.builder()
                .success(true)
                .message("Stock récupéré avec succès")
                .productId(produitId)
                .newStock(pointVente.getStockFinalTheorie().intValue())
                .build();
    }

    /**
     * Trouve un produit ou lève une exception
     */
    private Produit findProduitOrThrow(Long produitId) {
        return produitRepo.findByIdAndCompagnie_Id(produitId, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new MetierException("Produit non trouvé: id=" + produitId));
    }

    /**
     * Trouve un point de vente ou lève une exception
     */
    private PointVente findPointVenteOrThrow(Long pointVenteId) {
        return pointVenteRepo.findById(pointVenteId)
                .orElseThrow(() -> new MetierException("Point de vente non trouvé: id=" + pointVenteId));
    }

    /**
     * Trouve l'entreprise active
     */
    private Entreprise findActiveEntreprise() {
        return entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
    }

    /**
     * Valide qu'une quantité est positive
     */
    private void validateQuantite(BigDecimal quantite) {
        if (quantite == null || quantite.compareTo(ZERO) <= 0) {
            throw new MetierException("La quantité doit être positive");
        }
    }

    /**
     * Valide que le stock ne dépasse pas le maximum
     */
    private void validateMaxStock(BigDecimal stock) {
        if (stock.compareTo(MAX_STOCK) > 0) {
            throw new MetierException("Stock maximum dépassé: " + MAX_STOCK);
        }
    }

    /**
     * Valide que le stock est suffisant
     */
    private void validateStockSuffisant(BigDecimal stockActuel, BigDecimal quantiteDemandee) {
        if (stockActuel.compareTo(quantiteDemandee) < 0) {
            throw new MetierException(
                    String.format("Stock insuffisant: disponible=%.2f, demandé=%.2f",
                            stockActuel, quantiteDemandee));
        }
    }

    /**
     * Détermine le type de filtre à appliquer
     */
    private FiltreType determineFiltreType(Long produitId, Long pointVenteId) {
        if (produitId != null && pointVenteId != null) {
            return FiltreType.BOTH;
        }
        if (produitId != null) {
            return FiltreType.PRODUIT_ONLY;
        }
        if (pointVenteId != null) {
            return FiltreType.POINT_VENTE_ONLY;
        }
        return FiltreType.NONE;
    }

    /**
     * Enum pour les types de filtres
     */
    private enum FiltreType {
        PRODUIT_ONLY, POINT_VENTE_ONLY, BOTH, NONE
    }

    public ProduitDto lastProduitForPointVente(Long produitid, Long boutiqueid) {
        var produit = findProduitOrThrow(produitid);
        Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        return mapperdto.mapperProduitDto(produit, boutique.isPresent() ? boutique.get() : new Boutique());

    }

    // ========================================
    // MÉTHODE 1: DÉCRÉMENTATION STOCK UNITAIRE
    // ========================================
    /**
     * Décrémente le stock d'un article et retourne le nouveau stock
     *
     * @param articleId ID de l'article
     * @param quantite Quantité à décrémenter
     * @return Le nouveau stock après décrémentation
     * @throws IllegalArgumentException si l'article n'existe pas ou stock
     * insuffisant
     */
    @Transactional
    public Integer decrementStock(Long articleId, Integer quantite, Long boutiqueid) {
        log.info("📉 Décrémentation stock article {} de {}", articleId, quantite);

        // Récupérer le produit
        Produit produit = produitRepo.findByIdAndCompagnie_Id(articleId, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new IllegalArgumentException("Article " + articleId + " non trouvé"));

        // Vérifier le stock disponible
        Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        ProduitDto produitDto = mapperdto.mapperProduitDto(produit, boutique.get());
        Integer stockActuel = produitDto.getStockFinal().intValue();
        if (stockActuel < quantite) {
            log.warn("⚠️ Stock insuffisant pour article {}: {} < {}",
                    articleId, stockActuel, quantite);
            throw new IllegalArgumentException("Stock insuffisant pour l'article " + articleId);
        }

        // Décrémenter
        Integer nouveauStock = stockActuel - quantite;
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        // produitDto.setStockFinal();
        Optional<PointVente> pv = pointVenteRepo.findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique.get(), e);
        pv.get().setStockFinalTheorie(new BigDecimal(nouveauStock));
        BigDecimal sortie = pv.get().getSortiProduit();
        pv.get().setSortiProduit(sortie.add(new BigDecimal(quantite)));
        pointVenteRepo.save(pv.get());
        // Sauvegarder
        //produitRepository.save(produit);

        log.info("✅ Nouveau stock article {}: {}", articleId, nouveauStock);

        // Vérifier alerte stock bas
        if (nouveauStock <= 5) {
            log.warn("🚨 ALERTE STOCK BAS: Article {} = {}", articleId, nouveauStock);
        }

        return nouveauStock;
    }

    // ========================================
    // MÉTHODE 2: RÉCUPÉRATION BATCH PRODUITS
    // ========================================
    /**
     * Récupère les détails complets de plusieurs produits
     *
     * @param articleIds Liste des IDs d'articles
     * @return Liste des produits trouvés
     */
    public List<ProduitDto> getProductsByIds(List<Long> articleIds, Long boutiqueid) {
        log.info("🔍 Récupération de {} produits", articleIds.size());

        // Récupérer tous les produits en une seule requête
        List<Produit> produits = produitRepo.findProductsByIdsAndBoutique(articleIds, boutiqueid);

        log.info("✅ {} produits trouvés sur {} demandés",
                produits.size(), articleIds.size());

        // Optionnel: Logger les IDs manquants
        if (produits.size() < articleIds.size()) {
            Set<Long> foundIds = new HashSet<>();
            for (Produit p : produits) {
                foundIds.add(p.getId());
            }

            Set<Long> missingIds = new HashSet<>();
            for (Long id : articleIds) {
                if (!foundIds.contains(id)) {
                    missingIds.add(id);
                }
            }

            log.warn("⚠️ Articles non trouvés: {}", missingIds);
        }
        Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        List<ProduitDto> produitsDtos = produits.stream().map(pd -> mapperdto.mapperProduitDto(pd, boutique.get())).toList();
        return produitsDtos;
    }

    /**
     * Décrémente les stocks de plusieurs articles en une seule transaction
     * atomique
     *
     * IMPORTANT: Utilise @Transactional pour garantir: - Atomicité: Soit tous
     * les stocks sont décrémentés, soit aucun - Isolation: Évite les problèmes
     * de concurrence - Rollback automatique en cas d'erreur
     *
     * @param items Liste des articles avec quantités à décrémenter
     * @return Map des nouveaux stocks par ID d'article (clés en String)
     * @throws IllegalArgumentException si article non trouvé ou stock
     * insuffisant
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Map<String, Integer> decrementStocksBatch(List<StockDecrementItem> items, Long boutiqueid) {
        log.info("📉 Début décrémentation batch de {} articles", items.size());

        try {
            // ========================================
            // ÉTAPE 1: RÉCUPÉRER TOUS LES ARTICLES
            // ========================================
            List<Long> articleIds = items.stream()
                    .map(StockDecrementItem::getArticleId)
                    .collect(Collectors.toList());

            log.debug("🔍 Récupération articles: {}", articleIds);

            // Récupérer tous les produits en une seule requête
            List<Produit> produits = produitRepo.findProductsByIdsAndBoutique(articleIds, boutiqueid);

            // Créer une map pour accès rapide par ID
            Map<Long, Produit> produitsMap = produits.stream()
                    .collect(Collectors.toMap(Produit::getId, p -> p));

            log.debug("✅ {} articles récupérés", produits.size());

            // ========================================
            // ÉTAPE 2: VALIDER QUE TOUS LES ARTICLES EXISTENT
            // ========================================
            for (StockDecrementItem item : items) {
                if (!produitsMap.containsKey(item.getArticleId())) {
                    String errorMsg = "Article " + item.getArticleId() + " non trouvé";
                    log.error("❌ {}", errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            }

            log.debug("✅ Tous les articles existent");

            // ========================================
            // ÉTAPE 3: VALIDER QUE TOUS ONT ASSEZ DE STOCK
            // ========================================
            Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
            ProduitDto pdto;
            for (StockDecrementItem item : items) {
                Produit produit = produitsMap.get(item.getArticleId());
                pdto = mapperdto.mapperProduitDto(produit, boutique.get());
                BigDecimal stockActuel = pdto.getStockFinal();
                Integer quantiteDemandee = item.getQuantite();

                if (stockActuel.intValue() < quantiteDemandee) {
                    String errorMsg = String.format(
                            "Stock insuffisant pour article %d: disponible=%d, demandé=%d",
                            item.getArticleId(), stockActuel.intValue(), quantiteDemandee
                    );
                    log.error("❌ {}", errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            }

            log.debug("✅ Tous les stocks sont suffisants");

            // ========================================
            // ÉTAPE 4: DÉCRÉMENTER TOUS LES STOCKS
            // ========================================
            Map<String, Integer> newStocks = new HashMap<>();
            List<PointVente> produitsToUpdate = new ArrayList<>();
            Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
            //   Optional<PointVente> pv =pointVenteRepo.findLatestActiveByProduitBoutiqueAndEntreprise(cde, b, e)
            for (StockDecrementItem item : items) {
                Produit produit = produitsMap.get(item.getArticleId());
                Optional<PointVente> pv = pointVenteRepo.findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique.get(), e);
                BigDecimal stockActuel = pv.get().getStockFinalTheorie();
                Integer quantite = item.getQuantite();
                Integer nouveauStock = stockActuel.intValue() - quantite;

                // Mettre à jour le stock
                pv.get().setStockFinalTheorie(new BigDecimal(nouveauStock));
                BigDecimal sortie = pv.get().getSortiProduit();
                pv.get().setSortiProduit(sortie.add(new BigDecimal(quantite)));
                produitsToUpdate.add(pv.get());

                // Ajouter au résultat (clé en String pour le frontend)
                newStocks.put(String.valueOf(item.getArticleId()), nouveauStock);

                log.info("✅ Article {}: {} → {} (-{})",
                        item.getArticleId(), stockActuel, nouveauStock, quantite);

                // Vérifier alerte stock bas
                if (nouveauStock == 0) {
                    log.warn("🚨 RUPTURE DE STOCK: Article {} = 0", item.getArticleId());
                } else if (nouveauStock <= SEUIL_ALERTE) {
                    log.warn("⚠️ ALERTE STOCK BAS: Article {} = {}",
                            item.getArticleId(), nouveauStock);
                }
            }

            // ========================================
            // ÉTAPE 5: SAUVEGARDER TOUS LES CHANGEMENTS
            // ========================================
            pointVenteRepo.saveAll(produitsToUpdate);

            log.info("✅ Décrémentation batch terminée avec succès: {} articles mis à jour",
                    newStocks.size());

            return newStocks;

        } catch (IllegalArgumentException e) {
            // Relancer l'exception métier (transaction sera rollback)
            log.error("❌ Erreur métier, rollback transaction: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            // Erreur système inattendue
            log.error("❌ Erreur système inattendue, rollback transaction", e);
            throw new RuntimeException("Erreur lors de la décrémentation batch: " + e.getMessage(), e);
        }
    }

    /**
     * Version alternative avec gestion d'erreur partielle (NON RECOMMANDÉ)
     *
     * Cette version continue même si un article échoue, mais ne garantit pas
     * l'atomicité. Utilisez uniquement si votre cas d'usage l'exige.
     */
    @Transactional
    public Map<String, Integer> decrementStocksBatchPartial(List<StockDecrementItem> items, Long boutiqueid) {
        log.info("📉 Décrémentation batch partielle de {} articles", items.size());

        Map<String, Integer> newStocks = new HashMap<>();

        for (StockDecrementItem item : items) {
            try {
                Produit produit = produitRepo.findByIdAndCompagnie_Id(item.getArticleId(), tenantContext.currentCompagnieId())
                        .orElseThrow(() -> new IllegalArgumentException(
                        "Article " + item.getArticleId() + " non trouvé"));
                Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
                ProduitDto pdto = mapperdto.mapperProduitDto(produit, boutique.get());
                BigDecimal stockActuel = pdto.getStockFinal();
                Integer quantite = item.getQuantite();

                if (stockActuel.intValue() < quantite) {
                    log.warn("⚠️ Stock insuffisant pour article {}, ignoré", item.getArticleId());
                    newStocks.put(String.valueOf(item.getArticleId()), stockActuel.intValue());
                    continue;
                }

                Integer nouveauStock = stockActuel.intValue() - quantite;
                Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
                Optional<PointVente> pv = pointVenteRepo.findLatestActiveByProduitBoutiqueAndEntreprise(produit, boutique.get(), e);

                pv.get().setStockFinalTheorie(new BigDecimal(nouveauStock));
                BigDecimal sortie = pv.get().getSortiProduit();
                pv.get().setSortiProduit(sortie.add(new BigDecimal(quantite)));
                //produit.setStockFinal(nouveauStock);
                pointVenteRepo.save(pv.get());

                newStocks.put(String.valueOf(item.getArticleId()), nouveauStock);

                log.info("✅ Article {} décrémenté: {} → {}",
                        item.getArticleId(), stockActuel, nouveauStock);

            } catch (Exception e) {
                log.error("❌ Erreur article {}: {}", item.getArticleId(), e.getMessage());
                newStocks.put(String.valueOf(item.getArticleId()), 0);
            }
        }

        log.info("✅ Décrémentation batch partielle terminée: {}/{} réussis",
                newStocks.size(), items.size());

        return newStocks;
    }

    /**
     * Récupère les stocks de plusieurs articles
     */
    public Map<String, Integer> getStocksBatch(List<Long> articleIds, Long boutiqueid) {
        Map<String, Integer> stocks = new HashMap<>();

        for (Long articleId : articleIds) {
            try {
                // Récupérer le produit
                Produit produit = produitRepo.findByIdAndCompagnie_Id(articleId, tenantContext.currentCompagnieId())
                        .orElse(null);

                if (produit != null) {
                    // Calculer le stock
                    Integer stock = calculateStock(produit, boutiqueid);
                    stocks.put(String.valueOf(articleId), stock);
                } else {
                    stocks.put(String.valueOf(articleId), 0);
                }

            } catch (Exception e) {
                log.error("Erreur récupération stock article {}", articleId, e);
                stocks.put(String.valueOf(articleId), 0);
            }
        }

        return stocks;
    }

    /**
     * Calcule le stock réel d'un produit
     */
    private Integer calculateStock(Produit produit, Long boutiqueid) {
        // Adapter selon votre logique métier
        // Exemple :
        Optional<Boutique> boutique = boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
        ProduitDto pdto = mapperdto.mapperProduitDto(produit, boutique.get());
        return pdto.getStockFinal().intValue(); // ou votre méthode de calcul
    }
}
