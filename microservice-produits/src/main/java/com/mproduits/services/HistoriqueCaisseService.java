/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.MargeVenteDto;
import com.mproduits.dto.VenteDto;
import com.mproduits.enums.StatutVente;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Annee;
import com.mproduits.model.PrixAchat;
import com.mproduits.repositories.LigneVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.VenteRepositories;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class HistoriqueCaisseService {

    VenteRepositories venteRepositories;
    LigneVenteRepositories ligneVenteRepositories;
    PrixAchatRepositories prixAchatRepositories;
    @Autowired
    MapperDtoImpl mapperDtoImpl;

    @Autowired
    public HistoriqueCaisseService(VenteRepositories venteRepositories, LigneVenteRepositories ligneVenteRepositories, PrixAchatRepositories prixAchatRepositories) {
        this.venteRepositories = venteRepositories;
        this.ligneVenteRepositories = ligneVenteRepositories;
        this.prixAchatRepositories = prixAchatRepositories;
    }

    public List<Annee> listeAnneeByVenteAndVendeur(String username) {
        List<Annee> listeAnneeByVente = venteRepositories.listeVenteUserByAnnee(username);
        return listeAnneeByVente;
    }

    public List<Annee> listeAnneeByVente() {
        List<Annee> listeAnneeByVente = venteRepositories.listeVenteByAnnee();
        return listeAnneeByVente;
    }

    public List<Date> listeDateVente(Long anneeid, String vendeur) {

        List<Date> allDates = venteRepositories.listeDateVentByVendeur(anneeid, vendeur);
        return allDates;
    }
 public List<Date> listeDateVente(Long anneeid) {

        List<Date> allDates = venteRepositories.listeDateVente(anneeid);
        return allDates;
    }
    public List<Date> listeDateVenteByAnnee(Long anneeid) {

        List<Date> allDates = venteRepositories.listeDateVenteByAnnee(anneeid);
        return allDates;
    }

    public List<VenteDto> allVenteByVendeurByDate(Date datevente, Long anneid, String vendeur) {
        List<VenteDto> allVentesDtos = venteRepositories.allVentesByUsersDate(datevente, vendeur, anneid).stream()
                .filter(ve -> ve.getStatut().equals(StatutVente.TERMINEE))
                .map(v -> mapperDtoImpl.mapperVentByVenteDto(v))
                .collect(Collectors.toList());
        return allVentesDtos;
    }

    public List<MargeVenteDto> margeJournaliere(Date Vente, Long anneeid) {
        List<MargeVenteDto> marge = ligneVenteRepositories.listeProduitVendueJournalier(Vente, anneeid).stream()
                .filter(lgv -> lgv.getVente().getStatut().equals(StatutVente.TERMINEE))
                .map(lv -> mapperDtoImpl.mapperMargeByLigneVente(lv, Vente))
                .collect(Collectors.toList());
        return marge;
    }

    public List<MargeVenteDto> margeMensuel(Date debut, Date fin, Long anneeid) {
        List<MargeVenteDto> marge = ligneVenteRepositories.listeProduitVendueMensuelle(debut, fin, anneeid).stream()
                .filter(lgv -> lgv.getVente().getStatut().equals(StatutVente.TERMINEE))
                .map(lv -> mapperDtoImpl.mapperMargeByLigneVente(lv, debut, fin))
                .collect(Collectors.toList());
        return marge;
    }

    public MargeVenteDto validerPrixAchat(MargeVenteDto margeVenteDto, Date datevente) {

        if (prixAchatRepositories.findTopByProduitAndDatedebutLessThanEqualAndDatefinIsNullOrderByDatedebutDesc(mapperDtoImpl.mapperProduit(margeVenteDto.getP()), convertirEnFinDeJournee(datevente)).isEmpty()) {
            PrixAchat pra = new PrixAchat();
            pra.setDatedebut(datevente);
            pra.setPrix(BigDecimal.valueOf(margeVenteDto.getAchat().longValue()));
            pra.setProduit(mapperDtoImpl.mapperProduit(margeVenteDto.getP()));
            pra.setUsercreat(margeVenteDto.getUsercreat());
            PrixAchat saveAchat = prixAchatRepositories.save(pra);
            margeVenteDto.setPrixachatid(saveAchat.getId());
            System.out.println("id new PrixAchat" + pra.getId());

            return margeVenteDto;
        }
        Optional<PrixAchat> pa = prixAchatRepositories.findTopByProduitAndDatedebutLessThanEqualAndDatefinIsNullOrderByDatedebutDesc(mapperDtoImpl.mapperProduit(margeVenteDto.getP()), convertirEnFinDeJournee(datevente));
        // Produit p = this.mapperDtoImpl.mapperProduit(margeVenteDto.getP());

        if (pa.isEmpty()) {

            throw new RuntimeException("prix achat n existe pas id " + margeVenteDto.getPrixachatid());
        }

        PrixAchat prixAchat = pa.get();
        if (prixAchat.getPrix().intValue() == margeVenteDto.getAchat().intValue()) {
            return margeVenteDto;
        }
        PrixAchat newPrixAchat = new PrixAchat();
        newPrixAchat.setDatedebut(datevente);
        newPrixAchat.setPrix(BigDecimal.valueOf(margeVenteDto.getAchat().longValue()));
        newPrixAchat.setUsercreat(margeVenteDto.getUsercreat());
        newPrixAchat.setProduit(mapperDtoImpl.mapperProduit(margeVenteDto.getP()));
        //pa.get().setPrix(BigDecimal.valueOf(margeVenteDto.getAchat().longValue()));
        prixAchat.setDatefin(datevente);
        prixAchatRepositories.save(prixAchat);
        prixAchatRepositories.save(newPrixAchat);

        return margeVenteDto;
    }

    private Date convertirEnFinDeJournee(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    
    
}
