package com.mproduits.model;

import com.mproduits.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Entité NotificationClient - Gestion des notifications clients
 * 
 * TYPES DE NOTIFICATIONS:
 * - Création de facture
 * - Rappel de paiement (3, 7, 15 jours avant échéance)
 * - Facture en retard
 * - Confirmation de paiement reçu
 * - Facture soldée
 * - Facture annulée
 * 
 * CANAUX DE NOTIFICATION:
 * - EMAIL    → Envoi par email
 * - SMS      → Envoi par SMS
 * - WHATSAPP → Envoi via WhatsApp
 * - SYSTEME  → Notification dans l'application
 * 
 * RÈGLES MÉTIER:
 * - Notifications automatiques selon événements
 * - Tentatives de renvoi en cas d'échec
 * - Historique complet des notifications
 * - Personnalisation des messages selon le client
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 2.0
 */
@Entity
@Table(name = "notification_client", indexes = {
    @Index(name = "idx_notification_client", columnList = "client_id"),
    @Index(name = "idx_notification_facture", columnList = "facture_id"),
    @Index(name = "idx_notification_date", columnList = "date_creation DESC"),
    @Index(name = "idx_notification_statut", columnList = "statut"),
    @Index(name = "idx_notification_type", columnList = "type_notification"),
    @Index(name = "idx_notification_categorie", columnList = "categorie")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationClient implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== IDENTIFIANT ==========
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== TYPE ET CATÉGORIE ==========
    
    /**
     * Type de notification (canal d'envoi)
     */
    @Column(name = "type_notification", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TypeNotification typeNotification;

    /**
     * Catégorie de notification
     * Valeurs: FACTURE_CREEE, RAPPEL_PAIEMENT, FACTURE_RETARD, 
     *          PAIEMENT_RECU, FACTURE_SOLDEE, FACTURE_ANNULEE, RELANCE_AVANT_ECHEANCE
     */
    @Column(name = "categorie", nullable = false, length = 50)
    private String categorie;

    // ========== CONTENU ==========
    
    /**
     * Titre/Objet de la notification
     */
    @Column(name = "titre", length = 255)
    private String titre;

    /**
     * Message de la notification
     */
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Message HTML (pour emails)
     */
    @Column(name = "message_html", columnDefinition = "TEXT")
    private String messageHtml;

    // ========== DESTINATAIRE ==========
    
    /**
     * Adresse du destinataire (email, téléphone selon le type)
     */
    @Column(name = "destinataire", length = 255, nullable = false)
    private String destinataire;

    /**
     * Nom du destinataire
     */
    @Column(name = "nom_destinataire", length = 255)
    private String nomDestinataire;

    // ========== DATES ==========
    
    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    /**
     * Date d'envoi de la notification
     */
    @Column(name = "date_envoi")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;

    /**
     * Date de lecture/ouverture de la notification
     */
    @Column(name = "date_lecture")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateLecture;

    /**
     * Date prévue d'envoi (pour les notifications programmées)
     */
    @Column(name = "date_prevue_envoi")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datePrevueEnvoi;

    // ========== STATUT ==========
    
    /**
     * Statut de la notification
     * Valeurs: EN_ATTENTE, EN_COURS, ENVOYE, ECHEC, ANNULEE
     */
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private String statut = "EN_ATTENTE";

    /**
     * Nombre de tentatives d'envoi
     */
    @Column(name = "tentatives")
    @Builder.Default
    private Integer tentatives = 0;

    /**
     * Nombre maximum de tentatives autorisées
     */
    @Column(name = "max_tentatives")
    @Builder.Default
    private Integer maxTentatives = 3;

    // ========== RÉSULTAT D'ENVOI ==========
    
    /**
     * Code de réponse du service d'envoi
     */
    @Column(name = "code_reponse", length = 50)
    private String codeReponse;

    /**
     * Message d'erreur (si échec)
     */
    @Column(name = "message_erreur", columnDefinition = "TEXT")
    private String messageErreur;

    /**
     * Identifiant externe (ID du message dans le service tiers)
     */
    @Column(name = "id_externe", length = 255)
    private String idExterne;

    // ========== RELATIONS ==========
    
    /**
     * Client destinataire de la notification
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Facture concernée par la notification (si applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    /**
     * Versement concerné par la notification (si applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "versement_id")
    private VersementClient versement;

    // ========== INFORMATIONS COMPLÉMENTAIRES ==========
    
    /**
     * Priorité de la notification (1=haute, 2=normale, 3=basse)
     */
    @Column(name = "priorite")
    @Builder.Default
    private Integer priorite = 2;

    /**
     * Données supplémentaires (JSON)
     */
    @Column(name = "donnees_supplementaires", columnDefinition = "TEXT")
    private String donneesSupplementaires;

    // ========== TRAÇABILITÉ ==========
    
    /**
     * Utilisateur ayant créé la notification
     */
    @Column(name = "username_create", length = 100)
    private String usernameCreate;

    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Vérifie si la notification peut être envoyée
     */
    public boolean isPeutEtreEnvoyee() {
        return ("EN_ATTENTE".equals(statut) || "ECHEC".equals(statut))
                && tentatives < maxTentatives;
    }

    /**
     * Vérifie si la notification peut être renvoyée
     */
    public boolean isPeutEtreRenvoyee() {
        return "ECHEC".equals(statut) && tentatives < maxTentatives;
    }

    /**
     * Vérifie si la notification a été envoyée avec succès
     */
    public boolean isEnvoyee() {
        return "ENVOYE".equals(statut);
    }

    /**
     * Vérifie si la notification a été lue
     */
    public boolean isLue() {
        return dateLecture != null;
    }

    /**
     * Vérifie si toutes les tentatives ont échoué
     */
    public boolean isTentativesEpuisees() {
        return tentatives >= maxTentatives;
    }

    /**
     * Incrémente le nombre de tentatives
     */
    public void incrementerTentatives() {
        if (tentatives == null) {
            tentatives = 0;
        }
        tentatives++;
    }

    /**
     * Marque la notification comme envoyée
     */
    public void marquerEnvoyee() {
        this.statut = "ENVOYE";
        this.dateEnvoi = new Date();
    }

    /**
     * Marque la notification comme échouée
     */
    public void marquerEchec(String messageErreur) {
        this.statut = "ECHEC";
        this.messageErreur = messageErreur;
        incrementerTentatives();
    }

    /**
     * Marque la notification comme lue
     */
    public void marquerLue() {
        this.dateLecture = new Date();
    }

    /**
     * Génère une description courte de la notification
     */
    public String getDescription() {
        return String.format("%s - %s vers %s",
                categorie != null ? categorie : "N/A",
                typeNotification != null ? typeNotification.getLibelle() : "N/A",
                destinataire);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationClient)) return false;
        NotificationClient that = (NotificationClient) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("NotificationClient[id=%d, type=%s, categorie=%s, destinataire=%s, statut=%s]",
                id,
                typeNotification != null ? typeNotification.getLibelle() : "N/A",
                categorie, destinataire, statut);
    }
}