package com.mproduits.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO pour les statistiques des notifications
 * Contient les métriques calculées sur une période donnée
 * 
 * @author USER01
 */

@Builder
@NoArgsConstructor
@Data
public class NotificationStatistiques {
    
    // ========== COMPTEURS PAR STATUT ==========
    
    /**
     * Nombre total de notifications
     */
    private Long totalNotifications;
    
    /**
     * Nombre de notifications en attente
     */
    @Builder.Default
    private Long notificationsEnAttente = 0L;
    
    /**
     * Nombre de notifications en cours d'envoi
     */
    @Builder.Default
    private Long notificationsEnCours = 0L;
    
    /**
     * Nombre de notifications envoyées avec succès
     */
    @Builder.Default
    private Long notificationsEnvoyees = 0L;
    
    /**
     * Nombre de notifications en échec
     */
    @Builder.Default
    private Long notificationsEnEchec = 0L;
    
    /**
     * Nombre de notifications annulées
     */
    @Builder.Default
    private Long notificationsAnnulees = 0L;
    
    // ========== COMPTEURS PAR TYPE ==========
    
    /**
     * Nombre de notifications EMAIL
     */
    @Builder.Default
    private Long notificationsEmail = 0L;
    
    /**
     * Nombre de notifications SMS
     */
    @Builder.Default
    private Long notificationsSms = 0L;
    
    /**
     * Nombre de notifications PUSH
     */
    @Builder.Default
    private Long notificationsPush = 0L;
    
    // ========== INDICATEURS DE PERFORMANCE ==========
    
    /**
     * Taux de réussite (pourcentage)
     * Calcul : (notificationsEnvoyees / total) * 100
     */
    private BigDecimal tauxReussite;
    
    /**
     * Taux d'échec (pourcentage)
     * Calcul : (notificationsEnEchec / total) * 100
     */
    private BigDecimal tauxEchec;
    
    /**
     * Nombre de notifications à réessayer
     * (en échec mais tentatives non épuisées)
     */
    @Builder.Default
    private Long notificationsAReessayer = 0L;
    
    /**
     * Nombre de notifications avec tentatives épuisées
     */
    @Builder.Default
    private Long notificationsTentativesEpuisees = 0L;
    
    /**
     * Nombre de notifications non lues
     * (envoyées mais pas encore lues par le destinataire)
     */
    @Builder.Default
    private Long notificationsNonLues = 0L;
    
    /**
     * Nombre de notifications lues
     */
    @Builder.Default
    private Long notificationsLues = 0L;
    
    /**
     * Taux de lecture (pourcentage)
     * Calcul : (notificationsLues / notificationsEnvoyees) * 100
     */
    private BigDecimal tauxLecture;
    
    /**
     * Nombre moyen de tentatives par notification
     */
    private BigDecimal nombreMoyenTentatives;
    
    // ========== COMPTEURS PAR CATÉGORIE ==========
    
    /**
     * Répartition par catégorie de notification
     * Clé : catégorie (FACTURE_CREEE, RAPPEL_PAIEMENT, etc.)
     * Valeur : nombre de notifications
     */
    @Builder.Default
    private Map<String, Long> repartitionParCategorie = new HashMap<>();
    
    /**
     * Répartition par priorité
     * Clé : niveau de priorité (1, 2, 3)
     * Valeur : nombre de notifications
     */
    @Builder.Default
    private Map<Integer, Long> repartitionParPriorite = new HashMap<>();
    
    // ========== CONSTRUCTEUR POUR JPQL ==========
    
