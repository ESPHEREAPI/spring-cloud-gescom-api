/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de réponse pour la validation des pré-requis de reconduction.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReconductionResponse {
      private Boolean valide;
    private Boolean entrepriseSourceExiste;
    private Boolean entrepriseCibleExiste;
    private Boolean anneeCibleValide;
    private Integer nombrePointsVente;
    private Integer nombrePrixArticles;
    
    @Builder.Default
    private List<String> avertissements = new ArrayList<>();
    
    @Builder.Default
    private List<String> erreurs = new ArrayList<>();
    
    private Long dureeEstimeeSecondes;

    public void ajouterAvertissement(String message) {
        if (this.avertissements == null) {
            this.avertissements = new ArrayList<>();
        }
        this.avertissements.add(message);
    }

    public void ajouterErreur(String message) {
        if (this.erreurs == null) {
            this.erreurs = new ArrayList<>();
        }
        this.erreurs.add(message);
        this.valide = false;
    }
}
