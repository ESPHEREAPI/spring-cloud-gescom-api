package com.mproduits.repositories;


import com.mproduits.model.Entreprise;
import com.mproduits.model.Mois;
import com.mproduits.model.Personne;
import com.mproduits.model.Photocopie;
import com.mproduits.model.Photocopie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité Photocopie.
 * Fournit les opérations CRUD de base ainsi que des requêtes personnalisées
 * pour les besoins métier spécifiques.
 *
 * @author Équipe Développement
 * @version 1.0
 * @since 2026-01-27
 */
@Repository
public interface PhotocopieRepository extends JpaRepository<Photocopie, Long>, 
        JpaSpecificationExecutor<Photocopie> {

    /**
     * Trouve toutes les photocopies actives (non supprimées) pour un mois donné.
     *
     * @param mois Le mois comptable
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid  and p.boutique.id= :boutiqueid  and p.personne.userName= :username AND p.isDeleted = false ORDER BY p.dateReception DESC")
    Page<Photocopie> findByMoisAndNotDeleted(@Param("anneeid") int anneeid,@Param("boutiqueid") Long  boutiqueid,@Param("username") String  username, Pageable pageable);

    /**
     * Trouve toutes les photocopies actives pour un mois et une entreprise.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois comptable
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
//    @Query("SELECT p FROM Photocopie p WHERE p.entreprise = :entreprise AND p.mois = :mois AND p.isDeleted = false ORDER BY p.dateReception DESC")
//    Page<Photocopie> findByEntrepriseAndMoisAndNotDeleted(
//        @Param("entreprise") Entreprise entreprise,
//        @Param("mois") Mois mois,
//        Pageable pageable
//    );

    /**
     * Trouve toutes les photocopies pour une date spécifique.
     *
     * @param dateReception La date de réception
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    Page<Photocopie> findByDateReceptionAndBoutiqueIdAndIsDeletedFalse(LocalDate dateReception,Long boutiqueid, Pageable pageable);

    /**
     * Trouve toutes les photocopies entre deux dates pour un mois donné.
     *
     * @param mois Le mois comptable
     * @param dateDebut Date de début de la période
     * @param dateFin Date de fin de la période
     * @return Liste des photocopies
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid  and p.boutique.id= :boutiqueid  and p.personne.userName= :username and  p.dateReception BETWEEN :dateDebut AND :dateFin AND p.isDeleted = false ORDER BY p.dateReception DESC")
    List<Photocopie> findByMoisAndDateReceptionBetween(
        @Param("anneeid") int anneeid,
             @Param("boutiqueid") Long boutiqueid,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin,
        @Param("username") String username
    );

    /**
     * Recherche des photocopies par libellé (recherche partielle insensible à la casse).
     *
     * @param libelle Le libellé à rechercher
     * @param mois Le mois comptable
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid and  LOWER(p.libelle) LIKE LOWER(CONCAT('%', :libelle, '%')) AND p.isDeleted = false")
    Page<Photocopie> searchByLibelleInMois(
        @Param("libelle") String libelle,
        @Param("anneeid") int anneeid,
         @Param("boutiqueid") Long boutiqueid,
        Pageable pageable
    );

    /**
     * Calcule la somme totale des montants pour un mois donné.
     *
     * @param mois Le mois comptable
     * @return La somme des montants ou 0 si aucune photocopie
     */
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid AND p.isDeleted = false")
    BigDecimal calculateTotalByAnnee(@Param("anneeid") int anneeid,@Param("boutiqueid") Long boutiqueid);

    /**
     * Calcule la somme totale des montants pour un mois et une entreprise.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois comptable
     * @return La somme des montants ou 0 si aucune photocopie
     */
//    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Photocopie p WHERE p.entreprise = :entreprise AND p.mois = :mois AND p.isDeleted = false")
//    BigDecimal calculateTotalByEntrepriseAndMois(
//        @Param("anneeid") Entreprise entreprise,
//        @Param("mois") Mois mois
//    );

    /**
     * Calcule la somme des montants pour une date spécifique.
     *
     * @param dateReception La date de réception
     * @return La somme des montants
     */
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Photocopie p WHERE p.dateReception = :dateReception AND p.isDeleted = false")
    BigDecimal calculateTotalByDate(@Param("dateReception") LocalDate dateReception);

    /**
     * Calcule la somme des montants pour une date et une entreprise.
     *
     * @param entreprise L'entreprise
     * @param dateReception La date de réception
     * @return La somme des montants
     */
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid AND p.dateReception = :dateReception AND p.isDeleted = false")
    BigDecimal calculateTotalByEntrepriseAndDate(
        @Param("anneeid") int anneeid,
             @Param("boutiqueid") Long boutiqueid,
        @Param("dateReception") LocalDate dateReception
    );

    /**
     * Compte le nombre de photocopies pour un mois donné.
     *
     * @param mois Le mois comptable
     * @return Le nombre de photocopies
     */
    @Query("SELECT COUNT(p) FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid  AND p.isDeleted = false")
    Long countByAnnee(@Param("anneeid") int anneeid, @Param("boutiqueid") Long boutiqueid);

    /**
     * Compte le nombre de photocopies pour une entreprise et un mois.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois comptable
     * @return Le nombre de photocopies
     */
//    @Query("SELECT COUNT(p) FROM Photocopie p WHERE p.entreprise = :entreprise AND p.mois = :mois AND p.isDeleted = false")
//    Long countByEntrepriseAndMois(
//        @Param("entreprise") Entreprise entreprise,
//        @Param("mois") Mois mois
//    );

    /**
     * Trouve toutes les photocopies créées par un utilisateur spécifique.
     *
     * @param createdBy Identifiant de l'utilisateur
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    Page<Photocopie> findByCreatedByAndIsDeletedFalse(String createdBy, Pageable pageable);

    /**
     * Trouve la dernière photocopie créée pour un mois donné.
     *
     * @param mois Le mois comptable
     * @return La dernière photocopie créée
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid  AND p.isDeleted = false ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Photocopie> findLastCreatedByAnnee(@Param("anneeid") int anneeid, @Param("boutiqueid") Long boutiqueid);

    /**
     * Trouve les photocopies avec un montant supérieur à une valeur donnée.
     *
     * @param montantMin Le montant minimum
     * @param mois Le mois comptable
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid AND p.montant >= :montantMin AND p.isDeleted = false ORDER BY p.montant DESC")
    Page<Photocopie> findByAnneeAndMontantGreaterThanEqual(
        @Param("montantMin") BigDecimal montantMin,
        @Param("anneeid") int anneeid,
         @Param("boutiqueid") Long boutiqueid,
        Pageable pageable
    );

    /**
     * Trouve les photocopies avec un montant compris dans une fourchette.
     *
     * @param montantMin Le montant minimum
     * @param montantMax Le montant maximum
     * @param mois Le mois comptable
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid AND p.montant BETWEEN :montantMin AND :montantMax AND p.isDeleted = false ORDER BY p.montant DESC")
    Page<Photocopie> findByAnneeAndMontantBetween(
        @Param("montantMin") BigDecimal montantMin,
        @Param("montantMax") BigDecimal montantMax,
        @Param("anneeid") int anneeid,
         @Param("boutiqueid") Long boutiqueid,
        Pageable pageable
    );

    /**
     * Vérifie si une photocopie existe pour une date et une entreprise données.
     *
     * @param entreprise L'entreprise
     * @param dateReception La date de réception
     * @param libelle Le libellé
     * @return true si une photocopie similaire existe
     */
//    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Photocopie p WHERE p.entreprise = :entreprise AND p.dateReception = :dateReception AND p.libelle = :libelle AND p.isDeleted = false")
//    boolean existsByEntrepriseAndDateReceptionAndLibelle(
//        @Param("entreprise") Entreprise entreprise,
//        @Param("dateReception") LocalDate dateReception,
//        @Param("libelle") String libelle
//    );

    /**
     * Trouve toutes les photocopies supprimées (pour restauration éventuelle).
     *
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies supprimées
     */
    @Query("SELECT p FROM Photocopie p WHERE  p.boutique.id= :boutiqueid and  p.isDeleted = true ORDER BY p.deletedAt DESC")
    Page<Photocopie> findDeleted( @Param("boutiqueid") Long boutiqueid,Pageable pageable);

    /**
     * Statistiques groupées par date pour un mois donné.
     *
     * @param mois Le mois comptable
     * @return Liste de tableaux [date, somme, nombre]
     */
    @Query("SELECT p.dateReception, SUM(p.montant), COUNT(p) FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid and  p.isDeleted = false GROUP BY p.dateReception ORDER BY p.dateReception")
    List<Object[]> getStatisticsByDateForAnnee(@Param("anneeid") int anneeid,@Param("boutiqueid") Long boutiqueid);

    /**
     * Trouve les photocopies avec un commentaire.
     *
     * @param mois Le mois comptable
     * @param pageable Les paramètres de pagination
     * @return Une page de photocopies avec commentaire
     */
    @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid AND  p.commentaire IS NOT NULL AND p.isDeleted = false")
    Page<Photocopie> findByMoisWithCommentaire(@Param("anneeid") int anneeid,@Param("boutiqueid") Long boutiqueid, Pageable pageable);
    
     @Query("SELECT p FROM Photocopie p WHERE p.entreprise.annee.id = :anneeid AND p.boutique.id= :boutiqueid  AND p.isDeleted = false and MONTH(p.dateReception) = :mois ORDER BY p.dateReception ")
    List<Photocopie> listePhotocopieByBoutiqueByAnnee(@Param("anneeid") int anneeid, @Param("boutiqueid") Long boutiqueid,@Param("mois") int mois);
    
    
    /**
     * Récupère toutes les dates avec des photocopies pour un mois donné.
     * Utilisé pour remplir le sélecteur de date.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @return Liste des dates distinctes
     */
    @Query("""
        SELECT DISTINCT p.dateReception
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.isDeleted = false
        ORDER BY p.dateReception DESC
    """)
    List<LocalDate> findDistinctDatesByMois(
        @Param("entreprise") Entreprise entreprise,
             @Param("boutiqueid") Long boutiqueid,
        @Param("mois") Mois mois
    );

    /**
     * Récupère l'historique des photocopies pour une date et un mois donnés.
     * Version sans filtre par caissier.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @param date La date
     * @param pageable Paramètres de pagination
     * @return Page de photocopies
     */
    @Query("""
        SELECT p FROM Photocopie p
        LEFT JOIN FETCH p.personne per
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.dateReception = :date
        AND p.isDeleted = false
        ORDER BY p.createdAt DESC
    """)
    Page<Photocopie> findByDateAndMois(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
        @Param("date") LocalDate date,
         @Param("boutiqueid") Long boutiqueid,
        Pageable pageable
    );

    /**
     * Récupère l'historique des photocopies pour une date, un mois et un caissier.
     * Version avec filtre par caissier (mode multi-caisse).
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @param date La date
     * @param personne Le caissier
     * @param pageable Paramètres de pagination
     * @return Page de photocopies
     */
    @Query("""
        SELECT p FROM Photocopie p
     
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois
        AND p.dateReception = :date
        AND p.personne = :personne
        AND p.isDeleted = false
        ORDER BY p.createdAt DESC
    """)
    Page<Photocopie> findByDateAndMoisAndPersonne(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
        @Param("date") LocalDate date,
        @Param("personne") Personne personne,
        Pageable pageable
    );

    /**
     * Calcule le total des photocopies pour une date donnée.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @param date La date
     * @return Total des montants
     */
    @Query("""
        SELECT COALESCE(SUM(p.montant), 0)
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.dateReception = :date
        AND p.isDeleted = false
    """)
    BigDecimal calculateTotalByDate(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
         @Param("boutiqueid") Long boutiqueid,
        @Param("date") LocalDate date
    );

    /**
     * Calcule le total des photocopies pour une date et un caissier.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @param date La date
     * @param personne Le caissier
     * @return Total des montants
     */
    @Query("""
        SELECT COALESCE(SUM(p.montant), 0)
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois
        AND p.dateReception = :date
        AND p.personne = :personne
        AND p.isDeleted = false
    """)
    BigDecimal calculateTotalByDateAndPersonne(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
        @Param("date") LocalDate date,
        @Param("personne") Personne personne
    );

    /**
     * Calcule le total des photocopies pour un mois complet.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @return Total des montants
     */
    @Query("""
        SELECT COALESCE(SUM(p.montant), 0)
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.isDeleted = false
    """)
    BigDecimal calculateTotalByMois(
        @Param("entreprise") Entreprise entreprise,
             @Param("boutiqueid") Long boutiqueid,
        @Param("mois") Mois mois
    );

    /**
     * Compte le nombre d'opérations pour une date.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @param date La date
     * @return Nombre d'opérations
     */
    @Query("""
        SELECT COUNT(p)
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.dateReception = :date
        AND p.isDeleted = false
    """)
    Long countByDate(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
         @Param("boutiqueid") Long boutiqueid,
        @Param("date") LocalDate date
    );

    /**
     * Récupère les statistiques par caissier pour un mois.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @return Liste de [personne, nombre, total]
     */
    @Query("""
        SELECT p.personne.nom,
               COUNT(p),
               COALESCE(SUM(p.montant), 0)
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.isDeleted = false
        GROUP BY p.personne.nom
        ORDER BY SUM(p.montant) DESC
    """)
    List<Object[]> getStatisticsByPersonne(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
         @Param("boutiqueid") Long boutiqueid
    );

    /**
     * Récupère les statistiques par jour pour un mois.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @return Liste de [date, nombre, total]
     */
    @Query("""
        SELECT p.dateReception,
               COUNT(p),
               COALESCE(SUM(p.montant), 0)
        FROM Photocopie p
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois and p.boutique.id= :boutiqueid
        AND p.isDeleted = false
        GROUP BY p.dateReception
        ORDER BY p.dateReception
    """)
    List<Object[]> getStatisticsByDate(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
         @Param("boutiqueid") Long boutiqueid
    );

    /**
     * Recherche dans les photocopies par libellé.
     *
     * @param entreprise L'entreprise
     * @param mois Le mois
     * @param searchTerm Terme de recherche
     * @param pageable Paramètres de pagination
     * @return Page de photocopies
     */
    @Query("""
        SELECT p FROM Photocopie p
        LEFT JOIN FETCH p.personne per
        WHERE p.entreprise = :entreprise
        AND p.mois = :mois
        AND LOWER(p.libelle) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        AND p.isDeleted = false and p.boutique.id= :boutiqueid
        ORDER BY p.dateReception DESC, p.createdAt DESC
    """)
    Page<Photocopie> searchByLibelle(
        @Param("entreprise") Entreprise entreprise,
        @Param("mois") Mois mois,
        @Param("boutiqueid") Long boutiqueid,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    //recuper les mois d une caissiere
    
    @Query("SELECT p.mois FROM Photocopie p WHERE p.entreprise.annee.id =:anneeid and p.personne.userName= :username")
    
    List<Mois>listMoisByAnnee(@Param("anneeid") int  anneeid, @Param("username") String  userName);
    
    @Query("SELECT p.mois FROM Photocopie p WHERE p.entreprise.annee.id =:anneeid and p.boutique.id= :boutiqueid")
    
    List<Mois>listMoisByAnnee(@Param("anneeid") int  anneeid,@Param("boutiqueid") Long  boutiqueid);
    
    @Query("SELECT p.personne FROM Photocopie p WHERE  p.boutique.id= :boutiqueid and p.personne.profilid.code= :profile")
    
    List<Personne>listCaissier(@Param("boutiqueid") Long  boutiqueid,@Param("profile") String  profile);
}
