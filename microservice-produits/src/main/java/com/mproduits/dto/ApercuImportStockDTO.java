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
    // Nombre de lignes sans prix exploitable - non bloquant, affiche en
    // avertissement cote ecran d'import (voir LigneApercuImportStockDTO.prixManquant).
    private int nombreSansPrix;

    public ApercuImportStockDTO(List<LigneApercuImportStockDTO> lignes) {
        this.lignes = lignes;
        this.hasErreurs = lignes.stream().anyMatch(l -> l.getErreur() != null);
        this.nombreSansPrix = (int) lignes.stream().filter(LigneApercuImportStockDTO::isPrixManquant).count();
    }
}
