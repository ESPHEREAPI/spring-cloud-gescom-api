package com.mproduits.services;

import com.mproduits.dto.*;
import com.mproduits.enums.StatutFacture;
import com.mproduits.enums.TypeNotification;
import com.mproduits.exceptions.*;
import com.mproduits.model.*;
import com.mproduits.repositories.ClientRepository;
import com.mproduits.repositories.FactureRepository;
import com.mproduits.repositories.NotificationClientRepository;
import com.mproduits.repositories.VersementClientRepository;
import com.mproduits.specifications.NotificationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des notifications clients
 * 
 * RESPONSABILITÉS:
 * - Envoi de notifications (EMAIL, SMS, WhatsApp, Système)
 * - Notifications automatiques selon événements factures
 * - Relances automatiques avant échéance
 * - Alertes pour factures en retard
 * - Gestion des échecs et réessais
 * - Statistiques de notification
 * 
 * NOTIFICATIONS AUTOMATIQUES:
 * - Création de facture → Notification immédiate
 * - 7 jours avant échéance → Relance préventive
 * - 3 jours avant échéance → Relance urgente
 * - 1 jour avant échéance → Dernière relance
 * - Jour d'échéance → Alerte échéance
 * - Après échéance → Notification retard (quotidienne)
 * - Paiement reçu → Confirmation
 * - Facture soldée → Notification solde
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationClientRepository notificationRepository;
    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final VersementClientRepository versementRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsappService whatsappService;

    /**
     * Envoie une notification manuelle
     * 
     * @param request Données de la notification
     * @return Notification envoyée
     */
    @Transactional
    public NotificationResponse envoyerNotification(NotificationSendRequest request,String username) {
        log.info("Envoi d'une notification manuelle au client ID: {}", request.getClientId());
        
        // 1. Validation du client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'ID: " + request.getClientId()));
        
        // 2. Détermination du destinataire selon le type
        String destinataire = determinerDestinataire(client, request.getTypeNotification());
        if (destinataire == null || destinataire.trim().isEmpty()) {
            throw new GlobalException("Aucune coordonnée disponible pour le type de notification: " + 
                                        request.getTypeNotification().getLibelle());
        }
        
        // 3. Création de la notification
        NotificationClient notification = NotificationClient.builder()
                .client(client)
                .typeNotification(request.getTypeNotification())
                .categorie(request.getCategorie())
                .titre(request.getTitre())
                .message(request.getMessage())
                .messageHtml(request.getMessageHtml())
                .destinataire(destinataire)
                .nomDestinataire(client.getNom())
                .statut("EN_ATTENTE")
                .priorite(request.getPriorite() != null ? request.getPriorite() : 2)
                .datePrevueEnvoi(request.getDatePrevueEnvoi())
                .usernameCreate(username)
                .build();
        
        // Liaison avec facture/versement si fourni
        if (request.getFactureId() != null) {
            Facture facture = factureRepository.findById(request.getFactureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable"));
            notification.setFacture(facture);
        }
        
        if (request.getVersementId() != null) {
            VersementClient versement = versementRepository.findById(request.getVersementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable"));
            notification.setVersement(versement);
        }
        
        notification = notificationRepository.save(notification);
        
        // 4. Envoi immédiat si pas de date prévue
        if (request.getDatePrevueEnvoi() == null) {
            envoyerNotificationMaintenant(notification);
        }
        
        return mapToResponse(notification);
    }

    /**
     * Notification automatique - Création de facture
     */
    @Transactional
    public void notifierCreationFacture(Long factureId) {
        log.info("Envoi de notification de création de facture ID: {}", factureId);
        
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable"));
        
        String message = String.format(
            "Bonjour %s,\n\nVotre facture %s d'un montant de %s XAF a été créée.\n" +
            "Date d'échéance: %s\n\nMerci de votre confiance.",
            facture.getClient().getNom(),
            facture.getNumeroFacture(),
            facture.getTotalTtc(),
            facture.getDateEcheance()
        );
        
        envoyerNotificationFacture(facture, "FACTURE_CREEE", "Nouvelle facture", message);
    }

    /**
     * Notification automatique - Paiement reçu
     */
    @Transactional
    public void notifierPaiementRecu(Long versementId) {
        log.info("Envoi de notification de paiement reçu pour versement ID: {}", versementId);
        
        VersementClient versement = versementRepository.findById(versementId)
                .orElseThrow(() -> new ResourceNotFoundException("Versement introuvable"));
        
        Facture facture = versement.getFacture();
        
        String message = String.format(
            "Bonjour %s,\n\nNous avons bien reçu votre paiement de %s XAF pour la facture %s.\n" +
            "Solde restant: %s XAF\n\nMerci pour votre règlement.",
            facture.getClient().getNom(),
            versement.getMontant(),
            facture.getNumeroFacture(),
            facture.getSoldeRestant()
        );
        
        envoyerNotificationFacture(facture, "PAIEMENT_RECU", "Paiement reçu", message);
    }

    /**
     * Notification automatique - Facture soldée
     */
    @Transactional
    public void notifierFactureSoldee(Long factureId) {
        log.info("Envoi de notification de facture soldée ID: {}", factureId);
        
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable"));
        
        String message = String.format(
            "Bonjour %s,\n\nVotre facture %s est maintenant entièrement soldée.\n" +
            "Montant total: %s XAF\n\nMerci pour votre règlement complet.",
            facture.getClient().getNom(),
            facture.getNumeroFacture(),
            facture.getTotalTtc()
        );
        
        envoyerNotificationFacture(facture, "FACTURE_SOLDEE", "Facture soldée", message);
    }

    /**
     * Notification automatique - Facture annulée
     */
    @Transactional
    public void notifierAnnulationFacture(Long factureId) {
        log.info("Envoi de notification d'annulation de facture ID: {}", factureId);
        
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable"));
        
        String message = String.format(
            "Bonjour %s,\n\nVotre facture %s a été annulée.\n" +
            "Motif: %s\n\nPour toute question, contactez-nous.",
            facture.getClient().getNom(),
            facture.getNumeroFacture(),
            facture.getMotifAnnulation()
        );
        
        envoyerNotificationFacture(facture, "FACTURE_ANNULEE", "Facture annulée", message);
    }

    /**
     * Tâche planifiée - Relances automatiques avant échéance
     * Exécutée tous les jours à 8h00
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void envoyerRelancesAutomatiques() {
        log.info("Démarrage des relances automatiques avant échéance");
        
        // Relances à 7, 3 et 1 jour avant échéance
        int[] joursRelances = {7, 3, 1};
        
        for (int jours : joursRelances) {
            List<Facture> factures = factureRepository.findByNombreRelances(jours);
            
            log.info("Relance {} jours avant échéance: {} factures trouvées", jours, factures.size());
            
            for (Facture facture : factures) {
                try {
                    envoyerRelanceEcheance(facture, jours);
                } catch (Exception e) {
                    log.error("Erreur lors de l'envoi de la relance pour la facture {}: {}", 
                             facture.getNumeroFacture(), e.getMessage());
                }
            }
        }
        
        log.info("Relances automatiques terminées");
    }

    /**
     * Tâche planifiée - Notifications de retard
     * Exécutée tous les jours à 9h00
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void envoyerNotificationsRetard() {
        log.info("Démarrage des notifications de retard");
        
        List<Facture> facturesEnRetard = factureRepository.findFacturesEnRetard();
        
        log.info("{} factures en retard trouvées", facturesEnRetard.size());
        
        for (Facture facture : facturesEnRetard) {
            try {
                envoyerNotificationRetard(facture);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de la notification de retard pour la facture {}: {}", 
                         facture.getNumeroFacture(), e.getMessage());
            }
        }
        
        log.info("Notifications de retard terminées");
    }

    /**
     * Tâche planifiée - Réessai des notifications échouées
     * Exécutée toutes les heures
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void reessayerNotificationsEchouees() {
        log.info("Démarrage du réessai des notifications échouées");
        
        List<NotificationClient> notificationsEchouees = notificationRepository.findNotificationsAReessayer();
        
        log.info("{} notifications à réessayer trouvées", notificationsEchouees.size());
        
        for (NotificationClient notification : notificationsEchouees) {
            try {
                envoyerNotificationMaintenant(notification);
            } catch (Exception e) {
                log.error("Erreur lors du réessai de la notification ID {}: {}", 
                         notification.getId(), e.getMessage());
            }
        }
        
        log.info("Réessai des notifications terminé");
    }

    /**
     * Récupère une notification par son ID
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long id) {
        NotificationClient notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable avec l'ID: " + id));
        return mapToResponse(notification);
    }

    /**
     * Liste les notifications avec filtres et pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationSummary> listerNotifications(NotificationSearchCriteria criteria) {
        Pageable pageable = createPageable(criteria);
        
        Page<NotificationClient> notifications = notificationRepository.findAll(
                NotificationSpecifications.withCriteria(criteria), 
                pageable
        );
        
        return notifications.map(this::mapToSummary);
    }

    /**
     * Récupère les notifications d'un client
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsClient(Long clientId) {
        List<NotificationClient> notifications = notificationRepository
                .findByClientIdOrderByDateCreationDesc(clientId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calcule les statistiques de notifications
     */
    @Transactional(readOnly = true)
    public NotificationStatistiques getStatistiques(Date dateDebut, Date dateFin) {
        return notificationRepository.calculerStatistiques(dateDebut, dateFin);
    }

    /**
     * Récupère l'historique des notifications d'un client
     */
    @Transactional(readOnly = true)
    public List<NotificationClient> getHistoriqueClient(Long clientId) {
        return notificationRepository.getHistoriqueNotificationClient(clientId);
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Envoie une relance avant échéance
     */
    private void envoyerRelanceEcheance(Facture facture, int joursRestants) {
        String urgence = joursRestants <= 1 ? "URGENTE" : "";
        
        String message = String.format(
            "Bonjour %s,\n\n%sRappel: Votre facture %s arrive à échéance dans %d jour(s).\n" +
            "Montant à payer: %s XAF\n" +
            "Date d'échéance: %s\n\nMerci de procéder au règlement.",
            facture.getClient().getNom(),
            urgence.isEmpty() ? "" : urgence + " - ",
            facture.getNumeroFacture(),
            joursRestants,
            facture.getSoldeRestant(),
            facture.getDateEcheance()
        );
        
        envoyerNotificationFacture(facture, "RELANCE_AVANT_ECHEANCE", "Échéance proche", message);
        
        // Mise à jour du compteur de relances
        facture.setNombreRelances(facture.getNombreRelances() + 1);
        facture.setDateDerniereRelance(new Date());
        factureRepository.save(facture);
    }

    /**
     * Envoie une notification de retard
     */
    private void envoyerNotificationRetard(Facture facture) {
        long joursRetard = facture.getJoursRetard();
        
        String message = String.format(
            "Bonjour %s,\n\nVotre facture %s est en retard de %d jour(s).\n" +
            "Montant impayé: %s XAF\n" +
            "Date d'échéance dépassée: %s\n\n" +
            "Merci de régulariser votre situation dans les plus brefs délais.",
            facture.getClient().getNom(),
            facture.getNumeroFacture(),
            joursRetard,
            facture.getSoldeRestant(),
            facture.getDateEcheance()
        );
        
        envoyerNotificationFacture(facture, "FACTURE_RETARD", "Facture en retard", message);
        
        // Mise à jour du statut si nécessaire
        if (facture.getStatut() != StatutFacture.EN_RETARD) {
            facture.setStatut(StatutFacture.EN_RETARD);
            factureRepository.save(facture);
        }
    }

    /**
     * Envoie une notification pour une facture (tous canaux configurés)
     */
    private void envoyerNotificationFacture(Facture facture, String categorie, String titre, String message) {
        Client client = facture.getClient();
        
        // Email si disponible
        if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
            envoyerNotificationCanal(client, facture, TypeNotification.EMAIL, categorie, titre, message);
        }
        
        // SMS si disponible
        if (client.getTelephone() != null && !client.getTelephone().trim().isEmpty()) {
            envoyerNotificationCanal(client, facture, TypeNotification.SMS, categorie, titre, message);
        }
    }

    /**
     * Envoie une notification sur un canal spécifique
     */
    private void envoyerNotificationCanal(Client client, Facture facture, 
                                          TypeNotification type, String categorie, 
                                          String titre, String message) {
        String destinataire = determinerDestinataire(client, type);
        if (destinataire == null || destinataire.trim().isEmpty()) {
            return;
        }
        
        NotificationClient notification = NotificationClient.builder()
                .client(client)
                .facture(facture)
                .typeNotification(type)
                .categorie(categorie)
                .titre(titre)
                .message(message)
                .destinataire(destinataire)
                .nomDestinataire(client.getNom())
                .statut("EN_ATTENTE")
                .priorite(2)
                .usernameCreate("SYSTEM")
                .build();
        
        notification = notificationRepository.save(notification);
        envoyerNotificationMaintenant(notification);
    }

    /**
     * Envoie une notification immédiatement
     */
    private void envoyerNotificationMaintenant(NotificationClient notification) {
        notification.setStatut("EN_COURS");
        notification = notificationRepository.save(notification);
        
        try {
            NotificationEnvoiResult result = null;
            
            switch (notification.getTypeNotification()) {
                case EMAIL:
                  //  result = emailService.envoyerEmail(notification);
                    break;
                case SMS:
                   // result = smsService.envoyerSms(notification);
                    break;
                case WHATSAPP:
                   // result = whatsappService.envoyerWhatsapp(notification);
                    break;
                case SYSTEME:
                    // Notification système (stockée en base uniquement)
                    result = NotificationEnvoiResult.builder()
                            .succes(true)
                            .message("Notification système enregistrée")
                            .build();
                    break;
            }
            
            if (result != null && result.getSucces()) {
                notification.marquerEnvoyee();
                notification.setCodeReponse(result.getCodeReponse());
                notification.setIdExterne(result.getIdExterne());
            } else {
                notification.marquerEchec(result != null ? result.getMessage() : "Erreur inconnue");
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification ID {}: {}", 
                     notification.getId(), e.getMessage());
            notification.marquerEchec(e.getMessage());
        }
        
        notificationRepository.save(notification);
    }

    /**
     * Détermine le destinataire selon le type de notification
     */
    private String determinerDestinataire(Client client, TypeNotification type) {
        switch (type) {
            case EMAIL:
                return client.getEmail();
            case SMS:
            case WHATSAPP:
                return client.getTelephone();
            case SYSTEME:
                return String.valueOf(client.getId());
            default:
                return null;
        }
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
    private Pageable createPageable(NotificationSearchCriteria criteria) {
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "dateCreation";
        Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortDirection()) ? 
                                  Sort.Direction.ASC : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /**
     * Convertit une entité NotificationClient en NotificationResponse
     */
    private NotificationResponse mapToResponse(NotificationClient notification) {
        // Mapping détaillé (code omis pour la brièveté)
        return NotificationResponse.builder()
                .id(notification.getId())
                .typeNotification(notification.getTypeNotification())
                // ... autres champs
                .build();
    }

    /**
     * Convertit une entité NotificationClient en NotificationSummary
     */
    private NotificationSummary mapToSummary(NotificationClient notification) {
        return NotificationSummary.builder()
                .id(notification.getId())
                .typeNotification(notification.getTypeNotification())
                .categorie(notification.getCategorie())
                .destinataire(notification.getDestinataire())
                .dateEnvoi(notification.getDateEnvoi())
                .statut(notification.getStatut())
                .clientNom(notification.getClient().getNom())
                .factureNumero(notification.getFacture() != null ? 
                              notification.getFacture().getNumeroFacture() : null)
                .build();
    }

    void ajouter(String nouveau_Devis, String format, String success) {
      //  throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}