package com.mproduits.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Une ligne de l'apercu d'un fichier de restauration de stock (voir
 * StockRestaurationService.previsualiserImport) - rien n'est encore ecrit en
 * base a ce stade. erreur non-null = ligne bloquante (reference/boutique
 * introuvable, quantite invalide).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LigneApercuImportStockDTO {
    private int ligneNo;
    private String reference;
    private String boutiqueNom;
    private BigDecimal ancienneQuantite;
    private BigDecimal nouvelleQuantite;
    private String erreur;
}
