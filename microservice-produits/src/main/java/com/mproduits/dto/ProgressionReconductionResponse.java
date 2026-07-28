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
import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour suivre la progression d'une reconduction en cours.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionReconductionResponse {

    private String reconductionId;
    private StatutReconduction statut;
    private EtapeReconduction etapeActuelle;
    private Integer pourcentage;
    private String message;
    private LocalDateTime heureDebut;
    private LocalDateTime heureEstimeeFin;
    private LocalDateTime heureFin;
    
    @Builder.Default
    private List<LogReconduction> logs = new ArrayList<>();
    
    private String messageErreur;
    private StatistiquesIntermediaires statistiques;

    public enum StatutReconduction {
        INITIALISATION("Initialisation"),
        VALIDATION("Validation des pré-requis"),
        EN_COURS("En cours d'exécution"),
        TERMINE("Terminé avec succès"),
        ERREUR("Terminé avec erreur"),
        ANNULE("Annulé par l'utilisateur");

        private final String libelle;

        StatutReconduction(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum EtapeReconduction {
        VALIDATION_PREREQ(1, "Validation des pré-requis", 5),
        CREATION_ENTREPRISE(2, "Création de l'entreprise", 10),
        RECONDUCTION_STOCKS(3, "Reconduction des stocks", 40),
        RECONDUCTION_PRIX(4, "Reconduction des prix", 40),
        FINALISATION(5, "Finalisation", 5);

        private final int ordre;
        private final String libelle;
        private final int poidsPourcentage;

        EtapeReconduction(int ordre, String libelle, int poidsPourcentage) {
            this.ordre = ordre;
            this.libelle = libelle;
            this.poidsPourcentage = poidsPourcentage;
        }

        public int getOrdre() {
            return ordre;
        }

        public String getLibelle() {
            return libelle;
        }

        public int getPoidsPourcentage() {
            return poidsPourcentage;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogReconduction {
        private LocalDateTime timestamp;
        private String niveau;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatistiquesIntermediaires {
        private Integer pointsVenteTraites;
        private Integer pointsVenteTotal;
        private Integer prixArticlesTraites;
        private Integer prixArticlesTotal;
        private Long tempsEcouleSecondes;
    }

    public void ajouterLog(String niveau, String message) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }

        LogReconduction log = LogReconduction.builder()
                .timestamp(LocalDateTime.now())
                .niveau(niveau)
                .message(message)
                .build();

        this.logs.add(log);

        if (this.logs.size() > 50) {
            this.logs.remove(0);
        }
    }

    public void calculerPourcentageGlobal(int progressionEtape) {
        if (etapeActuelle == null) {
            this.pourcentage = 0;
            return;
        }

        int pourcentageAvantEtape = 0;
        for (EtapeReconduction etape : EtapeReconduction.values()) {
            if (etape.getOrdre() < etapeActuelle.getOrdre()) {
                pourcentageAvantEtape += etape.getPoidsPourcentage();
            }
        }

        int pourcentageEtape = (etapeActuelle.getPoidsPourcentage() * progressionEtape) / 100;
        this.pourcentage = pourcentageAvantEtape + pourcentageEtape;
        this.pourcentage = Math.max(0, Math.min(100, this.pourcentage));
    }
    
}
