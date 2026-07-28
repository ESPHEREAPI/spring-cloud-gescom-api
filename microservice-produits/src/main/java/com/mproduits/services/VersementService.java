package com.mproduits.services;

import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.mproduits.dto.ClientDto;
import com.mproduits.dto.ClientSummary;
import com.mproduits.dto.FactureSummary;
import com.mproduits.dto.HistoriquePaiementClient;
import com.mproduits.dto.RapportClientData;
import com.mproduits.dto.RapportEmailRequest;
import com.mproduits.dto.RecuPaiementDTO;
import com.mproduits.dto.VersementAnnulationRequest;
import com.mproduits.dto.VersementCreateRequest;
import com.mproduits.dto.VersementFacture;
import com.mproduits.dto.VersementMultipleRequest;
import com.mproduits.dto.VersementResponse;
import com.mproduits.dto.VersementSearchCriteria;
import com.mproduits.dto.VersementStatistiques;
import com.mproduits.dto.VersementSummary;
import com.mproduits.dto.VersementValidationRequest;
import com.mproduits.enums.StatutVersement;
import java.time.LocalDateTime;
import java.time.LocalDate;
//import com.mproduits.enums.StatutFacture;

//import com.mproduits.enums.StatutFacture;
import com.mproduits.exceptions.*;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.*;
import com.mproduits.repositories.ClientRepository;
import com.mproduits.repositories.FactureRepository;
import com.mproduits.repositories.VersementClientRepository;
import com.mproduits.specifications.VersementSpecifications;
import com.mproduits.utiles.PDFGenerator;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Service de gestion des versements clients
 *
 * RESPONSABILITÉS: - Enregistrement des paiements clients - Validation des
 * versements - Mise à jour automatique des statuts de factures - Génération de
 * reçus de paiement - Annulation de versements - Calcul des statistiques de
 * paiement
 *
 * RÈGLES MÉTIER: - Un versement ne peut pas dépasser le solde de la facture -
 * La validation d'un versement met à jour le solde de la facture - Le statut de
 * la facture est automatiquement mis à jour (PARTIELLE/PAYEE) - Une
 * notification est envoyée au client après chaque paiement - Un reçu de
 * paiement est généré automatiquement
 *
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VersementService {
    
    private final VersementClientRepository versementRepository;
    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;
    private final NumeroGeneratorService numeroGeneratorService;
    private final RecuPaiementService recuPaiementService;
    private final HistoriquePaiementService historiquePaiementService;
    private final VersementStatistiqueService versementStatistiqueService;
    private final PDFGenerator pdfGenerator;
    //private final JavaMailSender mailSender;
    
    @Autowired
    private MapperDtoImpl mappers;

    /**
     * Crée un nouveau versement pour une facture
     *
     * PROCESSUS: 1. Valide la facture et le client 2. Vérifie que le montant ne
     * dépasse pas le solde 3. Vérifie la référence de paiement si obligatoire
     * 4. Crée le versement en statut EN_ATTENTE 5. Génère le numéro unique
     *
     * @param request Données du versement
     * @return Versement créé
     */
    @Transactional
    public VersementResponse creerVersement(VersementCreateRequest request, String username) {
        log.info("Création d'un versement pour la facture ID: {}", request.getFactureId());

        // 1. Validation de la facture
        Facture facture = factureRepository.findById(request.getFactureId())
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable avec l'ID: " + request.getFactureId()));
        
        if (!facture.isPeutRecevoirPaiement()) {
            throw new ErrorResponse("La facture ne peut pas recevoir de paiement. Statut: " + facture.getStatut());
        }

        // 2. Vérification du montant
        if (request.getMontant().compareTo(facture.getSoldeRestant()) > 0) {
            throw new ErrorResponse(
                    String.format("Le montant du versement (%s) dépasse le solde de la facture (%s)",
                            request.getMontant(), facture.getSoldeRestant()));
        }

        // 3. Vérification de la référence si obligatoire
        if (request.getModePaiement().isReferenceObligatoire()
                && (request.getReferencePaiement() == null || request.getReferencePaiement().trim().isEmpty())) {
            throw new ErrorResponse("La référence de paiement est obligatoire pour le mode: "
                    + request.getModePaiement().getLibelle());
        }

        // 4. Création du versement
        VersementClient versement = VersementClient.builder()
                .facture(facture)
                .client(facture.getClient())
                .montant(request.getMontant())
                .dateVersement(request.getDateVersement() != null ? request.getDateVersement() : new Date())
                .modePaiement(request.getModePaiement())
                .referencePaiement(request.getReferencePaiement())
                .banque(request.getBanque())
                .numeroCompte(request.getNumeroCompte())
                .remarques(request.getRemarques())
                .statut("EN_ATTENTE")
                .usernameCreate(username)
                .build();

        // 5. Sauvegarde et génération du numéro
        versement = versementRepository.save(versement);
        versement.setNumeroVersement(numeroGeneratorService.genererNumeroVersement());
        versement = versementRepository.save(versement);
        
        log.info("Versement créé avec succès: {}", versement.getNumeroVersement());
        return mapToResponse(versement);
    }

    /**
     * Valide un versement
     *
     * PROCESSUS CRITIQUE: 1. Change le statut du versement vers VALIDE 2. Met à
     * jour le montant payé de la facture 3. Recalcule le solde restant 4. Met à
     * jour le statut de la facture (PARTIELLE ou PAYEE) 5. Génère un reçu de
     * paiement 6. Envoie notification au client
     *
     * @param request Données de validation
     * @return Versement validé
     */
    @Transactional
    public VersementResponse validerVersement(VersementValidationRequest request, String username) {
        log.info("Validation du versement ID: {}", request.getVersementId());

        // 1. Récupération du versement
        VersementClient versement = versementRepository.findById(request.getVersementId())
                .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable avec l'ID: " + request.getVersementId()));

        // 2. Vérification du statut
        if (!versement.isPeutEtreValide()) {
            throw new ErrorResponse("Le versement ne peut pas être validé. Statut: " + versement.getStatut());
        }

        // 3. Mise à jour du versement
        versement.setStatut("VALIDE");
        versement.setDateValidation(request.getDateValidation() != null ? request.getDateValidation() : new Date());
        versement.setUsernameValidation(username);
        
        versement = versementRepository.save(versement);

        // 4. Mise à jour de la facture
        Facture facture = versement.getFacture();
        facture.recalculerMontantPaye();
        facture.mettreAJourStatut();
        factureRepository.save(facture);

        // 5. Génération du reçu de paiement
        try {
            //String recuPath = recuPaiementService.genererRecu(versement.getId());
            // versement.setRecuPaiement(recuPath);
            versement = versementRepository.save(versement);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du reçu pour le versement {}: {}",
                    versement.getNumeroVersement(), e.getMessage());
        }

        // 6. Notification client
        try {
            notificationService.notifierPaiementRecu(versement.getId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de paiement pour le versement {}: {}",
                    versement.getNumeroVersement(), e.getMessage());
        }
        
        log.info("Versement {} validé avec succès. Nouveau solde facture: {}",
                versement.getNumeroVersement(), facture.getSoldeRestant());
        return mapToResponse(versement);
    }

    /**
     * Annule un versement validé
     *
     * PROCESSUS: 1. Vérifie que le versement peut être annulé 2. Change le
     * statut vers ANNULE 3. Remet à jour le montant payé de la facture 4. Remet
     * à jour le statut de la facture 5. Envoie notification
     *
     * @param request Données d'annulation
     * @return Versement annulé
     */
    /**
     * Exporte le rapport en Excel
     */
    @Transactional(readOnly = true)
    public byte[] exporterRapportExcel(Long clientId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        log.info("Export Excel du rapport pour le client {} entre {} et {}", clientId, dateDebut, dateFin);

        // Récupérer les versements
        List<VersementClient> versements = versementRepository.findByClientIdAndDateVersementBetweenOrderByDateVersementDesc(clientId, dateDebut, dateFin);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Versements");

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "N° Versement", "N° Facture", "Mode Paiement",
                "Référence", "Montant", "Statut"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            int rowNum = 1;
            for (VersementClient versement : versements) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(versement.getDateVersement().toString());
                row.createCell(1).setCellValue(versement.getNumeroVersement());
                row.createCell(2).setCellValue(versement.getFacture().getNumeroFacture());
                row.createCell(3).setCellValue(versement.getModePaiement().name());
                row.createCell(4).setCellValue(versement.getReferencePaiement() != null
                        ? versement.getReferencePaiement() : "");
                row.createCell(5).setCellValue(versement.getMontant().doubleValue());
                row.createCell(6).setCellValue(versement.getStatut());
            }

            // Auto-size des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir en byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Erreur lors de la génération du fichier Excel", e);
            throw new ErrorResponse("Erreur lors de la génération du fichier Excel");
        } catch (java.io.IOException ex) {
            Logger.getLogger(VersementService.class.getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse("Erreur lors de la génération du fichier Excel");
        }
    }

    /**
     * Récupère les statistiques globales
     */
    @Transactional(readOnly = true)
    public VersementStatistiques getStatistiquesGlobales(LocalDate dateDebut, LocalDate dateFin) {
        // TODO: Implémenter selon vos besoins
        return new VersementStatistiques();
    }

    /**
     * Génère un rapport client en PDF
     */
    @Transactional(readOnly = true)
    public byte[] genererRapportClientPDF(Long clientId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        log.info("Génération du rapport PDF pour le client {} entre {} et {}", clientId, dateDebut, dateFin);

        // Récupérer les versements du client pour la période
        List<VersementClient> versements = versementRepository
                .findByClientIdAndDateVersementBetweenOrderByDateVersementDesc(
                        clientId, dateDebut, dateFin);

        // Récupérer les factures associées
        List<Facture> factures = versements.stream()
                .map(VersementClient::getFacture)
                .distinct()
                .collect(Collectors.toList());

        // Construire les données du rapport
        RapportClientData rapportData = construireRapportData(clientId, versements, factures, dateDebut, dateFin);

        // Générer le PDF
        return pdfGenerator.genererRapportClient(rapportData);
    }

    @Transactional
    public VersementResponse annulerVersement(VersementAnnulationRequest request) {
        log.info("Annulation du versement ID: {}", request.getVersementId());

        // 1. Récupération du versement
        VersementClient versement = versementRepository.findById(request.getVersementId())
                .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable avec l'ID: " + request.getVersementId()));

        // 2. Vérification du statut
        if (!versement.isPeutEtreAnnule()) {
            throw new ErrorResponse("Le versement ne peut pas être annulé. Statut: " + versement.getStatut());
        }

        // 3. Mise à jour du versement
        versement.setStatut("ANNULE");
        versement.setDateAnnulation(new Date());
        versement.setMotifAnnulation(request.getMotifAnnulation());
        versement.setUsernameAnnulation(request.getUsername());
        
        versement = versementRepository.save(versement);

        // 4. Mise à jour de la facture
        Facture facture = versement.getFacture();
        facture.recalculerMontantPaye();
        facture.mettreAJourStatut();
        factureRepository.save(facture);
        
        log.info("Versement {} annulé avec succès", versement.getNumeroVersement());
        return mapToResponse(versement);
    }

    /**
     * Enregistre plusieurs versements pour une facture
     */
    @Transactional
    public List<VersementResponse> creerVersementsMultiples(VersementMultipleRequest request, String username) {
        log.info("Création de {} versements pour la facture ID: {}",
                request.getVersements().size(), request.getFactureId());
        
        return request.getVersements().stream()
                .map(detail -> {
                    VersementCreateRequest createRequest = VersementCreateRequest.builder()
                            .factureId(request.getFactureId())
                            .montant(detail.getMontant())
                            .dateVersement(detail.getDateVersement())
                            .modePaiement(detail.getModePaiement())
                            .referencePaiement(detail.getReferencePaiement())
                            .banque(detail.getBanque())
                            .build();
                    return creerVersement(createRequest, username);
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère un versement par son ID
     */
    @Transactional(readOnly = true)
    public VersementResponse getVersement(Long id) {
        VersementClient versement = versementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable avec l'ID: " + id));
        return mapToResponse(versement);
    }

    /**
     * Récupère un versement par son numéro
     */
    @Transactional(readOnly = true)
    public VersementResponse getVersementParNumero(String numeroVersement) {
        VersementClient versement = versementRepository.findByNumeroVersement(numeroVersement)
                .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable avec le numéro: " + numeroVersement));
        return mapToResponse(versement);
    }

    /**
     * Liste les versements avec filtres et pagination
     */
    @Transactional(readOnly = true)
    public Page<VersementSummary> listerVersements(VersementSearchCriteria criteria) {
        Pageable pageable = createPageable(criteria);
        
        Page<VersementClient> versements = versementRepository.findAll(
                VersementSpecifications.withCriteria(criteria),
                pageable
        );
        
        return versements.map(this::mapToSummary);
    }

    /**
     * Récupère les versements d'une facture
     */
    @Transactional(readOnly = true)
    public List<VersementResponse> getVersementsFacture(Long factureId) {
        List<VersementClient> versements = versementRepository.findByFactureId(factureId);
        return versements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les versements d'un client
     */
    @Transactional(readOnly = true)
    public List<VersementResponse> getVersementsClient(Long clientId) {
        List<VersementClient> versements = versementRepository.findByClientIdOrderByDateVersementDesc(clientId);
        return versements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques de versements
     */
    @Transactional(readOnly = true)
    public VersementStatistiques getStatistiques(Date dateDebut, Date dateFin) {
        return versementStatistiqueService.getStatistiques(dateDebut, dateFin);
    }

    /**
     * Récupère l'historique de paiement d'un client
     */
    @Transactional(readOnly = true)
    public HistoriquePaiementClient getHistoriquePaiementClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'ID: " + clientId));
        
        return historiquePaiementService.getHistoriquePaiementClient(clientId);
    }

    /**
     * Génère un reçu de paiement pour un versement
     */
    @Transactional(readOnly = true)
    public RecuPaiementDTO genererRecuPaiement(Long versementId) {
        VersementClient versement = versementRepository.findById(versementId)
                .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable avec l'ID: " + versementId));
        
        if (!"VALIDE".equals(versement.getStatut())) {
            throw new ErrorResponse("Le reçu ne peut être généré que pour les versements validés");
        }
        
        return recuPaiementService.genererRecu(versement.getId());
    }

    // ========== MÉTHODES PRIVÉES ==========
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
    private Pageable createPageable(VersementSearchCriteria criteria) {
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "dateVersement";
        Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /**
     * Convertit une entité VersementClient en VersementResponse
     */
    private VersementResponse mapToResponse(VersementClient versement) {
        return VersementResponse.builder()
                .id(versement.getId())
                .numeroVersement(versement.getNumeroVersement())
                .dateVersement(versement.getDateVersement())
                .dateEnregistrement(versement.getDateEnregistrement())
                .dateValidation(versement.getDateValidation())
                .montant(versement.getMontant())
                .modePaiement(versement.getModePaiement())
                .modePaiementLibelle(versement.getModePaiement() != null
                        ? versement.getModePaiement().getLibelle() : null)
                .referencePaiement(versement.getReferencePaiement())
                .banque(versement.getBanque())
                .numeroCompte(versement.getNumeroCompte())
                .statut(StatutVersement.valueOf(versement.getStatut()))
                .factureId(versement.getFacture().getId())
                .factureNumero(versement.getFacture().getNumeroFacture())
                .factureTotalTtc(versement.getFacture().getTotalTtc())
                .factureSoldeRestant(versement.getFacture().getSoldeRestant())
                .clientId(versement.getClient().getId())
                .clientNom(versement.getClient().getNom())
                .clientEmail(versement.getClient().getEmail())
                .facture(mapToVersementFacture(versement))
                .remarques(versement.getRemarques())
                .motifAnnulation(versement.getMotifAnnulation())
                .recuPaiement(versement.getRecuPaiement())
                .dateCreation(versement.getDateEnregistrement())
                .usernameCreate(versement.getUsernameCreate())
                .usernameValidation(versement.getUsernameValidation())
                .usernameAnnulation(versement.getUsernameAnnulation())
                .build();
    }

    private VersementFacture mapToVersementFacture(VersementClient vcl) {
        return VersementFacture.builder()
                .id(vcl.getId())
                .montantRestant(BigDecimal.ZERO)
                .montantTTC(vcl.getFacture().getTotalTtc())
                .numeroFacture(vcl.getFacture().getNumeroFacture())
                .montantRestant(vcl.getFacture().getSoldeRestant())
                .build();
    }

    /**
     * Convertit une entité VersementClient en VersementSummary
     */
    private VersementSummary mapToSummary(VersementClient versement) {
        return VersementSummary.builder()
                .id(versement.getId())
                .numeroVersement(versement.getNumeroVersement())
                .dateVersement(versement.getDateVersement())
                .montant(versement.getMontant())
                .modePaiement(versement.getModePaiement())
                .referencePaiement(versement.getReferencePaiement())
                .statut(StatutVersement.valueOf(versement.getStatut()))
                .factureNumero(versement.getFacture().getNumeroFacture())
                .clientNom(versement.getClient().getNom())
                .build();
    }

    /**
     * Construit les données du rapport
     */
    private RapportClientData construireRapportData(
            Long clientId, List<VersementClient> versements, List<Facture> factures,
            LocalDateTime dateDebut, LocalDateTime dateFin) {

        // Calculer les totaux
        BigDecimal totalVersements = versements.stream()
                .map(VersementClient::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalValides = versements.stream()
                .filter(v -> v.getStatut() == StatutVersement.VALIDE.name())
                .map(VersementClient::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalEnAttente = versements.stream()
                .filter(v -> v.getStatut() == StatutVersement.EN_ATTENTE.name())
                .map(VersementClient::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal soldeTotal = factures.stream()
                .map(Facture::getSoldeRestant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return RapportClientData.builder()
                .clientId(clientId)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .versements(versements.stream().map(v -> mapToResponse(v)).toList())
                .factures(factures)
                .totalVersements(totalVersements)
                .totalValides(totalValides)
                .totalEnAttente(totalEnAttente)
                .soldeTotal(soldeTotal)
                .nombreVersements(versements.size())
                .build();
    }

    /**
     * Construit le corps de l'email
     */
    private String construireCorpsEmail(Long clientId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        return String.format("""
                <html>
                <body>
                    <h2>Rapport de Versements</h2>
                    <p>Bonjour,</p>
                    <p>Veuillez trouver ci-joint votre rapport de versements pour la période du %s au %s.</p>
                    <p>Ce rapport contient l'historique détaillé de tous vos paiements.</p>
                    <br>
                    <p>Cordialement,</p>
                    <p><strong>L'équipe de Gestion Facturation</strong></p>
                </body>
                </html>
                """, dateDebut.toLocalDate(), dateFin.toLocalDate());
    }

    /**
     * Convertit une entité Versement en VersementResponse
     */
//    private VersementResponse mapToResponse(VersementClient versement) {
//        return VersementResponse.builder()
//                .id(versement.getId())
//                .numeroVersement(versement.getNumeroVersement())
//                .montant(versement.getMontant())
//                .dateVersement(versement.getDateVersement())
//                .dateEnregistrement(versement.getDateEnregistrement())
//                .dateValidation(versement.getDateValidation())
//                .dateAnnulation(versement.getDateAnnulation())
//                .modePaiement(versement.getModePaiement().name())
//                .referencePaiement(versement.getReferencePaiement())
//                .banque(versement.getBanque())
//                .numeroCompte(versement.getNumeroCompte())
//                .remarques(versement.getRemarques())
//                .motifAnnulation(versement.getMotifAnnulation())
//                .statut(versement.getStatut().name())
//                .facture(mapFactureToSummary(versement.getFacture()))
//                .client(mapClientToSummary(versement.getClient()))
//                .usernameCreate(versement.getUsernameCreate())
//                .usernameValidation(versement.getUsernameValidation())
//                .usernameAnnulation(versement.getUsernameAnnulation())
//                .build();
//    }
//    
    private FactureSummary mapFactureToSummary(Facture facture) {
        if (facture == null) {
            return null;
        }
        return FactureSummary.builder()
                .id(facture.getId())
                .numeroFacture(facture.getNumeroFacture())
                .totalTtc(facture.getTotalTtc())
                .montantPaye(facture.getMontantPaye())
                .montantRestant(facture.getSoldeRestant())
                .build();
    }
    
    private ClientSummary mapClientToSummary(Client client) {
        if (client == null) {
            return null;
        }
        return ClientSummary.builder()
                .id(client.getId())
                .code(client.getCode())
                .nom(client.getNom())
                .build();
    }

    /**
     * Envoie le rapport par email
     */
    public void envoyerRapportParEmail(Long clientId, RapportEmailRequest request) {
        log.info("Envoi du rapport par email au client {} à l'adresse {}", clientId, request.getEmail());
        
        try {
            // Générer le rapport PDF
            byte[] pdfContent = genererRapportClientPDF(
                    clientId,
                    request.getDateDebut(),
                    request.getDateFin());

            // Créer le message email
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            
//            helper.setTo(request.getEmail());
//            helper.setSubject("Rapport de versements - " + LocalDate.now());
//            helper.setText(construireCorpsEmail(clientId, request.getDateDebut(), request.getDateFin()), true);

            // Ajouter le PDF en pièce jointe
            String filename = String.format("rapport-versements-%s-%s.pdf",
                    request.getDateDebut().toLocalDate(),
                    request.getDateFin().toLocalDate());
           // helper.addAttachment(filename, () -> new java.io.ByteArrayInputStream(pdfContent));

            // Envoyer l'email
          //  mailSender.send(message);
            log.info("Rapport envoyé avec succès à {}", request.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email", e);
            throw new ErrorResponse("Erreur lors de l'envoi du rapport par email");
        }
    }

    /**
     * les clients aayant deja effectuer un versement
     */
    public List<ClientDto> listeClientVersement() {
        
        List<ClientDto> listeClientdto = versementRepository.listeClientVersement().stream()
                .map(cl -> mappers.mapperClientByClientDto(cl)).toList();
        return listeClientdto;
    }
    
}
