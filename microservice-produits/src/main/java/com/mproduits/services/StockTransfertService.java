/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Commande;
import com.mproduits.model.Magasin;
import com.mproduits.model.MagasinFournisseur;
import com.mproduits.model.MagasinFournisseurPK;
import com.mproduits.model.PointVente;
import com.mproduits.model.PrixArticles;
import com.mproduits.model.Produit;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.EntrepriseRepositories;
import com.mproduits.repositories.MagasinFournisseurRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixAchatRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.PrixVenteRepositories;
import com.mproduits.repositories.ProduitRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@Transactional
/**
 *
 * @author USER01
 */
public class StockTransfertService {
    
    @Autowired
    private MagasinRepositories magasinRepository;
    
    @Autowired
    private CommandeRepositories commandeRepository;
    
    @Autowired
    private PointVenteRepositories pointVenteRepository;
    
    @Autowired
    private ProduitRepositories produitRepository;
    
    @Autowired
    private PrixArticlesRepositories prixArticlesRepository;
    @Autowired
    private IServiceCommande commandeService;
    @Autowired
    private MagasinRepositories magasinRepositories;
    @Autowired
    private EntrepriseRepositories entrepriseRepositories;
    @Autowired
    private PrixAchatRepositories prixAchatRepositories;
    @Autowired
    private PrixVenteRepositories prixVenteRepositories;

    /**
     * Vérifie si un magasin est un magasin de stock (boutique == null)
     *
     * @param magasinId ID du magasin
     * @return true si c'est un magasin de stock, false si c'est un point de
     * vente
     */
    public boolean isMagasinDeStock(Long magasinId) {
        Optional<Magasin> magasin = magasinRepository.findById(magasinId);
        if (magasin.isEmpty()) {
            throw new IllegalArgumentException("Magasin non trouvé: " + magasinId);
        }
        return magasin.get().getBoutiqueId() == null;
    }

    /**
     * Récupère le stock actuel d'un produit dans un dépôt
     *
     * @param magasinId ID du dépôt
     * @param produitId ID du produit
     * @return Stock final théorique du produit
     */
    public BigDecimal getStockActuel(Long magasinId, Long produitId) {
        boolean isMagasin = isMagasinDeStock(magasinId);
        
        if (isMagasin) {
            // Pour un magasin: récupérer depuis Commande
            Optional<Commande> commande = commandeRepository
                    .findLatestCommandeByMagasinAndProduit(magasinId, produitId);
            if (commande.isEmpty()) {
                Optional<Produit> produit = produitRepository.findById(produitId);
                
                Commande cde = new Commande();
                cde.setPrixAchat(prixAchatRepositories.findLastPrixAchatByProduit(produit.get()).get().getPrix());
                cde.setPrixVente(prixVenteRepositories.findLastPrixventeByProduit(produit.get()).get().getPrix());
                cde.setQuantite(BigDecimal.ZERO);
                cde.setUsercreate("transfert mouvement");
                cde.setEntreprise(entrepriseRepositories.findByActif(Boolean.TRUE));
                cde.setProduit(produit.get());
                
                cde.setMagasinid(magasinRepositories.findById(magasinId).get());
                cde.setNumeroRepartition(generateCodeCommande());
                commandeRepository.save(cde);
                
            }            
            return commande.map(Commande::getStockFinalTheorie)
                    .orElse(BigDecimal.ZERO);
        } else {
            // Pour un point de vente: récupérer depuis PointVente
            Optional<PointVente> pointVente = pointVenteRepository
                    .findLatestPointVenteByMagasinAndProduit(magasinId, produitId);
            return pointVente.map(PointVente::getStockFinalTheorie)
                    .orElse(BigDecimal.ZERO);
        }
        
    }
    
    private String generateCodeCommande() {
        Long count = commandeService.count();
        boolean exists = true;
        String code = null;
        
        while (exists) {
            StringBuilder sb = new StringBuilder();
            int length = count.toString().length();
            
            for (int i = 0; i < 5 - length; i++) {
                sb.append("0");
            }
            sb.append(++count);
            
            code = sb.toString();
            exists = commandeService.existsByCode(code);
        }
        
        return code;
    }

