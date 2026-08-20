package com.mproduits.services;

import com.mproduits.dto.DevisDTO;
import com.mproduits.dto.DevisItemDTO;
import com.mproduits.enums.CanalOrigine;
import com.mproduits.enums.StatutDevis;
import com.mproduits.exceptions.MetierException;
import com.mproduits.exceptions.DevisException;
import com.mproduits.mappers.DevisMapper;
import com.mproduits.model.*;

import com.mproduits.repositories.*;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de Gestion Complète des Devis
 *
 * FONCTIONNALITÉS PRINCIPALES: ============================= 1. CRÉATION ET
 * MODIFICATION - Créer un nouveau devis - Modifier un devis existant (si
 * EN_ATTENTE) - Dupliquer un devis - Ajouter/Supprimer des articles -
 * Recalculer automatiquement les totaux
 *
 * 2. GESTION DU CYCLE DE VIE - Accepter un devis - Refuser un devis - Annuler
 * un devis - Marquer comme expiré - Conversion en facture
 *
 * 3. CONSULTATION ET RECHERCHE - Récupérer un devis par ID - Lister tous les
 * devis (avec pagination) - Rechercher par client - Filtrer par statut -
 * Rechercher par numéro
 *
 * 4. NOTIFICATIONS ET ALERTES - Devis proches de l'expiration (< 3 jours) -
 * Devis expirés - Notification après acceptation
 *
 * 5. STATISTIQUES ET REPORTING - Total devis par statut - Chiffre d'affaires
 * potentiel - Taux de conversion
 *
 * @author USER01
 * @version 2.0
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DevisService {

    // ========== DÉPENDANCES ==========
    private final DevisRepository devisRepo;
    private final DevisItemRepository devisItemRepo;
    private final ClientRepository clientRepo;
    private final ProduitRepositories produitRepo;
    private final NotificationService notificationService;
    private final StockService stockService;
    private final BoutiqueRepositories boutiqueRepositories;
  
    private final  DevisSequenceRepository sequenceRepo;
    private final DevisMapper mapper;
    private final TenantContext tenantContext;
    private final com.mproduits.utiles.PDFGeneratorProfessionnel pdfGeneratorProfessionnel;

    // Constantes métier
    private static final int VALIDITE_DEFAUT_JOURS = 30;
    private static final int JOURS_ALERTE_EXPIRATION = 3;
    private static final String PREFIX_NUMERO_DEVIS = "DEV";

    // ================================================================
    // SECTION 1: CRÉATION ET MODIFICATION DE DEVIS
    // ================================================================
    /**
     * Crée un nouveau devis complet
     *
     * ÉTAPES: 1. Valider le client 2. Valider les articles et le stock 3.
     * Générer le numéro de devis 4. Créer le devis avec ses articles 5.
     * Calculer les montants (HT, TVA, TTC, remises) 6. Définir la date
     * d'expiration 7. Enregistrer en base 8. Envoyer notification
     *
     * @param dto DTO contenant les informations du devis
     * @param username Utilisateur créant le devis
     * @return Devis créé
     * @throws MetierException Si validation échoue
     */
    public Devis creerDevis(DevisDTO dto, String username, Long boutiqueid, int anneeid) {
        return creerDevisInterne(dto, username, boutiqueid, anneeid, tenantContext.currentCompagnieId(), CanalOrigine.INTERNE);
    }

    /**
     * Chemin public (site e-commerce, visiteur invite ou client connecte -
     * voir EcomCheckoutController) : aucun JWT staff, donc pas de
     * TenantContext - compagnieId vient de la compagnie deja resolue par
     * l'URL publique (Compagnie.code), passee ici explicitement plutot que
     * de compter sur un attribut de requete pose artificiellement.
     * "e-commerce" comme usernameCreate (NOT NULL en base, pas d'utilisateur
     * staff pour une commande en ligne).
     */
    public Devis creerDevisPublic(DevisDTO dto, Long boutiqueid, int anneeid, Long compagnieId) {
        return creerDevisInterne(dto, "e-commerce", boutiqueid, anneeid, compagnieId, CanalOrigine.EN_LIGNE);
    }

    private Devis creerDevisInterne(DevisDTO dto, String username, Long boutiqueid, int anneeid, Long compagnieId, CanalOrigine origine) {
        log.info("═══════════════════════════════════════");
        log.info("DÉBUT CRÉATION DEVIS - Client ID: {}", dto.getClient().getNom());
        log.info("═══════════════════════════════════════");

        try {
            // 1. VALIDER CLIENT
            Client client = validerClient(dto.getClient().getId());
            log.debug("✓ Client validé: {} ({})", client.getNom(), client.getId());

            // 2. VALIDER ARTICLES
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new DevisException("Le devis doit contenir au moins un article");
            }
            Optional<Boutique> boutique = boutiqueRepositories.findById(boutiqueid);
            validerArticles(dto.getItems());
            log.debug("✓ {} article(s) validé(s)", dto.getItems().size());

            // 3. VÉRIFIER STOCK (optionnel selon configuration)
            if (dto.isVerifierStock()) {
                verifierStockDisponible(dto.getItems(), boutiqueid, anneeid, compagnieId);
                log.debug("✓ Stock vérifié et disponible");
            }

            // 4. CRÉER ENTITÉ DEVIS
            Devis devis = Devis.builder()
                    .numeroDevis(genererNumeroDevis())
                    .dateDevis(new Date())
                    .client(client)
                    .montantHT(dto.getMontantHT())
                    .totalTVA(dto.getTotalTVA())
                    .total(dto.getTotal())
                    .statut(StatutDevis.EN_ATTENTE)
                    .usernameCreate(username)
                    .dateCreation(new Date())
                    .appliquerTVA(dto.isAppliquerTVA())
                    .tauxTVA(dto.getTauxTVA() != null ? dto.getTauxTVA() : BigDecimal.ZERO)
                    .remarques(dto.getRemarques())
                    .conditions(dto.getConditions())
                    .validiteJours(dto.getValiditeJours() != null ? dto.getValiditeJours() : VALIDITE_DEFAUT_JOURS)
                    .totalRemise(dto.getTotalRemise())
                    .items(new ArrayList<>())
                    .boutique(boutique.get())
                    .canalOrigine(origine)
                    .build();

            // 5. CALCULER DATE EXPIRATION
            Calendar cal = Calendar.getInstance();
            cal.setTime(devis.getDateDevis());
            cal.add(Calendar.DAY_OF_MONTH, devis.getValiditeJours());
            devis.setDateExpiration(cal.getTime());

            log.debug("✓ Devis créé - Numéro: {}, Expiration: {}",
                    devis.getNumeroDevis(), devis.getDateExpiration());

            // 6. CRÉER LES ARTICLES
            int ordre = 1;
            for (DevisItemDTO itemDto : dto.getItems()) {
                Produit produit = produitRepo.findByIdAndCompagnie_Id(itemDto.getProduitId(), compagnieId)
                        .orElseThrow(() -> new DevisException(
                        "Produit non trouvé: " + itemDto.getProduitId()));

                DevisItem item = DevisItem.builder()
                        .devis(devis)
                        .produit(produit)
                        .produitCode(produit.getCode())
                        .produitLibelle(produit.getLibelle())
                        .description(produit.getDescription())
                        .quantite(itemDto.getQuantite())
                        .prixUnitaire(itemDto.getPrixUnitaire())
                        .tauxRemise(itemDto.getTauxRemise() != null
                                ? itemDto.getTauxRemise() : BigDecimal.ZERO)
                        .tauxTVA(devis.getAppliquerTVA() ? devis.getTauxTVA() : BigDecimal.ZERO)
                        .montantRemise(itemDto.getTauxRemise() != null
                                ? montantremise(itemDto, devis.getTauxTVA()) : itemDto.getMontantRemise())
                        .ordre(ordre++)
                        .build();

                // Les montants seront calculés automatiquement via @PrePersist
                devis.addItem(item);
            }

            // 7. CALCULER TOTAUX DU DEVIS
            // calculerTotauxDevis(devis);
            // 8. SAUVEGARDER
            devis = devisRepo.save(devis);

            log.info("✓ Devis enregistré - ID: {}, Total TTC: {} XAF",
                    devis.getId(), devis.getTotal());

            // 9. NOTIFICATION
            notificationService.ajouter(
                    "Nouveau Devis",
                    String.format("Devis %s créé pour le client %s. Montant: %.2f XAF",
                            devis.getNumeroDevis(), client.getNom(), devis.getTotal()),
                    "success"
            );

            log.info("═══════════════════════════════════════");
            log.info("FIN CRÉATION DEVIS - ID: {}", devis.getId());
            log.info("═══════════════════════════════════════");

            return devis;

        } catch (DevisException e) {
            log.error("❌ Erreur métier: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur technique lors de la création du devis", e);
            throw new MetierException("Erreur lors de la création du devis: " + e.getMessage(), e);
        }
    }

    /**
     * Modifie un devis existant (uniquement si EN_ATTENTE)
     *
     * @param devisId ID du devis à modifier
     * @param dto Nouvelles données
     * @param username Utilisateur effectuant la modification
     * @return Devis modifié
     */
    private BigDecimal montantremise(DevisItemDTO item, BigDecimal tauxtva) {
        double prixarticle = item.getPrixUnitaire().doubleValue() * item.getQuantite();
        float totaltva = (float) (prixarticle - (prixarticle * (tauxtva.doubleValue() / 100)));
        return BigDecimal.valueOf(Math.round(totaltva));

    }

    public Devis modifierDevis(Long devisId, DevisDTO dto, String username) {
        log.info("DÉBUT MODIFICATION DEVIS - ID: {}", devisId);

        try {
            Devis devis = devisRepo.findById(devisId)
                    .orElseThrow(() -> new DevisException("Devis non trouvé: " + devisId));

            // Vérifier que le devis peut être modifié
            if (!devis.isModifiable()) {
                throw new DevisException(
                        "Impossible de modifier le devis. Statut actuel: " + devis.getStatut());
            }

            // Mise à jour des informations générales
            if (dto.getRemarques() != null) {
                devis.setRemarques(dto.getRemarques());
            }
            if (dto.getConditions() != null) {
                devis.setConditions(dto.getConditions());
            }
            if (dto.getTauxTVA() != null) {
                devis.setTauxTVA(dto.getTauxTVA());
                devis.setTotalTVA(dto.getTotalTVA());
            }
            devis.setAppliquerTVA(dto.isAppliquerTVA());
            devis.setUsernameUpdate(username);
            devis.setDateModification(new Date());
            devis.setMontantHT(dto.getMontantHT());
            devis.setTotal(dto.getTotal());
            devis.setTotalRemise(dto.getTotalRemise());
            //devis = devisRepo.save(devis);

            // Mise à jour des articles si fournis
            if (dto.getItems() != null && !dto.getItems().isEmpty()) {
                // ✅ SOLUTION 1 : Créer une Map des items existants
                Map<Long, DevisItem> itemsExistantsMap = devis.getItems().stream()
                        .filter(item -> item.getId() != null)
                        .collect(Collectors.toMap(DevisItem::getId, item -> item));

                // Supprimer tous les items (ils seront recréés/réutilisés)
                devis.getItems().clear();

                int ordre = 1;
                for (DevisItemDTO itemDto : dto.getItems()) {
                    Produit produit = produitRepo.findById(itemDto.getProduitId())
                            .orElseThrow(() -> new DevisException(
                            "Produit non trouvé: " + itemDto.getProduitId()));

                    DevisItem item;

                    // ✅ Item existant : récupérer depuis la map
                    if (itemDto.getId() != null && itemsExistantsMap.containsKey(itemDto.getId())) {
                        item = itemsExistantsMap.get(itemDto.getId());

                        // Mettre à jour les propriétés
                        item.setProduit(produit);
                        item.setProduitCode(produit.getCode());
                        item.setProduitLibelle(produit.getLibelle());
                        item.setQuantite(itemDto.getQuantite());
                        item.setPrixUnitaire(itemDto.getPrixUnitaire());
                        item.setTauxRemise(itemDto.getTauxRemise() != null
                                ? itemDto.getTauxRemise() : BigDecimal.ZERO);
                        item.setTauxTVA(devis.getAppliquerTVA() ? devis.getTauxTVA() : BigDecimal.ZERO);
                        item.setOrdre(ordre++);
                        item.setMontantRemise(itemDto.getTauxRemise() != null
                                ? montantremise(itemDto, devis.getTauxTVA()) : itemDto.getMontantRemise());

                    } else {
                        // ✅ Nouvel item : créer sans ID
                        item = DevisItem.builder()
                                // .id(itemDto.getId())  ← NE PAS DÉFINIR L'ID !
                                .produit(produit)
                                .produitCode(produit.getCode())
                                .produitLibelle(produit.getLibelle())
                                .quantite(itemDto.getQuantite())
                                .prixUnitaire(itemDto.getPrixUnitaire())
                                .tauxRemise(itemDto.getTauxRemise() != null
                                        ? itemDto.getTauxRemise() : BigDecimal.ZERO)
                                .tauxTVA(devis.getAppliquerTVA() ? devis.getTauxTVA() : BigDecimal.ZERO)
                                .ordre(ordre++)
                                .montantRemise(itemDto.getTauxRemise() != null
                                        ? montantremise(itemDto, devis.getTauxTVA()) : itemDto.getMontantRemise())
                                .build();
                    }

                    // Ajouter l'item au devis (gère la relation bidirectionnelle)
                    devis.addItem(item);
                    //devisItemRepo.save(item);
                }
            }

            // Sauvegarder (le cascade s'occupe des items)
            log.debug("Devis avant sauvegarde: {}", devis);
            devis.getItems().forEach(i -> log.debug("Item: id={}, produit={}, montant={}",
                    i.getId(), i.getProduit(), i.getPrixUnitaire()));

            devis = devisRepo.save(devis);

            log.info("✓ Devis modifié - ID: {}, Version: {}", devis.getId(), devis.getVersion());

            notificationService.ajouter(
                    "Devis Modifié",
                    String.format("Le devis %s a été modifié", devis.getNumeroDevis()),
                    "info"
            );

            return devis;

        } catch (DevisException e) {
            log.error("❌ Erreur: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la modification", e);
            throw new MetierException("Erreur lors de la modification: " + e.getMessage(), e);
        }
    }

    /**
     * Duplique un devis existant Crée une copie avec un nouveau numéro et
     * statut EN_ATTENTE
     *
     * @param devisId ID du devis à dupliquer
     * @param username Utilisateur effectuant la duplication
     * @return Nouveau devis
     */
    public Devis dupliquerDevis(Long devisId, String username) {
        log.info("DUPLICATION DEVIS - ID source: {}", devisId);

        try {
            Devis devisSource = devisRepo.findByIdAndBoutique_Compagnie_Id(devisId, tenantContext.currentCompagnieId())
                    .orElseThrow(() -> new DevisException("Devis non trouvé: " + devisId));

            // Créer nouveau devis
            Devis nouveauDevis = Devis.builder()
                    .numeroDevis(genererNumeroDevis())
                    .dateDevis(new Date())
                    .client(devisSource.getClient())
                    .boutique(devisSource.getBoutique())
                    .statut(StatutDevis.EN_ATTENTE)
                    .usernameCreate(username)
                    .dateCreation(new Date())
                    .appliquerTVA(devisSource.getAppliquerTVA())
                    .tauxTVA(devisSource.getTauxTVA())
                    .remarques(devisSource.getRemarques())
                    .conditions(devisSource.getConditions())
                    .validiteJours(VALIDITE_DEFAUT_JOURS)
                    // Memes articles copies plus bas => memes totaux que la
                    // source. Sans les poser ici, le builder Lombok (pas de
                    // @Builder.Default sur ces champs) enverrait null pour
                    // total (total_ttc), NOT NULL en base.
                    .montantHT(devisSource.getMontantHT())
                    .totalRemise(devisSource.getTotalRemise())
                    .totalTVA(devisSource.getTotalTVA())
                    .total(devisSource.getTotal())
                    .items(new ArrayList<>())
                    .build();

            // Calculer nouvelle date expiration
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, VALIDITE_DEFAUT_JOURS);
            nouveauDevis.setDateExpiration(cal.getTime());

            // Copier les articles
            int ordre = 1;
            for (DevisItem itemSource : devisSource.getItems()) {
                DevisItem nouvelItem = DevisItem.builder()
                        .devis(nouveauDevis)
                        .produit(itemSource.getProduit())
                        .produitCode(itemSource.getProduitCode())
                        .produitLibelle(itemSource.getProduitLibelle())
                        .description(itemSource.getDescription())
                        .quantite(itemSource.getQuantite())
                        .prixUnitaire(itemSource.getPrixUnitaire())
                        .tauxRemise(itemSource.getTauxRemise())
                        .tauxTVA(itemSource.getTauxTVA())
                        .ordre(ordre++)
                        .build();

                nouveauDevis.addItem(nouvelItem);
            }

            // Calculer totaux
            calculerTotauxDevis(nouveauDevis);

            // Sauvegarder
            nouveauDevis = devisRepo.save(nouveauDevis);

            log.info("✓ Devis dupliqué - Nouveau numéro: {}", nouveauDevis.getNumeroDevis());

            notificationService.ajouter(
                    "Devis Dupliqué",
                    String.format("Devis %s créé par duplication de %s",
                            nouveauDevis.getNumeroDevis(), devisSource.getNumeroDevis()),
                    "info"
            );

            return nouveauDevis;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la duplication", e);
            throw new MetierException("Erreur lors de la duplication: " + e.getMessage(), e);
        }
    }

    // ================================================================
    // SECTION 2: GESTION DU CYCLE DE VIE
    // ================================================================
    /**
     * Accepte un devis (EN_ATTENTE → ACCEPTE)
     *
     * VALIDATIONS: - Devis doit être EN_ATTENTE - Devis ne doit pas être expiré
     * - Stock doit être disponible
     *
     * @param devisId ID du devis
     * @return DevisDTO accepté
     */
    public DevisDTO accepterDevis(Long devisId, int anneeid) {
        log.info("═══════════════════════════════════════");
        log.info("ACCEPTATION DEVIS - ID: {}", devisId);
        log.info("═══════════════════════════════════════");

        try {
            Devis devis = devisRepo.findByIdAndBoutique_Compagnie_Id(devisId, tenantContext.currentCompagnieId())
                    .orElseThrow(() -> new DevisException("Devis non trouvé: " + devisId));

            // Vérifications
            if (devis.getStatut() != StatutDevis.EN_ATTENTE) {
                throw new DevisException(
                        String.format("Le devis doit être EN_ATTENTE. Statut actuel: %s",
                                devis.getStatut().getLibelle()));
            }

            if (devis.isExpire()) {
                throw new DevisException("Le devis est expiré et ne peut être accepté");
            }

            // Vérifier stock
            List<DevisItemDTO> itemsDto = devis.getItems().stream()
                    .map(this::convertirEnDTO)
                    .collect(Collectors.toList());

            List<String> erreursStock = validerStockDisponible(itemsDto, devis.getBoutique().getId(), anneeid, tenantContext.currentCompagnieId());
            if (!erreursStock.isEmpty()) {
                throw new DevisException("Stock insuffisant: " + String.join(", ", erreursStock));
            }

            // Changer statut
            devis.setStatut(StatutDevis.ACCEPTE);
            devis.setDateAcceptation(new Date());

            devis = devisRepo.save(devis);

            log.info("✓ Devis ACCEPTÉ - Numéro: {}", devis.getNumeroDevis());

            // Notification
            notificationService.ajouter(
                    "Devis Accepté",
                    String.format("Le devis %s a été accepté. Prêt pour facturation.",
                            devis.getNumeroDevis()),
                    "success"
            );

            log.info("═══════════════════════════════════════");

            return mapper.toDto(devis);

        } catch (DevisException e) {
            log.error("❌ {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'acceptation", e);
            throw new MetierException("Erreur: " + e.getMessage(), e);
        }
    }

    /**
     * Refuse un devis (EN_ATTENTE → REFUSE)
     *
     * @param devisId ID du devis
     * @param motif Motif du refus
     * @return DevisDTO refusé
     */
    public DevisDTO refuserDevis(Long devisId, String motif, String username) {
        log.info("REFUS DEVIS - ID: {}", devisId);

        try {
            Devis devis = devisRepo.findByIdAndBoutique_Compagnie_Id(devisId, tenantContext.currentCompagnieId())
                    .orElseThrow(() -> new DevisException("Devis non trouvé: " + devisId));

            if (devis.getStatut() != StatutDevis.EN_ATTENTE) {
                throw new DevisException("Seul un devis EN_ATTENTE peut être refusé");
            }

            devis.setStatut(StatutDevis.REFUSE);
            devis.setMotifRefus(motif);
            devis.setUsernameUpdate(username);
            devis.setDateModification(new Date());

            devis = devisRepo.save(devis);

            log.info("✓ Devis REFUSÉ - Numéro: {}, Motif: {}",
                    devis.getNumeroDevis(), motif);

            notificationService.ajouter(
                    "Devis Refusé",
                    String.format("Le devis %s a été refusé", devis.getNumeroDevis()),
                    "warning"
            );

            return mapper.toDto(devis);

        } catch (Exception e) {
            log.error("❌ Erreur lors du refus", e);
            throw new MetierException("Erreur: " + e.getMessage(), e);
        }
    }

    /**
     * Annule un devis
     *
     * @param devisId ID du devis
     * @param motif Motif d'annulation
     * @param username Utilisateur effectuant l'annulation
     * @return DevisDTO annulé
     */
    public DevisDTO annulerDevis(Long devisId, String motif, String username) {
        log.info("ANNULATION DEVIS - ID: {}", devisId);

        try {
            Devis devis = devisRepo.findByIdAndBoutique_Compagnie_Id(devisId, tenantContext.currentCompagnieId())
                    .orElseThrow(() -> new DevisException("Devis non trouvé: " + devisId));

            // Ne peut pas annuler si déjà converti
            if (devis.getStatut() == StatutDevis.CONVERTI) {
                throw new DevisException(
                        "Impossible d'annuler un devis déjà converti en facture");
            }

            devis.setStatut(StatutDevis.ANNULE);
            devis.setMotifAnnulation(motif);
            devis.setUsernameUpdate(username);
            devis.setDateModification(new Date());

            devis = devisRepo.save(devis);

            log.info("✓ Devis ANNULÉ - Numéro: {}", devis.getNumeroDevis());

            notificationService.ajouter(
                    "Devis Annulé",
                    String.format("Le devis %s a été annulé", devis.getNumeroDevis()),
                    "warning"
            );

            return mapper.toDto(devis);

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'annulation", e);
            throw new MetierException("Erreur: " + e.getMessage(), e);
        }
    }

    /**
     * Marque les devis expirés (batch automatique) Change statut EN_ATTENTE →
     * EXPIRE pour devis dont date > date expiration
     *
     * @return Nombre de devis marqués comme expirés
     */
    @Transactional
    public int marquerDevisExpires() {
        log.info("BATCH: Vérification des devis expirés");

        try {
            List<Devis> devisEnAttente = devisRepo.findByStatut(StatutDevis.EN_ATTENTE.name());
            int count = 0;

            for (Devis devis : devisEnAttente) {
                if (devis.isExpire()) {
                    devis.setStatut(StatutDevis.EXPIRE);
                    devisRepo.save(devis);
                    count++;

                    log.debug("Devis {} marqué EXPIRÉ", devis.getNumeroDevis());
                }
            }

            if (count > 0) {
                log.info("✓ {} devis marqué(s) comme EXPIRÉ", count);

                notificationService.ajouter(
                        "Devis Expirés",
                        String.format("%d devis ont expiré", count),
                        "warning"
                );
            }

            return count;

        } catch (Exception e) {
            log.error("❌ Erreur lors du marquage des devis expirés", e);
            return 0;
        }
    }

    // ================================================================
    // SECTION 3: CONSULTATION ET RECHERCHE
    // ================================================================
    /**
     * Récupère un devis par son ID avec tous ses détails
     */
    @Transactional(readOnly = true)
    public Optional<Devis> findById(Long id) {
        log.debug("Recherche devis ID: {}", id);
        return devisRepo.findByIdAndBoutique_Compagnie_Id(id, tenantContext.currentCompagnieId());
    }

    /**
     * Génère le PDF du devis (aucun endpoint n'existait auparavant - voir
     * PDFGeneratorProfessionnel.genererDevisPDF).
     */
    @Transactional(readOnly = true)
    public byte[] genererPDF(Long id) {
        Devis devis = findById(id)
                .orElseThrow(() -> new DevisException("Devis non trouvé: " + id));
        return pdfGeneratorProfessionnel.genererDevisPDF(devis);
    }

    /**
     * Récupère un devis par son numéro
     */
    @Transactional(readOnly = true)
    public Optional<Devis> findByNumero(String numeroDevis) {
        log.debug("Recherche devis numéro: {}", numeroDevis);
        return devisRepo.findByNumeroDevisAndBoutique_Compagnie_Id(numeroDevis, tenantContext.currentCompagnieId());
    }

    /**
     * Liste tous les devis
     */
    @Transactional(readOnly = true)
    public List<Devis> findAll(Long boutiqueid) {
        log.debug("Récupération de tous les devis");
        return devisRepo.findByBoutiqueId(boutiqueid);
    }

    /**
     * Liste tous les devis avec pagination
     */
    @Transactional(readOnly = true)
    public Page<DevisDTO> findAllPaginated(Pageable pageable) {
        log.debug("Récupération devis paginés - Page: {}, Taille: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return devisRepo.findByBoutique_Compagnie_Id(tenantContext.currentCompagnieId(), pageable)
                .map(mapper::toDto);
    }

    /**
     * Liste les devis d'un client
     */
    @Transactional(readOnly = true)
    public List<DevisDTO> findByClientId(Long clientId, Long boutiqueid) {
        log.debug("Recherche devis du client: {}", clientId);
        return devisRepo.findByClientId(clientId, boutiqueid).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Liste les devis par statut
     */
    @Transactional(readOnly = true)
    public List<DevisDTO> findByStatut(String statut, Long boutiqueid) {
        log.debug("Recherche devis avec statut: {}", statut);
        try {
            StatutDevis statutEnum = StatutDevis.valueOf(statut.toUpperCase());
            return devisRepo.findByStatutAndBoutiqueId(statutEnum, boutiqueid).stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new DevisException("Statut invalide: " + statut);
        }
    }

    /**
     * Recherche devis par client et statut
     */
    @Transactional(readOnly = true)
    public List<DevisDTO> findByClientIdAndStatut(Long clientId, String statut, Long boutiqueid) {
        log.debug("Recherche devis - Client: {}, Statut: {}", clientId, statut);

        StatutDevis statutEnum = StatutDevis.valueOf(statut.toUpperCase());
        return devisRepo.findByClientIdAndStatut(clientId, statutEnum, boutiqueid).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Recherche devis par période
     */
    @Transactional(readOnly = true)
    public List<DevisDTO> findByPeriode(Date dateDebut, Date dateFin) {
        log.debug("Recherche devis entre {} et {}", dateDebut, dateFin);
        return devisRepo.findByCompagnieIdAndDateDevisBetween(tenantContext.currentCompagnieId(), dateDebut, dateFin).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    // ================================================================
    // SECTION 4: NOTIFICATIONS ET ALERTES
    // ================================================================
    /**
     * Récupère les devis proches de l'expiration (< 3 jours)
     */
    @Transactional(readOnly = true)
    public List<DevisDTO> findDevisProchesExpiration() {
        log.debug("Recherche devis proches de l'expiration");

        List<Devis> allDevis = devisRepo.findByStatut(StatutDevis.EN_ATTENTE.name());
        Date maintenant = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, JOURS_ALERTE_EXPIRATION);
        Date limite = cal.getTime();

        return allDevis.stream()
                .filter(d -> d.getDateExpiration() != null)
                .filter(d -> d.getDateExpiration().after(maintenant)
                && d.getDateExpiration().before(limite))
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les devis expirés (date expiration dépassée)
     */
    @Transactional(readOnly = true)
    public List<DevisDTO> findDevisExpires() {
        log.debug("Recherche devis expirés");

        List<Devis> allDevis = devisRepo.findByStatut(StatutDevis.EN_ATTENTE.name());
        Date maintenant = new Date();

        return allDevis.stream()
                .filter(d -> d.getDateExpiration() != null)
                .filter(d -> d.getDateExpiration().before(maintenant))
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Envoie des notifications pour devis proches expiration À appeler
     * périodiquement (scheduler)
     */
    public void notifierDevisProchesExpiration() {
        List<DevisDTO> devisProches = findDevisProchesExpiration();

        if (!devisProches.isEmpty()) {
            log.info("⚠ {} devis proche(s) de l'expiration", devisProches.size());

            for (DevisDTO devis : devisProches) {
                notificationService.ajouter(
                        "Devis Proche Expiration",
                        String.format("Le devis %s expire dans %d jour(s)",
                                devis.getNumeroDevis(),
                                calculerJoursRestants(devis.getDateExpiration())),
                        "warning"
                );
            }
        }
    }

    // ================================================================
    // SECTION 5: STATISTIQUES ET REPORTING
    // ================================================================
    /**
     * Compte le nombre de devis par statut
     */
    @Transactional(readOnly = true)
    public Map<String, Long> compterDevisParStatut() {
        Map<String, Long> stats = new HashMap<>();

        for (StatutDevis statut : StatutDevis.values()) {
            long count = devisRepo.countByStatut(statut);
            stats.put(statut.name(), count);
        }

        log.debug("Statistiques devis: {}", stats);
        return stats;
    }

    /**
     * Calcule le chiffre d'affaires potentiel (devis EN_ATTENTE + ACCEPTE)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerChiffreAffairesPotentiel() {
        List<Devis> devisActifs = new ArrayList<>();
        devisActifs.addAll(devisRepo.findByStatut(StatutDevis.EN_ATTENTE.name()));
        devisActifs.addAll(devisRepo.findByStatut(StatutDevis.ACCEPTE.name()));

        BigDecimal total = devisActifs.stream()
                .map(Devis::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("CA potentiel: {} XAF", total);
        return total;
    }

    /**
     * Calcule le taux de conversion (ACCEPTE / Total)
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerTauxConversion() {
        long total = devisRepo.count();
        if (total == 0) {
            return BigDecimal.ZERO;
        }

        long acceptes = devisRepo.countByStatut(StatutDevis.ACCEPTE);
        long convertis = devisRepo.countByStatut(StatutDevis.CONVERTI);

        BigDecimal taux = BigDecimal.valueOf(acceptes + convertis)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        log.debug("Taux conversion: {}%", taux);
        return taux;
    }

    /**
     * Génère un rapport statistique complet
     */
    @Transactional(readOnly = true)
    public Map<String, Object> genererRapportStatistiques() {
        Map<String, Object> rapport = new HashMap<>();

        rapport.put("total_devis", devisRepo.count());
        rapport.put("par_statut", compterDevisParStatut());
        rapport.put("ca_potentiel", calculerChiffreAffairesPotentiel());
        rapport.put("taux_conversion", calculerTauxConversion());
        rapport.put("proches_expiration", findDevisProchesExpiration().size());
        rapport.put("expires", findDevisExpires().size());

        log.info("Rapport statistiques généré: {}", rapport);
        return rapport;
    }

    // ================================================================
    // SECTION 6: MÉTHODES PRIVÉES UTILITAIRES
    // ================================================================
    /**
     * Valide qu'un client existe
     */
    private Client validerClient(Long clientId) {
        return clientRepo.findById(clientId)
                .orElseThrow(() -> new DevisException("Client non trouvé: " + clientId));
    }

    /**
     * Valide la cohérence des articles
     */
    private void validerArticles(List<DevisItemDTO> items) {
        for (DevisItemDTO item : items) {
            if (item.getProduitId() == null) {
                throw new DevisException("Produit ID requis");
            }
            if (item.getQuantite() == null || item.getQuantite() <= 0) {
                throw new DevisException("Quantité invalide pour produit: " + item.getProduitId());
            }
            if (item.getPrixUnitaire() == null
                    || item.getPrixUnitaire().compareTo(BigDecimal.ZERO) < 0) {
                throw new DevisException("Prix invalide pour produit: " + item.getProduitId());
            }
        }
    }

    /**
     * Vérifie la disponibilité du stock
     */
    private void verifierStockDisponible(List<DevisItemDTO> items, Long boutiqueid, int anneeid, Long compagnieId) {
        List<String> erreurs = validerStockDisponible(items, boutiqueid, anneeid, compagnieId);
        if (!erreurs.isEmpty()) {
            throw new DevisException("Stock insuffisant: " + String.join(", ", erreurs));
        }
    }

    /**
     * Valide le stock pour une liste d'articles
     */
    private List<String> validerStockDisponible(List<DevisItemDTO> items, Long boutiqueid, int anneeid, Long compagnieId) {
        List<String> erreurs = new ArrayList<>();

        for (DevisItemDTO item : items) {
            Produit produit = produitRepo.findByIdAndCompagnie_Id(item.getProduitId(), compagnieId).orElse(null);
            if (produit == null) {
                erreurs.add("Produit introuvable: " + item.getProduitId());
                continue;
            }
            //Optional<Boutique>boutique =boutiqueRepositories.findById(boutiqueid);
            BigDecimal stockDispo = stockService.getStockTotal(produit.getId(), boutiqueid, anneeid);
            BigDecimal quantiteDemandee = new BigDecimal(item.getQuantite());

            if (stockDispo.compareTo(quantiteDemandee) < 0) {
                erreurs.add(String.format(
                        "%s: stock insuffisant (demandé: %d, disponible: %.0f)",
                        produit.getCode(), item.getQuantite(), stockDispo));
            }
        }

        return erreurs;
    }

    /**
     * Calcule tous les totaux d'un devis
     */
    private void calculerTotauxDevis(Devis devis) {
        BigDecimal montantHT = BigDecimal.ZERO;
        BigDecimal totalRemises = BigDecimal.ZERO;
        BigDecimal totalTVA = BigDecimal.ZERO;

        for (DevisItem item : devis.getItems()) {
            montantHT = montantHT.add(item.getMontantHT());
            totalRemises = totalRemises.add(item.getMontantRemise());
            totalTVA = totalTVA.add(item.getMontantTVA());
        }

        devis.setMontantHT(montantHT);
        devis.setTotalRemise(totalRemises);
        devis.setTotalTVA(totalTVA);
        devis.setTotal(montantHT.add(totalTVA));
    }

    /**
     * Génère un numéro de devis unique (Format: DEV-2025-0001)
     */
    private String genererNumeroDevis_old() {
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);

        // Compter les devis de l'année
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date debutAnnee = cal.getTime();

        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date finAnnee = cal.getTime();

        long count = devisRepo.countByDateDevisBetween(debutAnnee, finAnnee);
        int numero = (int) (count + 1);

        String numeros_devis= String.format("%s-%d-%04d", PREFIX_NUMERO_DEVIS, annee, numero);
        do {            
             numero++;
        } while (devisRepo.existsByNumeroDevis(numeros_devis)==Boolean.TRUE);
        
       return String.format("%s-%d-%04d", PREFIX_NUMERO_DEVIS, annee, numero);
    }

     @Transactional
    public String genererNumeroDevis() {
        Calendar cal = Calendar.getInstance();
        int annee = cal.get(Calendar.YEAR);
        
        // Récupérer ou créer la séquence de l'année avec verrou pessimiste
        DevisSequence sequence = sequenceRepo.findByAnneeWithLock(annee)
            .orElseGet(() -> {
                // Première fois : vérifier s'il existe déjà des devis pour cette année
                DevisSequence nouvelleSequence = new DevisSequence(annee);
                int maxNumeroExistant = obtenirMaxNumeroDevisAnnee(annee);
                nouvelleSequence.setDernierNumero(maxNumeroExistant);
                return sequenceRepo.save(nouvelleSequence);
            });
        
        // Vérifier si la séquence est bien synchronisée avec les données
        int maxNumeroExistant = obtenirMaxNumeroDevisAnnee(annee);
        if (maxNumeroExistant > sequence.getDernierNumero()) {
            // Corriger la séquence si nécessaire
            sequence.setDernierNumero(maxNumeroExistant);
            sequenceRepo.save(sequence);
        }
        
        // Incrémenter et générer le numéro
        int nouveauNumero = sequence.getDernierNumero() + 1;
        String numeroDevis = String.format("%s-%d-%04d", PREFIX_NUMERO_DEVIS, annee, nouveauNumero);
        
        // Double vérification pour éviter les doublons (sécurité supplémentaire)
        int tentatives = 0;
        while (devisRepo.existsByNumeroDevis(numeroDevis) && tentatives < 100) {
            nouveauNumero++;
            numeroDevis = String.format("%s-%d-%04d", PREFIX_NUMERO_DEVIS, annee, nouveauNumero);
            tentatives++;
        }
        
        if (tentatives >= 100) {
            throw new RuntimeException("Impossible de générer un numéro de devis unique après 100 tentatives");
        }
        
        // Sauvegarder le nouveau dernier numéro
        sequence.setDernierNumero(nouveauNumero);
        sequenceRepo.save(sequence);
        
        return numeroDevis;
    }
    
    
    /**
     * Récupère le numéro maximum existant dans la base pour une année donnée
     */
    private int obtenirMaxNumeroDevisAnnee(int annee) {
        String pattern = PREFIX_NUMERO_DEVIS + "-" + annee + "-%";
        Integer maxNumero = devisRepo.findMaxNumeroByPattern(pattern);
        return (maxNumero != null) ? maxNumero : 0;
    }
    
    /**
     * Méthode utilitaire pour initialiser ou réparer les séquences
     * À exécuter une seule fois pour synchroniser avec les données existantes
     */
    @Transactional
    public void initialiserSequences() {
        Calendar cal = Calendar.getInstance();
        int anneeActuelle = cal.get(Calendar.YEAR);
        
        // Initialiser pour les 3 dernières années
        for (int annee = anneeActuelle - 2; annee <= anneeActuelle; annee++) {
            int maxNumero = obtenirMaxNumeroDevisAnnee(annee);
            
            DevisSequence sequence = sequenceRepo.findById(annee)
                .orElse(new DevisSequence(annee));
            
            sequence.setDernierNumero(maxNumero);
            sequenceRepo.save(sequence);
            
            System.out.println("Séquence initialisée pour " + annee + " : dernier numéro = " + maxNumero);
        }
    }

    /**
     * Convertit DevisItem en DevisItemDTO
     */
    private DevisItemDTO convertirEnDTO(DevisItem item) {
        DevisItemDTO dto = new DevisItemDTO();
        dto.setId(item.getId());
        dto.setProduitId(item.getProduit().getId());
        dto.setQuantite(item.getQuantite());
        dto.setPrixUnitaire(item.getPrixUnitaire());
        dto.setTauxRemise(item.getTauxRemise());
        return dto;
    }

    /**
     * Calcule le nombre de jours restants avant une date
     */
    private long calculerJoursRestants(Date date) {
        if (date == null) {
            return -1;
        }
        long diff = date.getTime() - new Date().getTime();
        return diff / (1000 * 60 * 60 * 24);
    }
}
