/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.InventaireDto;
import com.mproduits.dto.PointVenteDto;
import com.mproduits.mappers.MapperDtoImpl;
import com.mproduits.model.Boutique;
import com.mproduits.model.Categories;
import com.mproduits.model.DetailCommandeEntree;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Magasin;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
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
public class InventairesService {
    BoutiqueRepositories boutiqueRepositories;
    CategorieRepositories categorieRepositories;
    EntrepriseRepositories entrepriseRepositories;
    PrixArticlesRepositories prixArticlesRepositories;
    PointVenteRepositories pointVenteRepositories;
    EntrepriseService entrepriseService;
    TenantContext tenantContext;

      @Autowired
      MapperDtoImpl mapperDto;

      @Autowired
      MagasinRepositories magasinRepositories;

      @Autowired
      CommandeRepositories commandeRepositories;

    @Autowired
    public InventairesService(BoutiqueRepositories boutiqueRepositories, CategorieRepositories categorieRepositories, EntrepriseRepositories entrepriseRepositories,  PrixArticlesRepositories prixArticlesRepositories, PointVenteRepositories pointVenteRepositories, EntrepriseService entrepriseService, TenantContext tenantContext) {
        this.boutiqueRepositories = boutiqueRepositories;
        this.categorieRepositories = categorieRepositories;
        this.entrepriseRepositories = entrepriseRepositories;
        this.prixArticlesRepositories=prixArticlesRepositories;
        this .pointVenteRepositories=pointVenteRepositories;
        this.entrepriseService = entrepriseService;
        this.tenantContext = tenantContext;
    }

    
    public List<PointVenteDto> chargeStockeForUpdate(Long boutiqueid,Long categorieid){
        Boutique boutique =boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée avec l'ID: " + boutiqueid));

        Categories cat= categorieRepositories.findByIdAndCompagnie_Id(categorieid, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Categorie non trouvée avec l'ID: " + categorieid));
        Entreprise e= entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        
        
        List<PointVenteDto> allStockByPointVente= prixArticlesRepositories.chargeStockPointVente(e.getAnnee().getId(), Boolean.TRUE, boutiqueid, categorieid).stream()
                .map(pv -> mapperDto.mapperPointVentByPointVentDtoi(pv))
                .collect(Collectors.toList());
        System.out.println("sizs:"+allStockByPointVente.size());
       allStockByPointVente.forEach(System.out::print);
        
        return allStockByPointVente;
        
        
    }

 // Inventaire "Par depot" : contrairement a une boutique, un depot pur
 // (magasin sans boutique) n'a pas de PointVente/PrixArticles - son stock
 // vit sur Commande (voir ServiceCommande.approvisionner). Pas de filtre
 // categorie ici (l'ecran ne le demande pas pour ce mode).
 public List<InventaireDto> chargeInventaireParDepot(Long depotid){
        Magasin depot = magasinRepositories.findByIdAndCompagnie_Id(depotid, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Dépôt non trouvé avec l'ID: " + depotid));
        Entreprise e = entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());

        return commandeRepositories.findByMagasinIdAndEntreprise(depotid, e).stream()
                .map(mapperDto::mapperCommandeByInventaireDto)
                .collect(Collectors.toList());
    }

 public List<InventaireDto> chargeInventaire(Long boutiqueid,Long categorieid){
        Boutique boutique =boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Boutique non trouvée avec l'ID: " + boutiqueid));

        Categories cat= categorieRepositories.findByIdAndCompagnie_Id(categorieid, tenantContext.currentCompagnieId())
                .orElseThrow(() -> new EntityNotFoundException("Categorie non trouvée avec l'ID: " + categorieid));
        Entreprise e= entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
        
        
        List<InventaireDto> allStockByPointVente= prixArticlesRepositories.chargeStockPointVente(e.getAnnee().getId(), Boolean.TRUE, boutiqueid, categorieid).stream()
                .map(pv -> mapperDto.mapperPointVentByInventaireDtto(pv))
                .collect(Collectors.toList());
       
       allStockByPointVente.forEach(System.out::print);
        
        return allStockByPointVente;
        
        
    }    
    public void saveStockInventaire( List<PointVenteDto> allStock){
        allStock.stream()
                .filter(pv -> pv.getStockFinalTheorie().intValue()!=0)
                .peek(pv -> {
                    if (pv.getStockFinalTheorie().compareTo(BigDecimal.ZERO) < 0) {
                        throw new BadRequestException("Le stock final theorique ne peut pas etre negatif (produit "
                                + pv.getProduit().getId() + " : " + pv.getStockFinalTheorie() + ")");
                    }
                })
                .map(pvc-> saveStock(pvc,pvc.getStockFinalTheorie()))
                .collect(Collectors.toList());


    }
    
    private PointVenteDto saveStock(PointVenteDto pointVenteDto ,BigDecimal quantite){
            Entreprise e= entrepriseService.obtenirOuCreerExerciceActif(tenantContext.currentCompagnieId());
            Long  boutiqueid=pointVenteDto.getBoutique().getId();
            Optional<Boutique> boutique=boutiqueRepositories.findByIdAndCompagnie_Id(boutiqueid, tenantContext.currentCompagnieId());
            Produit produit=mapperDto.mapperProduit(pointVenteDto.getProduit());
        Optional<PrixArticles> paNew=prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e,produit ,boutique.get().getId());
        if (paNew.isEmpty()) {
            throw  new EntityNotFoundException("Produit non trouvée avec l'ID: " + pointVenteDto.getId());
        }
        PrixArticles prixArticles=paNew.get();
        if (prixArticles.getPrixVenteNet().intValue()!=pointVenteDto.getPrix().intValue()) {
            prixArticles.setPrixVenteNet(pointVenteDto.getPrix());
            prixArticles.setPrixVenteTTC(pointVenteDto.getPrix());
                  prixArticlesRepositories.save(prixArticles);
        }
        prixArticles.getPointVente().setStockFinalTheorie(quantite);
        prixArticles.getPointVente().setEntreeProduit(quantite);
        prixArticles.getPointVente().setSortiProduit(BigDecimal.ZERO);
        prixArticles.setDatemiseajour(new Date());
      pointVenteRepositories.save(prixArticles.getPointVente());
//       if (prixArticles.getPrixVenteNet().intValue()!=pointVenteDto.getPrix().intValue()) {
//            prixArticles.setPrixVenteNet(pointVenteDto.getPrix());
//                  prixArticlesRepositories.save(prixArticles);
//        }

      return pointVenteDto;
       
    }
}
