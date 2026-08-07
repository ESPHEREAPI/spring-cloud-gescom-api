/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse après l'exécution d'une reconduction.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 *
 * @author USER01
 */
public class ExecutionReconductionResponse {
     private Boolean success;
    private String reconductionId;
    private EntrepriseInfo entrepriseCree;
    private StatistiquesReconduction statistiques;
    private String messageErreur;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntrepriseInfo {
        private Integer anneeId;
        private Long compagnieId;
        private String libelleSociete;
        private String codeAnnee;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatistiquesReconduction {
        private Integer pointsVenteReconduits;
        private Integer prixArticlesReconduits;
        private Integer produitsIgnores;
        private Long dureeExecutionSecondes;
        private String dureeFormatee;
        private Integer batchSize;
        private String coefficientPrix;

        public void calculerDureeFormatee() {
            if (dureeExecutionSecondes == null) {
                dureeFormatee = "N/A";
                return;
            }

            long minutes = dureeExecutionSecondes / 60;
            long secondes = dureeExecutionSecondes % 60;

            if (minutes > 0) {
                dureeFormatee = String.format("%dm %ds", minutes, secondes);
            } else {
                dureeFormatee = String.format("%ds", secondes);
            }
        }
    }
}
