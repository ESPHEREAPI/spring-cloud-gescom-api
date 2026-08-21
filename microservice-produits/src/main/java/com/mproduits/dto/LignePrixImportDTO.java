package com.mproduits.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Une ligne de l'apercu d'un import de prix (voir PrixImportService) - rien
 * n'est encore ecrit en base a ce stade. erreur non-null = ligne ignoree a
 * l'application (reference introuvable dans cette boutique, prix invalide).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LignePrixImportDTO {
    private int ligneNo;
    private String reference;
    private String libelle;
    private BigDecimal ancienPrix;
    private BigDecimal nouveauPrix;
    private String erreur;
    // Id de la ligne PrixArticles resolue (interne, pour appliquer() - deja
    // scope compagnie/boutique par la carte construite dans PrixImportService).
    private Long prixArticlesId;
}