    /**
     * Constructeur utilisé par la requête JPQL du repository calculerStatistiques()
     * L'ordre des paramètres doit correspondre exactement à celui de la requête SELECT
     * 
     * @param totalNotifications nombre total de notifications
     * @param notificationsEnAttente nombre de notifications en attente
     * @param notificationsEnCours nombre de notifications en cours
     * @param notificationsEnvoyees nombre de notifications envoyées
     * @param notificationsEnEchec nombre de notifications en échec
     * @param notificationsAnnulees nombre de notifications annulées
     * @param notificationsEmail nombre de notifications EMAIL
     * @param notificationsSms nombre de notifications SMS
     * @param notificationsPush nombre de notifications PUSH
     * @param tauxReussite taux de réussite en pourcentage (Double)
     * @param notificationsAReessayer nombre de notifications à réessayer
     * @param notificationsNonLues nombre de notifications non lues
     * @param notificationsLues nombre de notifications lues
     * @param nombreMoyenTentatives nombre moyen de tentatives (Double)
     */
    public NotificationStatistiques(
            Long totalNotifications,
            Long notificationsEnAttente,
            Long notificationsEnCours,
            Long notificationsEnvoyees,
            Long notificationsEnEchec,
            Long notificationsAnnulees,
            Long notificationsEmail,
            Long notificationsSms,
            Long notificationsPush,
            Double tauxReussite,
            Long notificationsAReessayer,
            Long notificationsNonLues,
            Long notificationsLues,
            Double nombreMoyenTentatives) {
        
        // Initialisation des compteurs par statut (protection contre null)
        this.totalNotifications = totalNotifications != null ? totalNotifications : 0L;
        this.notificationsEnAttente = notificationsEnAttente != null ? notificationsEnAttente : 0L;
        this.notificationsEnCours = notificationsEnCours != null ? notificationsEnCours : 0L;
        this.notificationsEnvoyees = notificationsEnvoyees != null ? notificationsEnvoyees : 0L;
        this.notificationsEnEchec = notificationsEnEchec != null ? notificationsEnEchec : 0L;
        this.notificationsAnnulees = notificationsAnnulees != null ? notificationsAnnulees : 0L;
        
        // Initialisation des compteurs par type
        this.notificationsEmail = notificationsEmail != null ? notificationsEmail : 0L;
        this.notificationsSms = notificationsSms != null ? notificationsSms : 0L;
        this.notificationsPush = notificationsPush != null ? notificationsPush : 0L;
        
        // Initialisation des indicateurs
        this.notificationsAReessayer = notificationsAReessayer != null ? notificationsAReessayer : 0L;
        this.notificationsNonLues = notificationsNonLues != null ? notificationsNonLues : 0L;
        this.notificationsLues = notificationsLues != null ? notificationsLues : 0L;
        this.notificationsTentativesEpuisees = 0L; // Pas calculé dans la requête JPQL
        
        // Conversion des Double vers BigDecimal avec arrondi
        this.tauxReussite = tauxReussite != null ? 
            BigDecimal.valueOf(tauxReussite).setScale(2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
            
        this.nombreMoyenTentatives = nombreMoyenTentatives != null ?
            BigDecimal.valueOf(nombreMoyenTentatives).setScale(2, BigDecimal.ROUND_HALF_UP) :
            BigDecimal.ZERO;
        
        // Calcul automatique des autres taux (tauxEchec, tauxLecture)
        calculerTaux();
        
        // Initialisation des maps
        this.repartitionParCategorie = new HashMap<>();
        this.repartitionParPriorite = new HashMap<>();
    }
    
    /**
     * Constructeur avec tous les paramètres (pour Lombok Builder et usage manuel)
     */
    public NotificationStatistiques(
            Long totalNotifications,
            Long notificationsEnAttente,
            Long notificationsEnCours,
            Long notificationsEnvoyees,
            Long notificationsEnEchec,
            Long notificationsAnnulees,
            Long notificationsEmail,
            Long notificationsSms,
            Long notificationsPush,
            BigDecimal tauxReussite,
            BigDecimal tauxEchec,
            Long notificationsAReessayer,
            Long notificationsTentativesEpuisees,
            Long notificationsNonLues,
            Long notificationsLues,
            BigDecimal tauxLecture,
            BigDecimal nombreMoyenTentatives,
            Map<String, Long> repartitionParCategorie,
            Map<Integer, Long> repartitionParPriorite) {
        
        this.totalNotifications = totalNotifications;
        this.notificationsEnAttente = notificationsEnAttente;
        this.notificationsEnCours = notificationsEnCours;
        this.notificationsEnvoyees = notificationsEnvoyees;
        this.notificationsEnEchec = notificationsEnEchec;
        this.notificationsAnnulees = notificationsAnnulees;
        this.notificationsEmail = notificationsEmail;
        this.notificationsSms = notificationsSms;
        this.notificationsPush = notificationsPush;
        this.tauxReussite = tauxReussite;
        this.tauxEchec = tauxEchec;
        this.notificationsAReessayer = notificationsAReessayer;
        this.notificationsTentativesEpuisees = notificationsTentativesEpuisees;
        this.notificationsNonLues = notificationsNonLues;
        this.notificationsLues = notificationsLues;
        this.tauxLecture = tauxLecture;
        this.nombreMoyenTentatives = nombreMoyenTentatives;
        this.repartitionParCategorie = repartitionParCategorie != null ? repartitionParCategorie : new HashMap<>();
        this.repartitionParPriorite = repartitionParPriorite != null ? repartitionParPriorite : new HashMap<>();
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Calcule automatiquement les taux à partir des compteurs
     */
    public void calculerTaux() {
        if (totalNotifications == null || totalNotifications == 0) {
            tauxReussite = BigDecimal.ZERO;
            tauxEchec = BigDecimal.ZERO;
            tauxLecture = BigDecimal.ZERO;
            return;
        }
        
        // Taux de réussite (si pas déjà calculé)
        if (tauxReussite == null && notificationsEnvoyees != null) {
            tauxReussite = BigDecimal.valueOf(notificationsEnvoyees)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalNotifications), 2, BigDecimal.ROUND_HALF_UP);
        } else if (tauxReussite == null) {
            tauxReussite = BigDecimal.ZERO;
        }
        
        // Taux d'échec
        if (notificationsEnEchec != null) {
            tauxEchec = BigDecimal.valueOf(notificationsEnEchec)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalNotifications), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            tauxEchec = BigDecimal.ZERO;
        }
        
