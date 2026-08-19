/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.enums.CanalOrigine;
import com.mproduits.enums.StatutDevis;
import com.mproduits.model.Devis;
import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Devis
 * 
 * MÉTHODES DISPONIBLES:
 * ====================
 * 
 * 1. RECHERCHES SIMPLES
 *    - findByNumeroDevis           : Recherche par numéro unique
 *    - findByClientId              : Tous les devis d'un client
 *    - findByStatut                : Devis par statut
 *    - findByClientIdAndStatut     : Combinaison client + statut
 * 
 * 2. RECHERCHES TEMPORELLES
 *    - findByDateDevisBetween      : Devis créés entre 2 dates
 *    - findByDateExpirationBefore  : Devis expirant avant une date
 *    - findByDateExpirationAfter   : Devis expirant après une date
 * 
 * 3. STATISTIQUES
 *    - countByStatut               : Nombre de devis par statut
 *    - countByClientId             : Nombre de devis d'un client
 *    - countByDateDevisBetween     : Nombre de devis dans une période
 *    - sumTotalByStatut            : Somme des montants par statut
 * 
 * 4. ALERTES ET MONITORING
 *    - findDevisProchesExpiration  : Devis expirant dans X jours
 *    - findDevisExpires            : Devis déjà expirés EN_ATTENTE
 * 
 * 5. AUTRES
 *    - findTopDevisByClient        : X derniers devis d'un client
 *    - findDevisWithHighAmount     : Devis > montant donné
 * 
 * @author USER01
 * @version 2.0
 */
/**
 *
 * @author USER01
 */
public interface DevisRepository  extends JpaRepository<Devis, Long>{
//    @Query("SELECT d FROM Devis d WHERE d.client.id= :clientId")
//    List<Devis> findByClientId(Long clientId);
    
//    @Query("SELECT d FROM Devis d WHERE d.client.id = :clientId")
//    List<Devis> findByClientId(@Param("clientId") Long clientId);
//    
    @Query("SELECT d FROM Devis d WHERE d.statut = :statut")
    List<Devis> findByStatut(@Param("statut") String statut);
    
     // ================================================================
    // SECTION 1: RECHERCHES SIMPLES
    // ================================================================

    /**
     * Recherche un devis par son numéro unique
     * Ex: DEV-2025-0001
     * 
     * @param numeroDevis Numéro du devis
     * @return Optional contenant le devis si trouvé
     */
    Optional<Devis> findByNumeroDevis(String numeroDevis);

    // Lookups scopes compagnie (via Devis.boutique.compagnie) - a utiliser a
    // la place de findById()/findByNumeroDevis() partout ou l'id/numero
    // vient d'une requete client (sinon IDOR : lecture/action cross-tenant).
    Optional<Devis> findByIdAndBoutique_Compagnie_Id(Long id, Long compagnieId);

    Optional<Devis> findByNumeroDevisAndBoutique_Compagnie_Id(String numeroDevis, Long compagnieId);

