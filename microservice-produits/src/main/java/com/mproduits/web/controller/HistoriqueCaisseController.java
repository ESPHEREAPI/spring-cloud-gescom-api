/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.web.controller;

import com.mproduits.dto.MargeVenteDto;
import com.mproduits.dto.VenteDto;
import com.mproduits.model.Annee;
import com.mproduits.security.BoutiqueAccessGuard;
import com.mproduits.services.HistoriqueCaisseService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author USER01
 */
@RestController
@RequestMapping("/microservice-produits")
public class HistoriqueCaisseController {
    
    @Autowired
    HistoriqueCaisseService historiqueCaisseService;
    @Autowired
    BoutiqueAccessGuard boutiqueAccessGuard;

    //recuperation des annee pour les ventes
    @GetMapping("/{vendeur}/{boutiqueid}/annees")
    public ResponseEntity<List<Annee>> listAnnee(@PathVariable("vendeur") String vendeur,@PathVariable("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<Annee> allAnnees = historiqueCaisseService.listeAnneeByVenteAndVendeur(vendeur,boutiqueid);
        return ResponseEntity.ok(allAnnees);
    }

    @GetMapping("/annees/{boutiqueid}")
    public ResponseEntity<List<Annee>> listAnnee(@PathVariable Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<Annee> allAnnees = historiqueCaisseService.listeAnneeByVente(boutiqueid);
        return ResponseEntity.ok(allAnnees);
    }

    @GetMapping("/all-dates/{anneeId}/{boutiqueid}")
    public ResponseEntity<List<Date>> listDates(@PathVariable("anneeId") Long anneeId,@PathVariable("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<Date> allDates = historiqueCaisseService.listeDateVenteByAnnee(anneeId,boutiqueid);
        return ResponseEntity.ok(allDates);
    }

    @GetMapping("/dates/{vendeur}")
    public ResponseEntity<List<Date>> listDates(@PathVariable("vendeur") String vendeur, @RequestParam("anneeId") int anneeid,@RequestParam("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<Date> allDates = historiqueCaisseService.listeDateVente(anneeid, vendeur,boutiqueid);
        return ResponseEntity.ok(allDates);
    }
     @GetMapping("/dates")
    public ResponseEntity<List<Date>> listDatesCaisse( @RequestParam("anneeId") int anneeid,@RequestParam("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        List<Date> allDates = historiqueCaisseService.listeDateVente(anneeid,boutiqueid);
        return ResponseEntity.ok(allDates);
    }
    @GetMapping("{datevente}/historique")
    public ResponseEntity<List<VenteDto>> listDates(@PathVariable("datevente") String datevente, @RequestParam("anneeId") int anneeid, @RequestParam("vendeur") String vendeur, @RequestParam("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(datevente);
            List<VenteDto> allventes = historiqueCaisseService.allVenteByVendeurByDate(parsedDate, anneeid, vendeur,boutiqueid);
            return ResponseEntity.ok(allventes);

            // return ResponseEntity.ok(allventes);
        } catch (ParseException e) {
            
        }
        return null;
    }

//    @GetMapping("/marge-journalier")
//    public ResponseEntity<List<MargeVenteDto>> margeJournaliere(@RequestParam("datevente") String datevente, @RequestParam("anneeId") Long anneeid) {
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            Date parsedDate = sdf.parse(datevente);
//            List<MargeVenteDto> allventes = historiqueCaisseService.margeJournaliere(parsedDate, anneeid);
//            return ResponseEntity.ok(allventes);
//
//            // return ResponseEntity.ok(allventes);
//        } catch (ParseException e) {
//
//        }
//        return null;
//    }
    @GetMapping("/marge-journalier")
    public ResponseEntity<?> margeJournaliere(
            @RequestParam("dateVente") String dateVente,
            @RequestParam("anneeId") int anneeid,
             @RequestParam("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(dateVente);
            
            List<MargeVenteDto> allventes = historiqueCaisseService.margeJournaliere(parsedDate, anneeid,boutiqueid);
            return ResponseEntity.ok(allventes);
            
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body("Format de date invalide. Format attendu: yyyy-MM-dd");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors du chargement des marges: " + e.getMessage());
        }
    }
//
//    @GetMapping("/marge-mensuel")
//    public ResponseEntity<List<MargeVenteDto>> margeJournaliere(@RequestParam("debut") String debut, @RequestParam("fin") String fin, @RequestParam("anneeId") Long anneeid) {
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            Date parsedDatedebut = sdf.parse(debut);
//            Date parsedDatefin = sdf.parse(fin);
//            List<MargeVenteDto> allventes = historiqueCaisseService.margeMensuel(parsedDatedebut, parsedDatefin, anneeid);
//            return ResponseEntity.ok(allventes);
//
//            // return ResponseEntity.ok(allventes);
//        } catch (ParseException e) {
//
//        }
//        return null;
//    }

    @GetMapping("/marge-mensuel")
    public ResponseEntity<?> margeMensuel(
            @RequestParam("debut") String debut,
            @RequestParam("fin") String fin,
            @RequestParam("anneeId") int anneeId,
            @RequestParam("boutiqueid") Long boutiqueid) {
        boutiqueAccessGuard.verifierBoutiqueUtilisateur(boutiqueid);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDebut = sdf.parse(debut);
            Date parsedFin = sdf.parse(fin);

            List<MargeVenteDto> allventes = historiqueCaisseService.margeMensuel(parsedDebut, parsedFin, anneeId,boutiqueid);
            return ResponseEntity.ok(allventes);

        } catch (ParseException e) {
            return ResponseEntity.badRequest().body("Format de date invalide. Utilisez yyyy-MM-dd.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur serveur: " + e.getMessage() + "causse " + e.getLocalizedMessage());
        }
    }
    
    @PutMapping("/valider")
    public ResponseEntity<?> validerAchat(@RequestBody MargeVenteDto margeVenteDto) {
        try {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //Date parsed = sdf.parse("2025-07-12");
            Date dateVente =convertirEnFinDeJournee( margeVenteDto.getDatevente());
             return ResponseEntity.ok(this.historiqueCaisseService.validerPrixAchat(margeVenteDto,dateVente));
            
       
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur serveur: " + e.getMessage());
        }
       
    }
    private Date convertirEnFinDeJournee(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 01);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
  
  
}