        // Taux de lecture
        if (notificationsEnvoyees != null && notificationsEnvoyees > 0 && notificationsLues != null) {
            tauxLecture = BigDecimal.valueOf(notificationsLues)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(notificationsEnvoyees), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            tauxLecture = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calcule le pourcentage de notifications d'un statut donné
     */
    public BigDecimal getPourcentageParStatut(Long nombreStatut) {
        if (totalNotifications == null || totalNotifications == 0 || nombreStatut == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(nombreStatut)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalNotifications), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Vérifie si les statistiques sont vides
     */
    public boolean isEmpty() {
        return totalNotifications == null || totalNotifications == 0;
    }
    
    /**
     * Ajoute une catégorie dans la répartition
     */
    public void ajouterCategorie(String categorie, Long nombre) {
        if (repartitionParCategorie == null) {
            repartitionParCategorie = new HashMap<>();
        }
        repartitionParCategorie.put(categorie, nombre);
    }
    
    /**
     * Ajoute une priorité dans la répartition
     */
    public void ajouterPriorite(Integer priorite, Long nombre) {
        if (repartitionParPriorite == null) {
            repartitionParPriorite = new HashMap<>();
        }
        repartitionParPriorite.put(priorite, nombre);
    }
    
    /**
     * Retourne un résumé textuel des statistiques
     */
    public String getResume() {
        return String.format(
            "Notifications: %d total | %d envoyées (%.1f%%) | %d en échec (%.1f%%) | " +
            "%d à réessayer | Taux lecture: %.1f%%",
            totalNotifications != null ? totalNotifications : 0,
            notificationsEnvoyees != null ? notificationsEnvoyees : 0,
            tauxReussite != null ? tauxReussite.doubleValue() : 0.0,
            notificationsEnEchec != null ? notificationsEnEchec : 0,
            tauxEchec != null ? tauxEchec.doubleValue() : 0.0,
            notificationsAReessayer != null ? notificationsAReessayer : 0,
            tauxLecture != null ? tauxLecture.doubleValue() : 0.0
        );
    }
    
    /**
     * Retourne la catégorie avec le plus de notifications
     */
    public Map.Entry<String, Long> getCategorieDominante() {
        if (repartitionParCategorie == null || repartitionParCategorie.isEmpty()) {
            return null;
        }
        return repartitionParCategorie.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }
    
    /**
     * Vérifie si le taux de réussite est bon (>= 90%)
     */
    public boolean isTauxReussiteBon() {
        return tauxReussite != null && tauxReussite.compareTo(new BigDecimal("90")) >= 0;
    }
    
    /**
     * Vérifie si le taux d'échec est préoccupant (>= 10%)
     */
    public boolean isTauxEchecPreoccupant() {
        return tauxEchec != null && tauxEchec.compareTo(new BigDecimal("10")) >= 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "NotificationStatistiques[" +
            "total=%d, envoyées=%d, échecs=%d, " +
            "taux réussite=%.2f%%, taux lecture=%.2f%%]",
            totalNotifications, notificationsEnvoyees, notificationsEnEchec,
            tauxReussite, tauxLecture
        );
    }
}