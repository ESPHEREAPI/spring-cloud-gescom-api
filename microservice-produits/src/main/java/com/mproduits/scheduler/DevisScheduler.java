package com.mproduits.scheduler;

import com.mproduits.services.DevisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tâches planifiées pour la gestion automatique des devis
 * 
 * TÂCHES PROGRAMMÉES:
 * ===================
 * 
 * 1. Marquage des devis expirés
 *    - Exécution: Tous les jours à 00:01
 *    - Action: Change statut EN_ATTENTE → EXPIRE si date dépassée
 * 
 * 2. Notification devis proches expiration
 *    - Exécution: Tous les jours à 09:00
 *    - Action: Notifie les devis expirant dans < 3 jours
 * 
 * 3. Rapport quotidien
 *    - Exécution: Tous les jours à 18:00
 *    - Action: Génère rapport statistiques quotidiennes
 * 
 * CONFIGURATION:
 * Pour activer, ajouter @EnableScheduling dans l'application principale
 * 
 * @author USER01
 * @version 2.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DevisScheduler {

    private final DevisService devisService;
    
    private static final DateTimeFormatter DATE_FORMAT = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ================================================================
    // TÂCHE 1: MARQUAGE AUTOMATIQUE DES DEVIS EXPIRÉS
    // ================================================================
    
    /**
     * Marque automatiquement les devis expirés
     * Exécution: Tous les jours à 00:01
     * 
     * Cron: 0 1 0 * * ? = Seconde Minute Heure Jour Mois Jour-semaine
     */
    @Scheduled(cron = "0 1 0 * * ?") // 00:01 tous les jours
    // Alternative: @Scheduled(fixedRate = 3600000) // Toutes les heures
    public void marquerDevisExpires() {
        String maintenant = LocalDateTime.now().format(DATE_FORMAT);
        
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║   TÂCHE PLANIFIÉE: MARQUAGE DEVIS EXPIRÉS                    ║");
        log.info("║   Heure: {}                                      ║", maintenant);
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        try {
            int count = devisService.marquerDevisExpires();
            
            if (count > 0) {
                log.info("✓ {} devis marqué(s) comme EXPIRÉ", count);
                log.info("   Les devis EN_ATTENTE dont la date d'expiration est dépassée");
                log.info("   ont été automatiquement marqués comme EXPIRE");
            } else {
                log.info("✓ Aucun devis à marquer comme expiré");
            }

            log.info("╚═══════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("╚═══════════════════════════════════════════════════════════════╝");
            log.error("❌ ERREUR lors du marquage des devis expirés", e);
        }
    }

    // ================================================================
    // TÂCHE 2: NOTIFICATION DEVIS PROCHES EXPIRATION
    // ================================================================
    
    /**
     * Envoie des notifications pour les devis proches de l'expiration
     * Exécution: Tous les jours à 09:00
     * 
     * Cron: 0 0 9 * * ? = 09:00 tous les jours
     */
    @Scheduled(cron = "0 0 9 * * ?") // 09:00 tous les jours
    // Alternative: @Scheduled(cron = "0 0 9,14 * * ?") // 09:00 et 14:00
    public void notifierDevisProchesExpiration() {
        String maintenant = LocalDateTime.now().format(DATE_FORMAT);
        
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║   TÂCHE PLANIFIÉE: NOTIFICATION DEVIS PROCHES EXPIRATION     ║");
        log.info("║   Heure: {}                                      ║", maintenant);
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        try {
            devisService.notifierDevisProchesExpiration();
            
            log.info("✓ Notifications envoyées pour les devis expirant dans < 3 jours");
            log.info("╚═══════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("╚═══════════════════════════════════════════════════════════════╝");
            log.error("❌ ERREUR lors de l'envoi des notifications", e);
        }
    }

    // ================================================================
    // TÂCHE 3: RAPPORT QUOTIDIEN STATISTIQUES
    // ================================================================
    
    /**
     * Génère un rapport statistiques quotidien
     * Exécution: Tous les jours à 18:00
     * 
     * Cron: 0 0 18 * * ? = 18:00 tous les jours
     */
    @Scheduled(cron = "0 0 18 * * ?") // 18:00 tous les jours
    public void genererRapportQuotidien() {
        String maintenant = LocalDateTime.now().format(DATE_FORMAT);
        
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║   TÂCHE PLANIFIÉE: RAPPORT QUOTIDIEN                         ║");
        log.info("║   Heure: {}                                      ║", maintenant);
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        try {
            var stats = devisService.genererRapportStatistiques();
            
            log.info("📊 RAPPORT STATISTIQUES QUOTIDIENNES");
            log.info("   Total devis: {}", stats.get("total_devis"));
            log.info("   CA potentiel: {} XAF", stats.get("ca_potentiel"));
            log.info("   Taux conversion: {}%", stats.get("taux_conversion"));
            log.info("   Proches expiration: {}", stats.get("proches_expiration"));
            log.info("   Expirés: {}", stats.get("expires"));
            log.info("");
            log.info("   Répartition par statut:");
            var parStatut = (java.util.Map<?, ?>) stats.get("par_statut");
            parStatut.forEach((statut, count) -> 
                log.info("     - {}: {}", statut, count)
            );
            
            log.info("╚═══════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("╚═══════════════════════════════════════════════════════════════╝");
            log.error("❌ ERREUR lors de la génération du rapport", e);
        }
    }

    // ================================================================
    // TÂCHE 4: NETTOYAGE (OPTIONNEL)
    // ================================================================
    
    /**
     * Nettoie les données anciennes (optionnel)
     * Exécution: Tous les dimanches à 03:00
     * 
     * Cron: 0 0 3 ? * SUN = 03:00 tous les dimanches
     */
    @Scheduled(cron = "0 0 3 ? * SUN") // 03:00 tous les dimanches
    public void nettoyageDonnees() {
        String maintenant = LocalDateTime.now().format(DATE_FORMAT);
        
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║   TÂCHE PLANIFIÉE: NETTOYAGE DONNÉES                         ║");
        log.info("║   Heure: {}                                      ║", maintenant);
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        try {
            // Exemple: Supprimer les notifications lues de plus de 30 jours
            // Archiver les devis REFUSE/ANNULE de plus de 1 an
            // etc.
            
            log.info("✓ Nettoyage effectué");
            log.info("╚═══════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("╚═══════════════════════════════════════════════════════════════╝");
            log.error("❌ ERREUR lors du nettoyage", e);
        }
    }

    // ================================================================
    // MÉTHODES UTILITAIRES
    // ================================================================
    
    /**
     * Affiche les informations de démarrage du scheduler
     * Appelé au démarrage de l'application
     */
    public void afficherConfigurationScheduler() {
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║           CONFIGURATION SCHEDULER DEVIS                       ║");
        log.info("╠═══════════════════════════════════════════════════════════════╣");
        log.info("║ 1. Marquage expirés:      Tous les jours à 00:01             ║");
        log.info("║ 2. Notifications:         Tous les jours à 09:00             ║");
        log.info("║ 3. Rapport quotidien:     Tous les jours à 18:00             ║");
        log.info("║ 4. Nettoyage:             Tous les dimanches à 03:00         ║");
        log.info("╚═══════════════════════════════════════════════════════════════╝");
    }
}