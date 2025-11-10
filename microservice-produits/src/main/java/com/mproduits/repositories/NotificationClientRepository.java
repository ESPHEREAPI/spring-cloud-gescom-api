/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;
import com.mproduits.dto.HistoriqueNotificationClient;
import com.mproduits.dto.NotificationStatistiques;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;


import com.mproduits.model.NotificationClient;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
@Repository
public interface NotificationClientRepository extends JpaRepository<NotificationClient, Long>, 
                                                      JpaSpecificationExecutor<NotificationClient> {
    
    /**
     * Trouve toutes les notifications d'un client
     * 
     * @param clientId identifiant du client
     * @return liste des notifications du client
     */
    @Query("SELECT n FROM NotificationClient n WHERE n.client.id = :clientId")
    List<NotificationClient> findByClientId(@Param("clientId") Long clientId);
    
    /**
     * Trouve les notifications à réessayer
     * Ce sont les notifications qui ont échoué mais qui n'ont pas épuisé leurs tentatives
     * OU les notifications en attente dont la date d'envoi prévue est passée
     * 
     * @return liste des notifications à réessayer
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "(n.statut = 'ECHEC' OR n.statut = 'EN_ATTENTE') AND " +
           "n.tentatives < n.maxTentatives AND " +
           "(n.datePrevueEnvoi IS NULL OR n.datePrevueEnvoi <= CURRENT_TIMESTAMP) " +
           "ORDER BY n.priorite ASC, n.dateCreation ASC")
    List<NotificationClient> findNotificationsAReessayer();
    
    /**
     * Trouve les notifications en attente pour un envoi immédiat ou programmé
     * 
     * @param dateLimit date limite pour les envois programmés
     * @return liste des notifications à envoyer
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.statut = 'EN_ATTENTE' AND " +
           "n.tentatives < n.maxTentatives AND " +
           "(n.datePrevueEnvoi IS NULL OR n.datePrevueEnvoi <= :dateLimit) " +
           "ORDER BY n.priorite ASC, n.dateCreation ASC")
    List<NotificationClient> findNotificationsEnAttente(@Param("dateLimit") Date dateLimit);
    
    /**
     * Trouve les notifications qui ont échoué et doivent être réessayées
     * 
     * @return liste des notifications en échec
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.statut = 'ECHEC' AND " +
           "n.tentatives < n.maxTentatives " +
           "ORDER BY n.dateCreation ASC")
    List<NotificationClient> findNotificationsEnEchec();
    
    /**
     * Trouve les notifications pour une facture donnée
     * 
     * @param factureId identifiant de la facture
     * @return liste des notifications de la facture
     */
    @Query("SELECT n FROM NotificationClient n WHERE n.facture.id = :factureId " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> findByFactureId(@Param("factureId") Long factureId);
    
    /**
     * Trouve les notifications pour un versement donné
     * 
     * @param versementId identifiant du versement
     * @return liste des notifications du versement
     */
    @Query("SELECT n FROM NotificationClient n WHERE n.versement.id = :versementId " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> findByVersementId(@Param("versementId") Long versementId);
    
    /**
     * Trouve les notifications d'un client par statut
     * 
     * @param clientId identifiant du client
     * @param statut statut recherché
     * @return liste des notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.client.id = :clientId AND n.statut = :statut " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> findByClientIdAndStatut(
            @Param("clientId") Long clientId, 
            @Param("statut") String statut);
    
    /**
     * Trouve les notifications d'un client par catégorie
     * 
     * @param clientId identifiant du client
     * @param categorie catégorie recherchée
     * @return liste des notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.client.id = :clientId AND n.categorie = :categorie " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> findByClientIdAndCategorie(
            @Param("clientId") Long clientId, 
            @Param("categorie") String categorie);
    
    /**
     * Trouve les notifications par type (EMAIL, SMS, etc.)
     * 
     * @param typeNotification type de notification
     * @param pageable pagination
     * @return page de notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE n.typeNotification = :typeNotification " +
           "ORDER BY n.dateCreation DESC")
    Page<NotificationClient> findByTypeNotification(
            @Param("typeNotification") String typeNotification, 
            Pageable pageable);
    
    /**
     * Compte les notifications par statut
     * 
     * @param statut statut recherché
     * @return nombre de notifications
     */
    @Query("SELECT COUNT(n) FROM NotificationClient n WHERE n.statut = :statut")
    Long countByStatut(@Param("statut") String statut);
    
    /**
     * Compte les notifications en échec qui peuvent encore être réessayées
     * 
     * @return nombre de notifications
     */
    @Query("SELECT COUNT(n) FROM NotificationClient n WHERE " +
           "n.statut = 'ECHEC' AND n.tentatives < n.maxTentatives")
    Long countNotificationsAReessayer();
    
    /**
     * Compte les notifications d'un client par statut
     * 
     * @param clientId identifiant du client
     * @param statut statut recherché
     * @return nombre de notifications
     */
    @Query("SELECT COUNT(n) FROM NotificationClient n WHERE " +
           "n.client.id = :clientId AND n.statut = :statut")
    Long countByClientIdAndStatut(
            @Param("clientId") Long clientId, 
            @Param("statut") String statut);
    
    /**
     * Trouve les notifications non lues d'un client
     * 
     * @param clientId identifiant du client
     * @return liste des notifications non lues
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.client.id = :clientId AND " +
           "n.statut = 'ENVOYE' AND " +
           "n.dateLecture IS NULL " +
           "ORDER BY n.dateEnvoi DESC")
    List<NotificationClient> findNotificationsNonLues(@Param("clientId") Long clientId);
    
    /**
     * Trouve les notifications envoyées sur une période
     * 
     * @param dateDebut date de début
     * @param dateFin date de fin
     * @return liste des notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.statut = 'ENVOYE' AND " +
           "n.dateEnvoi BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY n.dateEnvoi DESC")
    List<NotificationClient> findNotificationsEnvoyeesBetweenDates(
            @Param("dateDebut") Date dateDebut, 
            @Param("dateFin") Date dateFin);
    
    /**
     * Trouve les notifications programmées pour une date future
     * 
     * @return liste des notifications programmées
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.statut = 'EN_ATTENTE' AND " +
           "n.datePrevueEnvoi > CURRENT_TIMESTAMP " +
           "ORDER BY n.datePrevueEnvoi ASC")
    List<NotificationClient> findNotificationsProgrammees();
    
    /**
     * Trouve les notifications avec tentatives épuisées
     * 
     * @return liste des notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.statut = 'ECHEC' AND " +
           "n.tentatives >= n.maxTentatives " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> findNotificationsTentativesEpuisees();
    
    /**
     * Trouve les notifications d'une facture par catégorie
     * 
     * @param factureId identifiant de la facture
     * @param categorie catégorie de notification
     * @return liste des notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.facture.id = :factureId AND " +
           "n.categorie = :categorie " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> findByFactureIdAndCategorie(
            @Param("factureId") Long factureId,
            @Param("categorie") String categorie);
    
    /**
     * Vérifie si une notification existe déjà pour une facture et une catégorie
     * Utile pour éviter les doublons de notifications
     * 
     * @param factureId identifiant de la facture
     * @param categorie catégorie de notification
     * @return true si au moins une notification existe
     */
    @Query("SELECT COUNT(n) > 0 FROM NotificationClient n WHERE " +
           "n.facture.id = :factureId AND " +
           "n.categorie = :categorie AND " +
           "n.statut IN ('EN_ATTENTE', 'EN_COURS', 'ENVOYE')")
    boolean existsByFactureIdAndCategorie(
            @Param("factureId") Long factureId,
            @Param("categorie") String categorie);
    
    /**
     * Trouve la dernière notification envoyée pour une facture
     * 
     * @param factureId identifiant de la facture
     * @return dernière notification ou null
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.facture.id = :factureId AND " +
           "n.statut = 'ENVOYE' " +
           "ORDER BY n.dateEnvoi DESC")
    List<NotificationClient> findLastNotificationByFacture(@Param("factureId") Long factureId);
    
    /**
     * Annule toutes les notifications en attente pour une facture
     * Utilisé quand une facture est annulée ou payée
     * 
     * @param factureId identifiant de la facture
     * @return nombre de notifications annulées
     */
    @Modifying
    @Query("UPDATE NotificationClient n SET n.statut = 'ANNULEE' WHERE " +
           "n.facture.id = :factureId AND " +
           "n.statut IN ('EN_ATTENTE', 'EN_COURS')")
    int annulerNotificationsFacture(@Param("factureId") Long factureId);
    
    /**
     * Marque toutes les notifications d'un client comme lues
     * 
     * @param clientId identifiant du client
     * @return nombre de notifications marquées comme lues
     */
    @Modifying
    @Query("UPDATE NotificationClient n SET n.dateLecture = CURRENT_TIMESTAMP WHERE " +
           "n.client.id = :clientId AND " +
           "n.statut = 'ENVOYE' AND " +
           "n.dateLecture IS NULL")
    int marquerToutesCommeLues(@Param("clientId") Long clientId);
    
    /**
     * Supprime les anciennes notifications (older than X days)
     * 
     * @param dateLimit date limite (notifications avant cette date seront supprimées)
     * @return nombre de notifications supprimées
     */
    @Modifying
    @Query("DELETE FROM NotificationClient n WHERE " +
           "n.dateCreation < :dateLimit AND " +
           "n.statut IN ('ENVOYE', 'ANNULEE')")
    int supprimerAnciennesNotifications(@Param("dateLimit") Date dateLimit);
    
    /**
     * Calcule des statistiques sur les notifications sur une période
     * 
     * @param dateDebut date de début
     * @param dateFin date de fin
     * @return tableau d'objets contenant les statistiques [statut, count]
     */
    @Query("SELECT n.statut, COUNT(n) FROM NotificationClient n WHERE " +
           "n.dateCreation BETWEEN :dateDebut AND :dateFin " +
           "GROUP BY n.statut")
    List<Object[]> getStatistiquesParStatut(
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin);
    
    /**
     * Calcule des statistiques par type de notification sur une période
     * 
     * @param dateDebut date de début
     * @param dateFin date de fin
     * @return tableau d'objets contenant les statistiques [typeNotification, count]
     */
    @Query("SELECT n.typeNotification, COUNT(n) FROM NotificationClient n WHERE " +
           "n.dateCreation BETWEEN :dateDebut AND :dateFin " +
           "GROUP BY n.typeNotification")
    List<Object[]> getStatistiquesParType(
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin);
    
    /**
     * Calcule le taux de réussite des notifications sur une période
     * 
     * @param dateDebut date de début
     * @param dateFin date de fin
     * @return taux de réussite en pourcentage
     */
    @Query("SELECT " +
           "CASE WHEN COUNT(n) > 0 THEN " +
           "(CAST(SUM(CASE WHEN n.statut = 'ENVOYE' THEN 1 ELSE 0 END) AS double) * 100.0 / COUNT(n)) " +
           "ELSE 0 END " +
           "FROM NotificationClient n WHERE " +
           "n.dateCreation BETWEEN :dateDebut AND :dateFin")
    Double calculerTauxReussite(
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin);
    
    /**
     * Trouve les notifications par priorité
     * 
     * @param priorite niveau de priorité (1=haute, 2=normale, 3=basse)
     * @param statut statut recherché (optionnel)
     * @return liste des notifications
     */
    @Query("SELECT n FROM NotificationClient n WHERE " +
           "n.priorite = :priorite AND " +
           "(:statut IS NULL OR n.statut = :statut) " +
           "ORDER BY n.dateCreation ASC")
    List<NotificationClient> findByPriorite(
            @Param("priorite") Integer priorite,
            @Param("statut") String statut);
    
    List<NotificationClient> findByClientIdOrderByDateCreationDesc(Long clientId);
    
    /**
     * Calcule les statistiques complètes des notifications sur une période donnée
     * Cette méthode retourne un DTO avec tous les compteurs et indicateurs
     * 
     * @param dateDebut date de début de la période
     * @param dateFin date de fin de la période
     * @return statistiques complètes des notifications
     */
    @Query("SELECT new com.mproduits.dto.NotificationStatistiques(" +
           "COUNT(n), " +
           "SUM(CASE WHEN n.statut = 'EN_ATTENTE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.statut = 'EN_COURS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.statut = 'ENVOYE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.statut = 'ECHEC' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.statut = 'ANNULEE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.typeNotification = 'EMAIL' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.typeNotification = 'SMS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.typeNotification = 'PUSH' THEN 1 ELSE 0 END), " +
           "CASE WHEN COUNT(n) > 0 THEN " +
           "(CAST(SUM(CASE WHEN n.statut = 'ENVOYE' THEN 1 ELSE 0 END) AS double) * 100.0 / COUNT(n)) " +
           "ELSE 0 END, " +
           "SUM(CASE WHEN n.statut = 'ECHEC' AND n.tentatives < n.maxTentatives THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.dateLecture IS NULL AND n.statut = 'ENVOYE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN n.dateLecture IS NOT NULL THEN 1 ELSE 0 END), " +
           "COALESCE(AVG(n.tentatives), 0)) " +
           "FROM NotificationClient n " +
           "WHERE n.dateCreation BETWEEN :dateDebut AND :dateFin")
    NotificationStatistiques calculerStatistiques(
            @Param("dateDebut") Date dateDebut, 
            @Param("dateFin") Date dateFin);
    
    /**
     * Récupère l'historique complet des notifications d'un client
     * Trié par date de création décroissante (plus récent en premier)
     * Inclut toutes les notifications quel que soit leur statut
     * 
     * @param clientId identifiant du client
     * @return liste complète des notifications du client triée par date décroissante
     */
    @Query("SELECT n FROM NotificationClient n WHERE n.client.id = :clientId " +
           "ORDER BY n.dateCreation DESC")
    List<NotificationClient> getHistoriqueNotificationClient(@Param("clientId") Long clientId);
    
       /**
     * Récupère l'historique paginé des notifications d'un client
     * Permet de gérer efficacement de grands volumes de notifications
     * 
     * @param clientId identifiant du client
     * @param pageable paramètres de pagination
     * @return page de notifications triée par date décroissante
     */
    @Query("SELECT n FROM NotificationClient n WHERE n.client.id = :clientId " +
           "ORDER BY n.dateCreation DESC")
    Page<NotificationClient> getHistoriqueNotificationClientPagine(
            @Param("clientId") Long clientId,
            Pageable pageable);
}
