///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.mproduits.services;
//import com.mproduits.dto.*;
//import com.mproduits.enums.StatutDevis;
//import com.mproduits.exceptions.*;
//import com.mproduits.model.*;
//import com.mproduits.repositories.ClientRepository;
//import com.mproduits.repositories.DevisRepository;
//import com.mproduits.repositories.FactureRepository;
//import com.mproduits.repositories.PointVenteRepositories;
//import com.mproduits.repositories.PrixArticlesRepositories;
//import com.mproduits.repositories.ProduitRepositories;
//import com.mproduits.repositories.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.time.LocalTime;
///**
// * Service de Facturation - Orchestration des flux de facturation
// * Responsabilités:
// * - Création et validation des factures
// * - Gestion des stocks associés
// * - Historique des mouvements
// * - Audit trail
// */
//@Service
//@Transactional
//@Slf4j
//@RequiredArgsConstructor
//
///**
// * Service de gestion de la facturation
// * Gère la création des factures, le déstockage automatique et le calcul des montants
// */
//
///**
// *
// * @author USER01
// */
//public class FacturationService {
//    
//   
//    private final FactureRepository factureRepo;
//    private final DevisRepository devisRepo;
//    private final ClientRepository clientRepo;
//    private final ProduitRepositories produitRepo;
//    private final PointVenteRepositories pointVenteRepo;
//    private final PrixArticlesRepositories prixArticlesRepo;
//    private final StockMovementRepository stockMovementRepo;
//    private final PrixHistoriqueRepository prixHistoriqueRepo;
//    private final VersementClientRepository versementRepo;
//    private final PaiementAuditRepository paiementAuditRepo;
//    private final FactureItemSnapshotRepository snapshotRepo;
//    
//    private final StockService stockService;
//    private final PrixService prixService;
//    private final NotificationService notificationService;
//    
//    private static final String PREFIX_FACTURE = "FAC";
//    
//    /**
//     * Création d'une facture à partir d'un devis accepté
//     * 
//     * FLUX TRANSACTIONNEL:
//     * 1. Valider devis existe et est accepté
//     * 2. Valider stock disponible pour tous les articles
//     * 3. Créer facture et items
//     * 4. Effectuer destockage (StockMovement)
//     * 5. Créer historique de prix (PrixHistorique)
//     * 6. Générer numéro séquentiel facture
//     * 7. Enregistrer audit
//     * 8. Notifier client
//     * 
//     * @param devisId ID du devis
//     * @param username Utilisateur qui crée la facture
//     * @return Facture créée
//     * @throws MetierException en cas de validation échouée
//     */
//    public Facture creerFactureFromDevis(Long devisId, String username) {
//        log.info("DÉBUT création facture depuis devis {}", devisId);
//        
//        // 1. VALIDATION DEVIS
//        Devis devis = devisRepo.findById(devisId)
//            .orElseThrow(() -> new MetierException("Devis non trouvé: " + devisId));
//        
//        if (!"ACCEPTE".equals(devis.getStatut())) {
//            throw new MetierException("Devis doit être ACCEPTE, état actuel: " + devis.getStatut());
//        }
//        
//        Client client = devis.getClient();
//        if (client == null) {
//            throw new MetierException("Client requis pour le devis");
//        }
//        
//        // 2. VALIDATION STOCK DISPONIBLE
//        List<String> errorsStock = validerStockDisponible(devis.getItems());
//        if (!errorsStock.isEmpty()) {
//            throw new MetierException("Stock insuffisant: " + String.join(", ", errorsStock));
//        }
//        
//        try {
//            // 3. CRÉER FACTURE
//            Facture facture = new Facture();
//            facture.setClient(client);
//            facture.setDevis(devis);
//            facture.setDateFacture(new Date());
//            facture.setStatut("NON_PAYEE");
//            facture.setUsernameCreate(username);
//            facture.setDateCreation(new Date() );
//            
//            // Générer numéro séquentiel
//            facture.setNumeroFacture(genererNumeroFacture());
//            
//            // 4. CRÉER ITEMS ET SNAPSHOTS
//            List<FactureItem> items = new ArrayList<>();
//            BigDecimal totalHT = BigDecimal.ZERO;
//            BigDecimal totalTVA = BigDecimal.ZERO;
//            
//            for (DevisItem devisItem : devis.getItems()) {
//                FactureItem factureItem = creerFactureItemFromDevis(
//                    facture, devisItem, username);
//                items.add(factureItem);
//                
//                // Accumulation totaux
//                totalHT = totalHT.add(factureItem.getSnapshot().getMontantHT());
//                totalTVA = totalTVA.add(
//                    factureItem.getSnapshot().getMontantTTC()
//                        .subtract(factureItem.getSnapshot().getMontantHT())
//                );
//            }
//            
//            facture.setItems(items);
//            facture.setTotalHt(totalHT);
//            facture.setTotalTVA(totalTVA);
//            facture.setTotalTtc(totalHT.add(totalTVA));
//            facture.setMontantDejaPaye(BigDecimal.ZERO);
//            
//            // Persister facture
//            facture = factureRepo.save(facture);
//            log.info("Facture créée: {}", facture.getNumeroFacture());
//            
//            // 5. EFFECTUER DESTOCKAGE TRANSACTIONNEL
//            effectuerDestockage(facture, username);
//            
//            // 6. METTRE À JOUR STATUT DEVIS
//            devis.setStatut(StatutDevis.CONVERTI);
//            devis.setFacture(facture);
//            devisRepo.save(devis);
//            
//            // 7. AUDIT ET NOTIFICATION
//            log.info("Facture {} finalisée - Client: {}", 
//                facture.getNumeroFacture(), client.getNom());
//            notificationService.notifierFacturationCreee(facture);
//            
//            return facture;
//            
//        } catch (Exception e) {
//            log.error("ERREUR création facture: {}", e.getMessage(), e);
//            throw new MetierException("Erreur lors de la création: " + e.getMessage(), e);
//        }
//    }
//    
//    /**
//     * Effectue le destockage des articles facturés
//     * Crée un StockMovement pour chaque article
//     * 
//     * IMPORTANT: Cette méthode doit être appelée dans la transaction
//     * de création de facture pour atomicité
//     */
//    private void effectuerDestockage(Facture facture, String username) {
//        log.info("DÉBUT destockage facture {}", facture.getNumeroFacture());
//        
//        for (FactureItem item : facture.getItems()) {
//            Produit produit = item.getProduit();
//            Integer quantite = item.getQuantite();
//            
//            // Trouver le point de vente par défaut du client
//            PointVente pointVente = determinerPointVente(facture.getClient());
//            if (pointVente == null) {
//                throw new MetierException(
//                    "Impossible de déterminer le point de vente pour destockage");
//            }
//            
//            // Effectuer le destockage via StockService (transactionnel)
//            try {
//                stockService.diminuerStock(
//                    produit.getId(),
//                    pointVente.getId(),
//                    new BigDecimal(quantite),
//                    StockMovement.MovementType.SORTIE_VENTE,
//                    "Facture " + facture.getNumeroFacture(),
//                    username
//                );
//                
//                // Créer mouvement stock avec référence facture
//                StockMovement movement = new StockMovement();
//                movement.setType(StockMovement.MovementType.SORTIE_VENTE);
//                movement.setQuantite(new BigDecimal(quantite));
//                movement.setProduit(produit);
//                movement.setPointVente(pointVente);
//                movement.setFacture(facture);
//                movement.setMotif("Facturation " + facture.getNumeroFacture());
//                movement.setUsernameCreate(username);
//                movement.setDateCreation(LocalDateTime.now());
//                
//                stockMovementRepo.save(movement);
//                
//                log.info("Destockage: {} x {} de {}", 
//                    quantite, produit.getCode(), pointVente.getId());
//                    
//            } catch (Exception e) {
//                log.error("ERREUR destockage produit {}: {}", produit.getId(), e.getMessage());
//                throw new MetierException("Destockage échoué pour " + produit.getCode());
//            }
//        }
//        
//        log.info("FIN destockage facture {}", facture.getNumeroFacture());
//    }
//    
//    /**
//     * Crée un FactureItem avec snapshot de prix
//     */
//    private FactureItem creerFactureItemFromDevis(
//        Facture facture, DevisItem devisItem, String username) {
//        
//        Produit produit = devisItem.getProduit()!= null ? 
//            produitRepo.findById(devisItem.getProduit().getId())
//                .orElseThrow(() -> new MetierException("Produit non trouvé")) : null;
//        
//        if (produit == null) {
//            throw new MetierException("Produit requis pour item devis");
//        }
//        
//        // Créer snapshot des prix courants
//        FactureItemSnapshot snapshot = new FactureItemSnapshot();
//        snapshot.setFacture(facture);
//        snapshot.setProduit(produit);
//        snapshot.setQuantite(devisItem.getQuantite());
//        snapshot.setPrixUnitaireHT(devisItem.getPrixUnitaire());
//        snapshot.setRemisePercent(devisItem.getMontantRemise());
//        
//        // Récupérer TVA du produit/prixArticles
//        PrixArticles prixArticles = prixArticlesRepo.findCurrentPrice(produit.getId())
//            .orElse(null);
//        
//        BigDecimal tauxTVA = prixArticles != null ? prixArticles.getTva() : BigDecimal.ZERO;
//        snapshot.setTauxTVA(tauxTVA);
//        
//        // Calculer prix TTC
//        BigDecimal prixTTC = devisItem.getPrixUnitaire()
//            .multiply(BigDecimal.ONE.add(tauxTVA.divide(
//                new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP)))
//            .setScale(2, java.math.RoundingMode.HALF_UP);
//        snapshot.setPrixUnitaireTTC(prixTTC);
//        
//        // Montants lignes
//        BigDecimal montantHT = devisItem.getPrixUnitaire()
//            .multiply(new BigDecimal(devisItem.getQuantite()))
//            .setScale(2, java.math.RoundingMode.HALF_UP);
//        snapshot.setMontantHT(montantHT);
//        
//        BigDecimal montantTTC = prixTTC
//            .multiply(new BigDecimal(devisItem.getQuantite()))
//            .setScale(2, java.math.RoundingMode.HALF_UP);
//        snapshot.setMontantTTC(montantTTC);
//        
//        // Snapshots descriptifs
//        snapshot.setProduitCodeSnapshot(produit.getCode());
//        snapshot.setProduitLikelleSnapshot(produit.getLibelle());
//        
//        snapshot = snapshotRepo.save(snapshot);
//        
//        // Créer FactureItem
//        FactureItem item = new FactureItem();
//        item.setFacture(facture);
//        item.setProduit(produit);
//        item.setQuantite(devisItem.getQuantite());
//        item.setPrixUnitaire(devisItem.getPrixUnitaire());
//        item.setRemisePercent(devisItem.getMontantRemise());
//        item.setSnapshot(snapshot);
//        
//        return item;
//    }
//    
//    /**
//     * Valide que le stock est disponible pour tous les articles
//     */
//    private List<String> validerStockDisponible(List<DevisItem> items) {
//        List<String> errors = new ArrayList<>();
//        
//        for (DevisItem item : items) {
//            Produit produit = produitRepo.findById(item.getProduit().getId()).orElse(null);
//            if (produit == null) {
//                errors.add("Produit introuvable: " + item.getProduit().getId());
//                continue;
//            }
//            
//            // Récupérer stock total pour ce produit
//            BigDecimal stockTotal = stockService.getStockTotal(produit.getId());
//            
//            if (stockTotal.compareTo(new BigDecimal(item.getQuantite())) < 0) {
//                errors.add(String.format(
//                    "%s: stock insuffisant (demandé: %d, disponible: %s)",
//                    produit.getCode(), item.getQuantite(), stockTotal));
//            }
//        }
//        
//        return errors;
//    }
//    
//    /**
//     * Génère un numéro séquentiel de facture: FAC-YYYY-XXXXXX
//     */
//    private String genererNumeroFacture() {
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//        long count = factureRepo.count() + 1;
//        return String.format("%s-%d-%06d", PREFIX_FACTURE, year, count);
//    }
//    
//    /**
//     * Détermine le point de vente par défaut pour un client
//     */
//    private PointVente determinerPointVente(Client client) {
//        // Logique: premier point de vente, ou par défaut
//        return pointVenteRepo.findAll().stream().findFirst().orElse(null);
//    }
//    
//    /**
//     * Enregistre un versement client avec audit
//     * 
//     * Mettre à jour le statut facture:
//     * - PAYEE si montantDejaPaye >= totalTTC
//     * - PARTIELLE si 0 < montantDejaPaye < totalTTC
//     * - NON_PAYEE si montantDejaPaye = 0
//     */
//    @Transactional
//    public VersementClient enregistrerVersement(
//        VersementClientDTO dto, String username) {
//        
//        log.info("DÉBUT enregistrement versement facture {}", dto.getFactureId());
//        
//        Facture facture = factureRepo.findById(dto.getFactureId())
//            .orElseThrow(() -> new MetierException("Facture non trouvée"));
//        
//        // Valider montant
//        BigDecimal montantDejaPaye = facture.getMontantDejaPaye() != null ? 
//            facture.getMontantDejaPaye() : BigDecimal.ZERO;
//        BigDecimal soldeRestant = facture.getTotalTtc().subtract(montantDejaPaye);
//        
//        if (dto.getMontant().compareTo(soldeRestant) > 0) {
//            throw new MetierException(
//                String.format("Montant versement (%.2f) > solde restant (%.2f)",
//                    dto.getMontant(), soldeRestant));
//        }
//        
//        // Créer versement
//        VersementClient versement = new VersementClient();
//        versement.setClient(facture.getClient());
//        versement.setFacture(facture);
//        versement.setDate(new Date());
//        versement.setMontant(dto.getMontant());
//        versement.setModePaiement(dto.getModePaiement());
//        versement.setReferencePaiement(dto.getReferencePaiement());
//        
//        versement = versementRepo.save(versement);
//        
//        // Créer audit
//        PaiementAudit audit = new PaiementAudit();
//        audit.setVersement(versement);
//        audit.setFacture(facture);
//        audit.setClient(facture.getClient());
//        audit.setMontantVersement(dto.getMontant());
//        audit.setMontantFacture(facture.getTotalTtc());
//        audit.setSoldeRestant(soldeRestant.subtract(dto.getMontant()));
//        audit.setModePaiement(dto.getModePaiement());
//        audit.setStatut("VALIDE");
//        audit.setUsernameCreate(username);
//        
//        paiementAuditRepo.save(audit);
//        
//        // Mettre à jour facture
//        facture.setMontantDejaPaye(montantDejaPaye.add(dto.getMontant()));
//        
//        BigDecimal nouveauSolde = facture.getTotalTtc()
//            .subtract(facture.getMontantDejaPaye());
//        
//        if (nouveauSolde.compareTo(BigDecimal.ZERO) <= 0) {
//            facture.setStatut("PAYEE");
//        } else {
//            facture.setStatut("PARTIELLE");
//        }
//        
//        facture = factureRepo.save(facture);
//        
//        log.info("Versement enregistré: {} - Nouveau statut: {}", 
//            versement.getId(), facture.getStatut());
//        
//        notificationService.notifierVersementEnregistre(versement, facture);
//        
//        return versement;
//    }
//}
