
package com.mproduits.services;

import com.mproduits.dto.*;
import com.mproduits.enums.MovementType;
import com.mproduits.enums.StatutDevis;
import com.mproduits.enums.StatutFacture;
import com.mproduits.exceptions.*;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.*;
import com.mproduits.repositorieCustom.FactureRepositoryCustom;
import com.mproduits.repositories.*;
import com.mproduits.specifications.FactureSpecifications;
import com.mproduits.utiles.PDFGenerator;
import com.mproduits.utiles.PDFGeneratorFacture;
import com.mproduits.utiles.PDFGeneratorProfessionnel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service de gestion des factures
 * 
 * RESPONSABILITÉS:
 * - Création de factures (directe ou depuis devis)
 * - Validation de factures (sortie de stock)
 * - Annulation de factures (réintégration de stock)
 * - Gestion du cycle de vie complet
 * - Calculs automatiques
 * - Notifications automatiques
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
   private final  FactureRepositoryCustom factureRepositoryCustom;
    private final FactureItemRepository factureItemRepository;
    private final ClientRepository clientRepository;
    private final DevisRepository devisRepository;
    private final ProduitRepositories produitRepository;
    private final PointVenteRepositories pointVenteRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificationService notificationService;
    private final NumeroGeneratorService numeroGeneratorService;
    private final PDFGeneratorProfessionnel pdfGenerator;
    @Autowired
    private MapperDtoImpl mappers;

    /**
     * Crée une nouvelle facture (création directe, sans devis)
     * 
     * PROCESSUS:
     * 1.Valide les données
 2. Crée la facture en statut BROUILLON
 3. Génère le numéro unique
 4. Crée les articles avec snapshot produit
 5. Calcule les montants
 6. Sauvegarde en base
     * 
     * @param request Données de création
     * @param username
     * @return Facture créée
     */
    @Transactional
    public FactureResponse creerFacture(FactureCreateRequest request,String username) {
        log.info("Création d'une nouvelle facture pour le client ID: {}", request.getClientId());
        
        // 1. Validation du client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'ID: " + request.getClientId()));
        
        // 2. Création de la facture
        Facture facture = Facture.builder()
                .client(client)
                .dateFacture(request.getDateFacture() != null ? request.getDateFacture() : new Date())
                .statut(StatutFacture.BROUILLON)
                .modePaiementDefaut(request.getModePaiementDefaut())
                .delaiPaiementJours(request.getDelaiPaiementJours() != null ? request.getDelaiPaiementJours() : 30)
                .remarques(request.getRemarques())
                .conditionsPaiement(request.getConditionsPaiement())
                .referenceExterne(request.getReferenceExterne())
                .usernameCreate(username)
                .build();
        
        // 3. Calcul de la date d'échéance
        if (request.getDateEcheance() != null) {
            facture.setDateEcheance(request.getDateEcheance());
        } else {
            LocalDate dateEcheance = LocalDate.now().plusDays(facture.getDelaiPaiementJours());
            facture.setDateEcheance(Date.from(dateEcheance.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        
        // 4. Sauvegarde initiale pour obtenir l'ID
       // facture = factureRepository.save(facture);
        
        // 5. Génération du numéro unique
        facture.setNumeroFacture(numeroGeneratorService.genererNumeroFacture());
        
        // 6. Création des articles
        for (FactureItemRequest itemRequest : request.getItems()) {
            ajouterArticle(facture, itemRequest);
        }
        
        // 7. Recalcul des montants
        facture.recalculerMontants();
        facture.setSoldeRestant(facture.getTotalTtc());
        
        // 8. Sauvegarde finale
        facture = factureRepository.save(facture);
        
        log.info("Facture créée avec succès: {}", facture.getNumeroFacture());
        return mapToResponse(facture);
    }

    /**
     * Convertit un devis accepté en facture
     * 
     * PROCESSUS:
     * 1. Valide que le devis peut être converti (statut ACCEPTE)
     * 2. Crée la facture en copiant les informations du devis
     * 3. Copie les articles du devis
     * 4. Marque le devis comme CONVERTI
     * 5. Lie la facture au devis
     * 
     * @param request Données de conversion
     * @return Facture créée
     */
    @Transactional
    public FactureResponse convertirDevisEnFacture(DevisToFactureRequest request,String username) {
        log.info("Conversion du devis ID: {} en facture", request.getDevisId());
        
        // 1. Validation du devis
        Devis devis = devisRepository.findById(request.getDevisId())
                .orElseThrow(() -> new ResourceNotFoundException("Devis introuvable avec l'ID: " + request.getDevisId()));
        
        if (!devis.isPeutEtreConverti()) {
            throw new DevisException("Le devis ne peut pas être converti. Statut actuel: " + devis.getStatut());
        }
        
        // 2. Création de la facture depuis le devis
        Facture facture = Facture.builder()
                .client(devis.getClient())
                .devis(devis)
                .dateFacture(request.getDateFacture() != null ? request.getDateFacture() : new Date())
                .statut(StatutFacture.BROUILLON)
                .modePaiementDefaut(request.getModePaiementDefaut())
                .delaiPaiementJours(devis.getValiditeJours())
                .remarques(devis.getRemarques())
                .conditionsPaiement(devis.getConditions())
                .usernameCreate(username)
                .build();
        
        // 3. Calcul de la date d'échéance
        LocalDate dateEcheance = LocalDate.now().plusDays(facture.getDelaiPaiementJours());
        facture.setDateEcheance(Date.from(dateEcheance.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        // 4. Sauvegarde initiale
        facture = factureRepository.save(facture);
        facture.setNumeroFacture(numeroGeneratorService.genererNumeroFacture());
        
        // 5. Copie des articles du devis (si pas de modification demandée)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Utilise les articles personnalisés
            for (FactureItemRequest itemRequest : request.getItems()) {
                ajouterArticle(facture, itemRequest);
            }
        } else {
            // Copie les articles du devis
            for (DevisItem devisItem : devis.getItems()) {
                FactureItem factureItem = FactureItem.builder()
                        .facture(facture)
                        .produit(devisItem.getProduit())
                        .quantite(devisItem.getQuantite())
                        .prixUnitaireHT(devisItem.getPrixUnitaire())
                        .tauxRemise(devisItem.getTauxRemise())
                        .tauxTVA(devisItem.getTauxTVA())
                        .description(devisItem.getDescription())
                        .ordre(devisItem.getOrdre())
                        .build();
                
                factureItem.creerSnapshot();
                factureItem.calculerMontants();
                facture.addItem(factureItem);
            }
        }
        
        // 6. Recalcul des montants
        facture.recalculerMontants();
        facture.setSoldeRestant(facture.getTotalTtc());
        
        // 7. Mise à jour du devis
        devis.setStatut(StatutDevis.CONVERTI);
        devis.setDateConversion(new Date());
        devisRepository.save(devis);
        
        // 8. Sauvegarde finale
        facture = factureRepository.save(facture);
        
        log.info("Devis {} converti en facture {} avec succès", devis.getNumeroDevis(), facture.getNumeroFacture());
        return mapToResponse(facture);
    }

    /**
     * Valide une facture (passage de BROUILLON à NON_PAYEE)
     * 
     * PROCESSUS CRITIQUE:
     * 1. Vérifie que la facture peut être validée
     * 2. Change le statut vers NON_PAYEE
     * 3. SORTIE DE STOCK pour chaque article:
     *    - Diminue PointVente.stockFinalTheorie
     *    - Incrémente PointVente.sortiProduit
     *    - Crée un StockMovement de traçabilité
     * 4. Envoie notification au client
     * 
     * @param request Données de validation
     * @return Facture validée
     */
    @Transactional
    public FactureResponse validerFacture(FactureValidationRequest request,String username) {
        log.info("Validation de la facture ID: {}", request.getFactureId());
        
        // 1. Récupération de la facture
        Facture facture = factureRepository.findById(request.getFactureId())
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'ID: " + request.getFactureId()));
        
        // 2. Vérification du statut
        if (!facture.isPeutEtreValidee()) {
            throw new ErrorResponse("La facture ne peut pas être validée. Statut actuel: " + facture.getStatut());
        }
        
        // 3. Vérification des stocks disponibles
        verifierDisponibiliteStock(facture);
        
        // 4. SORTIE DE STOCK pour chaque article
        for (FactureItem item : facture.getItems()) {
            effectuerSortieStock(facture, item,username);
        }
        
        // 5. Mise à jour de la facture
        facture.setStatut(StatutFacture.NON_PAYEE);
        facture.setDateValidation(request.getDateValidation() != null ? request.getDateValidation() : new Date());
        facture.setUsernameValidation(username);
        
        facture = factureRepository.save(facture);
        
        // 6. Envoi de notification au client
        try {
            notificationService.notifierCreationFacture(facture.getId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification pour la facture {}: {}", 
                     facture.getNumeroFacture(), e.getMessage());
            // Ne pas bloquer la validation pour un échec de notification
        }
        
        log.info("Facture {} validée avec succès", facture.getNumeroFacture());
        return mapToResponse(facture);
    }

    /**
     * Annule une facture validée
     * 
     * PROCESSUS CRITIQUE:
     * 1. Vérifie que la facture peut être annulée
     * 2. RÉINTÉGRATION DU STOCK pour chaque article:
     *    - Augmente PointVente.stockFinalTheorie
     *    - Incrémente PointVente.entreeProduit
     *    - Crée un StockMovement de traçabilité
     * 3. Change le statut vers ANNULEE
     * 4. Envoie notification au client
     * 
     * @param request Données d'annulation
     * @return Facture annulée
     */
    @Transactional
    public FactureResponse annulerFacture(FactureAnnulationRequest request,String username) {
        log.info("Annulation de la facture ID: {}", request.getFactureId());
        
        // 1. Récupération de la facture
        Facture facture = factureRepository.findById(request.getFactureId())
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'ID: " + request.getFactureId()));
        
        // 2. Vérification du statut
        if (!facture.isPeutEtreAnnulee()) {
            throw new ErrorResponse("La facture ne peut pas être annulée. Statut actuel: " + facture.getStatut());
        }
        
        // 3. Vérification des paiements
        if (facture.getMontantPaye().compareTo(BigDecimal.ZERO) > 0) {
            throw new ErrorResponse("Impossible d'annuler une facture avec des paiements. Montant payé: " + 
                                       facture.getMontantPaye());
        }
        
        // 4. RÉINTÉGRATION DU STOCK (si la facture était validée)
        if (facture.getStatut() != StatutFacture.BROUILLON) {
            for (FactureItem item : facture.getItems()) {
                effectuerEntreeStock(facture, item, "Annulation facture",username);
            }
        }
        
        // 5. Mise à jour de la facture
        facture.setStatut(StatutFacture.ANNULEE);
        facture.setDateAnnulation(new Date());
        facture.setMotifAnnulation(request.getMotifAnnulation());
        facture.setUsernameAnnulation(username);
        
        facture = factureRepository.save(facture);
        
        // 6. Envoi de notification au client
        try {
            notificationService.notifierAnnulationFacture(facture.getId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification d'annulation pour la facture {}: {}", 
                     facture.getNumeroFacture(), e.getMessage());
        }
        
        log.info("Facture {} annulée avec succès", facture.getNumeroFacture());
        return mapToResponse(facture);
    }

    /**
     * Met à jour une facture (uniquement si BROUILLON)
     */
    @Transactional
    public FactureResponse modifierFacture(FactureUpdateRequest request,String username) {
        log.info("Modification de la facture ID: {}", request.getId());
        
        Facture facture = factureRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'ID: " + request.getId()));
        
        if (!facture.isModifiable()) {
            throw new ErrorResponse("La facture ne peut plus être modifiée. Statut: " + facture.getStatut());
        }
        
        // Mise à jour des champs modifiables
        if (request.getDateFacture() != null) {
            facture.setDateFacture(request.getDateFacture());
        }
        if (request.getDateEcheance() != null) {
            facture.setDateEcheance(request.getDateEcheance());
        }
        if (request.getModePaiementDefaut() != null) {
            facture.setModePaiementDefaut(request.getModePaiementDefaut());
        }
        if (request.getDelaiPaiementJours() != null) {
            facture.setDelaiPaiementJours(request.getDelaiPaiementJours());
        }
        if (request.getRemarques() != null) {
            facture.setRemarques(request.getRemarques());
        }
        if (request.getConditionsPaiement() != null) {
            facture.setConditionsPaiement(request.getConditionsPaiement());
        }
        if (request.getReferenceExterne() != null) {
            facture.setReferenceExterne(request.getReferenceExterne());
        }
        
        // Mise à jour des articles si fournis
        if (request.getItems() != null) {
            // Suppression des anciens articles
            facture.getItems().clear();
            factureItemRepository.flush();
            
            // Ajout des nouveaux articles
            for (FactureItemRequest itemRequest : request.getItems()) {
                ajouterArticle(facture, itemRequest);
            }
        }
        
        // Recalcul des montants
        facture.recalculerMontants();
        facture.setUsernameUpdate( username);
        
        facture = factureRepository.save(facture);
        
        log.info("Facture {} modifiée avec succès", facture.getNumeroFacture());
        return mapToResponse(facture);
    }

    /**
     * Récupère une facture par son ID
     */
    @Transactional(readOnly = true)
    public FactureResponse getFacture(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'ID: " + id));
        return mapToResponse(facture);
    }

    /**
     * Récupère une facture par son numéro
     */
    @Transactional(readOnly = true)
    public FactureResponse getFactureParNumero(String numeroFacture) {
        Facture facture = factureRepository.findByNumeroFacture(numeroFacture)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec le numéro: " + numeroFacture));
        return mapToResponse(facture);
    }

    /**
     * Liste les factures avec filtres et pagination
     */
    @Transactional(readOnly = true)
    public Page<FactureSummary> listerFactures(FactureSearchCriteria criteria) {
        Pageable pageable = createPageable(criteria);
        
        // Utilisation de specifications pour filtrage dynamique
        Page<Facture> factures = factureRepository.findAll(
                FactureSpecifications.withCriteria(criteria), 
                pageable
        );
        
        return factures.map(this::mapToSummary);
    }

    /**
     * Récupère les factures d'un client
     */
    @Transactional(readOnly = true)
    public List<FactureSummary> getFacturesClient(Long clientId) {
        List<Facture> factures = factureRepository.findByClientIdOrderByDateFactureDesc(clientId);
        return factures.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques de facturation
     */
    @Transactional(readOnly = true)
    public FactureStatistiques getStatistiques(Date dateDebut, Date dateFin) {
        return factureRepositoryCustom.calculerStatistiques(dateDebut, dateFin);
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Ajoute un article à une facture
     */
    private void ajouterArticle(Facture facture, FactureItemRequest request) {
        Produit produit = produitRepository.findById(request.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'ID: " + request.getProduitId()));
        
        FactureItem item = FactureItem.builder()
                .facture(facture)
                .produit(produit)
                .quantite(request.getQuantite())
                .prixUnitaireHT(request.getPrixUnitaireHT())
                .tauxRemise(request.getTauxRemise() != null ? request.getTauxRemise() : BigDecimal.ZERO)
                .typeRemise(request.getTypeRemise() != null ? request.getTypeRemise() : "POURCENTAGE")
                .tauxTVA(request.getTauxTVA())
                .description(request.getDescription())
                .commentaire(request.getCommentaire())
                .ordre(request.getOrdre())
                .build();
        
        item.creerSnapshot();
        item.calculerMontants();
        facture.addItem(item);
    }

    /**
     * Vérifie la disponibilité du stock pour tous les articles
     */
    private void verifierDisponibiliteStock(Facture facture) {
        for (FactureItem item : facture.getItems()) {
            PointVente pointVente = pointVenteRepository.findByProduitId(item.getProduit().getId())
                    .orElseThrow(() -> new ErrorResponse("Stock introuvable pour le produit: " + 
                                                            item.getProduitLibelleSnapshot()));
            
            BigDecimal stockDisponible = pointVente.getStockFinalTheorie();
            BigDecimal quantiteDemandee = new BigDecimal(item.getQuantite());
            
            if (stockDisponible.compareTo(quantiteDemandee) < 0) {
                throw new InsufficientStockException(
                    String.format("Stock insuffisant pour le produit '%s'. Disponible: %s, Demandé: %s",
                                 item.getProduitLibelleSnapshot(),
                                 stockDisponible,
                                 quantiteDemandee));
            }
        }
    }

    /**
     * Effectue la sortie de stock pour un article
     * - Diminue stockFinalTheorie
     * - Incrémente sortiProduit
     * - Crée un StockMovement
     */
    private void effectuerSortieStock(Facture facture, FactureItem item,String username) {
        PointVente pointVente = pointVenteRepository.findByProduitId(item.getProduit().getId())
                .orElseThrow(() -> new ErrorResponse("Stock introuvable pour le produit: " + 
                                                        item.getProduitLibelleSnapshot()));
        
        BigDecimal quantite = new BigDecimal(item.getQuantite());
        BigDecimal stockAvant = pointVente.getStockFinalTheorie();
        
        // Diminution du stock
        pointVente.setStockFinalTheorie(stockAvant.subtract(quantite));
        
        // Incrémentation des sorties
        BigDecimal sorties = pointVente.getSortiProduit() != null ? 
                           pointVente.getSortiProduit() : BigDecimal.ZERO;
        pointVente.setSortiProduit(sorties.add(quantite));
        
        pointVenteRepository.save(pointVente);
        
        // Création du mouvement de stock
        StockMovement movement = StockMovement.builder()
                .typeMouvement(MovementType.SORTIE_FACTURE)
                .produit(item.getProduit())
                .pointVente(pointVente)
                .facture(facture)
                .quantite( quantite.negate())
                .stockAvant(stockAvant)
               
                .motif("Sortie stock facture " + facture.getNumeroFacture())
                .reference(facture.getNumeroFacture())
                .usernameCreate( username)
                .build();
        
        stockMovementRepository.save(movement);
        
        log.debug("Sortie de stock effectuée: Produit={}, Quantité={}, Stock restant={}", 
                 item.getProduitLibelleSnapshot(), quantite, pointVente.getStockFinalTheorie());
    }

    /**
     * Effectue l'entrée de stock (annulation facture)
     * - Augmente stockFinalTheorie
     * - Incrémente entreeProduit
     * - Crée un StockMovement
     */
    private void effectuerEntreeStock(Facture facture, FactureItem item, String motif,String username) {
        PointVente pointVente = pointVenteRepository.findByProduitId(item.getProduit().getId())
                .orElseThrow(() -> new BadRequestException("Stock introuvable pour le produit: " + 
                                                        item.getProduitLibelleSnapshot()));
        
        BigDecimal quantite = new BigDecimal(item.getQuantite());
        BigDecimal stockAvant = pointVente.getStockFinalTheorie();
        
        // Augmentation du stock
        pointVente.setStockFinalTheorie(stockAvant.add(quantite));
        
        // Incrémentation des entrées
        BigDecimal entrees = pointVente.getEntreeProduit() != null ? 
                           pointVente.getEntreeProduit() : BigDecimal.ZERO;
        pointVente.setEntreeProduit(entrees.add(quantite));
        
        pointVenteRepository.save(pointVente);
        
        // Création du mouvement de stock
        StockMovement movement = StockMovement.builder()
                .typeMouvement(MovementType.ENTREE_ANNULATION)
                .produit(item.getProduit())
                .pointVente(pointVente)
                .facture(facture)
                .quantite(quantite)
                .stockAvant(stockAvant)
               
                .motif(motif + " - Facture " + facture.getNumeroFacture())
                .reference(facture.getNumeroFacture())
                .usernameCreate( username)
                .build();
        
        stockMovementRepository.save(movement);
        
        log.debug("Entrée de stock effectuée: Produit={}, Quantité={}, Stock actuel={}", 
                 item.getProduitLibelleSnapshot(), quantite, pointVente.getStockFinalTheorie());
    }

    /**
     * Récupère le nom d'utilisateur courant
     */
//    private String getCurrentUsername() {
//        try {
//            return SecurityContextHolder.getContext().getAuthentication().getName();
//        } catch (Exception e) {
//            return "SYSTEM";
//        }
//    }

    /**
     * Crée un Pageable à partir des critères de recherche
     */
    private Pageable createPageable(FactureSearchCriteria criteria) {
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "dateFacture";
        Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortDirection()) ? 
                                  Sort.Direction.ASC : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /**
     * Convertit une entité Facture en FactureResponse
     */
    private FactureResponse mapToResponse(Facture facture) {
        // Mapping détaillé
        // (Code de mapping DTO omis pour la brièveté)
        return FactureResponse.builder()
                .id(facture.getId())
                .numeroFacture(facture.getNumeroFacture())
                .dateFacture(facture.getDateFacture())
                .client(mappers.mapperClientByClientDto(facture.getClient()))
                .dateEcheance(facture.getDateEcheance())
                .delaiPaiementJours(facture.getDelaiPaiementJours())
                .statut(facture.getStatut())
                .usernameCreate(facture.getUsernameCreate())
                .totalHt(facture.getTotalHt())
                .totalTVA(facture.getTotalTVA())
                .totalTtc(facture.getTotalTtc())
                .montantPaye(facture.getMontantPaye())
                .dateCreation(facture.getDateCreation())
                .soldeRestant(facture.getSoldeRestant())
                .items(facture.getItems().stream().map(item -> mapToFactureItemResponse(item)).toList())
                
                // ... autres champs
                .build();
    }

    /**
     * Convertit une entité Facture en FactureSummary
     */
    private FactureSummary mapToSummary(Facture facture) {
        return FactureSummary.builder()
                .id(facture.getId())
                .numeroFacture(facture.getNumeroFacture())
                .dateFacture(facture.getDateFacture())
                .totalTtc(facture.getTotalTtc())
                .statut(facture.getStatut())
                .client(mappers.mapperClientByClientDto(facture.getClient()))
               
                .dateEcheance(facture.getDateEcheance())
                .enRetard(facture.isEnRetard())
                .joursRetard(facture.getJoursRetard())
                .montantPaye(facture.getMontantPaye())
                .nombreArticles(facture.getItems().size())
                .soldeRestant(facture.getSoldeRestant())
                 
                
                // ... autres champs
                .build();
    }
    private FactureItemResponse mapToFactureItemResponse(FactureItem factureItem){
        return FactureItemResponse.builder()
                .id(factureItem.getId())
          
                .commentaire(factureItem.getCommentaire())
                .description(factureItem.getDescription())
                .montantHT(factureItem.getMontantHT())
                .montantRemise(factureItem.getTauxRemise())
                .montantTTC(factureItem.getMontantTTC())
                .montantTVA(factureItem.getMontantTVA())
                .tauxTVA(factureItem.getTauxTVA())
                .tauxRemise(factureItem.getTauxRemise())
               
                .prixUnitaireHT(factureItem.getPrixUnitaireHT())
                .prixUnitaireTTC(factureItem.getPrixUnitaireTTC())
                .produit(mappers.mapperProduitDto(factureItem.getProduit()))
                .produitCode(factureItem.getProduit().getReference())
                .produitId(factureItem.getProduit().getId())
                .produitLibelle(factureItem.getProduit().getLibelle())
                .quantite(factureItem.getQuantite())
                
         
                .build();
        
        
    }
    
    @Transactional(readOnly = true)
public byte[] genererPDF(Long id) {
    Facture facture = factureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
    return pdfGenerator.genererFacturePDF(facture);
}
}