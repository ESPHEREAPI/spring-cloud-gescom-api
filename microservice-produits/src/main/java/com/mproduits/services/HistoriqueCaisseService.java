package com.mproduits.services;

import com.mproduits.dto.MargeVenteDto;
import com.mproduits.dto.VenteDto;
import com.mproduits.enums.StatutVente;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Annee;
import com.mproduits.model.PrixAchat;
import com.mproduits.model.Produit;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.VenteRepositories;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service de gestion de l'historique de caisse
 * 
 * @author USER01
 */
@Service
public class HistoriqueCaisseService {

    private final VenteRepositories venteRepositories;
    private final LigneVenteRepositories ligneVenteRepositories;
    private final PrixAchatRepositories prixAchatRepositories;
    private final BoutiqueRepositories boutiqueRepositories;
    private final MapperDtoImpl mapperDtoImpl;

    public HistoriqueCaisseService(
            VenteRepositories venteRepositories,
            LigneVenteRepositories ligneVenteRepositories,
            PrixAchatRepositories prixAchatRepositories,
            MapperDtoImpl mapperDtoImpl,BoutiqueRepositories boutiqueRepositories) {
        this.venteRepositories = venteRepositories;
        this.ligneVenteRepositories = ligneVenteRepositories;
        this.prixAchatRepositories = prixAchatRepositories;
        this.mapperDtoImpl = mapperDtoImpl;
        this.boutiqueRepositories=boutiqueRepositories;
    }

    /**
     * Récupère la liste des années de vente pour un vendeur
     */
    public List<Annee> listeAnneeByVenteAndVendeur(String username,Long boutiqueid) {
        return venteRepositories.listeVenteUserByAnnee(username,boutiqueid);
    }

    /**
     * Récupère la liste de toutes les années de vente
     */
    public List<Annee> listeAnneeByVente(Long boutiqueid) {
        return venteRepositories.listeVenteByAnnee(boutiqueid);
    }

    /**
     * Récupère les dates de vente pour un vendeur et une année
     */
    public List<Date> listeDateVente(int anneeid, String vendeur,Long boutiqueid) {
        return venteRepositories.listeDateVentByVendeur(anneeid, vendeur, boutiqueid);
    }

    /**
     * Récupère les dates de vente pour une année
     */
    public List<Date> listeDateVente(int anneeid,Long boutiqueid) {
        return venteRepositories.listeDateVente(anneeid, boutiqueid);
    }

    /**
     * Récupère les dates de vente par année
     */
    public List<Date> listeDateVenteByAnnee(Long anneeid,Long boutiqueid) {
        return venteRepositories.listeDateVenteByAnnee(anneeid, boutiqueid);
    }

    /**
     * Récupère toutes les ventes d'un vendeur pour une date donnée
     */
    public List<VenteDto> allVenteByVendeurByDate(Date datevente, int anneid, String vendeur,Long boutiqueid) {
        return venteRepositories.allVentesByUsersDate(datevente, vendeur, anneid,boutiqueid)
                .stream()
                .filter(ve -> ve.getStatut() == StatutVente.TERMINEE)
                .map(mapperDtoImpl::mapperVentByVenteDto)
                .toList();
    }

    /**
     * Calcule la marge journalière
     */
    public List<MargeVenteDto> margeJournaliere(Date vente, int anneeid,long boutiqueid ) {
        return ligneVenteRepositories.listeProduitVendueJournalier(vente, anneeid,boutiqueid)
                .stream()
                .filter(lgv -> lgv.getVente().getStatut() == StatutVente.TERMINEE)
                .map(lv -> mapperDtoImpl.mapperMargeByLigneVente(lv, vente))
                .toList();
    }

    /**
     * Calcule la marge mensuelle
     */
    public List<MargeVenteDto> margeMensuel(Date debut, Date fin, int anneeid,Long boutiqueid) {
        return ligneVenteRepositories.listeProduitVendueMensuelle(debut, fin, anneeid, boutiqueid)
                .stream()
                .filter(lgv -> lgv.getVente().getStatut() == StatutVente.TERMINEE)
                .map(lv -> mapperDtoImpl.mapperMargeByLigneVente(lv, debut, fin))
                .toList();
    }

    /**
     * Valide et met à jour le prix d'achat si nécessaire
     */
    public MargeVenteDto validerPrixAchat(MargeVenteDto margeVenteDto, Date datevente) {
        var produit = mapperDtoImpl.mapperProduit(margeVenteDto.getP());
        var dateFinJournee = convertirEnFinDeJournee(datevente);
        
        var prixAchatOptional = prixAchatRepositories
                .findTopByProduitAndDatedebutLessThanEqualAndDatefinIsNullOrderByDatedebutDesc(
                        produit, dateFinJournee);

        if (prixAchatOptional.isEmpty()) {
            return creerNouveauPrixAchat(margeVenteDto, datevente, produit);
        }

        var prixAchatExistant = prixAchatOptional.get();
        
        if (prixAchatExistant.getPrix().compareTo(new BigDecimal(margeVenteDto.getAchat())) == 0) {
            return margeVenteDto;
        }

        return mettreAJourPrixAchat(margeVenteDto, datevente, produit, prixAchatExistant);
    }

    /**
     * Crée un nouveau prix d'achat
     */
    private MargeVenteDto creerNouveauPrixAchat(
            MargeVenteDto margeVenteDto, 
            Date datevente, 
            Produit produit) {
        
        var nouveauPrixAchat = PrixAchat.builder()
                .datedebut(datevente)
                .prix(new BigDecimal(margeVenteDto.getAchat().doubleValue()))
                .produit(produit)
                .usercreat(margeVenteDto.getUsercreat())
                .build();

        var prixAchatSauvegarde = prixAchatRepositories.save(nouveauPrixAchat);
        margeVenteDto.setPrixachatid(prixAchatSauvegarde.getId());
        
        return margeVenteDto;
    }

    /**
     * Met à jour un prix d'achat existant
     */
    private MargeVenteDto mettreAJourPrixAchat(
            MargeVenteDto margeVenteDto,
            Date datevente,
            Produit produit,
            PrixAchat prixAchatExistant) {
        
        // Clôture l'ancien prix d'achat
        prixAchatExistant.setDatefin(datevente);
        prixAchatRepositories.save(prixAchatExistant);

        // Crée le nouveau prix d'achat
        var nouveauPrixAchat = PrixAchat.builder()
                .datedebut(datevente)
                .prix(new BigDecimal(margeVenteDto.getAchat()))
                .produit(produit)
                .usercreat(margeVenteDto.getUsercreat())
                .build();
        
        prixAchatRepositories.save(nouveauPrixAchat);
        
        return margeVenteDto;
    }

    /**
     * Convertit une date en fin de journée (23:59:59.999)
     */
    private Date convertirEnFinDeJournee(Date date) {
        return Date.from(
                LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault())
                        .atTime(23, 59, 59, 999_000_000)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }
    public List<Date>allDateForCaissier(int anneeid,String username,Long boutique){
        return venteRepositories.listeDateVentByVendeur(anneeid, username,boutique);
    }
}