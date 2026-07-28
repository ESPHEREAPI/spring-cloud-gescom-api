/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/**
 *
 * @author USER01
 */
public class DashboardStockDTO {
     /**
     * Valeur estimative totale du stock (tous les dépôts)
     */
    private BigDecimal totalValeurStock;

    /**
     * Valeur total des magasins de stock au prix d'achat
     */
    private BigDecimal totalValeurMagasins;

    /**
     * Valeur totale des points de vente au prix de vente TTC
     */
    private BigDecimal totalValeurPointsVente;

    /**
     * Nombre total de produits en stock faible
     */
    private Integer nombreProduitsFaibles;

    /**
     * Nombre total de mouvements de stock (dernier mois)
     */
    private Integer nombreMouvements;

    /**
     * Valeur estimative par magasin
     */
    private List<ValeurParMagasinDTO> valeurMagasins;

    /**
     * Valeur estimative par point de vente
     */
    private List<ValeurParPointVenteDTO> valeurPointsVente;

    /**
     * Produits en stock faible (quantité <= 10)
     */
    private List<ProduitFaibleStockDTO> produitsFaibleStock;

    /**
     * Données pour graphique d'évolution du stock
     */
    private List<EvolutionStockDTO> evolutionStock;

    /**
     * DTO pour les valeurs par magasin
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ValeurParMagasinDTO {
        private Long magasinId;
        private String magasinCode;
        private String magasinLibelle;
        private String ville;
        private BigDecimal valeurTotal;
        private Integer nombreArticles;
        private BigDecimal quantiteTotal;
    }

    /**
     * DTO pour les valeurs par point de vente
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ValeurParPointVenteDTO {
        private Long pointVenteId;
        private String pointVenteNom;
        private String magasinDepot;
        private BigDecimal valeurTotal;
        private Integer nombreArticles;
        private BigDecimal quantiteTotal;
    }

    /**
     * DTO pour les produits en stock faible
     * Colorisation selon la quantité:
     * - quantite <= 5 => "danger" (rouge)
     * - 6 <= quantite <= 10 => "warning" (orange)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProduitFaibleStockDTO {
        private Long produitId;
        private String produitCode;
        private String produitLibelle;
        private Long magasinId;
        private String magasinLibelle;
        private String ville;
        private BigDecimal quantite;
        /**
         * Couleur: "danger" (rouge), "warning" (orange), ou "success" (vert)
         */
        private String couleur;
        /**
         * Lien cliquable vers les détails du produit
         */
        private String lienDetail;
    }

    /**
     * DTO pour l'évolution du stock (graphique)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EvolutionStockDTO {
        private String mois;
        private BigDecimal valeurMagasins;
        private BigDecimal valeurPointsVente;
        private Integer nombreEntrees;
        private Integer nombreSorties;
    }
}
