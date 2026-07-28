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
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransfertStockResponse {

    /**
     * ID du transfert créé
     */
    private Long transfertId;

    /**
     * Informations sur le produit transféré
     */
    private ProduitDTO produit;

    /**
     * Informations sur le magasin source
     */
    private MagasinDTO source;

    /**
     * Informations sur le magasin destination
     */
    private MagasinDTO destination;

    /**
     * Quantité transférée
     */
    private BigDecimal quantite;

    /**
     * Valeur estimative du transfert
     */
    private BigDecimal valeurEstimation;

    /**
     * Date et heure du transfert
     */
    private Date dateTransfert;

    /**
     * Message de succès
     */
    private String message;

    /**
     * Statut de la réponse (SUCCESS, ERROR, etc.)
     */
    private String status;

    /**
     * DTO imbriqué pour les informations de produit
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProduitDTO {
        private Long id;
        private String code;
        private String libelle;
        private String reference;
    }

    /**
     * DTO imbriqué pour les informations de magasin
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MagasinDTO {
        private Long id;
        private String code;
        private String libelle;
        private String ville;
        /**
         * Type de dépôt: "MAGASIN" ou "POINT_VENTE"
         */
        private String type;
        /**
         * Stock final théorique après transfert
         */
        private BigDecimal stockFinal;
    }
    
}
