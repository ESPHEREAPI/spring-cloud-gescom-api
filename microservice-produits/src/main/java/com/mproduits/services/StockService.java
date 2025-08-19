/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.dto.StockUpdateRequest;
import com.mproduits.dto.StockUpdateResponse;
import com.mproduits.model.Entreprise;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Service
public class StockService {

    ProduitRepositories produitRepositories;

    PrixArticlesRepositories prixArticlesRepositories;
    EntrepriseRepositories entrepriseRepositories;

    @Autowired
    public StockService(ProduitRepositories produitRepositories, PrixArticlesRepositories prixArticlesRepositories, EntrepriseRepositories entrepriseRepositories) {
        this.produitRepositories = produitRepositories;
        this.prixArticlesRepositories = prixArticlesRepositories;
        this.entrepriseRepositories = entrepriseRepositories;
    }

    public StockUpdateResponse updateStockAfterSale(Long produitid, Integer quantite) {
        StockUpdateResponse response = new StockUpdateResponse();
        Produit p = produitRepositories.findById(produitid)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p);
        if (pa.isPresent()) {
            PointVente pv = pa.get().getPointVente();
            // BigDecimal newStock = pv.getStockFinalTheorie().subtract(BigDecimal.valueOf(quantite));

            response.setMessage("update stock");
            response.setNewStock(pv.getStockFinalTheorie().intValue());
            response.setProductId(produitid);
            response.setSuccess(true);
        }
        return response;

    }

    public boolean isStockAvailable(Long produitid, Integer quantite) {
        // StockUpdateResponse response = new StockUpdateResponse();
        BigDecimal stockFinal = BigDecimal.ZERO;
        Produit p = produitRepositories.findById(produitid)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p);
        if (pa.isPresent()) {
            stockFinal = pa.get().getPointVente().getStockFinalTheorie();
            return stockFinal.intValue() > quantite.intValue();

        }

        return Boolean.FALSE;

    }

    public Integer getCurrentStock(Long produitid) {
        Produit p = produitRepositories.findById(produitid)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
        Optional<PrixArticles> pa = prixArticlesRepositories.findLastActiveByEntrepriseAndProduit(e, p);
        if (pa.isPresent()) {
            PointVente pv = pa.get().getPointVente();
            return pv.getStockFinalTheorie().intValue();
        }

        return 0;

    }

}
