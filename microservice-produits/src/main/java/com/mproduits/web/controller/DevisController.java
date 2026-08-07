package com.mproduits.web.controller;

import com.mproduits.dto.DevisDTO;
import com.mproduits.mappers.DevisMapper;
import com.mproduits.model.Devis;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.DevisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.TransactionSystemException;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Contrôleur REST - Gestion Complète des Devis
 * 
 * ENDPOINTS DISPONIBLES:
 * ======================
 * 
 * 1. CRÉATION ET MODIFICATION
 *    POST   /api/v1/devis                    - Créer un devis
 *    PUT    /api/v1/devis/{id}               - Modifier un devis
 *    POST   /api/v1/devis/{id}/dupliquer     - Dupliquer un devis
 * 
 * 2. CONSULTATION
 *    GET    /api/v1/devis                    - Liste tous les devis
 *    GET    /api/v1/devis/paginated          - Liste paginée
 *    GET    /api/v1/devis/{id}               - Détails d'un devis
 *    GET    /api/v1/devis/numero/{numero}    - Recherche par numéro
 * 
 * 3. FILTRES ET RECHERCHES
 *    GET    /api/v1/devis/client/{clientId}  - Devis d'un client
 *    GET    /api/v1/devis/statut/{statut}    - Devis par statut
 *    GET    /api/v1/devis/periode             - Devis par période
 * 
 * 4. GESTION DU CYCLE DE VIE
 *    PUT    /api/v1/devis/{id}/accepter      - Accepter un devis
 *    PUT    /api/v1/devis/{id}/refuser       - Refuser un devis
 *    PUT    /api/v1/devis/{id}/annuler       - Annuler un devis
 * 
 * 5. ALERTES ET NOTIFICATIONS
 *    GET    /api/v1/devis/expiration/proches - Devis proches expiration
 *    GET    /api/v1/devis/expiration/expires - Devis expirés
 * 
 * 6. STATISTIQUES
 *    GET    /api/v1/devis/stats              - Statistiques globales
 *    GET    /api/v1/devis/stats/ca-potentiel - CA potentiel
 * 
 * @author USER01
 * @version 2.0
 */
@RestController
@RequestMapping("/microservice-produits/devis")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Devis", description = "API de gestion des devis")
//@CrossOrigin(origins = "*") // À configurer selon vos besoins
public class DevisController {

    private final DevisService devisService;
    private final DevisMapper mapper;
    private final BoutiqueAccessGuard boutiqueAccessGuard;

    // ================================================================
    // SECTION 1: CRÉATION ET MODIFICATION
    // ================================================================