    org.springframework.data.domain.Page<Devis> findByBoutique_Compagnie_Id(Long compagnieId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT d FROM Devis d WHERE d.boutique.compagnie.id = :compagnieId AND d.dateDevis BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY d.dateDevis DESC")
    List<Devis> findByCompagnieIdAndDateDevisBetween(@Param("compagnieId") Long compagnieId,
                                                       @Param("dateDebut") Date dateDebut,
                                                       @Param("dateFin") Date dateFin);

    /**
     * Récupère tous les devis d'un client spécifique
     * Triés par date décroissante (plus récents en premier)
     * 
     * @param clientId ID du client
     * @return Liste des devis du client
     */
    @Query("SELECT d FROM Devis d WHERE d.client.id = :clientId  and d.boutique.id= :boutiqueid ORDER BY d.dateDevis DESC")
    List<Devis> findByClientId(@Param("clientId") Long clientId,@Param("boutiqueid") Long boutiqueid);

    /**
     * Récupère tous les devis ayant un statut donné
     * 
     * @param statut Statut recherché (EN_ATTENTE, ACCEPTE, etc.)
     * @return Liste des devis avec ce statut
     */
    List<Devis> findByStatutAndBoutiqueId(StatutDevis statut,Long boutiqueid);

    // Ecran admin "Commandes en ligne" (voir CanalOrigine) : ne montrer que
    // les devis soumis par un client via le site public, jamais les devis
    // internes crees par le personnel.
    List<Devis> findByStatutAndCanalOrigineAndBoutiqueId(StatutDevis statut, CanalOrigine canalOrigine, Long boutiqueid);

    // "Mes commandes" cote client connecte (voir EcomCheckoutController) -
    // scope par client ET compagnie (jamais boutique seule, un client peut
    // avoir commande dans plusieurs boutiques de la meme compagnie).
    List<Devis> findByClient_IdAndBoutique_Compagnie_IdOrderByDateDevisDesc(Long clientId, Long compagnieId);

    /**
     * Recherche combinée: devis d'un client avec un statut spécifique
     * 
     * @param clientId ID du client
     * @param statut Statut recherché
     * @return Liste des devis correspondants
     */
    @Query("SELECT d FROM Devis d WHERE d.client.id = :clientId AND d.statut = :statut  and d.boutique.id= :boutiqueid " +
           "ORDER BY d.dateDevis DESC")
    List<Devis> findByClientIdAndStatut(
            @Param("clientId") Long clientId, 
            @Param("statut") StatutDevis statut, @Param("boutiqueid") Long boutiqueid);

    /**
     * Recherche les devis dont le numéro contient une chaîne
     * Utile pour recherche partielle
     * 
     * @param numeroPartiel Partie du numéro à rechercher
     * @return Liste des devis correspondants
     */
    List<Devis> findByNumeroDevisContaining(String numeroPartiel);

    // ================================================================
    // SECTION 2: RECHERCHES TEMPORELLES
    // ================================================================

    /**
     * Récupère les devis créés entre deux dates
     * 
     * @param dateDebut Date de début (inclusive)
     * @param dateFin Date de fin (inclusive)
     * @return Liste des devis dans la période
     */
    @Query("SELECT d FROM Devis d WHERE d.dateDevis BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY d.dateDevis DESC")
    List<Devis> findByDateDevisBetween(
            @Param("dateDebut") Date dateDebut, 
            @Param("dateFin") Date dateFin);

    /**
     * Récupère les devis expirant avant une date donnée
     * 
     * @param date Date limite
     * @return Liste des devis expirant avant cette date
     */
    @Query("SELECT d FROM Devis d WHERE d.dateExpiration < :date " +
           "AND d.statut = 'EN_ATTENTE' ORDER BY d.dateExpiration ASC")
    List<Devis> findByDateExpirationBefore(@Param("date") Date date);

    /**
     * Récupère les devis expirant après une date donnée
     * 
     * @param date Date de référence
     * @return Liste des devis expirant après cette date
     */
    @Query("SELECT d FROM Devis d WHERE d.dateExpiration > :date " +
           "AND d.statut = 'EN_ATTENTE' ORDER BY d.dateExpiration ASC")
    List<Devis> findByDateExpirationAfter(@Param("date") Date date);

    /**
     * Récupère les devis expirant entre deux dates
     * Utile pour alertes programmées
     * 
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des devis expirant dans cette période
     */
    @Query("SELECT d FROM Devis d WHERE d.dateExpiration BETWEEN :dateDebut AND :dateFin " +
           "AND d.statut = 'EN_ATTENTE' ORDER BY d.dateExpiration ASC")
    List<Devis> findByDateExpirationBetween(
            @Param("dateDebut") Date dateDebut, 
            @Param("dateFin") Date dateFin);

    /**
     * Récupère les devis créés aujourd'hui
     * 
     * @return Liste des devis du jour
     */
    @Query("SELECT d FROM Devis d WHERE CAST(d.dateDevis AS date) = CURRENT_DATE " +
           "ORDER BY d.dateCreation DESC")
    List<Devis> findCreatedToday();

    /**
     * Récupère les devis créés dans les X derniers jours
     * 
     * @param jours Nombre de jours
     * @return Liste des devis récents
     */
    @Query("SELECT d FROM Devis d WHERE d.dateDevis >= :dateDebut " +
           "ORDER BY d.dateDevis DESC")
    List<Devis> findCreatedInLastDays(@Param("dateDebut") Date dateDebut);

    // ================================================================
    // SECTION 3: STATISTIQUES ET COMPTAGES
    // ================================================================

    /**
     * Compte le nombre de devis par statut
     * 
     * @param statut Statut à compter
     * @return Nombre de devis
     */
    long countByStatut(StatutDevis statut);

    /**
     * Compte le nombre de devis d'un client
     * 
     * @param clientId ID du client
     * @return Nombre de devis
     */
    @Query("SELECT COUNT(d) FROM Devis d WHERE d.client.id = :clientId and d.boutique.id= :boutiqueid")
    long countByClientId(@Param("clientId") Long clientId,Long boutiqueid);

    /**
     * Compte les devis créés entre deux dates
     * 
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Nombre de devis
     */
    long countByDateDevisBetween(Date dateDebut, Date dateFin);

    /**
     * Calcule la somme des montants TTC par statut
     * Utile pour statistiques financières
     * 
     * @param statut Statut à analyser
     * @return Somme des montants (null si aucun devis)
     */
    @Query("SELECT COALESCE(SUM(d.total), 0) FROM Devis d WHERE d.statut = :statut")
    BigDecimal sumTotalByStatut(@Param("statut") StatutDevis statut);

    /**
     * Calcule la somme totale de tous les devis
     * 
     * @return Montant total
     */
    @Query("SELECT COALESCE(SUM(d.total), 0) FROM Devis d")
    BigDecimal sumTotalAllDevis();

    /**
     * Calcule la somme des devis d'un client
     * 
     * @param clientId ID du client
     * @return Montant total pour ce client
     */
    @Query("SELECT COALESCE(SUM(d.total), 0) FROM Devis d WHERE d.client.id = :clientId")
    BigDecimal sumTotalByClientId(@Param("clientId") Long clientId);

    /**
     * Calcule le montant moyen des devis
     * 
     * @return Montant moyen
     */
    @Query("SELECT COALESCE(AVG(d.total), 0) FROM Devis d")
    BigDecimal averageTotalDevis();

    /**
     * Calcule le montant moyen par statut
     * 
     * @param statut Statut à analyser
     * @return Montant moyen
     */
    @Query("SELECT COALESCE(AVG(d.total), 0) FROM Devis d WHERE d.statut = :statut")
    BigDecimal averageTotalByStatut(@Param("statut") StatutDevis statut);

    // ================================================================
    // SECTION 4: ALERTES ET MONITORING
    // ================================================================

    /**
     * Récupère les devis proches de l'expiration
     * Retourne les devis EN_ATTENTE expirant dans les X prochains jours
     * 
     * @param dateLimite Date limite (aujourd'hui + X jours)
     * @param maintenant Date actuelle
     * @return Liste des devis proches expiration
     */
    @Query("SELECT d FROM Devis d WHERE d.statut = 'EN_ATTENTE' " +
           "AND d.dateExpiration BETWEEN :maintenant AND :dateLimite " +
           "ORDER BY d.dateExpiration ASC")
    List<Devis> findDevisProchesExpiration(
            @Param("maintenant") Date maintenant,
            @Param("dateLimite") Date dateLimite);

    /**
     * Récupère les devis expirés (EN_ATTENTE avec date dépassée)
     * 
     * @param maintenant Date actuelle
     * @return Liste des devis expirés
     */
    @Query("SELECT d FROM Devis d WHERE d.statut = 'EN_ATTENTE' " +
           "AND d.dateExpiration < :maintenant " +
           "ORDER BY d.dateExpiration DESC")
    List<Devis> findDevisExpires(@Param("maintenant") Date maintenant);

    /**
     * Compte les devis expirés non traités
     * 
     * @param maintenant Date actuelle
     * @return Nombre de devis expirés
     */
    @Query("SELECT COUNT(d) FROM Devis d WHERE d.statut = 'EN_ATTENTE' " +
           "AND d.dateExpiration < :maintenant")
    long countDevisExpires(@Param("maintenant") Date maintenant);

    /**
     * Vérifie si un client a des devis EN_ATTENTE
     * 
     * @param clientId ID du client
     * @return true si le client a au moins un devis EN_ATTENTE
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Devis d " +
           "WHERE d.client.id = :clientId AND d.statut = 'EN_ATTENTE'")
    boolean hasDevisEnAttenteByClientId(@Param("clientId") Long clientId);

    // ================================================================
    // SECTION 5: REQUÊTES AVANCÉES
    // ================================================================

    /**
     * Récupère les X derniers devis d'un client
     * 
     * @param clientId ID du client
     * @param limit Nombre maximum de résultats
     * @return Liste des derniers devis
     */
    @Query(value = "SELECT * FROM devis WHERE client_id = :clientId " +
           "ORDER BY date_devis DESC LIMIT :limit", nativeQuery = true)
    List<Devis> findTopDevisByClient(
            @Param("clientId") Long clientId, 
            @Param("limit") int limit);

    /**
     * Récupère les devis avec montant supérieur à un seuil
     * Utile pour identifier les "gros" devis
     * 
     * @param montantMin Montant minimum
     * @return Liste des devis au-dessus du seuil
     */
    @Query("SELECT d FROM Devis d WHERE d.total >= :montantMin " +
           "ORDER BY d.total DESC")
    List<Devis> findDevisWithHighAmount(@Param("montantMin") BigDecimal montantMin);

    /**
     * Récupère les devis d'un client sur une période avec un statut
     * Requête complète pour reporting
     * 
     * @param clientId ID du client
     * @param statut Statut recherché
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Liste des devis correspondants
     */
    @Query("SELECT d FROM Devis d WHERE d.client.id = :clientId " +
           "AND d.statut = :statut " +
           "AND d.dateDevis BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY d.dateDevis DESC")
    List<Devis> findByClientStatutAndPeriode(
            @Param("clientId") Long clientId,
            @Param("statut") StatutDevis statut,
            @Param("dateDebut") Date dateDebut,
            @Param("dateFin") Date dateFin);

    /**
     * Recherche les devis par mot-clé dans remarques
     * 
     * @param keyword Mot-clé à rechercher
     * @return Liste des devis contenant le mot-clé
     */
    @Query("SELECT d FROM Devis d WHERE LOWER(d.remarques) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY d.dateDevis DESC")
    List<Devis> findByRemarquesContaining(@Param("keyword") String keyword);

    /**
     * Récupère les devis créés par un utilisateur
     * 
     * @param username Nom d'utilisateur
     * @return Liste des devis créés par cet utilisateur
     */
    @Query("SELECT d FROM Devis d WHERE d.usernameCreate = :username " +
           "ORDER BY d.dateCreation DESC")
    List<Devis> findByUsernameCreate(@Param("username") String username);

    /**
     * Récupère les devis liés à une facture
     * 
     * @param factureId ID de la facture
     * @return Devis lié à cette facture
     */
    @Query("SELECT d FROM Devis d WHERE d.facture.id = :factureId")
    Optional<Devis> findByFactureId(@Param("factureId") Long factureId);

    /**
     * Récupère les devis convertis (ayant une facture associée)
     * 
     * @return Liste des devis convertis
     */
    @Query("SELECT d FROM Devis d WHERE d.facture IS NOT NULL " +
           "ORDER BY d.dateConversion DESC")
    List<Devis> findDevisConvertis();

    /**
     * Vérifie si un numéro de devis existe déjà
     * Utile pour validation avant création
     * 
     * @param numeroDevis Numéro à vérifier
     * @return true si le numéro existe déjà
     */
    boolean existsByNumeroDevis(String numeroDevis);

    // ================================================================
    // SECTION 6: STATISTIQUES AVANCÉES
    // ================================================================

    /**
     * Récupère les statistiques par mois pour une année
     * Retourne: mois, nombre de devis, montant total
     * 
     * @param annee Année à analyser
     * @return Liste de tableaux [mois, count, total]
     */
    @Query(value = "SELECT MONTH(date_devis) as mois, " +
           "COUNT(*) as nombre, " +
           "COALESCE(SUM(total_ttc), 0) as montant " +
           "FROM devis " +
           "WHERE YEAR(date_devis) = :annee " +
           "GROUP BY MONTH(date_devis) " +
           "ORDER BY mois", nativeQuery = true)
    List<Object[]> getStatistiquesMensuelles(@Param("annee") int annee);

    /**
     * Top X clients par nombre de devis
     * 
     * @param limit Nombre de résultats
     * @return Liste [clientId, nom, nombre_devis]
     */
    @Query(value = "SELECT c.id, c.nom, COUNT(d.id) as nb_devis " +
           "FROM devis d " +
           "INNER JOIN client c ON d.client_id = c.id " +
           "GROUP BY c.id, c.nom " +
           "ORDER BY nb_devis DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopClientsByNombreDevis(@Param("limit") int limit);

    /**
     * Top X clients par montant total de devis
     * 
     * @param limit Nombre de résultats
     * @return Liste [clientId, nom, montant_total]
     */
    @Query(value = "SELECT c.id, c.nom, COALESCE(SUM(d.total_ttc), 0) as montant_total " +
           "FROM devis d " +
           "INNER JOIN client c ON d.client_id = c.id " +
           "GROUP BY c.id, c.nom " +
           "ORDER BY montant_total DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopClientsByMontant(@Param("limit") int limit);

    /**
     * Récupère le taux de conversion par période
     * Retourne: total_devis, acceptes, convertis, taux
     * 
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Tableau [total, acceptes, convertis]
     */
    @Query(value = "SELECT " +
           "COUNT(*) as total, " +
           "SUM(CASE WHEN statut = 'ACCEPTE' THEN 1 ELSE 0 END) as acceptes, " +
           "SUM(CASE WHEN statut = 'CONVERTI' THEN 1 ELSE 0 END) as convertis " +
           "FROM devis " +
           "WHERE date_devis BETWEEN :dateDebut AND :dateFin", nativeQuery = true)
    Object[] getTauxConversionByPeriode(
            @Param("dateDebut") Date dateDebut, 
            @Param("dateFin") Date dateFin);
    
    List<Devis>findByBoutiqueId(Long boutiqueid);
    
  
    
    // Récupérer le dernier numéro de devis de l'année
    @Query("SELECT MAX(CAST(SUBSTRING(d.numeroDevis, LENGTH(d.numeroDevis) - 3, 4) AS int)) " +
           "FROM Devis d WHERE d.numeroDevis LIKE :pattern")
    Integer findMaxNumeroByPattern(@Param("pattern") String pattern);
}
