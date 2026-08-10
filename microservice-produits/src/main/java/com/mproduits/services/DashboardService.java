/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;
import com.mproduits.dto.DashboardStockDTO;
import com.mproduits.model.*;
import com.mproduits.repositories.CommandeRepositories;
import com.mproduits.repositories.MagasinRepositories;
import com.mproduits.repositories.PointVenteRepositories;
import com.mproduits.repositories.PrixArticlesRepositories;
import com.mproduits.repositories.ProduitRepositories;
import com.mproduits.repositories.TransfertStockRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
/**
 *
 * @author USER01
 */
public class DashboardService {
    
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
    private TransfertStockRepository transfertStockRepository;

    @Autowired
    private StockTransfertService stockService;

    /**
     * Génère le tableau de bord complet avec tous les KPIs
     * 
     * @return Dashboard avec toutes les données agrégées
     */
    public DashboardStockDTO genererDashboard(Long compagnieId) {
        log.info("Génération du dashboard de stock pour la compagnie {}...", compagnieId);

        // Récupérer les magasins et points de vente de cette compagnie uniquement
        List<Magasin> magasinsDeStock = magasinRepository.findMagasinsDeStockByCompagnieId(compagnieId);
        List<Magasin> pointsDeVente = magasinRepository.findPointsDeVenteByCompagnieId(compagnieId);

        // Calculer les valeurs
        List<DashboardStockDTO.ValeurParMagasinDTO> valeurMagasins = calculerValeurParMagasin(magasinsDeStock);
        List<DashboardStockDTO.ValeurParPointVenteDTO> valeurPointsVente = calculerValeurParPointVente(pointsDeVente);

        BigDecimal totalValeurMagasins = valeurMagasins.stream()
                .map(DashboardStockDTO.ValeurParMagasinDTO::getValeurTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValeurPointsVente = valeurPointsVente.stream()
                .map(DashboardStockDTO.ValeurParPointVenteDTO::getValeurTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValeurStock = totalValeurMagasins.add(totalValeurPointsVente);

        // Récupérer les produits en stock faible
        List<DashboardStockDTO.ProduitFaibleStockDTO> produitsFaibles = obtenirProduitsFaibleStock(
                magasinsDeStock, pointsDeVente);

        // Récupérer les mouvements récents
        int nombreMouvements = (int) transfertStockRepository.countByEntreprise_EntreprisePK_CompagnieId(compagnieId);

        // Construire et retourner le dashboard
        DashboardStockDTO dashboard = DashboardStockDTO.builder()
                .totalValeurStock(totalValeurStock)
                .totalValeurMagasins(totalValeurMagasins)
                .totalValeurPointsVente(totalValeurPointsVente)
                .nombreProduitsFaibles(produitsFaibles.size())
                .nombreMouvements(nombreMouvements)
                .valeurMagasins(valeurMagasins)
                .valeurPointsVente(valeurPointsVente)
                .produitsFaibleStock(produitsFaibles)
                .evolutionStock(obtenirEvolutionStock(compagnieId))
                .build();

        log.info("Dashboard généré. Valeur totale: {}", totalValeurStock);
        return dashboard;
    }

    /**
     * Calcule la valeur de stock par magasin
     * Valeur = quantité * prix d'achat (pour les magasins)
     * 
     * @param magasins Liste des magasins de stock
     * @return Liste des valeurs par magasin
     */
    private List<DashboardStockDTO.ValeurParMagasinDTO> calculerValeurParMagasin(List<Magasin> magasins) {
        return magasins.stream().map(magasin -> {
            List<Commande> commandes = commandeRepository.findByMagasinid(magasin);

            BigDecimal valeurTotal = BigDecimal.ZERO;
            BigDecimal quantiteTotal = BigDecimal.ZERO;
            int nombreArticles = 0;

            for (Commande commande : commandes) {
                if (Objects.equals(commande.getEntreprise().getActif(), Boolean.FALSE)) {
                    continue;
                }
                BigDecimal stock = commande.getStockFinalTheorie() != null
                        ? commande.getStockFinalTheorie()
                        : BigDecimal.ZERO;
                BigDecimal prix = commande.getPrixAchat() != null
                        ? commande.getPrixAchat()
                        : BigDecimal.ZERO;

                valeurTotal = valeurTotal.add(stock.multiply(prix));
                quantiteTotal = quantiteTotal.add(stock);
                nombreArticles++;
            }

            return DashboardStockDTO.ValeurParMagasinDTO.builder()
                    .magasinId(magasin.getId())
                    .magasinCode(magasin.getCode())
                    .magasinLibelle(magasin.getLibelle())
                    .ville(magasin.getVilleid() != null ? magasin.getVilleid().getLibelle() : "")
                    .valeurTotal(valeurTotal)
                    .nombreArticles(nombreArticles)
                    .quantiteTotal(quantiteTotal)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Calcule la valeur de stock par point de vente
     * Valeur = quantité * prix de vente TTC
     * 
     * @param pointsDeVente Liste des points de vente
     * @return Liste des valeurs par point de vente
     */
    private List<DashboardStockDTO.ValeurParPointVenteDTO> calculerValeurParPointVente(List<Magasin> pointsDeVente) {
        return pointsDeVente.stream().map(magasin -> {
            List<PointVente> pointVentes = pointVenteRepository.findByBoutique(magasin.getBoutiqueId());

            BigDecimal valeurTotal = BigDecimal.ZERO;
            BigDecimal quantiteTotal = BigDecimal.ZERO;
            int nombreArticles = 0;

            for (PointVente pv : pointVentes) {
                if (Objects.equals(pv.getEntreprise().getActif(), Boolean.FALSE)) {
                    continue;
                }
                BigDecimal stock = pv.getStockFinalTheorie() != null
                        ? pv.getStockFinalTheorie()
                        : BigDecimal.ZERO;

                // Récupérer le prix de vente TTC
                Optional<PrixArticles> prixArticles = prixArticlesRepository.findActivePriceByPointVente(pv.getId());
                BigDecimal prix = prixArticles.isPresent()
                        ? prixArticles.get().getPrixVenteNet()
                        : BigDecimal.ZERO;

                valeurTotal = valeurTotal.add(stock.multiply(prix));
                quantiteTotal = quantiteTotal.add(stock);
                nombreArticles++;
            }

            return DashboardStockDTO.ValeurParPointVenteDTO.builder()
                    .pointVenteId(magasin.getId())
                    .pointVenteNom(magasin.getLibelle())
                    .magasinDepot(magasin.getCode())
                    .valeurTotal(valeurTotal)
                    .nombreArticles(nombreArticles)
                    .quantiteTotal(quantiteTotal)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Identifie les produits en stock faible (quantité <= 10)
     * 
     * Colorisation:
     * - quantité <= 5: "danger" (rouge)
     * - 5 < quantité <= 10: "warning" (orange)
     * - quantité > 10: "success" (vert)
     * 
     * @param magasinsDeStock Magasins de stock
     * @param pointsDeVente Points de vente
     * @return Liste des produits en stock faible
     */
    private List<DashboardStockDTO.ProduitFaibleStockDTO> obtenirProduitsFaibleStock(
            List<Magasin> magasinsDeStock, List<Magasin> pointsDeVente) {

        List<DashboardStockDTO.ProduitFaibleStockDTO> produitsFaibles = new ArrayList<>();

        // Vérifier les magasins de stock
        for (Magasin magasin : magasinsDeStock) {
            List<Commande> commandes = commandeRepository.findByMagasinid(magasin);

            for (Commande commande : commandes) {
                BigDecimal stock = commande.getStockFinalTheorie() != null
                        ? commande.getStockFinalTheorie()
                        : BigDecimal.ZERO;

                // Vérifier si le stock est faible
                if (stock.compareTo(new BigDecimal(10)) <= 0) {
                    Produit produit = commande.getProduit();
                    String couleur = determinerCouleur(stock);

                    produitsFaibles.add(DashboardStockDTO.ProduitFaibleStockDTO.builder()
                            .produitId(produit.getId())
                            .produitCode(produit.getCode())
                            .produitLibelle(produit.getLibelle())
                            .magasinId(magasin.getId())
                            .magasinLibelle(magasin.getLibelle())
                            .ville(magasin.getVilleid() != null ? magasin.getVilleid().toString() : "")
                            .quantite(stock)
                            .couleur(couleur)
                            .lienDetail("/stock/details/" + produit.getId() + "/" + magasin.getId())
                            .build());
                }
            }
        }

        // Vérifier les points de vente
        for (Magasin magasin : pointsDeVente) {
            List<PointVente> pointVentes = pointVenteRepository.findByDepotId(magasin);

            for (PointVente pv : pointVentes) {
                BigDecimal stock = pv.getStockFinalTheorie() != null
                        ? pv.getStockFinalTheorie()
                        : BigDecimal.ZERO;

                // Vérifier si le stock est faible
                if (stock.compareTo(new BigDecimal(10)) <= 0) {
                    Produit produit = pv.getProduit();
                    String couleur = determinerCouleur(stock);

                    produitsFaibles.add(DashboardStockDTO.ProduitFaibleStockDTO.builder()
                            .produitId(produit.getId())
                            .produitCode(produit.getCode())
                            .produitLibelle(produit.getLibelle())
                            .magasinId(magasin.getId())
                            .magasinLibelle(magasin.getLibelle())
                            .ville(magasin.getVilleid() != null ? magasin.getVilleid().toString() : "")
                            .quantite(stock)
                            .couleur(couleur)
                            .lienDetail("/stock/details/" + produit.getId() + "/" + magasin.getId())
                            .build());
                }
            }
        }

        return produitsFaibles.stream()
                .sorted(Comparator.comparing(DashboardStockDTO.ProduitFaibleStockDTO::getQuantite))
                .collect(Collectors.toList());
    }

    /**
     * Détermine la couleur d'alerte selon la quantité
     * 
     * @param quantite Quantité en stock
     * @return Couleur: "danger", "warning", ou "success"
     */
    private String determinerCouleur(BigDecimal quantite) {
        if (quantite.compareTo(new BigDecimal(5)) <= 0) {
            return "danger";  // rouge
        } else if (quantite.compareTo(new BigDecimal(10)) <= 0) {
            return "warning";  // orange
        } else {
            return "success";  // vert
        }
    }

    /**
     * Récupère l'évolution du stock pour les 12 derniers mois
     * Données pour les graphiques
     * 
     * @return Liste des évolutions mensuelles
     */
    private List<DashboardStockDTO.EvolutionStockDTO> obtenirEvolutionStock(Long compagnieId) {
        List<DashboardStockDTO.EvolutionStockDTO> evolution = new ArrayList<>();

        // Générer des données pour les 12 derniers mois
        Calendar cal = Calendar.getInstance();
        for (int i = 11; i >= 0; i--) {
            cal.add(Calendar.MONTH, -i);
            String mois = String.format("%tB %tY", cal, cal);

            // Calculer les valeurs pour ce mois
            Date dateDebut = new Date(cal.getTimeInMillis());
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date dateFin = new Date(cal.getTimeInMillis());

            // Récupérer les transferts du mois pour cette compagnie uniquement
            List<TransfertStock> transferts = transfertStockRepository
                    .findByCompagnieIdAndDateTransfertBetween(compagnieId, dateDebut, dateFin);

            int entrees = 0;
            int sorties = 0;
            BigDecimal valeurMagasins = BigDecimal.ZERO;
            BigDecimal valeurPointsVente = BigDecimal.ZERO;

            for (TransfertStock t : transferts) {
                if (stockService.isMagasinDeStock(t.getDestination().getId())) {
                    entrees++;
                    valeurMagasins = valeurMagasins.add(t.getValeurEstimation());
                } else {
                    sorties++;
                    valeurPointsVente = valeurPointsVente.add(t.getValeurEstimation());
                }
            }

            evolution.add(DashboardStockDTO.EvolutionStockDTO.builder()
                    .mois(mois)
                    .valeurMagasins(valeurMagasins)
                    .valeurPointsVente(valeurPointsVente)
                    .nombreEntrees(entrees)
                    .nombreSorties(sorties)
                    .build());

            cal = Calendar.getInstance();
        }

        return evolution;
    }
}
