/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.repositories;

import com.mproduits.enums.StatutVente;
import com.mproduits.model.Annee;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Vente;
import feign.Param;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author USER01
 */
public interface VenteRepositories extends JpaRepository<Vente, Long>, JpaSpecificationExecutor<Vente> {

    Optional<Vente> findByEntrepriseAndNumeroTicketAndBoutiqueId(Entreprise entreprise, String numeroTicket,Long id);

    long countByEntreprise(Entreprise entreprise);

    @Query("SELECT v.entreprise.annee FROM Vente v WHERE v.vendeur.userName= :username and v.boutique.id= :boutiqueid")
    public List<Annee> listeVenteUserByAnnee(@Param("username") String username,@Param("boutiqueid") Long boutiqueid);

    @Query("SELECT DISTINCT DATE(v.dateVente) FROM Vente v WHERE v.entreprise.annee.id= :annee and v.vendeur.userName= :username and v.boutique.id= :boutiqueid ")
    public List<Date> listeDateVentByVendeur(@Param("annee") int annee, @Param("username") String username,@Param("boutiqueid") Long boutiqueid);

    @Query("SELECT DISTINCT DATE(v.dateVente) FROM Vente v WHERE v.entreprise.annee.id= :annee and v.boutique.id= :boutiqueid ")
    public List<Date> listeDateVente(@Param("annee") int annee,@Param("boutiqueid") Long boutiqueid);
     @Query("SELECT  v FROM Vente v WHERE v.entreprise.annee.id= :annee and v.boutique.id= :boutiqueid and MONTH(v.dateVente) = :mois ")
    public List<Vente> listeAllVenteByBoutique(@Param("annee") int annee,@Param("boutiqueid") Long boutiqueid,@Param("mois")int mois);

    @Query("SELECT v FROM Vente v WHERE v.vendeur.userName= :vendeur and DATE(v.dateVente)= :dateVente and v.boutique.id= :boutiqueid  ")
    public Vente findByVendeurAndDateVente(String vendeur, Date dateVente,@Param("boutiqueid") Long boutiqueid);

    @Query("SELECT v FROM Vente v WHERE DATE(v.dateVente)= :datevente and v.vendeur.userName= :vendeur and v.entreprise.annee.id= :anneeid and v.boutique.id= :boutiqueid")
    public List<Vente> allVentesByUsersDate(@Param("datevente") Date datevente, @Param("vendeur") String vendeur, @Param("anneeid") int anneeid,@Param("boutiqueid") long boutiqueid);

    @Query("SELECT DISTINCT(v.entreprise.annee) FROM Vente v where v.boutique.id= :boutiqueid")
    public List<Annee> listeVenteByAnnee(@Param("boutiqueid") Long boutiqueid);

    @Query("SELECT DISTINCT DATE(v.dateVente) FROM Vente v WHERE v.entreprise.annee.id= :annee  and v.boutique.id= :boutiqueid ")
    public List<Date> listeDateVenteByAnnee(@Param("annee") Long annee,@Param("boutiqueid") Long boutiqueid);

    @Query("SELECT v FROM Vente v "
            + "WHERE (COALESCE(:search, '') = '' OR LOWER(v.numeroTicket) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(v.client.nom) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "AND (:statut = '' OR v.statut = :statut) "
            + "AND (:from IS NULL OR v.dateVente >= :from) "
            + "AND (:to IS NULL OR v.dateVente <= :to) and v.boutique.id= :boutiqueid ")
    Page<Vente> searchVentes(
            @Param("search") String search,
            @Param("statut") String statut,
            @Param("from") Date from,
            @Param("to") Date to,
            Pageable pageable,
            @Param("boutiqueid") Long boutiqueid
    );

    @Query("SELECT COUNT(v) FROM Vente v where  v.boutique.id= :boutiqueid ")
    long nbrRow(@Param("boutiqueid") Long boutiqueid);

  //  public Orders findByNumero(String numero);
    @Query("SELECT v FROM Vente v WHERE v.userecom= :username and v.entreprise.annee.id= :anneeid")
        public List<Vente> allVentesByUsernameEcom(@Param("username") String username, @Param("anneeid") long anneeid);
        @Query("SELECT v FROM Vente v  WHERE v.numerocommande= :numerocommande and v.statut= :statut and v.entreprise.annee.id= :anneeid and v.boutique.id= :boutiqueid ")
      Optional<Vente> findByNumeroTicketAndStatutForCommande(@Param("numerocommande") long numerocommande,
                                                       @Param("statut") StatutVente statut,@Param("anneeid") int anneeid,@Param("boutiqueid") Long boutiqueid);
}