    /**
     * Vérifie si le stock est disponible dans le dépôt source
     *
     * @param magasinSourceId ID du dépôt source
     * @param produitId ID du produit
     * @param quantiteATransferer Quantité à transférer
     * @return true si le stock est disponible, false sinon
     */
    public boolean verifierDisponibiliteStock(Long magasinSourceId, Long produitId, BigDecimal quantiteATransferer) {
        BigDecimal stockActuel = getStockActuel(magasinSourceId, produitId);
        
        if (quantiteATransferer.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La quantité à transférer doit être positive");
        }
        
        boolean disponible = stockActuel.compareTo(quantiteATransferer) >= 0;
        
        if (!disponible) {
            log.warn("Stock insuffisant pour le produit {} dans le magasin {}. "
                    + "Stock disponible: {}, Quantité demandée: {}",
                    produitId, magasinSourceId, stockActuel, quantiteATransferer);
        }
        
        return disponible;
    }

    /**
     * Calcule la valeur estimée d'une quantité de produit
     *
     * Règle: - Pour un magasin de stock: valeur = quantité * prixAchat - Pour
     * un point de vente: valeur = quantité * prixVenteTTC
     *
     * @param magasinId ID du dépôt
     * @param produitId ID du produit
     * @param quantite Quantité
     * @return Valeur estimée
     */
    public BigDecimal calculerValeurEstimee(Long magasinId, Long produitId, BigDecimal quantite) {
        boolean isMagasin = isMagasinDeStock(magasinId);
        BigDecimal prix;
        
        if (isMagasin) {
            // Pour magasin: utiliser prix d'achat (depuis Commande)
            Optional<Commande> commande = commandeRepository
                    .findLatestCommandeByMagasinAndProduit(magasinId, produitId);
            prix = commande.map(Commande::getPrixAchat)
                    .orElse(BigDecimal.ZERO);
        } else {
            // Pour point de vente: utiliser prix de vente TTC (depuis PrixArticles)
            Optional<PointVente> pointVente = pointVenteRepository
                    .findLatestPointVenteByMagasinAndProduit(magasinId, produitId);
            
            if (pointVente.isPresent()) {
                Optional<PrixArticles> prixArticles = prixArticlesRepository
                        .findActivePriceByPointVente(pointVente.get().getId());
                prix = prixArticles.map(PrixArticles::getPrixVenteTTC)
                        .orElse(BigDecimal.ZERO);
            } else {
                prix = BigDecimal.ZERO;
            }
        }

        // Calcul: valeur = quantité * prix
        return quantite.multiply(prix);
    }

    /**
     * Récupère les informations détaillées d'un produit
     *
     * @param produitId ID du produit
     * @return Produit trouvé
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    public Produit getProduit(Long produitId) {
        return produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé: " + produitId));
    }

    /**
     * Récupère les informations détaillées d'un magasin
     *
     * @param magasinId ID du magasin
     * @return Magasin trouvé
     * @throws IllegalArgumentException si le magasin n'existe pas
     */
    public Magasin getMagasin(Long magasinId) {
        return magasinRepository.findById(magasinId)
                .orElseThrow(() -> new IllegalArgumentException("Magasin non trouvé: " + magasinId));
    }

    /**
     * Récupère les enregistrements de commande pour un magasin et un produit
     *
     * @param magasinId ID du magasin
     * @param produitId ID du produit
     * @return Commande la plus récente ou null
     */
    public Commande getCommandeActuelle(Long magasinId, Long produitId) {
        return commandeRepository
                .findLatestCommandeByMagasinAndProduit(magasinId, produitId)
                .orElse(null);
    }

    /**
     * Récupère les enregistrements de point de vente pour un dépôt et un
     * produit
     *
     * @param magasinId ID du dépôt
     * @param produitId ID du produit
     * @return PointVente le plus récent ou null
     */
    public PointVente getPointVenteActuelle(Long magasinId, Long produitId) {
        return pointVenteRepository
                .findLatestPointVenteByMagasinAndProduit(magasinId, produitId)
                .orElse(null);
    }
}
