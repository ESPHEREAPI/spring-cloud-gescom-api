/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * TransfertStockRequest DTO
 * 
 * Objet de transfert de données (DTO) pour les requêtes de transfert de stock.
 * Cette classe encapsule les informations nécessaires pour effectuer un transfert.
 * 
 * Champs:
 * - produitId: ID du produit à transférer
 * - magasinSourceId: ID du magasin/point de vente source
 * - magasinDestinationId: ID du magasin/point de vente destination
 * - quantite: Quantité à transférer
 * - notes: Notes optionnelles sur le transfert
 * 
 * Exemple d'utilisation:
 * POST /api/transferts/transferer
 * {
 *   "produitId": 1,
 *   "magasinSourceId": 1,
 *   "magasinDestinationId": 2,
 *   "quantite": 50.0,
 *   "notes": "Transfert urgent"
 * }
 * 
 * @author Système de Gestion de Stock
 */
package com.mproduits.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransfertStockRequest {

    /**
     * ID du produit à transférer
     */
    @NotNull(message = "L'ID du produit ne peut pas être null")
    @Positive(message = "L'ID du produit doit être positif")
    private Long produitId;

    /**
     * ID du magasin/point de vente source
     */
    @NotNull(message = "L'ID du magasin source ne peut pas être null")
    @Positive(message = "L'ID du magasin source doit être positif")
    private Long magasinSourceId;

    /**
     * ID du magasin/point de vente destination
     */
    @NotNull(message = "L'ID du magasin destination ne peut pas être null")
    @Positive(message = "L'ID du magasin destination doit être positif")
    private Long magasinDestinationId;

    /**
     * Quantité à transférer
     */
    @NotNull(message = "La quantité ne peut pas être null")
    @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
    private BigDecimal quantite;

    /**
     * Notes optionnelles sur le transfert
     */
    @Size(max = 500, message = "Les notes ne doivent pas dépasser 500 caractères")
    private String notes;

    /**
     * Validation métier: le magasin source et destination ne doivent pas être identiques
     */
    public boolean isSourceDifferentFromDestination() {
        return !magasinSourceId.equals(magasinDestinationId);
    }
    private String username;
    
}
