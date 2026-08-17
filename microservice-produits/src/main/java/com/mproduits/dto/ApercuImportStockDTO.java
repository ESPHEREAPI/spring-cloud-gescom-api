package com.mproduits.dto;

import java.util.List;
import lombok.Data;

/**
 * Resultat complet d'une previsualisation de restauration de stock -
 * l'application (StockRestaurationService.appliquerImport) est refusee tant
 * que hasErreurs est vrai.
 */
@Data
public class ApercuImportStockDTO {
    private List<LigneApercuImportStockDTO> lignes;
    private boolean hasErreurs;

    public ApercuImportStockDTO(List<LigneApercuImportStockDTO> lignes) {
        this.lignes = lignes;
        this.hasErreurs = lignes.stream().anyMatch(l -> l.getErreur() != null);
    }
}
