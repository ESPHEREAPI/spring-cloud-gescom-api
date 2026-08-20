/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.CommandeDto;
import com.mproduits.model.Barcodeproduit;
import com.mproduits.model.Boutique;
import com.mproduits.model.Commande;
import com.mproduits.model.Entreprise;

import com.mproduits.model.Magasin;
import com.mproduits.model.Mois;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.exceptions.GlobalException;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 *
 * @author USER01
 */
public interface IServiceCommande {

    public Entreprise getEntrepriseActif(Boolean actif);

    public BigInteger calculateStock(Long produitId, Long depotId, Long boutiqueId, Entreprise entreprise);

    public Commande createCommandeFromRequest(CommandeDto commandeDto, Entreprise entreprise, Mois mois);

    public void handleBoutiqueApprovisionnement(Commande commande, CommandeDto commandeDto,
            Entreprise entreprise, Mois mois);

    public void updatePointVenteStock(PointVente pv);

    public String generateCodeCommande(HttpSession session);

    public Entreprise getEntrepriseFromSession(HttpSession session);

    public Mois getMoisFromSession();

    //public BigDecimal getDernierPrixAchat(Produit p, Magasin depot);
    //public BigDecimal getDernierPrixVente(Produit p, Magasin depot);
    public Commande approvisionner(Commande commande, Mois mois, Entreprise entreprise);

    public void addProduitByPointVente(Commande commandes, Boutique boutique,
            Entreprise entreprise, Mois mois, Magasin depot,
            Barcodeproduit barcodeProduit);

    public Commande getDernierCommandeByProduitByDepot(Produit produit, Magasin depot);

    public BigDecimal getDernierPrixAchat(Produit produit);

    public BigDecimal getDernierPrixVente(Produit produit);

    public Long count();

    public boolean existsByCode(String code);

    public String approvisionnement(Commande c, Mois m, Entreprise e);

    public void updateApprovisionnement(Commande c) throws GlobalException;
    // public String livraisonCommande(Livraison_old l, Commande c, PrixClient p, Entreprise e) throws GlobalException;

    //public void updateLivraison(Livraison_old l, Commande c, Entreprise e);

    public String addProduitByPointVente(List<Commande> colCommande, Boutique b, Entreprise e, Mois m, Magasin d);

    public String deleteManyProduit(List<Produit> produits);

    public String deleteOneProduit(Produit p);
    public Page<PrixArticles> getPrixArticles(int page, int size, String filter,Long boutiqueid);
    public Page<PrixArticles> getPrixArticlesByMagasin(int page, int size, String filter, Long magasinid);
    List<PrixArticles> getPrixArticlesFilster(String search,Long boutiqueid);
    public BigDecimal stockProduit(Long produitId ,Long boutiqueid);
    public BigDecimal stockDepot(Long produitId ,Long boutiqueid);
    public   List<com.mproduits.dto.AlerteStockDTO> alertstock(BigDecimal seuilStock);
 
    
}
