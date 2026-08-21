package com.mproduits.dto;

import java.util.List;
import lombok.Data;

/**
 * Resultat complet d'une previsualisation d'import de prix -
 * PrixImportService.appliquer ignore simplement les lignes en erreur (pas de
 * blocage total comme pour la restauration de stock : corriger 990 prix sur
 * 996 vaut mieux que rien si les 6 restants ont une reference introuvable).
 */
@Data
public class ApercuPrixImportDTO {
    private List<LignePrixImportDTO> lignes;
    private int nombreValides;
    private int nombreEnErreur;

    public ApercuPrixImportDTO(List<LignePrixImportDTO> lignes) {
        this.lignes = lignes;
        this.nombreEnErreur = (int) lignes.stream().filter(l -> l.getErreur() != null).count();
        this.nombreValides = lignes.size() - this.nombreEnErreur;
    }
}