    /**
     * Créer un nouveau devis
     * POST /api/v1/devis
     * 
     * BODY JSON:
     * {
     *   "clientId": 1,
     *   "appliquerTVA": true,
     *   "tauxTVA": 19.25,
     *   "validiteJours": 30,
     *   "remarques": "Remise spéciale",
     *   "items": [
     *     {
     *       "produitId": 10,
     *       "quantite": 5,
     *       "prixUnitaire": 1000,
     *       "tauxRemise": 10
     *     }
     *   ]
     * }
     */
    @PostMapping
    @Operation(
            summary = "Créer un devis",
            description = "Crée un nouveau devis avec articles. Validité par défaut: 30 jours. " +
                         "Vérifie automatiquement le stock et calcule les montants (HT, TVA, TTC)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Devis créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Erreur validation: client non trouvé, stock insuffisant, données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<Map<String, Object>> creerDevis(
            @Valid @RequestBody DevisDTO dto,
            @RequestHeader("X-Username") @Parameter(description = "Nom d'utilisateur") String username) {

        log.info("┌─────────────────────────────────────");
        log.info("│ POST /api/v1/devis");
        log.info("│ Client ID: {}", dto.getClient().getNom());
        log.info("│ Utilisateur: {}", username);
        log.info("└─────────────────────────────────────");

        try {
            boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(dto.getBoutiqueid());
            Devis devis = devisService.creerDevis(dto, username,dto.getBoutiqueid(),dto.getAnneeid());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Devis créé avec succès");
            response.put("data", mapper.toDto(devis));
            response.put("devisId", devis.getId());
            response.put("numeroDevis", devis.getNumeroDevis());
            response.put("total", devis.getTotal());

            log.info("✓ Devis créé: ID={}, Numéro={}", devis.getId(), devis.getNumeroDevis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Erreur création devis: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new Date());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Modifier un devis existant (seulement si EN_ATTENTE)
     * PUT /api/v1/devis/{id}
     * @param id
     * @return 
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier un devis",
            description = "Modifie un devis existant. Possible uniquement si statut = EN_ATTENTE. " +
                         "Les montants sont recalculés automatiquement."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devis modifié avec succès"),
        @ApiResponse(responseCode = "400", description = "Erreur: devis non modifiable ou données invalides"),
        @ApiResponse(responseCode = "404", description = "Devis non trouvé")
    })
    public ResponseEntity<Map<String, Object>> modifierDevis(
            @PathVariable Long id,
            @Valid @RequestBody DevisDTO dto,
            @RequestHeader("X-Username") String username) {

        log.info("PUT /api/v1/devis/{} - User: {}", id, username);

        try {
            Devis devis = devisService.modifierDevis(id, dto, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Devis modifié avec succès");
            response.put("data", mapper.toDto(devis));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur modification devis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Dupliquer un devis
     * POST /api/v1/devis/{id}/dupliquer
     */
    @PostMapping("/{id}/dupliquer")
    @Operation(
            summary = "Dupliquer un devis",
            description = "Crée une copie du devis avec un nouveau numéro et statut EN_ATTENTE. " +
                         "Tous les articles sont copiés avec leurs prix et remises."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Devis dupliqué avec succès"),
        @ApiResponse(responseCode = "404", description = "Devis source non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la duplication")
    })
    public ResponseEntity<Map<String, Object>> dupliquerDevis(
            @PathVariable Long id,
            @RequestHeader("X-Username") String username) {

        log.info("POST /api/v1/devis/{}/dupliquer - User: {}", id, username);

        try {
            Devis nouveauDevis = devisService.dupliquerDevis(id, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Devis dupliqué avec succès");
            response.put("data", mapper.toDto(nouveauDevis));
            response.put("nouveauId", nouveauDevis.getId());
            response.put("nouveauNumero", nouveauDevis.getNumeroDevis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Erreur duplication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================================================================
    // SECTION 2: CONSULTATION
    // ================================================================

    /**
     * Récupérer un devis par ID
     * GET /api/v1/devis/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer un devis",
            description = "Retourne les détails complets du devis avec ses articles, client, et montants"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Devis trouvé"),
        @ApiResponse(responseCode = "404", description = "Devis non trouvé")
    })
    public ResponseEntity<?> getDevis(@PathVariable Long id) {
        log.info("GET /api/v1/devis/{}", id);

        return devisService.findById(id)
                .map(devis -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", mapper.toDto(devis));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Devis non trouvé: " + id
                )));
    }

    /**
     * Récupérer un devis par numéro
     * GET /api/v1/devis/numero/{numero}
     */
    @GetMapping("/numero/{numero}")
    @Operation(
            summary = "Récupérer un devis par numéro",
            description = "Recherche un devis par son numéro unique (ex: DEV-2025-0001)"
    )
    public ResponseEntity<?> getDevisByNumero(@PathVariable String numero) {
        log.info("GET /api/v1/devis/numero/{}", numero);

        return devisService.findByNumero(numero)
                .map(devis -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", mapper.toDto(devis)
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Devis non trouvé: " + numero
                )));
    }

    /**
     * Lister tous les devis (sans pagination)
     * GET /api/v1/devis
     */
    @GetMapping("/all/{boutiqueid}")
    @Operation(
            summary = "Lister les devis",
            description = "Retourne tous les devis. Pour de meilleures performances, " +
                         "utilisez /api/v1/devis/paginated"
    )
    @ApiResponse(responseCode = "200", description = "Liste des devis")
    public ResponseEntity<Map<String, Object>> listDevis(@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);
        log.info("GET /api/v1/devis");

        List<DevisDTO> devis = devisService.findAll(boutiqueid).stream()
                .map(mapper::toDto)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devis);
        response.put("total", devis.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Lister les devis avec pagination
     * GET /api/v1/devis/paginated?page=0&size=10&sort=dateDevis,desc
     */
    @GetMapping("/paginated")
    @Operation(
            summary = "Lister les devis (paginé)",
            description = "Retourne les devis avec pagination et tri. " +
                         "Paramètres: page (défaut: 0), size (défaut: 10), sort (défaut: dateDevis,desc)"
    )
    public ResponseEntity<Map<String, Object>> listDevisPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateDevis,desc") String[] sort) {

        log.info("GET /api/v1/devis/paginated?page={}&size={}", page, size);

        try {
            // Construire le Sort
            Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sortObj = Sort.by(direction, sort[0]);

            Pageable pageable = PageRequest.of(page, size, sortObj);
            Page<DevisDTO> pageResult = devisService.findAllPaginated(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pageResult.getContent());
            response.put("currentPage", pageResult.getNumber());
            response.put("totalItems", pageResult.getTotalElements());
            response.put("totalPages", pageResult.getTotalPages());
            response.put("pageSize", pageResult.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur pagination: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Erreur de pagination: " + e.getMessage()
            ));
        }
    }

    // ================================================================
    // SECTION 3: FILTRES ET RECHERCHES
    // ================================================================

    /**
     * Lister les devis d'un client
     * GET /api/v1/devis/client/{clientId}
     */
    @GetMapping("/client/{clientId}/{boutiqueid}")
    @Operation(
            summary = "Devis d'un client",
            description = "Retourne tous les devis d'un client spécifique"
    )
    public ResponseEntity<Map<String, Object>> listDevisByClient(
            @PathVariable Long clientId,@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("GET /api/v1/devis/client/{}", clientId);

        List<DevisDTO> devis = devisService.findByClientId(clientId,boutiqueid);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devis);
        response.put("total", devis.size());
        response.put("clientId", clientId);

        return ResponseEntity.ok(response);
    }

    /**
     * Lister les devis par statut
     * GET /api/v1/devis/statut/{statut}
     * Statuts possibles: EN_ATTENTE, ACCEPTE, REFUSE, CONVERTI, EXPIRE, ANNULE
     * @return 
     */
    @GetMapping("/statut/{statut}/{boutiqueid}")
    @Operation(
            summary = "Devis par statut",
            description = "Retourne les devis filtrés par statut. " +
                         "Valeurs possibles: EN_ATTENTE, ACCEPTE, REFUSE, CONVERTI, EXPIRE, ANNULE"
    )
    public ResponseEntity<Map<String, Object>> listDevisByStatut(
            @PathVariable String statut,@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("GET /api/v1/devis/statut/{}", statut);

        try {
            List<DevisDTO> devis = devisService.findByStatut(statut,boutiqueid);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", devis);
            response.put("total", devis.size());
            response.put("statut", statut);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Rechercher devis par période
     * GET /api/v1/devis/periode?dateDebut=2025-01-01&dateFin=2025-12-31
     * @return 
     */
    @GetMapping("/periode")
    @Operation(
            summary = "Devis par période",
            description = "Retourne les devis créés entre deux dates (format: yyyy-MM-dd)"
    )
    public ResponseEntity<Map<String, Object>> listDevisByPeriode(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {

        log.info("GET /api/v1/devis/periode?dateDebut={}&dateFin={}", dateDebut, dateFin);

        List<DevisDTO> devis = devisService.findByPeriode(dateDebut, dateFin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devis);
        response.put("total", devis.size());
        response.put("dateDebut", dateDebut);
        response.put("dateFin", dateFin);

        return ResponseEntity.ok(response);
    }

    /**
     * Recherche combinée: client + statut
     * GET /api/v1/devis/recherche?clientId=1&statut=EN_ATTENTE
     */
    @GetMapping("/recherche/{boutiqueid}")
    @Operation(
            summary = "Recherche avancée",
            description = "Recherche devis par client ET statut"
    )
    public ResponseEntity<Map<String, Object>> rechercheDevis(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = true) Long boutiqueid) {
        boutiqueAccessGuard.verifierAppartientALaCompagnieCourante(boutiqueid);

        log.info("GET /api/v1/devis/recherche?clientId={}&statut={}", clientId, statut);

        try {
            List<DevisDTO> devis;

            if (clientId != null && statut != null) {
                devis = devisService.findByClientIdAndStatut(clientId, statut,boutiqueid);
            } else if (clientId != null) {
                devis = devisService.findByClientId(clientId,boutiqueid);
            } else if (statut != null) {
                devis = devisService.findByStatut(statut,boutiqueid);
            } else {
                devis = devisService.findAll(boutiqueid).stream()
                        .map(mapper::toDto)
                        .toList();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", devis);
            response.put("total", devis.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================================================================
    // SECTION 4: GESTION DU CYCLE DE VIE
    // ================================================================

    /**
     * Accepter un devis (EN_ATTENTE → ACCEPTE)
     * PUT /api/v1/devis/{id}/accepter
     * @param id
     * @return 
     */
    @PutMapping("/{id}/accepter/{anneeid}")
  public ResponseEntity<Map<String, Object>> accepterDevis(
        @PathVariable Long id,
        @PathVariable int anneeid) {
    
    log.info("PUT /api/v1/devis/{}/accepter/{}", id, anneeid);
    
    try {
        DevisDTO devis = devisService.accepterDevis(id, anneeid);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Devis accepté avec succès");
        response.put("data", devis);
        response.put("devisId", devis.getId());
        response.put("statut", devis.getStatut());
        
        return ResponseEntity.ok(response);
        
    } catch (IllegalArgumentException e) {
        // Erreurs métier (devis introuvable, statut invalide, etc.)
        log.error("❌ Erreur validation acceptation devis {}: {}", id, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
        ));
        
    } catch (DataIntegrityViolationException e) {
        // Violations de contraintes BD (clé unique, FK, etc.)
        log.error("❌ Erreur contrainte BD lors de l'acceptation du devis {}: {}", 
                  id, e.getMessage(), e);
        
        String message = "Erreur d'intégrité des données";
        if (e.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
            message = "Violation de contrainte: " + cve.getConstraintName();
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", message
        ));
        
    } catch (OptimisticLockingFailureException e) {
        // Conflit de version (modification concurrente)
        log.error("❌ Conflit de version pour le devis {}: {}", id, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", "Le devis a été modifié par un autre utilisateur. Veuillez réessayer."
        ));
        
    } catch (TransactionSystemException e) {
        // Erreurs transactionnelles
        log.error("❌ Erreur transaction lors de l'acceptation du devis {}: {}", 
                  id, e.getMessage(), e);
        
        // Extraire la cause racine
        Throwable rootCause = e.getRootCause();
        String detailMessage = rootCause != null ? rootCause.getMessage() : e.getMessage();
        
        log.error("Cause racine: {}", detailMessage);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur lors de la sauvegarde: " + detailMessage
        ));
        
    } catch (Exception e) {
        // Toute autre exception
        log.error("❌ Erreur inattendue lors de l'acceptation du devis {}: {}", 
                  id, e.getMessage(), e);
        
        // Logger toutes les causes
        Throwable cause = e.getCause();
        int level = 1;
        while (cause != null) {
            log.error("  Cause niveau {}: {} - {}", level, 
                      cause.getClass().getSimpleName(), cause.getMessage());
            cause = cause.getCause();
            level++;
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur interne: " + e.getMessage()
        ));
    }
        
    }

    /**
     * Refuser un devis (EN_ATTENTE → REFUSE)
     * PUT /api/v1/devis/{id}/refuser
     * BODY: { "motif": "Client a refusé les conditions" }
     */
    @PutMapping("/{id}/refuser")
    @Operation(
            summary = "Refuser un devis",
            description = "Change le statut du devis de EN_ATTENTE à REFUSE. " +
                         "Le motif de refus est obligatoire."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devis refusé"),
        @ApiResponse(responseCode = "400", description = "Erreur: devis non EN_ATTENTE"),
        @ApiResponse(responseCode = "404", description = "Devis non trouvé")
    })
    public ResponseEntity<Map<String, Object>> refuserDevis(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        log.info("PUT /api/v1/devis/{}/refuser", id);

        try {
            String motif = body.getOrDefault("motif", "Refusé par le client");
            DevisDTO devis = devisService.refuserDevis(id, motif);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Devis refusé");
            response.put("data", devis);
            response.put("devisId", devis.getId());
            response.put("statut", devis.getStatut());
            response.put("motifRefus", motif);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur refus: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Annuler un devis
     * PUT /api/v1/devis/{id}/annuler
     * BODY: { "motif": "Erreur de saisie" }
     */
    @PutMapping("/{id}/annuler")
    @Operation(
            summary = "Annuler un devis",
            description = "Annule un devis. Impossible si déjà converti en facture. " +
                         "Le motif d'annulation est obligatoire."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devis annulé"),
        @ApiResponse(responseCode = "400", description = "Erreur: devis déjà converti"),
        @ApiResponse(responseCode = "404", description = "Devis non trouvé")
    })
    public ResponseEntity<Map<String, Object>> annulerDevis(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("X-Username") String username) {

        log.info("PUT /api/v1/devis/{}/annuler - User: {}", id, username);

        try {
            String motif = body.getOrDefault("motif", "Annulé");
            DevisDTO devis = devisService.annulerDevis(id, motif, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Devis annulé");
            response.put("data", devis);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur annulation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================================================================
    // SECTION 5: ALERTES ET NOTIFICATIONS
    // ================================================================

    /**
     * Lister les devis proches de l'expiration (< 3 jours)
     * GET /api/v1/devis/expiration/proches
     */
    @GetMapping("/expiration/proches")
             @Operation(   summary = "Devis proches de l'expiration",
            description = "Retourne les devis EN_ATTENTE qui expirent dans moins de 3 jours"
    )
    @ApiResponse(responseCode = "200", description = "Liste des devis")
    public ResponseEntity<Map<String, Object>> getDevisProchesExpiration() {
        log.info("GET /api/v1/devis/expiration/proches");

        List<DevisDTO> devis = devisService.findDevisProchesExpiration();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devis);
        response.put("total", devis.size());
        response.put("message", String.format("%d devis proche(s) de l'expiration", devis.size()));

        return ResponseEntity.ok(response);
    }

    /**
     * Lister les devis expirés
     * GET /api/v1/devis/expiration/expires
     */
    @GetMapping("/expiration/expires")
    @Operation(
            summary = "Devis expirés",
            description = "Retourne les devis EN_ATTENTE dont la date de validité est dépassée"
    )
    @ApiResponse(responseCode = "200", description = "Liste des devis expirés")
    public ResponseEntity<Map<String, Object>> getDevisExpires() {
        log.info("GET /api/v1/devis/expiration/expires");

        List<DevisDTO> devis = devisService.findDevisExpires();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", devis);
        response.put("total", devis.size());
        response.put("message", String.format("%d devis expiré(s)", devis.size()));

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // SECTION 6: STATISTIQUES ET REPORTING
    // ================================================================

    /**
     * Obtenir les statistiques globales
     * GET /api/v1/devis/stats
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Statistiques globales",
            description = "Retourne les statistiques complètes: total devis, répartition par statut, " +
                         "CA potentiel, taux de conversion, alertes expiration"
    )
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        log.info("GET /api/v1/devis/stats");

        try {
            Map<String, Object> stats = devisService.genererRapportStatistiques();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Erreur lors du calcul des statistiques"
            ));
        }
    }

    /**
     * Obtenir le chiffre d'affaires potentiel
     * GET /api/v1/devis/stats/ca-potentiel
     */
    @GetMapping("/stats/ca-potentiel")
    @Operation(
            summary = "Chiffre d'affaires potentiel",
            description = "Calcule le CA potentiel (somme des devis EN_ATTENTE + ACCEPTE)"
    )
    public ResponseEntity<Map<String, Object>> getChiffreAffairesPotentiel() {
        log.info("GET /api/v1/devis/stats/ca-potentiel");

        BigDecimal caPotentiel = devisService.calculerChiffreAffairesPotentiel();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("caPotentiel", caPotentiel);
        response.put("devise", "XAF");
        response.put("message", String.format("CA potentiel: %.2f XAF", caPotentiel));

        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir le taux de conversion
     * GET /api/v1/devis/stats/taux-conversion
     */
    @GetMapping("/stats/taux-conversion")
    @Operation(
            summary = "Taux de conversion",
            description = "Calcule le taux de conversion (devis acceptés/convertis / total devis)"
    )
    public ResponseEntity<Map<String, Object>> getTauxConversion() {
        log.info("GET /api/v1/devis/stats/taux-conversion");

        BigDecimal tauxConversion = devisService.calculerTauxConversion();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tauxConversion", tauxConversion);
        response.put("unite", "%");
        response.put("message", String.format("Taux de conversion: %.2f%%", tauxConversion));

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // SECTION 7: UTILITAIRES
    // ================================================================

    /**
     * Health check endpoint
     * GET /api/v1/devis/health
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Vérifie que le service de gestion des devis fonctionne correctement"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "DevisService");
        health.put("timestamp", new Date());
        health.put("message", "Service de gestion des devis opérationnel");

        return ResponseEntity.ok(health);
    }
}